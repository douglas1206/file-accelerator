package com.lwtx.wisdom.cache;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 
 * This trait is to remove old cache file, control the size of cache dir.
 * 
 * @author Administrator
 *
 */
@Component
class CacheControlTasks {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private Logger logger = Logger.getLogger(getClass());

	@Value("${cache}")
	private String cache;
	
	@Value("${cache.maxsize}")
	private String cacheMaxsize;
	
	@Value("${cache.outdate}")
	private String cacheOutdate;
	
	@Autowired
	private FileTrait fileTrait;

	@PostConstruct
	private void pc() {
		logger.info("init cache-control trait, cache=" + cache + " cacheMaxsize=" + cacheMaxsize + " cacheOutdate=" + cacheOutdate);
	}

	@Scheduled(cron = "0 0 3  * * ? ")
//	@Scheduled(cron = "0/5 * * * * ? ")
	public void task() {
		
		if (!Boolean.valueOf(cache)) {
			return;
		}
		
		removeOutdateFiles();
		releaseSpace();
	}
	
	private void releaseSpace() {
		new SpaceReleaser().release(fileTrait.getCacheRoot());
	}

	private void removeOutdateFiles() 
	{
		File cacheRoot = fileTrait.getCacheRoot();
		File[] outdateFile = cacheRoot.listFiles(new OutdateFileFilter());
		remove(outdateFile);
		logger.info("run cache-control task, remove outdate file:" + outdateFile.length + " at " + dateFormat.format(new Date()));
	}

	private void remove(File[] outdateFile) {
		for (File f : outdateFile) {
			FileUtils.deleteQuietly(f);
		}
	}
	
	class OutdateFileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			try {
				BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getAbsolutePath()),
						BasicFileAttributes.class);
				Date lat = new Date(attr.lastAccessTime().toMillis());
				return outdate(lat);
			} catch (IOException e) {
				logger.error("get lastAccessTime fail for", e);
				return false;
			}
		}

		private boolean outdate(Date lat) {
			Date deadTime = DateUtils.addDays(lat, Integer.valueOf(cacheOutdate));
			return deadTime.before(new Date());
		}
	}
	
	class SpaceReleaser
	{
		static final long MAX_SIZE = 20 * 1024 * 1024 * 1024 * 1024;	//20 G
		
		void release(File f) {
			if (getTotalSizeOfFilesInDir(f) > Long.valueOf(cacheMaxsize)) {
				return;
			}
			
			//remove big size files ? more complicate design
			
			//use simple design, just remove all
			remove(f.listFiles());
			logger.info("run cache-control task, remove all cache files at " + dateFormat.format(new Date()));
		}
		
		long getTotalSizeOfFilesInDir(final File file) {
	        if (file.isFile())
	            return file.length();
	        final File[] children = file.listFiles();
	        long total = 0;
	        if (children != null)
	            for (final File child : children)
	                total += getTotalSizeOfFilesInDir(child);
	        return total;
	    }

	}
}

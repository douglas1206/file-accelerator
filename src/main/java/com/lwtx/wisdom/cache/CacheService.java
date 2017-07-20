package com.lwtx.wisdom.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lwtx.wisdom.service.TFSService;

@Service
public class CacheService {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private Logger logger = Logger.getLogger(getClass());

	@Value("${tfs.maxWait}")
	private String maxWait;
	
	@Value("${cache}")
	private String cache;
	
	@Autowired
	private FileTrait fileTrait;

	@Autowired
	private TFSService tfsService;

	public boolean hit(String tfsId) {
		return getCache(tfsId).exists();
	}

	public File getCache(String tfsId) {
		return fileTrait.getCache(tfsId);
	}

	public boolean pull(String tfsId, HttpServletResponse resp) {
		InputStream input = null;
		try {

			List<StreamHandler> handlers = new ArrayList<StreamHandler>();
			handlers.add(new ServletStreamHandler(resp));
			if (Boolean.valueOf(cache)) {
				handlers.add(new CacheStreamHandler(tfsId));
			}
			
			for (StreamHandler handler : handlers) { handler.before(); };

			input = tfsService.getStream(tfsId);
			int n;
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			while (IOUtils.EOF != (n = input.read(buffer))) {
				for (StreamHandler handler : handlers) {	handler.write(buffer, 0, n);	}
			}

			for (StreamHandler handler : handlers) { handler.after(); };
			
			logger.info("pull file " + tfsId + " success");
			return true;
		} catch (Throwable e) {
			logger.error("pull file " + tfsId + " fail");
			return false;
		} finally {
			IOUtils.closeQuietly(input);
		}
	}
	
	public boolean store(String tfsId, File inputFile) {
		
		File file = null;
		String tmpFilePath = FileUtils.getTempDirectoryPath() + File.separator + System.currentTimeMillis();
		
		try {
			File tmpFile = new File(tmpFilePath); 
			FileUtils.copyFile(inputFile, tmpFile);
			
			file = getCache(tfsId);
			boolean fileExist = file.exists();
			if (fileExist) {
				FileUtils.deleteQuietly(file);
			}
			FileUtils.moveFile(tmpFile, file);
			logger.info(fileExist ? String.format("file[%s] is override into cache success", tfsId) : String.format("file[%s] is stored into cache success", tfsId));
			return true;
		} catch (IOException e) {
			logger.error(String.format("file[%s] is stored into cache fail for %s", tfsId, e.getMessage()));
			FileUtils.deleteQuietly(file);	//delete file if store fail
			return false;
		} finally {
			FileUtils.deleteQuietly(new File(tmpFilePath));
		}
	}
	
	public void cache(File file, String tfsId) throws IOException {
		File target = getCache(tfsId); // file to cache
		target.createNewFile();
		FileUtils.copyFile(file, target);
	}
	
	public void remove(String tfsId) {
		FileUtils.deleteQuietly(getCache(tfsId));
	}
	
	interface StreamHandler {
		boolean canWrite();
		void write(byte[] buffer, int off, int len);
		void before();
		void after();
	}

	abstract class StreamHandlerAdapter implements StreamHandler {
		boolean error = false;

		@Override
		public boolean canWrite() {
			return !error;
		}

		@Override
		public void write(byte[] buffer, int off, int len) {
			if (canWrite()) {
				doWrite(buffer, off, len);
			}
		}

		abstract protected void doWrite(byte[] buffer, int off, int len) ;

		@Override
		public void before() {
		}

		@Override
		public void after() {
		}
	}
	
	class ServletStreamHandler extends StreamHandlerAdapter {
		HttpServletResponse resp;

		ServletStreamHandler(HttpServletResponse resp) {
			super();
			this.resp = resp;
		}

		@Override
		protected void doWrite(byte[] buffer, int off, int len) {
			try {
				resp.getOutputStream().write(buffer, 0, len);
			} catch (IOException e) {
				error = true;
				logger.error("serlvet write stream fail for", e);
			}
		}

		@Override
		public void after() {
			super.after();
			if (!error) {
				resp.setHeader("Tomcat-Cache", "Stream");
				try {
					resp.getOutputStream().flush();
				} catch (IOException e) {
					logger.error("serlvet flush stream fail for", e);
				}
			}
		}
	}

	class CacheStreamHandler extends StreamHandlerAdapter {

		String tfsId;
		File tmpdestination;
		FileOutputStream tmpdestinationOutput;
		File destination;

		CacheStreamHandler(String tfsId) {
			super();
			
			this.tfsId = tfsId;
			try {
				tmpdestination = new File(FileUtils.getTempDirectoryPath() + File.separator + tfsId); // file to store stream temporary
				tmpdestination.createNewFile();
				tmpdestinationOutput = null;
				tmpdestinationOutput = FileUtils.openOutputStream(tmpdestination);
			} catch (Throwable e) {
				error = true;
				logger.error("cache is disabled for can't create file", e);
				IOUtils.closeQuietly(tmpdestinationOutput);
			}
		}

		@Override
		public void after() {
			try {
				tmpdestinationOutput.flush();
				tmpdestinationOutput.close();

				cache(tmpdestination, tfsId);	// file to cache
			} catch (IOException e) {
				logger.error("serlvet write stream fail for", e);
			} finally {
				IOUtils.closeQuietly(tmpdestinationOutput);
				FileUtils.deleteQuietly(tmpdestination);
			}
		}

		@Override
		protected void doWrite(byte[] buffer, int off, int len) {
			try {
				tmpdestinationOutput.write(buffer, 0, len);
			} catch (IOException e) {
				error = true;
				logger.error("serlvet write stream fail for", e);
			}
		}
	}
}
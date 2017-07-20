package com.lwtx.wisdom.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lwtx.wisdom.cache.CacheService;
import com.lwtx.wisdom.service.TFSService;

@Service
public class UploadService {

	private static final Logger LOGGER = Logger.getLogger(UploadService.class);

	@Value("${upload.pass.tomcat}")
	private String pass; // passby to tfs directly.

	@Autowired
	private CacheService cacheService;

	@Autowired
	private TFSService tfsService;

	@Autowired
	private UploadTfsTrait syncTrait;

	@Autowired
	private UploadTmpTrait tmpTrait;

	public String upload(MultipartFile file) throws UploadException {
		File tmpFile = null;

		try {
			if (Boolean.valueOf(pass)) {
				return uploadPass(file, tmpFile);
			}

			return uploadSync(file, tmpFile);
		} catch (Throwable e) {
			throw new UploadException("You failed to upload " + file.getName() + " => " + e.getMessage());
		} finally {
			FileUtils.deleteQuietly(tmpFile);
		}
	}

	private String uploadPass(MultipartFile file, File tmpFile) throws FileNotFoundException, IOException {
		
		Date startAt = new Date();
		Date endAt;
		LOGGER.info(String.format("upload using pass pattern, start at %tT%n", startAt));
		
		String name = file.getName();
		tmpFile = writeToTmp(name, file);

		String tfsId = uploadDirectToTfs(tmpFile);
		cacheService.cache(tmpFile, tfsId); // cache file
		
		endAt = new Date();
		LOGGER.info(String.format("upload using pass pattern, cost %d ms, end at %tT%n", endAt.getTime() - startAt.getTime(), endAt));
		return tfsId;
	}

	private String uploadSync(MultipartFile file, File tmpFile)
			throws FileNotFoundException, IOException, UploadException {
		LOGGER.info("upload using sync pattern");
		
		String name = tfsService.newLocalTfsId();
		// String name = tfsService.newTfsId();

		tmpFile = writeToTmp(name, file);

		cache(name, tmpFile); // copy to cache
		sync(name, tmpFile); // sync to tfs

		return name;
	}

	private String uploadDirectToTfs(File tmpFile) {
		return tfsService.newFileWithStream(tmpFile);
	}

	private File writeToTmp(String name, MultipartFile file) throws IOException, FileNotFoundException {
		File tmpFile = tmpTrait.getTmpFile(name);
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(tmpFile));
		FileCopyUtils.copy(file.getInputStream(), stream);
		stream.close();
		return tmpFile;
	}

	private void sync(String name, File tmpFile) {
		// post a json to

		syncTrait.push(name, tmpFile);
	}

	private void cache(String name, File tmpFile) throws UploadException {
		boolean success = cacheService.store(name, tmpFile);
		if (!success) {
			LOGGER.error("You failed to upload " + name + " because store into cache fail");
			throw new UploadException("You failed to upload " + name + " because store into cache fail");
		}
	}
}

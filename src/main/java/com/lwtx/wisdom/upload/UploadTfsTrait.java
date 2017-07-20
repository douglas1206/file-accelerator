package com.lwtx.wisdom.upload;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class UploadTfsTrait {
	public static final String UPLOAD_SYNC_DIR = "wisdom_acc_upload_sync_dir";
	private static final Logger LOGGER = Logger.getLogger(UploadTfsTrait.class);

	@Autowired
	private UploadTmpTrait tmpTrait;

	@Autowired
	private UploadRootSubDirTrait dirTrait;

	@PostConstruct
	private void pc() throws IOException {
		dirTrait.setName(UPLOAD_SYNC_DIR);
		LOGGER.info(String.format("Using upload tfs sync dir: %s", dirTrait.getPath()));
	}

	public void push(String name, File inputFile) {

		// copy to temp
		File tmpFile = null;
		File destFile = new File(dirTrait.getPath() + File.separator + name);

		if (same(inputFile, destFile)) {
			LOGGER.warn(String.format("file[%s] 's sync task is cancel for same file", name));
			return;
		}

		try {
			tmpFile = pushTo(name, inputFile, destFile);
		} catch (IOException e) {
			LOGGER.error(String.format("file[%s] is pushed to sync sequence fail for %s", name, e.getMessage()));
			FileUtils.deleteQuietly(destFile); // clean
		} finally {
			FileUtils.deleteQuietly(tmpFile);
		}
	}

	private boolean same(File inputFile, File destFile) {
		return inputFile.exists() && destFile.exists() && destFile.length() == inputFile.length();
	}

	private File pushTo(String name, File inputFile, File destFile) throws IOException {
		File tmpFile = tmpTrait.getTmpFile(name);
		FileUtils.copyFile(inputFile, tmpFile);
		FileUtils.moveFile(tmpFile, destFile);
		return tmpFile;
	}
	
	public UploadRootSubDirTrait getDirTrait()
	{
		return dirTrait;
	}
}

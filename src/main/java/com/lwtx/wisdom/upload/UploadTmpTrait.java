package com.lwtx.wisdom.upload;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class UploadTmpTrait {
	private static final Logger LOGGER = Logger.getLogger(UploadTmpTrait.class);

	public static final String UPLOAD_TMP_DIR = "wisdom_acc_upload_tmp_dir";

	@Value("${upload.root}")
	private String uploadRoot;

	@Autowired
	private UploadRootSubDirTrait dirTrait;

	@PostConstruct
	private void pc() throws IOException {
		dirTrait.setName(UPLOAD_TMP_DIR);
		LOGGER.info(String.format("Using upload temp dir: %s", dirTrait.getPath()));
	}

	public File getTmpFile(String name) throws IOException {
		File tmpFile = new File(dirTrait.getPath() + File.separator + name + "_" + UUID.randomUUID().toString());

		if (!tmpFile.exists()) {
			tmpFile.createNewFile();
		}
		
		return tmpFile;
	}

}

package com.lwtx.wisdom.upload;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lwtx.wisdom.service.FileService;

/**
 * 
 * 
 * 
 * @author Administrator
 *
 */
@Component
@Scope("prototype")
class UploadRootSubDirTrait {

	private static final Logger LOGGER = Logger.getLogger(UploadRootSubDirTrait.class);

	@Value("${upload.root}")
	private String uploadRoot;

	private String name;

	@Autowired
	private FileService fileService;

	public File getDir() {
		return new File(getPath());
	}

	public String getPath() {
		initDir();
		return getInnerPath();
	}
	
	private File getInnerDir() {
		return new File(getInnerPath());
	}
	
	private String getInnerPath() {
		return uploadRoot + File.separator + getName();
	}

	private void initDir() {
		try {
			fileService.mkdir(getInnerDir());
		} catch (IOException e) {
			LOGGER.error("mkdir for upload sync dir fail for", e);
		}
	}

	public File getChild(String name) {
		return new File(getChildPath(name));
	}

	private String getChildPath(String name) {
		return getPath() + File.separator + name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		
		initDir();
	}

	public File[] children() {
		return getDir().listFiles();
	}
}

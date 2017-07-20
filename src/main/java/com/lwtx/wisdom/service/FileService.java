package com.lwtx.wisdom.service;

import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {

	private static final Logger LOGGER = Logger.getLogger(FileService.class);
	
	@Autowired
	private RequestService requestService;

	public String getMimeType(File f) {
		return new MimetypesFileTypeMap().getContentType(f);
	}

	public String getMimeTypeWithServletContext(File f) {
		return requestService.currentServletContext().getMimeType(f.getAbsolutePath());
	}
	
	public void mkdir(String dirPath) throws IOException
	{
		mkdir(new File(dirPath));
	}
	
	public void mkdir(File dir) throws IOException
	{
		try {
			FileUtils.forceMkdir(dir);
		} catch (IOException e) {
			LOGGER.error(String.format("mkdir dir %s fail", dir));
			if (dir.isFile()) {
				LOGGER.warn(String.format("dir %s is file, delete", dir));
				FileUtils.deleteQuietly(dir);
				try {
					FileUtils.forceMkdir(dir);
				} catch (IOException e1) {
					LOGGER.error(String.format("mkdir dir %s fail again! check disk space enough or not", dir));
					throw e1;
				}
			}
		}
		LOGGER.debug(String.format("Using upload temp dir: %s", dir.getAbsolutePath()));
	}
}

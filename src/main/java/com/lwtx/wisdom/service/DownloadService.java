package com.lwtx.wisdom.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lwtx.wisdom.cache.CacheService;

@Component
public class DownloadService {

	@Autowired
	private CacheService cacheService;

	@Autowired
	private FileService fileService;

	private Logger logger = Logger.getLogger(DownloadService.class);

	private static final int BUFFER_SIZE = 4096;

	public void write(HttpServletResponse response, String tfsId) throws IOException {

		// construct the complete absolute path of the file
		File downloadFile = cacheService.getCache(tfsId);
		FileInputStream inputStream = new FileInputStream(downloadFile);

		// get MIME type of the file
		String mimeType = fileService.getMimeType(downloadFile);
		if (mimeType == null) {
			// set to binary type if MIME mapping not found
			mimeType = "application/octet-stream";
		}

		logger.debug("MIME type: " + mimeType);

		// set content attributes for the response
		response.setContentType(mimeType);
		response.setContentLength((int) downloadFile.length());

		// set headers for the response
//		String headerKey = "Content-Disposition";
//		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
//		response.setHeader(headerKey, headerValue);

		// FIX-ME: client may request part data, code:206, temp force nocache anything; This will be no problem in LAN temp
		response.setHeader("Cache-Control", "public");
		response.setHeader("Tomcat-Cache", "File");
		
		// get output stream of the response
		OutputStream outStream = response.getOutputStream();

		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;

		// write bytes read from the input stream into the output stream
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}

		logger.info("hit tfs \"" + tfsId + "\" from cache success");
		inputStream.close();
		outStream.close();
	}
}
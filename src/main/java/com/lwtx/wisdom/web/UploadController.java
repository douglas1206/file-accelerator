package com.lwtx.wisdom.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.lwtx.wisdom.upload.UploadException;
import com.lwtx.wisdom.upload.UploadService;

@Controller
public class UploadController {

	private static final Logger LOGGER = Logger.getLogger(UploadController.class);

	@Autowired
	private UploadService uploadService;

	@RequestMapping(value = "/v1/wisdom-pc-client", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> handleFileUpload(@RequestParam("file") MultipartFile file,
			HttpServletRequest req) {
		logFileInfo(file);
		logRequestInfo(req);

		try {
			String generateTfsId = uploadService.upload(file);
			LOGGER.info(String.format("file[%s] is upload success", generateTfsId));
			return createSuccessResp(generateTfsId, "You successfully uploaded " + generateTfsId + "!",
					file.getOriginalFilename());
		} catch (UploadException e) {
			return createFailResp(e.getMessage());
		}
	}

	private void logFileInfo(MultipartFile file) {
		LOGGER.info(String.format("upload request arrive, file name is %s, contentype is %s", file.getOriginalFilename(), file.getContentType()));
	}

	private void logRequestInfo(HttpServletRequest req) {
		logRequestHeaderInfo(req);
		logRequestAttrInfo(req);
	}

	private void logRequestAttrInfo(HttpServletRequest req) {
		Enumeration<String> names = req.getParameterNames();
		StringBuilder headerBuilder = new StringBuilder();
		headerBuilder.append("upload request arrive, param:");
		while (names.hasMoreElements()) {
			String n = names.nextElement();
			headerBuilder.append(String.format(" %s=%s", n, req.getParameter(n)));
		}
		LOGGER.info(headerBuilder.toString());
	}

	private void logRequestHeaderInfo(HttpServletRequest req) {
		Enumeration<String> names = req.getHeaderNames();
		StringBuilder headerBuilder = new StringBuilder();
		headerBuilder.append("upload request arrive, header:");
		while (names.hasMoreElements()) {
			String n = names.nextElement();
			headerBuilder.append(String.format(" %s=%s", n, req.getHeader(n)));
		}
		LOGGER.info(headerBuilder.toString());
	}

	private Map<String, Object> createSuccessResp(String name, String msg, String originalFileName) {
		Map<String, Object> success = new HashMap<String, Object>();
		success.put("status", "success");
		success.put("TFS_FILE_NAME", name);
		success.put("msg", msg);
		return success;
	}

	private Map<String, Object> createFailResp(String msg) {
		Map<String, Object> fail = new HashMap<String, Object>();
		fail.put("status", "fail");
		fail.put("msg", msg);
		return fail;
	}
}
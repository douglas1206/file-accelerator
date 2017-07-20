package com.lwtx.wisdom.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.lwtx.wisdom.cache.CacheService;
import com.lwtx.wisdom.service.DownloadService;
import com.lwtx.wisdom.service.TFSService;

@Controller
public class DownloadController {

	@Autowired
	private CacheService cacheService;

	@Autowired
	private DownloadService downloadTrait;
	
	@Autowired
	private TFSService tfsService;

	private Logger logger = Logger.getLogger(getClass());
	
	@RequestMapping(value = "/v1/wisdom-pc-client/{tfsId}", method = RequestMethod.GET)
	public void doDownload(HttpServletResponse response, @PathVariable("tfsId") String tfsId) throws IOException {

		if (cacheService.hit(tfsId)) {
			downloadTrait.write(response, tfsId);
			return;
		}

		boolean success = cacheService.pull(tfsId, response);
		if (success) {	
			logger.info("hit tfs \"" + tfsId + "\" from stream success");
			return;
		} else {
			logger.warn("find tfs \"" + tfsId + "\" fail, redirect to tfs server!");
			response.sendRedirect(tfsService.getURLStr(tfsId));
		}
	}
}
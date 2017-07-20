package com.lwtx.wisdom.upload;

import java.io.File;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.lwtx.wisdom.service.WisdomService;

@Component
class UploadWisdomSyncTask {

	private static final Logger LOGGER = Logger.getLogger(UploadWisdomSyncTask.class);

	@Value("${wisdom.notify}")
	private String notifyAction;

	@Autowired
	private UploadWisdomTrait wisdomTrait;

	@Autowired
	private WisdomService wisdomService;
	
	@PostConstruct
	private void pc() {
		LOGGER.info(String.format("Using wisdom notify: %s", notifyAction));
	}

	// @Autowired
	// private CacheService cacheService;

	@Scheduled(fixedDelay = 1000)
	private void sync() {
		for (File f : wisdomTrait.getDirTrait().children()) {
			sync(f);
		}
	}

	private void sync(File taskFile) {
		String name = taskFile.getName();
		Map<String, String> paramMap = wisdomTrait.decode(name);
		JSONObject jo = wisdomService.getJson(notifyAction, paramMap);

		if (null != jo && jo.getBooleanValue("ok")) {
			String tfsId = wisdomTrait.decodeTfsId(name);
			String returnTfsId = wisdomTrait.descodeReturnTfsId(name);
			LOGGER.info(String.format("wisdom replace [%s] with [%s] ", tfsId, returnTfsId));

			FileUtils.deleteQuietly(taskFile);
			LOGGER.info(String.format("wisdom remove task file"));

			// remove image of tfs file
			removeLocalCache(tfsId);

			LOGGER.info(String.format("wisdom sync success"));
			return;
		}

		LOGGER.error(String.format("notify wisdom of replace [%s] with [%s] fail, retry later",
				wisdomTrait.decodeTfsId(name), wisdomTrait.descodeReturnTfsId(name)));

	}

	private void removeLocalCache(String tfsId) {
		// FIXME: this function may be support in future.
		// cacheService.remove(tfsId);
		// LOGGER.info(String.format("wisdom remove local tfs file %s", tfsId));
	}
}
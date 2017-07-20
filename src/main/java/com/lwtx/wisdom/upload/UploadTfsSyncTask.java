package com.lwtx.wisdom.upload;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lwtx.wisdom.cache.CacheService;
import com.lwtx.wisdom.service.TFSService;

@Component
class UploadTfsSyncTask {

	private static final Logger LOGGER = Logger.getLogger(UploadTfsSyncTask.class);

	@Autowired
	private UploadTfsTrait syncTrait;

	@Autowired
	private TFSService tfsService;

	@Autowired
	private UploadWisdomTrait syncWisdomTrait;

	@Autowired
	private CacheService cacheService;

	@Scheduled(fixedDelay = 1000)
	private void sync() {
		File[] files = syncTrait.getDirTrait().children();

		for (File f : files) {
			sync(f);
		}
	}

	private void sync(File f) {
		Date start = new Date();
		LOGGER.info(String.format("tfs [%s] sync start at time %tT%n", f.getName(), start));

		if (dosync(f)) {
			FileUtils.deleteQuietly(f);
			Date end = new Date();
			LOGGER.info(String.format("tfs [%s] sync success, cost %d'ms, delete from sync sequence", f.getName(),
					(end.getTime() - start.getTime())));
		} else {
			LOGGER.warn(String.format("file[%s] sync fail, retry later", f.getName()));
		}
	}

	private boolean dosync(File f) {
		String tfsId = f.getName();
		String returnTfsId = tfsService.newFileWithStream(f);

		if (StringUtils.equals(tfsId, returnTfsId))
			return true;
		if (syncWisdomTrait.buildTask(tfsId, returnTfsId)) {
			// copy
			try {
				cacheService.cache(f, returnTfsId);
				return true;
			} catch (IOException e) {
				LOGGER.error(String.format("file[%s] backup fail", returnTfsId), e);
			}
		}
		return false;
	}

	// WRAN: expect PUT method to override tfsId but handle large file create a
	// new tfsId.
	// private boolean dosync(File f) {
	// return
	// !StringUtils.isBlank(tfsService.overrideFileWithStream(f.getName(), f));
	// }
}

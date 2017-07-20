package com.lwtx.wisdom.log;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 
 * back up local log to wisdom server
 * 
 * @author Administrator
 *
 */
@Component
class TomcatLogBackupTask {

	private static final Logger LOGGER = Logger.getLogger(TomcatLogCleanTask.class);
	
	@Scheduled(cron = "0 0 3  * * ? ")
	private void autoclean() {
		//TODO: add implemetation in future.
		LOGGER.warn("run task to backup recent tomcat's log to wisdom server for analyse, THIS FUNCTION IS NOT IMPLEMTEMT YET, TAKE A PLACEHOLDER");
	}
}

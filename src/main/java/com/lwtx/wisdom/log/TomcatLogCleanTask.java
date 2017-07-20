package com.lwtx.wisdom.log;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/***
 * 
 * clean unused logs for save disk space. 
 * @author Administrator
 *
 */
@Component
class TomcatLogCleanTask {

	private static final Logger LOGGER = Logger.getLogger(TomcatLogCleanTask.class);
	
	@Scheduled(cron = "0 0 4  * * ? ")
	private void autoclean() {
		//TODO: add implemetation in future.
		LOGGER.warn("run task to clean outdate tomcat's log, THIS FUNCTION IS NOT IMPLEMTEMT YET, TAKE A PLACEHOLDER");
	}
}
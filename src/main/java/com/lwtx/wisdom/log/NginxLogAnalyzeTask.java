package com.lwtx.wisdom.log;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/***
 * 
 * clean unused logs for analyze. 
 * @author Administrator
 *
 */
@Component
class NginxLogAnalyzeTask {

	private static final Logger LOGGER = Logger.getLogger(NginxLogAnalyzeTask.class);
	
	@Scheduled(cron = "0 0 4  * * ? ")
	private void autoclean() {
		//TODO: add implemetation in future.
		LOGGER.warn("run task to analyze nginx's log to generate reports, THIS FUNCTION IS NOT IMPLEMTEMT YET, TAKE A PLACEHOLDER");
	}
}
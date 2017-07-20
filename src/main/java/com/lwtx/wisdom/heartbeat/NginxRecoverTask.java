package com.lwtx.wisdom.heartbeat;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class NginxRecoverTask {

//	@Scheduled(cron = "0/5 * * * * ?")
	/**
	 * make sure nginx is running, restart if nginx is down.
	 */
	private void nginxRunManageTask() {
		// TODO: add implementation in future ...
	}

	
}

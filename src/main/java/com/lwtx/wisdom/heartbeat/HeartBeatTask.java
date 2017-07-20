package com.lwtx.wisdom.heartbeat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.lwtx.wisdom.service.WisdomService;

@Component
class HeartBeatTask {

	private static final Logger LOGGER = Logger.getLogger(HeartBeatTask.class);

	@Value("${wisdom.heartbeat}")
	private String wisdomHeartbeat;

	@Value("${wisdom.heartbeat.servercode}")
	private String servercode;

	@Autowired
	private NginxTrait nginxTrait;

	@Autowired
	private WisdomService wisdomService;

	@PostConstruct
	private void pc() {
		LOGGER.info(String.format("Using wisdom heartbeat: %s, serverCode:%s", wisdomHeartbeat, servercode));
	}

	@Scheduled(fixedDelay = 240000) // production environments
//	@Scheduled(fixedDelay = 1000) // production environments
	// @Scheduled(cron = "0/5 * * * * ?") //develop environments
	private void heartbeat() {
		LOGGER.info(String.format("heart beat at time %tT%n", new Date()));

		try {
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("serverCode", servercode);
			paramMap.put("nginx", nginxTrait.getNginx());
			// paramMap.put("nginxStatus", nginxTrait.getStatus().name());
			paramMap.put("time", String.format("%d", new Date().getTime()));

			String content = wisdomService.post(wisdomHeartbeat, paramMap);
			if (null != content) {
				JSONObject jo = (JSONObject) JSONObject.parse(content);
				if (0 == jo.getIntValue("ret")) {
					LOGGER.info(String.format("heart beat success, return tfsId[%s] from server", content));
					return;
				}
				
				LOGGER.error(String.format("heart beat fail for wisdom return %s", content));
			} else {
				LOGGER.error(String.format("heart beat fail"));
			}
		} catch (Throwable e) {
			LOGGER.error(String.format("heart beat fail for execption %s", e.getMessage()));
		}
	}
}

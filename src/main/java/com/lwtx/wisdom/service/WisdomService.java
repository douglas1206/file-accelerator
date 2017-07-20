package com.lwtx.wisdom.service;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class WisdomService {

	private static final Logger LOGGER = Logger.getLogger(WisdomService.class);

	@Value("${wisdom}")
	private String wisdom;

	@Autowired
	private HTTPService httpService;

	@PostConstruct
	private void pc() {
		LOGGER.info(String.format("Using wisdom server: %s", wisdom));
	}

	public JSONObject postJson(String action, Map<String, String> paramMap) {
		return httpService.postJson(getWisdomAction(action), paramMap);
	}

	public String post(String action, Map<String, String> paramMap) {
		return httpService.post(getWisdomAction(action), paramMap);
	}

	public JSONObject getJson(String action, Map<String, String> paramMap) {
		return httpService.getJson(getWisdomAction(action), paramMap);
	}

	public String get(String action, Map<String, String> paramMap) {
		return httpService.get(getWisdomAction(action), paramMap);
	}

	private String getWisdomAction(String action) {
		return wisdom + "/" + action;
	}
}
package com.lwtx.wisdom.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lwtx.wisdom.service.WisdomService;

@Controller
@RequestMapping("/wisdom")
public class WisdomController {

	@Autowired
	private WisdomService wisdomService;

	@RequestMapping("/test_get")
	public @ResponseBody String testGet(@RequestParam("action") String action) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("classId", "150104");
		
		return wisdomService.get(action, map);
	}

	@RequestMapping("/test_post")
	public @ResponseBody String testPost(@RequestParam("action") String action) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("classId", "150104");
		
		return wisdomService.post(action, map);
	}
}

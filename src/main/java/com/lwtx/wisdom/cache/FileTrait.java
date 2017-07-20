package com.lwtx.wisdom.cache;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class FileTrait {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Value("${cache.root}")
	private String cacheRoot;
	
	@PostConstruct
	private void pc() {
		if (!getCacheRoot().exists()) {
			getCacheRoot().mkdirs();
		}
		logger.info("Using cache root: " + getCacheRoot() + "");
	}
	
	public File getCache(String tfsId) {
		return new File(getCachePath(tfsId));
	}

	public String getCachePath(String tfsId) {
		return getCacheRootPath() + tfsId;
	}

	public File getCacheRoot() {
		return new File(getCacheRootPath());
	}

	public String getCacheRootPath() {
		return cacheRoot + File.separator;
	}
}

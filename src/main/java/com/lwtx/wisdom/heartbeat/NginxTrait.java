package com.lwtx.wisdom.heartbeat;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class NginxTrait {

	private static final Logger LOGGER = Logger.getLogger(NginxTrait.class);
	
	@Value("${nginx}")
	private String nginx;
	
	@PostConstruct
	private void pc() {
		LOGGER.info(String.format("Using nginx server: %s", getNginx()));
	}

	public String getNginx() {
		return nginx;
	}
	
	public NginxStatus getStatus() {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet get = new HttpGet(getNginx());

		try {
			HttpResponse response = client.execute(get);
			StatusLine sl = response.getStatusLine();
			if (HttpStatus.SC_OK == sl.getStatusCode()) {
				return NginxStatus.RUN;
			}
		} catch (Throwable e) {
			LOGGER.error("nginx status check fail for", e);
		}

		return NginxStatus.DOWN;
	}
}
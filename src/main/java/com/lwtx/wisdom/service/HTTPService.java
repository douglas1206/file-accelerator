package com.lwtx.wisdom.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class HTTPService {

	private static final Logger LOGGER = Logger.getLogger(HTTPService.class);

	public JSONObject postJson(String url, Map<String, String> paramMap) {
		return parseStringToJson(post(url, paramMap));
	}

	public String post(String url, Map<String, String> paramMap) {
		HttpPost req = new HttpPost(url);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (String key : paramMap.keySet()) {
			params.add(new BasicNameValuePair(key, paramMap.get(key)));
		}

		try {
			req.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			return execute(req);

		} catch (UnsupportedEncodingException e) {
			LOGGER.error(String.format("send request [%s] fail for", req.toString()), e);
			return null;
		}
	}

	public JSONObject getJson(String url, Map<String, String> params) {
		return parseStringToJson(get(url, params));
	}

	public String get(String url, Map<String, String> params) {
		StringBuilder qsBuilder = new StringBuilder();
		for (String key : params.keySet()) {
			qsBuilder.append(String.format("%s=%s&", key, params.get(key)));
		}
		String qs = qsBuilder.toString();
		if (qs.endsWith("&")) {
			qs = qs.substring(0, qs.length() - 1);
		}

		HttpGet req = new HttpGet(url + "?" + qs);

		return execute(req);
	}

	public String responseContent(HttpResponse resp) throws IOException {
		HttpEntity httpEntity = resp.getEntity();
		return EntityUtils.toString(httpEntity);
	}

	public boolean responseIsOK(HttpResponse resp) {
		return HttpStatus.SC_OK == resp.getStatusLine().getStatusCode();
	}

	public JSONObject parseStringToJson(String jsonStr) {
		return (JSONObject) (null != jsonStr ? JSONObject.parse(jsonStr) : null);
	}

	private String execute(HttpUriRequest req) {

		CloseableHttpResponse resp = null;
		try {
			LOGGER.info(String.format("send request[%s]", req.toString()));
			resp = HttpClients.createDefault().execute(req);
			String content = responseContent(resp);
			LOGGER.info(String.format("send request[%s], return[%s]", req.toString(), content));

			if (responseIsOK(resp)) {
				return content;
			}

			LOGGER.error(String.format("send request[%s] fail for return[%s]", req.toString(), content));
			return null;

		} catch (Throwable e) {
			LOGGER.error(String.format("send request[%s] fail for ", req.toString()), e);
			return null;
		} finally {
			if (null != resp) {
				try {
					resp.close();
				} catch (IOException e) {
					LOGGER.error(String.format("close resp of request[%s] fail", req.toString()), e);
				}
			}
		}
	}
}

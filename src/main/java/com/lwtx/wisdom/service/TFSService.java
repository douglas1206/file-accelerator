package com.lwtx.wisdom.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.catalina.Server;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class TFSService {

	private static final Logger LOGGER = Logger.getLogger(TFSService.class);

	@Value("${tfs}")
	private String tfs;

	@Autowired
	private FileService fileService;
	
	@Autowired
	private ServerService serverService;

	@PostConstruct
	private void pc() {
		LOGGER.info("Using TFS: " + tfs);
	}

	public InputStream getStream(String tfsId) throws IOException {
		return getURL(tfsId).openStream();
	}

	public URL getURL(String tfsId) throws MalformedURLException {
		return new URL(getURLStr(tfsId));
	}

	public String getURLStr(String tfsId) {
		return tfs + "/" + tfsId;
	}

	public URL getTFSURL() throws MalformedURLException {
		return new URL(getTFS());
	}

	public String getTFS() {
		return tfs;
	}

	public boolean post(String tfsId, File file) {
		LOGGER.info(String.format("tfs[%s] post start", tfsId));

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPut post = new HttpPut(getTFS() + "/" + tfsId);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());

		HttpEntity entity = builder.build();
		post.setEntity(entity);

		post.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);

		try {
			HttpResponse response = client.execute(post);
			StatusLine sl = response.getStatusLine();

			if (HttpStatus.SC_OK == sl.getStatusCode()) {

				byte[] bytes = EntityUtils.toByteArray(response.getEntity());
				String content = new String(bytes);
				JSONObject jo = JSONObject.parseObject(content);
				String returnTfsId = jo.getString("TFS_FILE_NAME");

				LOGGER.info(String.format("tfs[%s] post success, return tfsId[%s] from server", tfsId, returnTfsId));
				return true;
			}

			LOGGER.error(String.format("tfs[%s] post fail with response code %d", tfsId, sl.getStatusCode()));
		} catch (Throwable e) {
			LOGGER.error(String.format("tfs[%s] post fail for", tfsId), e);
		}

		return false;
	}

	public boolean postWithStream(String tfsId, File file) {
		String BOUNDARY = "---------------------------" + System.currentTimeMillis();

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
		builder.addTextBody("filename", tfsId, ContentType.TEXT_PLAIN);
		builder.addTextBody("text", "sync tfs file", ContentType.TEXT_PLAIN);
		HttpEntity entity = builder.build();

		HttpURLConnection conn = null;
		OutputStream out = null;

		try {
			URL server = new URL(getTFS() + "/" + tfsId);
			conn = (HttpURLConnection) server.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			conn.setRequestProperty("Content-length", entity.getContentLength() + "");
			// conn.setRequestProperty(entity.getContentType().getName(),
			// entity.getContentType().getValue());

			out = new DataOutputStream(conn.getOutputStream());

			entity.writeTo(out);
			out.close();

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuilder sbuilder = new StringBuilder();
			String rl;
			while (null != (rl = br.readLine())) {
				sbuilder.append(rl);
			}

			JSONObject jo = JSONObject.parseObject(sbuilder.toString());
			LOGGER.info(String.format("tfs[%s] post success, return tfsId[%s] from server", tfsId,
					jo.get("TFS_FILE_NAME")));

		} catch (IOException e) {
			LOGGER.error(String.format("tfs[%s] post fail for", tfsId), e);
			return false;
		} catch (Throwable e) {
			LOGGER.error(String.format("tfs[%s] post fail for", tfsId), e);
			return false;
		} finally {
			IOUtils.closeQuietly(out);

			if (null != conn) {
				conn.disconnect();
			}
		}

		return true;
	}

	/**
	 * 
	 * 内存限制 计算 File ContentLength时， 会将整个文件读入内存。
	 * 
	 * @param tfsId
	 * @param file
	 * @return
	 */
	public boolean post2(String tfsId, File file) {

		String BOUNDARY = "---------------------------123821742118716";
		HttpURLConnection conn = null;
		InputStream in = null;
		OutputStream out = null;

		BufferedReader respReader = null;

		try {
			URL server = new URL(getTFS());
			conn = (HttpURLConnection) server.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

			out = new DataOutputStream(conn.getOutputStream());
			in = new FileInputStream(file);

			String filename = file.getName();
			String contentType = fileService.getMimeType(file);

			StringBuffer strBuf = new StringBuffer();
			strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
			strBuf.append("Content-Disposition: form-data; name=\"" + tfsId + "\"; filename=\"" + filename + "\"\r\n");
			strBuf.append("Content-Type:" + contentType + "\r\n\r\n");

			out.write(strBuf.toString().getBytes());
			out.flush();

			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) != -1) {
				out.write(bufferOut, 0, bytes);

			}
			in.close();

			// write end
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();

			// 读取返回数据
			strBuf = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}

			String result = strBuf.toString();
			JSONObject jo = JSONObject.parseObject(result);
			LOGGER.info(String.format("tfs[%s] post success, return tfsId[%s] from server", tfsId,
					jo.get("TFS_FILE_NAME")));

		} catch (Throwable e) {
			LOGGER.error(String.format("tfs[%s] post fail for", tfsId), e);
			return false;
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(respReader);

			if (null != conn) {
				conn.disconnect();
			}
		}

		return true;
	}

	public String post(File file) {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(getTFS());

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());

		HttpEntity entity = builder.build();
		post.setEntity(entity);

		post.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);

		try {
			HttpResponse response = client.execute(post);
			StatusLine sl = response.getStatusLine();

			if (HttpStatus.SC_OK == sl.getStatusCode()) {

				byte[] bytes = EntityUtils.toByteArray(response.getEntity());
				String content = new String(bytes);
				JSONObject jo = JSONObject.parseObject(content);
				String returnTfsId = jo.getString("TFS_FILE_NAME");

				LOGGER.info(String.format("tfs create tfsid %s", returnTfsId));
				return returnTfsId;
			}

			LOGGER.info(String.format("tfs create tfsid fail with response code %d", sl.getStatusCode()));
		} catch (Throwable e) {
			LOGGER.error(String.format("tfs create tfsid fail for"), e);
		}

		return null;
	}

	public String newTfsId() {
		File f = new File(FileUtils.getTempDirectoryPath() + File.separator + "random");
		try {
			f.createNewFile();
			FileUtils.writeStringToFile(f, "placeholder for file", "utf-8");

			return newLargeFileWithStream(f);
		} catch (IOException e) {
			return null;
		}
	}
	
	public String newLocalTfsId() {
		return String.format("FIA_v1_%s_%s", serverService.getMac(), UUID.randomUUID().toString());
	}

	public String newLargeFileWithStream(File file) {
		return saveWithStream("POST", getTFS(), file);
	}

	public String newFileWithStream(File file) {
		return saveWithStream("POST", getTFS(), file);
	}

	public String overrideFileWithStream(String tfsId, File file) {
		return saveWithStream("PUT", getTFS() + "/" + tfsId, file);
	}

	/*
	 * 
	 * only handle small file, maybe smaller than 2M
	 * 
	 * 
	 */
	public String saveWithStream(String method, String url, File file) {
		String BOUNDARY = "---------------------------" + System.currentTimeMillis();
		HttpURLConnection conn = null;
		OutputStream out = null;
		InputStream in = null;

		BufferedReader respReader = null;

		try {
			URL server = new URL(url);
			conn = (HttpURLConnection) server.openConnection();

			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod(method);
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			conn.setRequestProperty("Content-Disposition", "multipart/form-data; boundary=" + BOUNDARY);
			// conn.setRequestProperty("Content-length",
			// String.valueOf(file.length()));

			out = new DataOutputStream(conn.getOutputStream());
			in = new FileInputStream(file);
			int bytes = 0;
			byte[] buf = new byte[1024];
			while ((bytes = in.read(buf)) != -1) {
				out.write(buf, 0, bytes);
			}
			out.flush();
			out.close();

			respReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuilder sbuilder = new StringBuilder();
			String rl;
			while (null != (rl = respReader.readLine())) {
				sbuilder.append(rl);
			}

			JSONObject jo = JSONObject.parseObject(sbuilder.toString());
			String tfsFileName = jo.getString("TFS_FILE_NAME");

			LOGGER.info(String.format("tfs %s to %s success, return tfsId[%s]", method, url, tfsFileName));

			return tfsFileName;
		} catch (Throwable e) {
			LOGGER.error(String.format("tfs %s to %s fail for", method, url), e);
			return null;
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(respReader);

			if (null != conn) {
				conn.disconnect();
			}
		}
	}
}
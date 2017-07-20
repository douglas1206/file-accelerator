package com.lwtx.wisdom.service;

import java.net.InetAddress;
import java.net.NetworkInterface;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ServerService {

	private static final Logger LOGGER = Logger.getLogger(ServerService.class);

	public String getMac() {
		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
			byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < mac.length; i++) {
				if (i != 0) {
					sb.append("-");
				}
				int temp = mac[i] & 0xff;
				String str = Integer.toHexString(temp);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}

			return sb.toString().toUpperCase();

		} catch (Throwable e) {
			LOGGER.error("get mac address fail for", e);
			return "get mac address fail";
		}

	}
	
	public static void main(String[] args) {
		System.out.println(new ServerService().getMac());
	}
}

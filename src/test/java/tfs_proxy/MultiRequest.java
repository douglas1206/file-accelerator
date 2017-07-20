package tfs_proxy;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class MultiRequest {

	@Test
	public void test() throws IOException, InterruptedException {
		
		int i = 40;
		while(0 < i--) {
			
			final int index = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("start test " + index);

					try {
						File destination = new File(System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis());
						destination.createNewFile();
						URL source = new URL("http://localhost:8080/v1/wisdom-pc-client/T1MyLTBKxv1RCvBVdK");
						System.out.println("test " + index + " try to copy");
						FileUtils.copyURLToFile(source, destination);
						System.out.println("test " + index + " copy part");
						
						assertTrue(FileUtils.sizeOf(destination) > 0);
						System.out.println("test " + index + " assert part");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		System.out.println("Main Thread wait");
		Thread.sleep(10000);
		System.out.println("Main Thread end wait");
	}

}

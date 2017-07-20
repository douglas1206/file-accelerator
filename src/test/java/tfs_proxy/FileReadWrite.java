package tfs_proxy;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

public class FileReadWrite {

	File file;

	@Test
	public void test() throws IOException, InterruptedException {

		file = new File(System.getProperty("java.io.tmpdir") + File.separator + "media");
		file.createNewFile();

		new Consumer(file).start();
		
		Thread.sleep(100);
		new Producer(file).start();
		
		Thread.sleep(100000);
	}

	class Producer extends Thread {

		File file;
		public Producer(File file) {
			this.file = file;
		}

		@Override
		public void run() {
			super.run();
			
			try {
				FileOutputStream o = new FileOutputStream(file);
				
				while(true) {
					Random rand = new Random();
					o.write(rand.nextInt(100));
					o.flush();
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	class Consumer extends Thread {
		File file;
		public Consumer(File file) {
			this.file = file;
		}

		
		@Override
		public void run() {
			super.run();
			
			FileInputStream i;
			try {
				i = new FileInputStream(file);
				
				System.out.println("start ");
				
				while(-1 != i.read()) {
					System.out.println(i.read());
				}
				
				System.out.println("end ");
				i.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}

	}
}

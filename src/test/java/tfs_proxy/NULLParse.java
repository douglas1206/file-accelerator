package tfs_proxy;

import static org.junit.Assert.*;

import java.io.Closeable;

import org.junit.Test;

public class NULLParse {

	@Test
	public void test() {
		Object o = null;
		
		Closeable c = (Closeable) o;
		System.out.println("i am here");
	}

}

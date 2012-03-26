package util;

import java.security.SecureRandom;
import java.util.Random;

public class SecurityUtil {

	private static final Random rand = new SecureRandom();
	
	public static byte[] getRandomBytes(int size) {
		byte[] bytes = new byte[size];
		rand.nextBytes(bytes);
		return bytes;
	}
	
}

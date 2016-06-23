package de.ps15.campusquiz;
import java.util.UUID;


import sun.misc.BASE64Encoder;

public class UUIDUtils {
	public static String generatedActiveCode() {
		UUID uuid = UUID.randomUUID();
		BASE64Encoder base64Encoder = new BASE64Encoder();
		return base64Encoder.encode(uuid.toString().getBytes());//
	}

	public static void main(String[] args) {
		System.out.println(UUIDUtils.generatedActiveCode());
	}
}
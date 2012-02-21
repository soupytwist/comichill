package models.siena;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Random;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import play.Logger;
import play.data.validation.Validation;

import siena.Id;
import siena.Model;

public class UserAuthentication extends Model {

	public static final int SALT_LENGTH = 20;
	
	@Id
	public Long id;
	
	public String hash, salt;
	
	// Default no-arguments constructor
	public UserAuthentication() {
	}
	
	public UserAuthentication(Long id, String plaintext) {
		byte[] salt = generateSalt();
		
		if (salt == null || salt.length != SALT_LENGTH) {
			Logger.error("Salt generation failed");
		}
		
		try {
			// Generate the encrypted password
			applySalt(plaintext, salt);
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e, "Password encryption failed");
		} catch (UnsupportedEncodingException e) {
			Logger.error(e, "Password encryption failed");
		}
	}
	
	public static byte[] generateSalt() {
		Random r = new SecureRandom();
		byte[] salt = new byte[SALT_LENGTH];
		r.nextBytes(salt);
		return salt;
	}
	 
	public void applySalt(String password, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		digest.update(salt);
		this.hash = Base64.encode(digest.digest(password.getBytes("UTF-8")));
		this.salt = Base64.encode(salt);
	}
	
	public boolean match(String plaintext) {
		UserAuthentication check = new UserAuthentication();
		try {
			check.applySalt(plaintext, Base64.decode(this.salt));
			return check.hash.equals(this.hash);
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e, "Password encryption failed");
		} catch (UnsupportedEncodingException e) {
			Logger.error(e, "Password encryption failed");
		} catch (Base64DecodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// If for some reason there was an exception, return false
		return false;
	}
	
	public String toString() {
		return "[UserAuthentication hash="+hash+" salt="+salt+"]";
	}
}

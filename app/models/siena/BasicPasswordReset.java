package models.siena;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

import play.Logger;

import com.ning.http.util.Base64;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Unique;
import util.SecurityUtil;

public class BasicPasswordReset extends Model {

	@Id(Generator.NONE)
	public String email;
	
	public String resetCode;
	
	public Long expireTime;
	
	public BasicPasswordReset() {
		// Default no-arguments constructor
		super();
	}
	
	public BasicPasswordReset(String email) {
		super();
		this.email = email;
	}
	
	public boolean isExpired() {
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis() > expireTime;
	}
	
	/**
	 * Generates a password reset code that is valid for a limited time
	 */
	@Override
	public void save() {
		try {
			this.resetCode = URLEncoder.encode(Base64.encode(SecurityUtil.getRandomBytes(20)), "UTF-8");
			this.resetCode = URLDecoder.decode(this.resetCode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Logger.error("URLEncoding failed for BasicPsswordReset");
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 1);
		this.expireTime = cal.getTimeInMillis();
		super.save();
	}
	
	public static BasicPasswordReset getByEmail(String email) {
		BasicPasswordReset reset = Model.getByKey(BasicPasswordReset.class, email);
		if (reset != null && reset.isExpired()) {
			Logger.debug("Password reset key is expired and being purged");
			reset.delete();
			reset = null;
		}
		return reset;
	}
	
	public static BasicPasswordReset getByCode(String code) {
		BasicPasswordReset reset = Model.all(BasicPasswordReset.class).filter("resetCode", code).get();
		if (reset != null && reset.isExpired()) {
			Logger.debug("Password reset key is expired and being purged");
			reset.delete();
			reset = null;
		}
		return reset;
	}
}

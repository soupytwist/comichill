package controllers;

import play.Logger;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import models.siena.User;
import models.siena.BasicAuthentication;

public class Authentication extends Controller {

	/**
	 * Adds the user object to the renderArgs
	 */
	@Before
    static void setuser() {
        User user = null;
        // Check if the userid is in the session
        if (session.contains("uid")) {
            try {
            	Long uid = Long.parseLong(session.get("uid"));
            	user = User.getById(uid);
            	if (user == null) {
            		Logger.error("Unable to find user with ID=%d", uid);
            	} else {
            		Logger.debug("Found logged in user %s", user.toString());
            	}
            } catch (NumberFormatException nfe) {
            	Logger.warn(nfe, "Unable to parse uid in the session; uid=%s", session.get("uid"));
            }
        } else {
        	user = getRememberedUser();
        }
        renderArgs.put("user", user);
    }
	
	/**
	 * Gets the remembered user from a cookie and sets them in the session if one exists
	 * @return The user that is active
	 */
	static User getRememberedUser() {
		User user = null;
    	Cookie remembered = request.cookies.get("RememberMe");
    	if (remembered != null) {
    		String cookieText = Crypto.decryptAES(remembered.value);
    		String[] split = cookieText.split(" ");
    		if (split.length == 2) {
    			String email = split[0];
    			try {
    				Long uid = Long.parseLong(split[1]);
    				user = User.getById(uid);
    				if (email.equals(user.email)) {
    					Logger.debug("Found logged in user via cookie %s", user.toString());
    					setAuthenticated(user, true);
    				} else {
    					user = null;
    					Logger.error("Could not authenticate user from cookie with ID=%d, email=%s", uid, email);
    				}
    			} catch (NumberFormatException nfe) {
    				Logger.warn(nfe, "Unable to parse uid in the RememberMe cookie; uid=%s", split[1]);
    			}
    		} else {
    			Logger.warn("Invalid cookie; %s", cookieText);
    		}
    	}
    	return user;
	}
	
	/**
	 * Get the logged in user
	 * @return
	 * 	The current user that is logged in
	 */
	static User connected() {
		return (User)renderArgs.get("user");
	}
	
	/**
	 * Standard login type that requests only email & password combination
	 * @param email
	 * 	The user's email address
	 * @param pass
	 *  The user's plaintext password
	 * @param rememberMe
	 *  Whether the user wants to be logged in automatically next time
	 * @return
	 * 	Whether or not the login was successful
	 */
	static boolean standardLogin(String email, String pass, boolean rememberMe) {
		// TODO Check if this is necessary, may cause problems
		//session.clear();
		
		// Get the user and his Authentication object
		User user = User.getByEmail(email);
		
		if (user!=null) {
			BasicAuthentication auth = BasicAuthentication.getByUid(user.id);
			
			// Perform the authentication
			if (auth.match(pass)) {
				Logger.debug("Authentication accepted; user %s successfully logged in", email);
				setAuthenticated(user, rememberMe);
			} else {
				Logger.debug("Authentication failed for user %s", email);
				user = null;
			}
		}
		return user != null;
	}
	
	/**
	 * Stores a user in the session as logged in
	 * @param user
	 * 	The user that has successfully authenticated and should be logged in, or guest user\
	 * @param rememberMe
	 *  Whether or not to set the RememberMe cookie for this user
	 */
	static void setAuthenticated(User user, boolean rememberMe) {
		session.put("uid", user.id);
		if (rememberMe) response.setCookie("RememberMe", Crypto.encryptAES(user.email+" "+user.id), "14d");
	}
	
	/**
	 * Ends the session and logs out the current user
	 */
	public static void logout() {
		User user = Authentication.connected();
		if (user.isGuest())
			Logger.warn("Guest user attempted to log out");
		else
			Logger.info("User has logged out %s", user.toString());
		session.clear();
		response.removeCookie("RememberMe");
		Application.index();
	}

	public static User requireLoggedIn() {
		User connected = Authentication.connected();
		if (connected == null || connected.isGuest()) {
			response.status = 401;
			renderText("You must be logged in to perform this action.");
		}
		return connected;
	}

	public static User requireAdmin() {
		User connected = Authentication.connected();
		if (connected == null || !connected.isAdmin()) {
			response.status = 401;
			renderText("You must be logged in as an administrator to perform this action.");
		}
		return connected;
	}
	
	public static User requireUser(User connected, User user) {
		if (connected.id != user.id) {
			response.status = 401;
			renderText("You are not authorized to view this content.");
		}
		return connected;
	}
	
	public static User redirectUnlessLoggedIn() {
		User connected = Authentication.connected();
		if (connected == null || connected.isGuest())
			Application.login();
		return connected;
	}
}

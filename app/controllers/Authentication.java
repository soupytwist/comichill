package controllers;

import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import models.siena.User;
import models.siena.UserAuthentication;

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
        }
        if (user == null) {
        	// If the user is not logged in, track them as a guest
            user = User.guest();
            Logger.debug("Setting guest user %s", user.toString());
            setAuthenticated(user);
        }
        renderArgs.put("user", user);
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
	 * @return
	 * 	Whether or not the login was successful
	 */
	public static boolean standardLogin(String email, String pass) {
		// TODO Check if this is necessary, may cause problems
		session.clear();
		
		// Get the user and his Authentication object
		User user = User.getByEmail(email);
		
		if (user!=null) {
			user.fetchUserAuth();
			
			// Perform the authentication
			if (user.auth.match(pass)) {
				Logger.info("Authentication accepted");
				setAuthenticated(user);
			} else {
				Logger.info("Authentication failed");
				user = null;
			}
		}
		return user != null;
	}
	
	/**
	 * Stores a user in the session as logged in
	 * @param user
	 * 	The user that has successfully authenticated and should be logged in, or guest user
	 */
	static void setAuthenticated(User user) {
		if (!user.isGuest())
			session.put("uid", user.id);
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
		Application.logout();
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

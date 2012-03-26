package controllers;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;

import notifiers.Mails;

import models.siena.BasicPasswordReset;
import models.siena.Comic;
import models.siena.Strip;
import models.siena.StripQueue;
import models.siena.Subscription;
import models.siena.User;
import models.siena.BasicAuthentication;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.With;

@With(Authentication.class)
public class Users extends Controller {
	
	public static void login(String email, String password, boolean rememberMe) {
		if (email != null) {
			checkAuthenticity();
			if (Authentication.standardLogin(email, password, rememberMe)) {
				Application.index();
			} else {
				validation.addError("password", "Your email or password was incorrect.");
				params.flash();
			}
		}
		render();
	}
	
	public static void logout() {
		Authentication.logout();
	}
	
	public static void register(
			@Required @Email String email,
			@Required @MinSize(User.PASSWORD_LENGTH_MIN) @MaxSize(User.PASSWORD_LENGTH_MAX) String password,
			String retype_password,
			boolean rememberMe) {
		
		if (email == null) {
			validation.clear();
			render();
		}
		checkAuthenticity();
		
		if (!password.equals(retype_password))
			validation.addError("retype_password", "The passwords you entered did not match");
		if (validation.hasErrors()) {
			Logger.debug("Validation errors: %s", validation.errorsMap());
			params.flash();
			render();
		}
		
		// Create the user object
		User newUser = new User(email);
		
		// First, check if the email address is already in use
		Logger.debug("Checking for existing user with email=%s", email);
		User existing = User.getByEmail(email);
		
		if (existing != null) {
			validation.addError("email", "This email address is already registered");
			Logger.warn("A user has attempted to register with an existing email address; email=%s existing=%s", email, existing.toString());
		} else {
			newUser.insert();
			if (newUser.id == -1) {
				Logger.error("User creation failed; Tried to insert the following user:\n%s", newUser.toString());
				params.flash();
				flash.put("message", "There was an error processing your request. I apologize for the inconvenience. Please try again later.");
				render();
			} else {
				// Generate the authentication object for this user
				BasicAuthentication auth = new BasicAuthentication(newUser.id, password);
				auth.insert();
			}
		}
		
		if (validation.hasErrors()) {
			Logger.debug("Validation errors exist; returning to registration page");
			params.flash();
			render();
		} else {
			Logger.info("User successfully created; %s", newUser);
			Logger.debug("Sending registration email");
			Mails.welcome(newUser);
			Logger.debug("Registration is complete; Redirecting user...");
			Authentication.setAuthenticated(newUser, rememberMe);
			Application.index();
		}
	}
	
	public static void forgotPassword(@Email String email) {
		if (Authentication.connected() != null) {
			flash.put("message", "If you're looking for a way to change your password, just click your email at the top of the page.");
			Application.index();
		}
		
		if (email == null) {
			render();
		} else if (validation.hasErrors()) {
			render();
		} else if (User.getByEmail(email) == null) {
			validation.addError("email", "The email address you entered is not registered.");
			render();
		} else {
			BasicPasswordReset reset = BasicPasswordReset.getByEmail(email);
			if (reset == null)
				reset = new BasicPasswordReset(email);
			reset.save();
			Mails.passwordResetLink(reset);
			flash.put("message", "Check your email for a link to reset your password");
		}
		Application.index();
	}
	
	public static void resetPassword(String code,
			@Required @MinSize(User.PASSWORD_LENGTH_MIN) @MaxSize(User.PASSWORD_LENGTH_MAX) String new_password,
			String retype_password) {
		if (Authentication.connected() != null) {
			flash.put("message", "You need to log out first in order to reset a password.");
			Application.index();
		}
		
		if (code != null) {
			try {
				String decodedCode = URLDecoder.decode(code, "UTF-8");
				Logger.debug("%s decoded to %s", code, decodedCode);
				BasicPasswordReset reset = BasicPasswordReset.getByCode(code);
				if (reset != null) {
					
					if (new_password == null && retype_password == null) {
						validation.clear();
						render(reset);
					}
					
					// Make sure the passwords match
					if (!new_password.equals(retype_password))
						validation.addError("retype_password", "The passwords you entered did not match");
					
					if (validation.hasErrors()) {
						render(reset);
					} else {
						// Get the user that this reset key belongs to
						User user = User.getByEmail(reset.email);
						if (user != null) {
							BasicAuthentication auth = BasicAuthentication.getByUid(user.id);
							
							// Delete the old authentication, assuming it exists
							if (auth != null) {
								auth.delete();
								Logger.debug("Deleted old BasicAuthentication for %s", user.email);
							}
							
							// Set the new password
							BasicAuthentication newAuth = new BasicAuthentication(user.id, new_password);
							newAuth.save();
							
							reset.delete();
							
							// Successful! Log them in and redirect to home page
							flash.put("message", "Your password was successfully changed! Welcome back, "+user.email+"!");
							Authentication.setAuthenticated(user, false);
							Application.index();
							
						} else {
							// This shouldn't happen, but it might be possible
							Logger.warn("Password reset was attempted, but the user's email was not found.");
							flash.put("message", "There was a problem resetting your password.");
							Mails.notifyMe("Password reset failure", "Password reset was attempted, but the user's email was not found. Email="+reset.email);
							Application.index();
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				// This (hopefully) won't happen
				Logger.error("URLDecoder failed; code="+code);
				Mails.notifyMe("Password reset failure", "URL Decoding failed, this isn't good. Code was "+code);
			}
		}
		flash.put("message", "The reset link you used is either invalid or has expired.");
		Application.index();
	}
	
	public static void accountInfo(
			@Required @Email String email,
			@Required String current_password,
			@MinSize(User.PASSWORD_LENGTH_MIN) @MaxSize(User.PASSWORD_LENGTH_MAX) String new_password,
			String retype_new_password) {
		
		// Get the logged in user
		User connected = Authentication.requireLoggedIn();
		
		// If the email field is absent, form has not been submitted yet
		if (email == null) {
			validation.clear();
			render();
		}
		// Check the authenticity token
		checkAuthenticity();
		
		// Check that the current_password is correct
		BasicAuthentication auth = BasicAuthentication.getByUid(connected.id);
		if (auth != null && current_password != null && (! auth.match(current_password)))
			validation.addError("current_password", "The password you entered is incorrect");
		
		// Check for validation errors before doing anything
		if (validation.hasErrors()) {
			Logger.debug("Validation errors: %s", validation.errorsMap());
			params.flash();
			render();
		}
		
		// If the email has changed, update it
		if (! email.equalsIgnoreCase(connected.email)) {
			// First, check if the email address is already in use
			Logger.debug("Checking for existing user with email=%s", email);
			User existing = User.getByEmail(email);
			
			if (existing != null) {
				Logger.warn("A user has attempted to update their email to an existing email address; email=%s existing=%s", email, existing.toString());
				validation.addError("email", "This email address is already in use");
				params.flash();
				render();
			} else {
				connected.email = email;
				connected.update();
			}
		}
		
		// Update the password if the user entered a new password
		if (new_password != null && new_password.length() > 0) {
			// Check that the new password matches the retyped password
			if (!new_password.equals(retype_new_password)) {
				validation.addError("retype_new_password", "The passwords you entered did not match");
			} else {
				try {
					// Set the new password
					auth.applySalt(new_password, auth.generateSalt());
					auth.save();
				} catch (Exception e) {
					// This shouldn't happen!
					Logger.error("Generating salt failed");
					e.printStackTrace();
				}
			}
		}
		
		if (validation.hasErrors()) {
			Logger.debug("Validation errors exist; returning to accountInfo page");
			params.flash();
			render();
		} else {
			Logger.info("User updated their preferences; %s", connected);
			flash.put("message", "Your account has been updated!");
			render();
		}
	}
	
	public static void getQueue(Long id) {
		User connected = Authentication.connected();
		StripQueue queue = connected.queue;
		if (queue != null) {
			queue.get();
			Strip strip = Strip.getById(id);
			queue.setCurrent(strip);
			queue.save();
			renderJSON(queue.toJSON());
		} else {
			Users.getQueue(id);
		}
	}
	
}

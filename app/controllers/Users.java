package controllers;
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
	
	public static void login(String email, String password) {
		if (email != null) {
			checkAuthenticity();
			if (Authentication.standardLogin(email, password)) {
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
	
	public static void register(@Required @Email String email, @Required @MinSize(User.PASSWORD_LENGTH_MIN) @MaxSize(User.PASSWORD_LENGTH_MAX) String password, String retype_password) {
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
			validation.keep();
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
			validation.keep();
			render();
		} else {
			Logger.info("User successfully created; %s", newUser);
			Logger.debug("Registration is complete; Redirecting user...");
			Authentication.setAuthenticated(newUser);
			Application.index();
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

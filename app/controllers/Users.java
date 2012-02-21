package controllers;
import models.siena.Comic;
import models.siena.Strip;
import models.siena.StripQueue;
import models.siena.Subscription;
import models.siena.User;
import models.siena.UserAuthentication;
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
		if (Authentication.standardLogin(email, password))
			Application.index();
		else
			Application.login();
	}
	
	public static void logout() {
		Authentication.logout();
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
	
	public static void createUser(@Required @Email String email, @Required @MinSize(User.PASSWORD_LENGTH_MIN) @MaxSize(User.PASSWORD_LENGTH_MAX) String password) {
		if (validation.hasErrors()) {
			Logger.debug("Validation errors: %s", validation.errorsMap());
			params.flash();
			validation.keep();
			Application.register();
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
			// Generate the authentication object for this user
			newUser.generateAuthentication(password);
			insertUser(newUser);
		}
		
		if (validation.hasErrors()) {
			Logger.debug("Validation errors exist; returning to registration page");
			params.flash();		
			validation.keep();
			Application.register();
		} else {
			Logger.debug("Registration is complete; Redirecting user...");
			Authentication.setAuthenticated(newUser);
			Application.index();
		}
	}
	
	static void insertUser(@Valid User user) {
		user.insert();
		if (! validation.hasErrors()) {
			Logger.info(user.auth.toString());
			Logger.info("User successfully created; %s", user);
		} else {
			Logger.debug("Inserting user failed: %s", validation.errorsMap());
		}
	}
	
}

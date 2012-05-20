package controllers;

import notifiers.Mails;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.With;

@With(Authentication.class)
public class Application extends Controller {

	public static void index() {
		render();
	}

	public static void doContact(@Required @Email String email, @Required @MaxSize(2048) String messageBody) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			Application.contact();
		}
		Mails.contactForm(email, messageBody);
		flash.put("message", "Message sent! Thanks for your feedback!");
		Application.index();
	}

	public static void home() {
		Authentication.redirectUnlessLoggedIn();
		render();
	}

	public static void all() {
		render();
	}

	public static void tags() {
		render();
	}

	public static void login() {
		render();
	}

	public static void about() {
		render();
	}

	public static void contact() {
		render();
	}

	public static void privacy() {
		render();
	}

	public static void terms() {
		render();
	}
}
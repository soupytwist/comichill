package controllers;

import play.*;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.mvc.*;
import pojo.StripNode;

import java.util.*;

import notifiers.Mails;

import models.*;
import models.siena.Comic;
import models.siena.RssStripSource;
import models.siena.StripSource;
import models.siena.Subscription;
import models.siena.User;

@With(Authentication.class)
public class Application extends Controller {
    
    public static void index() {
    	User connected = Authentication.connected();
    	if (connected != null && !connected.isGuest())
    		render();
    	else
    		renderTemplate("Application/all.html");
    }
    
    public static void about() {
    	render();
    }
    
    public static void contact() {
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
    	// User must be logged in to add a subscription
		List<Comic> comics = Comic.all().fetch();
    	render(comics);
    }

    public static void login() {
    	render();
    }
    
    public static void logout() {
    	render();
    }
    
}
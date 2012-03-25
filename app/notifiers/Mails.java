package notifiers;
 
import play.*;
import play.mvc.*;
import java.util.*;

import models.siena.User;
 
public class Mails extends Mailer {
 
	public static void contactForm(String email, String messageBody) {
		addRecipient("nick@comichill.com");
		setSubject("[CONTACT] Message from %s", email);
		setReplyTo(email);
		setFrom("ComicHill.com <contact-form@comichill.com>");
		send(email, messageBody);
	}
	
	public static void welcome(User user) {
		if (user==null) {
			Logger.warn("Tried to send an email to null user");
		} else {
			addRecipient(user.email);
			setSubject("Welcome to Comic Hill!");
			setReplyTo("no-reply@comichill.com");
			setFrom("ComicHill.com <no-reply@comichill.com>");
			send(user);
		}
	}
	
	public static void notifyMe(String sub, String body) {
		addRecipient("nick@comichill.com");
		setSubject("[NOTIFY] %s", sub);
		setReplyTo("no-reply@comichill.com");
		setFrom("Application Notification <application@comichill.com>");
		send(body);
	}
}
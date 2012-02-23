package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import models.siena.Comic;
import models.siena.Strip;
import models.siena.Subscription;
import models.siena.User;
import models.siena.StripQueue;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import util.My;

@With(Authentication.class)
public class Subscriptions extends Controller {
	
	public static void generateUpdateQueue() {
		Logger.debug("start");
		User connected = Authentication.requireLoggedIn();
		List<Subscription> subscriptions = Subscription.getByUser(connected);
		StripQueue q = new StripQueue();
		int count = StripQueue.MAX_QUEUE_SIZE;
		for (Subscription sub : subscriptions) {
			List<Strip> strips = Strip.getStripsByCid(sub.cid).order("sid").filter("sid >", sub.latest) .fetch(count);
			q.add(strips);
			if ((count = StripQueue.MAX_QUEUE_SIZE - q.size()) <= 0)
				break;
		}
		if (q.isEmpty()) {
			flash.put("message", "There are no new updates!");
			Application.index();
		}
		if (connected.queue != null)
			connected.queue.delete();
		q.save();
		connected.queue = q;
		connected.save();
		Logger.debug("done");
		Strip strip = q.getCurrent();
		Comic comic = strip.getComic();
		Viewer.viewQueue(comic.label, strip.sid);
	}
	
	public static void get(Long id) {
		User connected = Authentication.requireLoggedIn();
		Logger.debug("Getting subscription by ID=%d", id);
		Subscription sub = Subscription.getById(id);
		
		if (sub != null) {
			// Make sure the subscription belongs to this user
			Authentication.requireUser(connected, sub.owner);
			renderJSON(sub);
		} else {
			Logger.warn("Failed to get subcription with ID=%d; NOTFOUND", id);
			response.status = 404;
			renderText("Error; No subscription found for ID="+id);
		}
	}
	
    
    public static void getAll() {
    	User connected = Authentication.connected();
    	List<Subscription> subscriptions = new ArrayList<Subscription>();
    	if (connected != null && !connected.isGuest()) {
    		subscriptions = Subscription.getByUser(connected);
    	}
    	
    	renderJSON(My.mapByKey("cid", subscriptions.toArray()));
    }
    
    public static void visit(Long id) {
    	User connected = Authentication.requireLoggedIn();
    	Strip strip = Strip.getById(id);
    	Subscription sub = Subscription.getByUserAndCid(connected, strip.cid);
    	if (sub != null) {
	    	sub.bookmark = strip.sid;
	    	sub.latest = Math.max(sub.latest, strip.sid);
	    	sub.update();
    	}
    	ok();
    }
	
	public static void create(JsonObject body) {
		// User must be logged in to add a subscription
		User connected = Authentication.requireLoggedIn();
		
		// Remove the id, this will be auto-generated
		body.remove("id");
		
		Subscription sub = new Gson().fromJson(body, Subscription.class);
		Comic comic = sub.getComic();
		sub.owner = connected;
		
		if (comic == null) {
			response.status = 400;
			renderText("Could not create subscription; Invalid comic ID="+sub.cid);
		}
		
		Logger.debug("Subscriptions.create: received "+sub.toString());
		
		// First check if the user is already subscribed to this comic
		Subscription existing = Subscription.getByUserAndCid(connected, comic.id);
		
		if (existing == null) {
			sub.insert();
			renderJSON(sub);
		} else {
			// Subscription can not be created! It already exists
			response.status = 400;
			renderText("Failed to create Subscription; already exists");
		}
	}
	
	public static void update(Long id, JsonObject body) {
		// User must be logged in to add a subscription
		User connected = Authentication.requireLoggedIn();
		
		body.remove("comic");
		
		Subscription sub = new Gson().fromJson(body, Subscription.class);
		Logger.debug("Subscriptions.update: received "+sub.toString());
		
		// Get the existing subscription
		Subscription existing = Subscription.getById(id);
		
		if (existing != null) {
			Authentication.requireUser(connected, existing.owner);
			existing.updateWith(sub);
			existing.update();
			renderJSON(existing);
		} else {
			// Subscription can not be updated, It does not exist
			response.status = 400;
			renderText("Failed to update Subscription; subscription with ID="+id+" does not exist");
		}
	}
	
	public static void delete(Long id) {
		// User must be logged in to add a subscription
		User connected = Authentication.requireLoggedIn();
		
		Subscription sub = Subscription.getById(id);
		if (sub != null) {
			// Make sure the subscription belongs to this user
			Authentication.requireUser(connected, sub.owner);
			
			sub.delete();
			renderText("SUCCESS");
		} else {
			response.status = 400;
			renderText("Failed to delete Subscription; subscription with ID="+id+" does not exist");
		}
	}
}

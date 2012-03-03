package controllers;

import models.siena.Comic;
import models.siena.Strip;
import models.siena.StripQueue;
import models.siena.Subscription;
import models.siena.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import pojo.NavigationFrame;

@With(Authentication.class)
public class Viewer extends Controller {
	
	public static void viewBySid(String label, int sid) {
		Comic comic = Comic.getByLabel(label);
		
		if (comic == null) {
			flash.put("message", "The comic you are trying to view does not exist!");
			Application.index();
		}
		
		Strip strip = Strip.get(comic.id, sid);
		
		if (strip == null) {
			flash.put("message", "The strip you are trying to view does not exist!");
			Application.index();
		}
		
		view(strip, comic, false);
	}
	
	public static void viewByLabel(String label) {
		Comic comic = Comic.getByLabel(label);
		if (comic == null) {
			flash.put("message", "The comic you were trying to view doesn't exist  :(");
			Application.index();
		} else if (comic.numStrips == 0) {
			flash.put("message", "The comic you were trying to view doesn't have any strips added yet  :(");
			Application.index();
		}
		
		viewBySid(label, comic.numStrips);
	}
	
	public static void viewQueueBySid(String label, int sid) {
		Comic comic = Comic.getByLabel(label);
		
		if (comic == null) {
			flash.put("message", "The comic you are trying to view does not exist!");
			Application.index();
		}
		
		Strip strip = Strip.get(comic.id, sid);
		
		if (strip == null) {
			flash.put("message", "The strip you are trying to view does not exist!");
			Application.index();
		}
		
		view(strip, comic, true);
	}
	
	public static void viewQueue() {
		User connected = Authentication.connected();
		if (connected == null || connected.isGuest()) {
			flash.put("message", "You need to be logged in to have a queue!");
			Application.index();
		}
		
		StripQueue queue = connected.queue;
		
		if (queue != null) {
			queue.get();
			
			if (!queue.isEmpty()) {
				Strip strip = queue.getCurrent();
				Comic comic = strip.getComic();
				viewQueueBySid(comic.label, strip.sid);
			} else {
				Logger.debug("User's queue is empty");
				flash.put("message", "Your queue is empty!");
				Application.index();
			}
		} else  {
			Subscriptions.generateUpdateQueue();
		}
	}
	
	protected static void view(Strip strip, Comic comic, boolean useQueue) {
		User connected = Authentication.connected();
		NavigationFrame nav = null;
		
		if (useQueue && connected != null) {
			// Get the current queue
			StripQueue queue = connected.queue;
			if (queue != null) {
				queue.get();
				
				// If the queue is empty, forget it
				if (queue.isEmpty()) {
					Logger.debug("Queue is empty; id=%d", queue.id);
					flash.put("message", "The queue is empty!");
					Application.index();
				}
				
				// Get the strip and comic from the queue if they are not provided
				if (strip == null) {
					strip = queue.getCurrent();
					if (strip == null) {
						Logger.warn("Queue contains invalid strip id; queue id=%d", queue.id);
						flash.put("message", "An error occurred  :(");
						Application.index();
					} else {
						comic = strip.getComic();
					}
				} else if (! queue.setCurrent(strip)) { // Try to set our place in the queue
					flash.put("message", "Unable to set place in the queue!");
					Application.index();
				} else {
					// Update the user's queue
					queue.update();
				}
				
				// Generate the navigation frame
				nav = queue.getFrame();
			} else {
				Logger.warn("User tried to access nonexistant queue");
				flash.put("message", "The queue you are trying to access does not exist!");
				viewBySid(comic.label, strip.sid);
			}
		} else {
			// Get the navigation frame from the strip
			nav = new NavigationFrame(strip);
		}
		
		// Update the user's subscription if they are logged in
		if (connected != null) {
			Subscription sub = Subscription.getByUserAndCid(connected, strip.cid);
			if (sub != null)
				sub.visit(strip.sid);
		}
		
		renderTemplate("Viewer/view.html", strip, comic, nav, useQueue);
	}
	
}

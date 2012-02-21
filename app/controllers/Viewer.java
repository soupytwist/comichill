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
		User connected = Authentication.connected();
		Comic comic = Comic.getByLabel(label);
		Strip strip = Strip.get(comic.id, sid);
		
		if (strip == null) {
			flash.put("message", "The comic you are trying to view does not exist!");
			Application.index();
		}
		
		if (connected != null && !connected.isGuest()) {
			Subscription sub = Subscription.getByUserAndCid(connected, strip.cid);
			if (sub != null)
				sub.visit(strip.sid);
		}
		
		NavigationFrame nav = new NavigationFrame(strip);
		String mode = "noqueue";
		renderTemplate("Viewer/view.html", strip, comic, nav, mode);
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

	public static void viewQueue(String label, int sid) {
		User connected = Authentication.connected();
		if (connected == null || connected.isGuest())
			viewBySid(label, sid);
		
		StripQueue queue = connected.queue;
		
		if (queue != null) {
			queue.get();
			
			if (!queue.isEmpty()) {
				// TODO Implement error-checking for nulls
				Comic comic = Comic.getByLabel(label);
				Strip strip = Strip.get(comic.id, sid);
				
				if (queue.setCurrent(strip)) {
					Logger.debug("Queued successfully");
					queue.update();
					NavigationFrame nav = queue.getFrame();
					String mode = "queue";
					renderTemplate("Viewer/view.html", strip, comic, nav, mode);
				} else {
					Logger.warn("Failed to set position in queue; strip.id=%d", strip.id);
					viewBySid(label, sid);
				}
			} else {
				Logger.debug("User's queue is empty");
				viewBySid(label, sid);
			}
		} else  {
			Logger.debug("User tried to access nonexistant queue");
			viewBySid(label, sid);
		}
	}
	
	/*
	public static void viewBySid(String label, int sid) {
		User connected = Authentication.connected();
		boolean useQueue = session.get("useQueue") != null;
		Comic comic = Comic.getByLabel(label);
		Strip strip = Strip.get(comic.id, sid);
		
		if (strip == null) {
			flash.put("message", "The comic you are trying to view does not exist!");
			Application.index();
		}
		
		NavigationFrame nav;
		
		if (connected != null && !connected.isGuest()) {
			Subscription sub = Subscription.getByUserAndCid(connected, strip.cid);
	    	sub.bookmark = strip.sid;
	    	sub.latest = Math.max(sub.latest, strip.sid);
	    	sub.update();
	    	
			StripQueue queue = connected.queue;
			if (useQueue && queue != null) {
				queue.get();
				if (!queue.isEmpty() && queue.setCurrent(strip)) {
					Logger.debug("QUEUED");
					nav = queue.getFrame();
				} else {
					Logger.debug("QUEUE IS EMPTY");
					nav = new NavigationFrame(strip);
				}
			} else {
				Logger.debug("NO QUEUE");
				nav = new NavigationFrame(strip);
			}
		} else {
			Logger.debug("NOT LOGGED IN");
			nav = new NavigationFrame(strip);
		}
		
		renderTemplate("Viewer/view.html", strip, nav);
	}
	
	public static void view(String label, int sid) {
		Comic comic = Comic.getByLabel(label);
		System.err.println(comic);
		
		if (comic != null) {
			System.err.println(Strip.getStrip(comic, sid));
			
			Strip strip = Strip.getStrip(comic, sid);
			if (strip != null) {
				view(strip);
			} else {
				view(label, 1);
			}
		}
	}
	
	public static void viewStripById(Long id) {
		if (id != null) {
			Strip strip = Strip.getStripById(id);
			System.out.println(strip.comic);
			strip.comic.get();
			Comic comic = strip.comic;
			view(strip);
		}
	}*/
	
}

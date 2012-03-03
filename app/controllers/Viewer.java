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
		
		if (comic == null) {
			flash.put("message", "The comic you are trying to view does not exist!");
			Application.index();
		}
		
		Strip strip = Strip.get(comic.id, sid);
		
		if (strip == null) {
			flash.put("message", "The strip you are trying to view does not exist!");
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
				Comic comic = Comic.getByLabel(label);
				
				if (comic == null) {
					flash.put("message", "The comic you are trying to view does not exist!");
					Application.index();
				}
				
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
				flash.put("message", "Your queue is empty!");
				viewBySid(label, sid);
			}
		} else  {
			Logger.debug("User tried to access nonexistant queue");
			flash.put("message", "Your queue is empty!");
			viewBySid(label, sid);
		}
	}
	
}

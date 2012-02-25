package controllers;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.Logger;
import play.data.binding.Binder;
import play.mvc.Controller;
import pojo.StripNode;
import util.Serializers;

import models.siena.ArchiveStripSource;
import models.siena.Comic;
import models.siena.RssStripSource;
import models.siena.StripSource;

public class RssController extends Controller {
	
	public static void fetch(JsonObject body) {
		try {
			RssStripSource rss = Serializers.gson.fromJson(body, RssStripSource.class);
			Logger.debug("Fetching RssStripSource: %s", rss.toString());
			
			List<StripNode> strips = null;
			
			if (rss != null) {
				strips = rss.load();
			}
			
			renderJSON(Serializers.gson.toJson(strips));
		} catch (Exception e) {
			Logger.error("Error fetching RSS feed;\n%s", e.toString());
			response.status = 400;
			renderText("ERROR");
		}
	}
	
	public static void get(Long id) {
		RssStripSource rss = RssStripSource.getById(id);
		renderJSON(Serializers.gson.toJson(rss));
	}
	
	public static void create(JsonObject body) {
		try {
			RssStripSource rss = Serializers.gson.fromJson(body, RssStripSource.class);
			Logger.debug("Got RssStripSource: %s", rss.toString());
			
			if (rss != null && validation.valid(rss).ok) {
				rss.insert();
				renderJSON(Serializers.gson.toJson(rss));
			} else {
				response.status = 400;
				renderText("ERROR");
			}
		} catch (Exception e) {
			Logger.error("Error creating RSS feed;\n%s", e.toString());
			response.status = 400;
			renderText("ERROR");
		}
	}
	
	public static void update(Long id, JsonObject body) {
		try {
			Logger.debug("Received RSS update object: %s", body.toString());
			RssStripSource rss = Serializers.gson.fromJson(body, RssStripSource.class);
			RssStripSource existing = RssStripSource.getById(rss.id);
			
			if (rss != null && existing != null) {
				if (validation.valid(rss).ok) {
					// Record the current state
					String old = existing.toString();
					
					// Update the fields
					existing.updateWith(rss);
					
					// Persist the changes
					existing.update();
					
					Logger.info("Updating rss %d: %s -> %s", rss.id, old, existing.toString());
					renderJSON(Serializers.gson.toJson(rss));
				} else {
					Logger.error("Error updating RSS feed; changes failed to validate");
					response.status = 400;
					renderText("ERROR");
				}
			} else {
				Logger.error("Error updating RSS feed; Models are missing");
				response.status = 400;
				renderText("ERROR");
			}
		} catch (Exception e) {
			Logger.error("Error updating RSS feed;\n%s", e.toString());
			response.status = 400;
			renderText("ERROR");
		}
	}
}
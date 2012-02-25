package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import play.Logger;
import play.data.validation.Required;
import play.mvc.Controller;
import pojo.StripNode;
import models.siena.Comic;
import models.siena.Strip;
import models.siena.StripQueue;
import models.siena.User;

import util.My;
import util.Serializers;

public class Strips extends Controller {
	
	public static void get(Long id) {
		Logger.debug("Getting strip by id %d", id);
		Strip strip = Strip.getById(id);
		Comic comic = strip.getComic();
		renderJSON(Serializers.gson.toJson(My.map(strip, comic)));
	}
	
	public static void getBySid(String label, int sid) {
		Comic comic = Comic.getByLabel(label);
		if (comic != null) {
			Strip strip = Strip.get(comic.id, sid);
			renderJSON(Serializers.gson.toJson(strip));
		}
	}
	
	public static void getByComic(String label) {
		Comic comic = Comic.getByLabel(label);
		List<Strip> strips = Strip.getStripsByCid(comic.id).order("sid").fetch();
		renderJSON(Serializers.gson.toJson(strips));
	}
	
	public static void getQueue(Long id) {
		StripQueue queue = new StripQueue();
		Strip strip = Strip.getById(id);
		queue.add(Strip.getStripsByCid(strip.cid).fetch());
		queue.setCurrent(strip);
		renderJSON(Serializers.gson.toJson(queue.toJSON()));
	}
	
	public static String bulkImport(List<StripNode> nodes, Comic comic) {
		int created = 0;
		
		try {
			
			for (StripNode nodeCheck : nodes) {
				// TODO Implement Strip.getWithUrl to filter which comic
				Strip existing = Strip.getWithUrl(comic.id, nodeCheck.url);
				if (existing != null) {
					response.status = 400;
					return "Error for comic "+comic.label+" - A strip with URL="+nodeCheck.url+" already exists;\n"+existing;
				}
			}
			
			for (StripNode node : nodes) {
				Strip s = comic.newStrip(node.url, node.title);
				s.save();
				comic.save();
				created++;
			}
		} catch (Exception e) {
			response.status = 400;
			return "ERROR. There was an unexpected error; "+created+" strips were added for comic "+comic.label+".\n\nException: "+e;
		}
		
		return "Done. "+created+" strips were added for comic "+comic.label+".";
	}
}

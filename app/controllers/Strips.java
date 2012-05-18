package controllers;

import java.util.List;

import models.siena.Comic;
import models.siena.Strip;
import models.siena.StripQueue;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import pojo.StripNode;
import util.My;
import util.Serializers;

import com.google.gson.JsonObject;

@With(Authentication.class)
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

	public static void create(JsonObject body) {
		Authentication.requireAdmin();
		Strip stripData = Serializers.gson.fromJson(body, Strip.class);
		Comic comic = stripData.getComic();
		if (comic == null) {
			error("The comic with cid="+stripData.cid+" does not exist");
		}

		// Check if the strip with this URL already exists
		Strip existing = Strip.getWithUrl(comic.id, stripData.url);
		if (existing != null) {
			error("A strip with URL="+stripData.url+" already exists");
		} else {
			Strip s = null;
			try {
				s = comic.newStrip(stripData.url, stripData.title);
				s.save();
				comic.save();
				renderJSON(s);
			} catch (Exception e) {
				Logger.error("Unexpected error creating a new strip: strip=%s\n%s", s, e.getMessage());
				error("There was an unexpected error! See the logs for more information.");
			}
		}
	}

	public static String bulkImport(List<StripNode> nodes, Comic comic) {
		int created = 0;

		try {
			for (StripNode nodeCheck : nodes) {
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

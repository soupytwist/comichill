package controllers;

import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.Logger;
import play.cache.Cache;
import play.data.binding.Binder;
import play.mvc.Controller;
import play.libs.F;
import play.libs.F.T2;
import pojo.StripNode;
import util.JsonObjectBinder;
import util.Serializers;

import models.siena.ArchiveStripSource;
import models.siena.Comic;
import models.siena.ArchiveStripSource;
import models.siena.StripSource;

public class ArchiveController extends Controller {
	
	public static void fetch(JsonObject body) {
		try {
			body.remove("created");
			body.remove("updated");
			
			ArchiveStripSource archive = new Gson().fromJson(body, ArchiveStripSource.class);
			Logger.debug("Got ArchiveStripSource: %s", archive.toString());
			
			List<StripNode> strips = null;
			if (archive != null) {
				strips = archive.load();
			}
			
			renderJSON(strips);
		} catch (Exception e) {
			Logger.error("Error fetching archive links;\n%s", e.toString());
			response.status = 400;
			renderJSON("ERROR");
		}
	}
	
	public static void get(Long id) {
		ArchiveStripSource archive = ArchiveStripSource.getById(id);
		renderJSON(archive);
	}
	
	public static void create(JsonObject body) {
		try {
			body.remove("created");
			body.remove("updated");
			body.remove("id");
			
			ArchiveStripSource archive = new Gson().fromJson(body, ArchiveStripSource.class);
			
			Logger.debug("Got ArchiveStripSource: %s", archive.toString());
			
			if (archive != null && validation.valid(archive).ok) {
				archive.insert();
				renderJSON(archive);
			} else {
				response.status = 400;
				renderJSON("ERROR");
			}
		} catch (Exception e) {
			Logger.error("Error creating archive;\n%s", e.toString());
			response.status = 400;
			renderJSON("ERROR");
		}
	}
	
	public static void update(Long id, JsonObject body) {
		try {
			// TODO More error checking in update
			Logger.debug("Received RSS update object: %s", body.toString());
			body.remove("created");
			body.remove("updated");
			ArchiveStripSource archive = new Gson().fromJson(body, ArchiveStripSource.class);
			ArchiveStripSource existing = ArchiveStripSource.getById(archive.id);
			Logger.debug(existing.toString()+"\n"+archive.toString());
			
			if (archive != null && existing != null) {
				if (validation.valid(archive).ok) {
					// Record the current state
					String old = existing.toString();
					
					// Update the fields
					existing.updateWith(archive);
					
					// Persist the changes
					existing.update();
					
					Logger.info("Updated archive %d: %s -> %s", archive.id, old, existing.toString());
					renderJSON(archive);
				} else {
					Logger.error("Error updating archive; changes failed to validate");
					response.status = 400;
					renderJSON("ERROR");
				}
			} else {
				Logger.error("Error updating archive; Models are missing");
				response.status = 400;
				renderJSON("ERROR");
			}
		} catch (Exception e) {
			Logger.error("Error updating archive;\n%s", e.toString());
			response.status = 400;
			renderJSON("ERROR");
		}
	}
}
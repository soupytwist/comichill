package controllers;

import java.util.List;

import org.apache.log4j.Priority;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import models.siena.Comic;
import models.siena.RssStripSource;
import models.siena.Subscription;

import play.Logger;
import play.data.validation.Required;
import play.mvc.Controller;
import siena.Query;
import util.My;

public class Comics extends Controller {
	
	public static void index(String label) {
		Comic comic = Comic.getByLabel(label);
		render(comic);
	}
	
    public static void getAll() {
		List<Comic> comics = Comic.all().fetch();
		Logger.debug("Found %d comics", comics.size());
    	renderJSON(My.mapByKey("id", comics.toArray()));
    }
    
	// REST API
	public static void getByLabel(String label) {
		Logger.debug("Getting comic by label %s", label);
		renderJSON(Comic.getByLabel(label));
	}
	
	public static void getByTag(String tag) {
		List<Comic> comics = Comic.all().search( "|"+tag+"|", "tags").fetch();
		renderJSON(My.mapByKey("id", comics.toArray()));
	}
	
	public static void get(Long id) {
		Logger.debug("Getting comic by id %d", id);
		renderJSON(Comic.getById(id));
	}
	
	public static void create(JsonObject body) {
		// First check if the comic already exists
		body.remove("created");
		body.remove("updated");
		body.remove("id");
		Comic comic = new Gson().fromJson(body, Comic.class);
		Comic existing = Comic.getByLabel(comic.label);
		Logger.debug("Comics.create: received "+comic.toString());
		
		if (existing == null) {
			// Check validation
			if (validation.valid(comic).ok) {
				// Insert the comic
				Logger.info("Creating comic %s %s", comic.label, comic.toString());
				comic.insert();
				renderJSON(comic);
			} else {
				// TODO Improve method for returning validation errors
				response.status = 400;
				renderJSON(validation.errorsMap());
			}
		} else {
			// Comic can not be created! It already exists
			response.status = 400;
			renderText("Could not create comic "+comic.toString()+"; A comic with this label already exists.");
		}
	}
	
	public static void update(Long id, JsonObject body) {
		body.remove("created");
		body.remove("updated");
		Comic comic = new Gson().fromJson(body, Comic.class);
		Comic existing = Comic.getById(id);
		Logger.debug("Comics.update: received "+comic.toString());
		
		if (existing != null) {
			// Check validation
			if (validation.valid(comic).ok) {
				// Store the old information for this comic for the log
				String old = existing.toString();
				
				// Update the fields
				existing.updateWith(comic);
				
				// Persist in the database
				Logger.info("Updating comic %s: %s -> %s", comic.label, old, existing.toString());
				existing.update();
				renderJSON(existing);
			} else {
				response.status = 400;
				renderJSON(validation.errorsMap());
			}
		} else {
			// Comic can not be created! It already exists
			response.status = 400;
			renderText("Could not create comic "+comic.toString()+"; A comic with this label already exists.");
		}
	}
	
	public static void delete(Long id) {
		Comic comic = Comic.getById(id);
		if (comic != null) {
			comic.delete();
			renderText("SUCCESS");
		} else {
			response.status = 400;
			renderText("ERROR Comic does not exist");
		}
	}
	
	public static void list() {
		List<Comic> comics = Comic.all().fetch();
		Logger.debug("There are %d comics", comics.size());
		render(comics);
	}
	
}

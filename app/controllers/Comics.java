package controllers;

import java.util.List;

import models.siena.Comic;
import play.Logger;
import play.mvc.Controller;
import util.My;
import util.Serializers;

import com.google.gson.JsonObject;

public class Comics extends Controller {

	public static void index(String label) {
		Comic comic = Comic.getByLabel(label);
		render(comic);
	}

	public static void getAll() {
		List<Comic> comics = Comic.allEnabled().fetch();
		Logger.debug("Found %d comics", comics.size());
		renderJSON(Serializers.gson.toJson(My.mapByKey("id", comics.toArray())));
	}

	public static void getByLabel(String label) {
		Logger.debug("Getting comic by label %s", label);
		renderJSON(Serializers.gson.toJson(Comic.getByLabel(label)));
	}

	public static void getByTag(String tag) {
		List<Comic> comics = Comic.all().search( "|"+tag+"|", "tags").fetch();
		renderJSON(Serializers.gson.toJson(My.mapByKey("id", comics.toArray())));
	}

	public static void get(Long id) {
		Logger.debug("Getting comic by id %d", id);
		renderJSON(Serializers.gson.toJson(Comic.getById(id)));
	}

	public static void create(JsonObject body) {
		// First check if the comic already exists
		Comic comic = Serializers.gson.fromJson(body, Comic.class);
		Logger.debug("Comics.create: received "+comic.toString());
		Comic existing = Comic.getByLabel(comic.label);

		if (existing == null) {
			// Check validation
			if (validation.valid(comic).ok) {
				// Insert the comic
				Logger.info("Creating comic %s %s", comic.label, comic.toString());
				comic.insert();
				renderJSON(Serializers.gson.toJson(comic));
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
		Comic comic = Serializers.gson.fromJson(body, Comic.class);
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
				renderJSON(Serializers.gson.toJson(existing));
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

}

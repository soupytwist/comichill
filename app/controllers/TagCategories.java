package controllers;

import models.siena.TagCategory;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@With(Authentication.class)
public class TagCategories extends Controller {

	public static void create(JsonObject body) {
		// Ensure Admin user is connected
		Authentication.requireAdmin();

		// Remove the id, this will be auto-generated
		body.remove("id");

		TagCategory cat = new Gson().fromJson(body, TagCategory.class);

		Logger.debug("TagCategories.create: received "+cat.toString());
		cat.insert();
		renderJSON(cat);
	}

	public static void update(Long id, JsonObject body) {
		// Ensure Admin user is connected
		Authentication.requireAdmin();

		TagCategory cat = new Gson().fromJson(body, TagCategory.class);
		cat.id = id;

		Logger.debug("TagCategories.update: received "+cat.toString());
		cat.update();
		renderJSON(cat);
	}

	public static void delete(Long id) {
		// Ensure Admin user is connected
		Authentication.requireAdmin();

		TagCategory cat = TagCategory.getById(id);
		if (cat != null) {
			cat.delete();
			renderText("SUCCESS");
		} else {
			response.status = 400;
			renderText("Invalid Category id");
		}
	}
}

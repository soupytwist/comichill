package controllers;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.Logger;
import play.data.binding.Binder;
import play.mvc.Controller;
import pojo.StripNode;

import models.siena.ArchiveStripSource;
import models.siena.Comic;
import models.siena.RssStripSource;
import models.siena.StripSource;

public class StripSourceController extends Controller {
	
	public static void delete(Long id) {
		Logger.debug("Going to delete strip source with id=%d", id);
		StripSource ss = StripSource.getById(id);
		
		if (ss != null) {
			Logger.warn("Deleting a StripSource: %s", ss.toString());
			ss.delete();
			ok();
		} else {
			response.status = 400;
			renderText("ERROR StripSource does not exist; Invalid id");
		}
	}
}
package controllers;

import java.util.List;

import com.google.gson.JsonObject;

import models.siena.Comic;
import models.siena.Strip;

import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;
import pojo.StripNode;

public class CommitController extends Controller {

	public static void commitStrips(String commitKey) {
		try {
			List<StripNode> nodes = (List<StripNode>) Cache.get(commitKey);
			
			if (nodes == null) {
				response.status = 400;
				renderText("Invalid key");
			}
			String keyTemp = commitKey.substring(8);
			int uscore = keyTemp.indexOf('_');
			keyTemp = keyTemp.substring(0, uscore);
			Comic comic = Comic.getById(Long.parseLong(keyTemp));
			renderText(Strips.bulkImport(nodes, comic));
			
		} catch (IndexOutOfBoundsException oob) {
			Logger.error("Malformed key passed to CommitController.commitStrips; %s", commitKey);
			response.status = 400;
			renderText("Malformed key; "+commitKey);
		} catch (NumberFormatException nfe) {
			Logger.error("Malformed key passed to CommitController.commitStrips; %s", commitKey);
			response.status = 400;
			renderText("Malformed key; Invalid Comic id; "+commitKey);
		} catch (Exception e) {
			Logger.error("CommitController.commitStrips failed; key is %s", commitKey);
			response.status = 400;
			renderText("Operation failed; key is "+commitKey);
		}
	}
}

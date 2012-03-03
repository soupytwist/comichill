package jobs;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.siena.Comic;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Scope.RenderArgs;
import play.mvc.results.RenderTemplate;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.My;
import util.Serializers;

public class ComicCacher extends Job {

	public void doJob() {
    	// Create the comics.js file
        try {
        	Logger.info("[COMICCACHER] Starting comicCache.js file creation");
        	Template template = TemplateLoader.load("Data/comicCache.js");
        	List<Comic> comics = Comic.all().fetch();
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("jsonData", Serializers.gson.toJson(My.mapListByKey("id", comics)));
        	RenderTemplate parsed = new RenderTemplate(template, map);
        	
        	File cacheFile = Play.getFile("public/javascripts/comicCache.js");
        	FileWriter fw = new FileWriter(cacheFile);
        	fw.write(parsed.getContent());
        	fw.close();
        	Logger.info("[COMICCACHER] Finished creating cacheFile.js file");
        } catch (Exception e) {
        	// Couldn't open the file for writing, put an error!
        	Logger.error("[COMICCACHER] Creating cacheFile.js file failed; %s", e.getMessage());
        	e.printStackTrace();
        }
	}
	
}

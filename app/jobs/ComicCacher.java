package jobs;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
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

public class ComicCacher extends TrackedJob {

	public static final int JOB_ID = 2;
	
	public void doTrackedJob() {
    	// Create the comics.js file
        try {
        	Logger.info("[COMICCACHER] Starting comics.js file creation");
        	Template template = TemplateLoader.load("Data/jsvar.js");
        	List<Comic> comics = Comic.all().fetch();
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("jsVar", "sys_comics"); 
        	map.put("jsonData", Serializers.gson.toJson(My.mapListByKey("id", comics)));
        	RenderTemplate parsed = new RenderTemplate(template, map);
        	
        	File cacheFile = Play.getFile("public/javascripts/cache/comics.js");
        	FileWriter fw = new FileWriter(cacheFile);
        	fw.write(parsed.getContent());
        	fw.close();
        	Logger.info("[COMICCACHER] Finished creating comics.js file");
        	track("Successful", "health", 1);
        } catch (Exception e) {
        	// Couldn't open the file for writing, put an error!
        	Logger.error("[COMICCACHER] Creating comics.js file failed; %s", e.getMessage());
        	track("Failed; "+e.getMessage(), "health", -1);
        }
	}
	
	@Override
	public int getJobId() {
		return JOB_ID;
	}
	
}
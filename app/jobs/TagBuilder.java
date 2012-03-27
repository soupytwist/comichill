package jobs;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
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

public class TagBuilder extends TrackedJob {
	
	public static final int JOB_ID = 1;
	
	public void doJob() {
		startTracking();
		
    	// Create the tags.js file
        try {
        	Logger.info("[TAGBUILDER] Starting tags.js file creation");
        	Template template = TemplateLoader.load("Data/jsvar.js");
        	List<Comic> comics = Comic.all().fetch();
        	
        	Map<String, List<Long>> byTag = new HashMap<String, List<Long>>();
        	
        	// Iterate through the comics
        	for (Comic comic : comics) {
        		// Iterate through the tags for a comic
        		String[] tags = comic.tagsAsList();
        		for (String tag : tags) {
        			// If there are no comics here yet, create a list
					if (!byTag.containsKey(tag))
						byTag.put(tag, new ArrayList<Long>());
					
					// Add the comic's id to this tag
					byTag.get(tag).add(comic.id);
        		}
        	}
        	
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("jsVar", "sys_tags");
        	map.put("jsonData", Serializers.gson.toJson(byTag));
        	RenderTemplate parsed = new RenderTemplate(template, map);
        	
        	File cacheFile = Play.getFile("public/javascripts/cache/tags.js");
        	FileWriter fw = new FileWriter(cacheFile);
        	fw.write(parsed.getContent());
        	fw.close();
        	Logger.info("[TAGBUILDER] Finished creating tags.js file");
        	track("Successful", "health", 1);
        } catch (Exception e) {
        	// Couldn't open the file for writing, put an error!
        	Logger.error("[TAGBUILDER] Creating tags.js file failed; %s", e.getMessage());
        	track("Failed; "+e.getMessage(), "health", -1);
        }
        endTracking();
	}
	
	@Override
	public int getJobId() {
		return JOB_ID;
	}
	
}

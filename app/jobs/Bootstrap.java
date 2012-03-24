package jobs;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.HashMap;

import models.siena.JobResult;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.results.RenderTemplate;
import play.templates.Template;
import play.templates.TemplateLoader;

@OnApplicationStart
public class Bootstrap extends TrackedJob {
	
    public void doJob() {
    	startTracking();
    	
    	// Create the routes.js file with URLs for the REST API / AJAX
        try {
        	Logger.info("[BOOTSTRAP] Starting jsroutes file creation");
        	Template template = TemplateLoader.load("Data/jsroutes.js");
        	RenderTemplate parsed = new RenderTemplate(template, new HashMap<String, Object>());
        	
        	File jsroutesFile = Play.getFile("public/javascripts/routes.js");
        	FileWriter fw = new FileWriter(jsroutesFile);
        	fw.write(parsed.getContent());
        	fw.close();
        	Logger.info("[BOOTSTRAP] Finished creating jsroutes file");
        	track("Successful", "health", 1);
        } catch (Exception e) {
        	// Couldn't open the file for writing, put an error!
        	Logger.error("[BOOTSTRAP] Creating jsroutes file failed; %s", e.getMessage());
        	track("Failed; "+e.getMessage(), "health", -1);
        }
        endTracking();
        
    	// Call the ComicCacher job
        new ComicCacher().now();
    }

	@Override
	public int getJobId() {
		return 0;
	}
    
}
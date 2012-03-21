package jobs;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.results.RenderTemplate;
import play.templates.Template;
import play.templates.TemplateLoader;

@OnApplicationStart
public class Bootstrap extends Job {
    
	public static Calendar lastRun = null;
	
	public static String status = "not run";
	
    public void doJob() {
    	// Tracking
    	lastRun = Calendar.getInstance();
    	
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
        	status = "Successful";
        } catch (Exception e) {
        	// Couldn't open the file for writing, put an error!
        	Logger.error("[BOOTSTRAP] Creating jsroutes file failed; %s", e.getMessage());
        	status = "Failed";
        }
        
    	// Call the ComicCacher job
        new ComicCacher().now();
    }
    
}
package jobs;

import java.io.File;
import java.io.FileWriter;
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
    
    public void doJob() {
    	// Create the routes.js file with URLs for the REST API / AJAX
        try {
        	Logger.info("[BOOTSTRAP] Starting jsroutes file creation");
        	Template template = TemplateLoader.load("jsroutes.html");
        	RenderTemplate parsed = new RenderTemplate(template, new HashMap<String, Object>());
        	File jsroutesFile = new File("public/javascripts/routes.js");
        	FileWriter fw = new FileWriter(jsroutesFile);
        	fw.write(parsed.getContent());
        	fw.close();
        	Logger.info("[BOOTSTRAP] Finished creating jsroutes file");
        } catch (Exception e) {
        	// Couldn't open the file for writing, put an error!
        	Logger.error("[BOOTSTRAP] Creating jsroutes file failed; %s", e.getMessage());
        }
    }
    
}
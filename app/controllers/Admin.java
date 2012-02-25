package controllers;

import models.siena.ArchiveStripSource;
import models.siena.Comic;
import models.siena.RssStripSource;
import models.siena.StripSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;

import jobs.Judgement;
import jobs.RssUpdater;

import pojo.StripNode;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.With;

@With(Authentication.class)
public class Admin extends Controller {
		
	public static void addRssStripSource(String comic_label, String src, String linkTag, String titleTag, String pattern) {
		Authentication.requireAdmin();
		Comic comic = Comic.getByLabel(comic_label);
		if (comic == null) {
			render();
		}
	}
	
	public static void adminPanel() {
		Authentication.requireAdmin();
		List<Comic> comics = Comic.all().fetch();
		render(comics);
	}
	
	public static void editComic(String label) {
		Authentication.requireAdmin();
		Comic comic = null;
		Boolean found = false;
		List<RssStripSource> feeds;
		List<ArchiveStripSource> archives;
		if (label != null) {
			// Retrieve the comic information if it is available
			comic = Comic.getByLabel(label);
			
			// If the comic is found, log that it is being edited
			if (comic != null) {
				found = true;
				Logger.info("Editing comic "+comic.id+": "+comic.label);
				feeds = RssStripSource.getByComic(comic.id);
				archives = ArchiveStripSource.getByComic(comic.id);
				render(comic, feeds, archives, found);
			}
		}
		
		comic = new Comic();
		comic.label = label;
		feeds = new ArrayList<RssStripSource>();
		archives = new ArrayList<ArchiveStripSource>();
		render(comic, feeds, archives, found);
	}
	
	public static void uploadBanner(String label) {
		Authentication.requireAdmin();
		render(label);
	}
	
	public static void doUploadBanner(String label, File img) {
		Authentication.requireAdmin();
		if (img == null) {
			flash.put("message", "There was a problem uploading.");
			Admin.uploadBanner(label);
		}
		try {
			String basePath = Play.configuration.getProperty("banner.path");
			
			try {
				File banner = new File(basePath+"/"+label+".png");
				FileUtils.copyFile(img, banner);
			} catch (Exception e) {
				flash.put("message", "Error writing to the file");
				Logger.warn("There was an error uploading a banner for %s;\nError details: %s", label, e);
				Admin.uploadBanner(label);
			}
			
			flash.put("message", "Upload successful!");
			Admin.editComic(label);
		} catch (Exception e) {
			flash.put("message", "There was a problem uploading.");
			Logger.warn("There was an error uploading a banner for %s;\nError details: %s", label, e);
			Admin.uploadBanner(label);
		}
	}
	
	public static void loadArchive(Long id) {
		Authentication.requireAdmin();
		ArchiveStripSource archive = ArchiveStripSource.getById(id);
		
		if (archive != null) {
			Comic comic = archive.getComic();
			
			try {
				List<StripNode> nodes = archive.load();
				String commitKey = "archive_"+comic.id+"_"+new Date().getTime();
				
				// Cache the result so that it does not need to be resent or recalculated
				Cache.set(commitKey, nodes, "5mn");
				
				render(archive, comic, nodes, commitKey);
			} catch (Exception e) {
				Logger.error("Failed to load  archive with id: %d;\n%s", archive.id, e);
				response.status = 400;
				renderText("Error: Failed to load archive\n"+archive+"\n\nException:\n"+e);
			}
		} else {
			response.status = 400;
			renderText("The archive with ID="+id+" does not exist.");
		}
	}
	
	public static void doUpdates() {
		Authentication.requireAdmin();
		List<RssStripSource> feeds = RssStripSource.getAllEnabled();
		new RssUpdater().now();
		render(feeds);
	}
	
	public static void doJudgement() {
		Authentication.requireAdmin();
		new Judgement().now();
		flash.put("message", "Judgement has begun...");
		Application.index();
	}
}

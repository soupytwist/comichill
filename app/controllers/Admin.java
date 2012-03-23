package controllers;

import models.siena.ArchiveStripSource;

import models.siena.Comic;
import models.siena.JobResult;
import models.siena.RssStripSource;
import models.siena.StripSource;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jobs.RssUpdater;
import org.apache.commons.io.FileUtils;

import jobs.Bootstrap;
import jobs.ComicCacher;
import jobs.Judgement;
import jobs.RssUpdater;
import jobs.TagBuilder;

import pojo.StripNode;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;

import au.com.bytecode.opencsv.CSVReader;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.binding.As;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.With;
import util.My;
import util.Serializers;

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
		Map<Object, Object> rssFeeds = My.mapByKey("cid", RssStripSource.all().fetch().toArray());
		Map<Integer, Object> results = My.map(JobResult.getByJobId(0), JobResult.getByJobId(1), JobResult.getByJobId(2), JobResult.getByJobId(3), JobResult.getByJobId(4));
		render(comics, rssFeeds, results);
	}
	
	public static void getJobResultsChart(int jobId, @As(",") String[] params) {
		try {
			List<JobResult> results = JobResult.getByJobId(jobId);
			String result = "[ ";
			
			int r = 0;
			for (JobResult jr : results) {
				if (r++ != 0)
					result += ",";
				result += "["+jr.startTime;
				for (String param : params) {
					Field f = JobResult.class.getField(param);
					result += ","+f.getInt(jr);
				}
				result += " ]";
			}
			result += " ]";
			
			renderJSON(result);
		} catch (Exception e) {
			
		}
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
	
	public static void uploadBanner(String label, File img) {
		Authentication.requireAdmin();
		if (img == null) {
			flash.put("message", "There was a problem uploading.");
		} else {
			try {
				//String basePath = Play.configuration.getProperty("banner.path");
				
				try {
					File banner = Play.getFile("data/banners/"+label+".png");
					FileUtils.copyFile(img, banner);
					flash.put("message", "Upload successful!");
				} catch (Exception e) {
					flash.put("message", "Error writing to the file");
					Logger.warn("There was an error uploading a banner for %s;\nError details: %s", label, e);
				}
			} catch (Exception e) {
				flash.put("message", "There was a problem uploading.");
				Logger.warn("There was an error uploading a banner for %s;\nError details: %s", label, e);
			}
		}
		
		Admin.editComic(label);
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
	
	public static void importCSV(String label, File csv) {
		Comic comic = Comic.getByLabel(label);
		if (comic == null) {
			flash.put("message", "The comic with label "+label+" does not exist!");
			Admin.editComic(label);
		}
		
		if (csv == null) {
			flash.put("message", "No file was received!");
			Admin.editComic(label);
		}
		
		List<StripNode> nodes = new ArrayList<StripNode>();
		
		try {
			CSVReader in = new CSVReader(new FileReader(csv));
			String line[];
			while ((line = in.readNext()) != null && line.length >= 2) {
				StripNode node = new StripNode(line[0], line[1]);
				nodes.add(node);
			}
			
			// Cache the result so that it does not need to be resent or recalculated
			String commitKey = "csv_"+comic.id+"_"+new Date().getTime();
			Cache.set(commitKey, nodes, "5mn");
			
			render(nodes, comic, commitKey);
			
		} catch (Exception e) {
			flash.put("message", "There was an error processing the CSV file!");
			Admin.editComic(label);
		}
	}
	
	public static void doUpdates() {
		Authentication.requireAdmin();
		new RssUpdater().now();
		flash.put("message", "Updater has begun...");
		Admin.adminPanel();
	}
	
	public static void doJudgement() {
		Authentication.requireAdmin();
		new Judgement().now();
		flash.put("message", "Judgement has begun...");
		Admin.adminPanel();
	}
	
	public static void doComicCacher() {
		Authentication.requireAdmin();
		new ComicCacher().now();
		flash.put("message", "ComicCacher has begun...");
		Admin.adminPanel();
	}
	
	public static void doTagBuilder() {
		Authentication.requireAdmin();
		new TagBuilder().now();
		flash.put("message", "TagBuilder has begun...");
		Admin.adminPanel();
	}
	
	public static void doBootstrap() {
		Authentication.requireAdmin();
		new Bootstrap().now();
		flash.put("message", "Bootstrap has begun...");
		Admin.adminPanel();
	}
}

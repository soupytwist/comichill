package jobs;

import java.util.Calendar;
import java.util.List;

import notifiers.Mails;

import models.siena.Comic;
import models.siena.RssStripSource;
import models.siena.Strip;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import pojo.StripNode;

/**
 * RssUpdater
 * Collects updates from RssStripSource's and creates strips appropriately.
 * This job should run at least several times a day
 * @author nick
 */
@Every("2h")
public class RssUpdater extends Job<String> {
	
	public static Calendar lastRun = null;
	
	public static String status = "not run";
	
	/**
	 * Runs the job. Collects all updates from RSS feeds and adds them.
	 */
	public void doJob() {
		// Tracking
		lastRun = Calendar.getInstance();
    	
		Logger.info("[RSSUPDATER] Updater is starting!");
		List<RssStripSource> sources = RssStripSource.getAllEnabled();
		int totalCreated = 0;
		
		if (sources == null) {
			Logger.warn("[RSSUPDATER] RssStripSource.getAllEnabled returned null");
			status = "no enabled RSS feeds";
		} else {
			Logger.info("[RSSUPDATER] Found %d RssStripSources; beginning import", sources.size());
			
			for (RssStripSource rss : sources) {
				try {
					// Load each source
					List<StripNode> nodes = rss.load();
					Comic comic = rss.getComic();
					int created = 0;
					
					Logger.info("[RSSUPDATER] Importing strips; comic=%s count=%d", comic.label, nodes.size());
					
					for (StripNode node : nodes) {
						// Check if the strip already exists; if it does, do nothing!
						Strip existing = Strip.getWithUrl(comic.id, node.url);
						if (existing == null) {
							Strip s = comic.newStrip(node.url, node.title); // this increments comic's numStrips
							s.save();
							comic.save();
							created++;
						}
					}
					Logger.info("[RSSUPDATER] Finished importing strips; comic=%s added=%d", comic.label, created);
					totalCreated += created;
					if (created > 2 && created == nodes.size()) {
						Logger.warn("[RSSUPDATER] Rss Updater created strips for every comic in an Rss Feed; this could indicate a problem; comic=%s", comic.label);
						Mails.notifyMe("RssUpdater WARNING", "Rss Updater created strips for every comic in an Rss Feed; this could indicate a problem; comic="+comic.label);
					}
					
				} catch (Exception e) {
					// If for some reason we can't load the feed...
					Logger.error("[RSSUPDATER] Failed to load Rss Feed: %s", rss.src);
					Mails.notifyMe("RssUpdater WARNING", "Failed to load Rss Feed: "+rss.src);
					e.printStackTrace();
				}
			}
			status = totalCreated + " strips added";
		}
		
		Logger.info("[RSSUPDATER] Updater is finished!");
		
		// Refresh the comic cache
		new ComicCacher().now();
	}
	
}

package jobs;

import java.util.List;

import models.siena.Comic;
import models.siena.RssStripSource;
import models.siena.Strip;
import notifiers.Mails;
import play.Logger;
import play.jobs.Every;
import pojo.StripNode;

/**
 * RssUpdater
 * Collects updates from RssStripSource's and creates strips appropriately.
 * This job should run at least several times a day
 * @author nick
 */
@Every("2h")
public class RssUpdater  extends TrackedJob<String> {

	public static final int JOB_ID = 3;

	/**
	 * Runs the job. Collects all updates from RSS feeds and adds them.
	 */
	@Override
	public void doTrackedJob() {
		List<RssStripSource> sources = RssStripSource.getAllEnabled();
		int totalCreated = 0;
		String emailMessage = "";

		if (sources == null) {
			Logger.warn("[RSSUPDATER] RssStripSource.getAllEnabled returned null");
			track("No RSS Feeds enabled", "problems", 1);
		} else {
			Logger.info("[RSSUPDATER] Found %d RssStripSources; beginning import", sources.size());
			track("Importing from "+sources.size()+" sources");

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
					track("Imported strips for comic="+comic.label+" added="+created);
					totalCreated += created;

					if (created > 2 && created == nodes.size()) {
						Logger.warn("[RSSUPDATER] Rss Updater created strips for every item in an Rss Feed; this could indicate a problem; comic=%s", comic.label);
						Mails.notifyMe("RssUpdater WARNING", "Rss Updater created strips for every comic in an Rss Feed; this could indicate a problem; comic="+comic.label);
						track("Created strips for every item in an Rss Feed for comic="+comic.label+" added="+created, "problems", 1);
					}

				} catch (Exception e) {
					// If for some reason we can't load the feed...
					Logger.error("[RSSUPDATER] Failed to load Rss Feed: %s", rss.src);
					emailMessage += "Failed to load Rss Feed: "+rss.src+"\n"+e.getMessage()+"\n\n";
					track("Failed to load Rss Feed: "+rss.src, "problems", 1);
				}
			}
			track("Total strips added: "+totalCreated, "updates", totalCreated);
		}

		// Send me one message to tell me which couldn't load
		if (emailMessage.length() != 0)
			Mails.notifyMe("RssUpdater RSS Failure(s)", emailMessage);

		// Refresh the comic cache if there were updates
		if (totalCreated > 0) {
			new ComicCacher().now();
			track("Started ComicCacher");
		}
	}

	@Override
	public int getJobId() {
		return JOB_ID;
	}

}

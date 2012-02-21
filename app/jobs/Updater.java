package jobs;

import java.util.List;

import models.siena.Comic;
import models.siena.RssStripSource;
import models.siena.Strip;

import play.Logger;
import play.jobs.Job;
import pojo.StripNode;

public class Updater extends Job<String> {
	
	public void doJob() {
		List<RssStripSource> sources = RssStripSource.getAllEnabled();
		
		if (sources == null) {
			Logger.warn("RssStripSource.getAllEnabled returned null");
		} else {
			Logger.info("Found %d RssStripSources; beginning import", sources.size());
			for (RssStripSource rss : sources) {
				try {
					List<StripNode> nodes = rss.load();
					Comic comic = rss.getComic();
					int created = 0;
					
					Logger.info("Importing strips; comic=%s count=%d", comic.label, nodes.size());
					
					for (StripNode node : nodes) {
						Strip existing = Strip.getWithUrl(comic.id, node.url);
						if (existing == null) {
							Strip s = comic.newStrip(node.url, node.title);
							s.save();
							comic.save();
							created++;
						}
					}
					Logger.info("Finished importing strips; comic=%s added=%d", comic.label, created);
					
				} catch (Exception e) {
					Logger.error("Failed to load Rss Feed: %s;\n%s", rss.src, e.getStackTrace().toString());
				}
			}
		}
	}
	
}

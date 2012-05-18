package jobs;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.siena.Comic;
import models.siena.Subscription;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.mvc.results.RenderTemplate;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.My;
import util.Serializers;

@Every("6h")
public class Judgement extends TrackedJob {

	public static final int JOB_ID = 4;

	@Override
	public void doTrackedJob() {
		List<Subscription> subs = Subscription.all().fetch();
		List<Comic> comics = Comic.all().fetch();
		Map<Long, Comic> cmap = new HashMap<Long, Comic>();

		// Fill the map with 0's for every comic
		for (Comic comic : comics) {
			cmap.put(comic.id, comic);
			comic.rankPop = 0;
		}

		int totalHits = 0;

		// Now, sum the total number of subscriptions for each comic
		// and the number of hits from each subscription
		for (Subscription sub : subs) {
			cmap.get(sub.cid).rankPop++;

			// Only need to reset the hit counter if it has changed
			if (sub.hits > 0) {
				Comic comic = cmap.get(sub.cid);
				comic.rankHits += sub.hits;
				totalHits += sub.hits;
				sub.hits = -1;
				sub.save();
			}
		}

		// Get the list of comics again from the db
		// This will prevent any changes from being overwritten
		List<Comic> saved = Comic.all().fetch();

		// Now, fill in the values!
		for (Comic c : saved) {
			Comic mapped = cmap.get(c.id);
			c.rankHits = mapped.rankHits;
			c.rankPop = mapped.rankPop;
			c.save();
		}

		track("Total hits: " + totalHits, "hits", totalHits);


		// Create the rankings.js file
		try {
			Logger.info("[JUDGEMENT] Starting rankings.js file creation");
			Template template = TemplateLoader.load("Data/jsvar.js");
			List<Comic> newest = Comic.allEnabled().order("-created").fetch(8);
			List<Comic> subbed = Comic.allEnabled().order("-rankPop").fetch(8);
			List<Comic> hits = Comic.allEnabled().order("-rankHits").fetch(8);
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> innermap = new HashMap<String, Object>();
			innermap.put("new", My.listKeys("id", newest));
			innermap.put("sub", My.listKeys("id", subbed));
			innermap.put("hits", My.listKeys("id", hits));

			map.put("jsVar", "sys_rankings");
			map.put("jsonData", Serializers.gson.toJson(innermap));
			RenderTemplate parsed = new RenderTemplate(template, map);

			File cacheFile = Play.getFile("public/javascripts/cache/rankings.js");
			FileWriter fw = new FileWriter(cacheFile);
			fw.write(parsed.getContent());
			fw.close();
			Logger.info("[JUDGEMENT] Finished creating rankings.js file");
			track("Successfully created rankings.js", "health", 1);
		} catch (Exception e) {
			// Couldn't open the file for writing, put an error!
			Logger.error("[JUDGEMENT] Creating rankings.js file failed; %s", e.getMessage());
			track("Creating rankings.js failed; "+e.getMessage(), "health", -1);
		}
	}

	@Override
	public int getJobId() {
		return JOB_ID;
	}

}

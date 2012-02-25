package jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.siena.Comic;
import models.siena.Subscription;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("6h")
public class Judgement extends Job {

	public void doJob() {
		Logger.info("[JUDGEMENT] Job has started!");
		
		List<Subscription> subs = Subscription.all().fetch();
		List<Comic> comics = Comic.all().fetch();
		Map<Long, Comic> cmap = new HashMap<Long, Comic>();
		
		// Fill the map with 0's for every comic
		for (Comic comic : comics) {
			cmap.put(comic.id, comic);
			comic.rankPop = 0;
		}
		
		// Now, sum the total number of subscriptions for each comic
		// and the number of hits from each subscription
		for (Subscription sub : subs) {
			cmap.get(sub.cid).rankPop++;
			cmap.get(sub.cid).rankHits += sub.hits;
			sub.hits = -1;
			sub.save();
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
		
		Logger.info("[JUDGEMENT] Job has completed!");
	}
	
}

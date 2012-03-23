package jobs;

import models.siena.JobResult;
import play.Logger;
import play.jobs.Job;
import play.mvc.After;
import play.mvc.Before;

public abstract class TrackedJob<V> extends Job<V> {

	private JobResult myResult;
	
	protected void startTracking() {
		myResult = new JobResult(getJobId());
		Logger.debug("JobResult has been initialized for tracking");
	}
	
	protected void track(String message, int level) {
		if (myResult == null)
			Logger.warn("Job tracking failed; JobResult was not initialized");
		else
			myResult.track(message, level);
	}
	
	protected void track(String message) {
		track(message, 0);
	}
	
	protected void endTracking() {
		if (myResult == null)
			Logger.warn("Job tracking failed; JobResult was not initialized");
		else
			myResult.insert();
	}
	
	public abstract int getJobId();
	
}

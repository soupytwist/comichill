package jobs;

import models.siena.JobResult;
import play.Logger;
import play.jobs.Job;
import play.mvc.After;
import play.mvc.Before;

public abstract class TrackedJob<V> extends Job<JobResult> {

	private JobResult myResult;
	
	public final JobResult doJobWithResult() {
		Logger.info("[%s] Job is starting...", this.getClass().getSimpleName().toUpperCase());
		myResult = new JobResult(getJobId());
		Logger.debug("JobResult has been initialized for tracking");
		doTrackedJob();
		myResult.insert();
		Logger.debug("JobResult has been saved");
		Logger.info("[%s] Job has ended", this.getClass().getSimpleName().toUpperCase());
		
		// Return the current result and clear it
		JobResult tmp = myResult;
		myResult = null;
		return tmp;
	}
	
	protected void track(String message, String param, int value) {
		if (myResult == null)
			Logger.warn("Job tracking failed; JobResult was not initialized");
		else
			myResult.track(message, param, value);
	}
	
	protected void track(String message) {
		track(message, null, 0);
	}
	
	public abstract void doTrackedJob();
	
	public abstract int getJobId();
	
}

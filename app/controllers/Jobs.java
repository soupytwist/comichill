package controllers;

import java.util.List;
import java.util.concurrent.ExecutionException;

import models.siena.JobResult;
import jobs.Bootstrap;
import jobs.ComicCacher;
import jobs.Judgement;
import jobs.RssUpdater;
import jobs.StatTracker;
import jobs.TagBuilder;
import jobs.TrackedJob;
import play.Logger;
import play.data.binding.As;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.With;

@With(Authentication.class)
public class Jobs extends Controller {

	private static Promise<JobResult> runningJob = null;
	
	public static void startJob(int jobId) {
		Logger.debug("Job ID is %d", jobId);
		Authentication.requireAdmin();
		Class<? extends TrackedJob> jobClass = lookupJobId(jobId);
		
		if (jobClass == null) {
			badRequest();
		} else {
			try {
				runningJob = jobClass.newInstance().now();
				ok();
			} catch (InstantiationException e) {
				badRequest();
			} catch (IllegalAccessException e) {
				badRequest();
			}
		}
	}
	
	public static void checkJobStatus(int jobId) {
		Authentication.requireAdmin();
		
		// Check the status of the currently running job
		if (runningJob == null) {
			renderText("No job is currently running.");
		} else if (runningJob.isDone()) {
			try {
				// Return the message
				String message = runningJob.get().message; 
				runningJob = null;
				renderText(message);
			} catch (InterruptedException e) {
				renderText("The job that was running has been halted.");
			} catch (ExecutionException e) {
				renderText("The job that was running has failed with an exception.");
			}
		} else {
			ok();
		}
	}
	
	public static void getJobResultsChart(int jobId, int hours, @As(",") String[] params) {
		try {
			List<JobResult> results = JobResult.getByJobId(jobId, hours);
			String result = "[";
			int r = 0;
			for (JobResult jr : results) {
				if (r++ != 0)
					result += ",";
				result += "["+jr.startTime;
				for (String param : params) {
					result += ","+jr.getParam(param);
				}
				result += "]";
			}
			result += "]";
			
			renderJSON(result);
		} catch (Exception e) {
			badRequest();
		}
	}
	
	public static void getLastResult(int jobId) {
		JobResult jr = JobResult.getLastResult(jobId);
		if (jr != null)
			jr.message = jr.message.replace("\n", "<br/>");
		renderJSON(jr);
	}
	
	public static Class<? extends TrackedJob> lookupJobId(int jobId) {
		switch(jobId) {
		case Bootstrap.JOB_ID:
			return Bootstrap.class;
		case ComicCacher.JOB_ID:
			return ComicCacher.class;
		case TagBuilder.JOB_ID:
			return TagBuilder.class;
		case RssUpdater.JOB_ID:
			return RssUpdater.class;
		case Judgement.JOB_ID:
			return Judgement.class;
		case StatTracker.JOB_ID:
			return StatTracker.class;
		default:
			return null;
		}
	}
	
}

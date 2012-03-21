package jobs;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import play.jobs.Job;
import util.My;
import util.Serializers;

public abstract class TrackedJob<V> extends Job<V> {
	
	public static Calendar lastRun = null;
	
	public static String status = "not run";
	
	@Override
	public void doJob() {
		lastRun = Calendar.getInstance();
	}
	
	public static String timeSinceLastRun() {
		Calendar cal = Calendar.getInstance();
		
		if (lastRun == null) {
			return "never";
		}
		
		if (cal.before(lastRun)) {
			return "In the future? " + lastRun.toString();
		}
		
		Long millis = cal.getTimeInMillis() - lastRun.getTimeInMillis();
		Long minutes = (millis / 60000L);
		Long hours = minutes / 60;
		Long days = hours / 24;
		hours = hours % 24;
		minutes = minutes % 60;
		
		if (days > 0) {
			return days + " day(s) " + hours + " hours ago"; 
		} else if (hours > 0) {
			return hours + " hours " + minutes + " minutes ago";
		} else {
			return minutes + " minutes ago";
		}
	}
	
}

package models.siena;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;

import siena.Model;
import siena.Query;
import siena.embed.Embedded;

public class JobResult extends Model {

	public static final int HISTORY_LENGTH = 12;
	
	public int jobId;
	
	public Long startTime;
	
	@Embedded
	public Map<String, Integer> data;
	
	public String message;
	
	public JobResult() {
		super();
		data = new HashMap<String, Integer>();
		startTime = new Date().getTime();
	}
	
	public JobResult(int jobId) {
		this();
		this.jobId = jobId;
	}
	
	public void track(String message, String key, int value) {
		if (this.message == null)	this.message = message;
		else if (message != null)	this.message += "\n"+message;
		
		if (key != null) {
			Integer cur = data.get(key);
			if (cur != null)	data.put(key, cur + value);
			else				data.put(key, value);
		}
	}
	
	public int getParam(String key) {
		Integer res = data.get(key);
		if (res == null)	return 0;
		else				return res;
	}
	
	@Override
	public void insert() {
		data.put("runtime", (int) (new Date().getTime() - startTime));
		super.insert();
	}
	
	public static Query<JobResult> all() {
		return Model.all(JobResult.class);
	}
	
	public static List<JobResult> getByJobId(int jobId, int hours) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -hours);
		return all().filter("jobId", jobId).order("-startTime").filter("startTime>", cal.getTimeInMillis()).fetch();
	}
	
	public static JobResult getLastResult(int jobId) {
		return all().filter("jobId", jobId).order("-startTime").get();
	}
}

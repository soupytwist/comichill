package models.siena;

import java.util.Date;
import java.util.List;

import siena.Model;
import siena.Query;

public class JobResult extends Model {

	public static final int HISTORY_LENGTH = 12;
	
	public int jobId;
	
	public Long startTime;
	
	public int runtime, count_bad, count_ok;
	
	public String message;
	
	public JobResult() {
		super();
		startTime = new Date().getTime();
		count_bad = 0;
		count_ok = 0;
	}
	
	public JobResult(int jobId) {
		this();
		this.jobId = jobId;
	}
	
	public void track(String message, int level) {
		if (this.message == null)
			this.message = message;
		else
			this.message += "\n"+message;
		
		if (level < 0)
			count_bad -= level;
		else if (level > 0)
			count_ok += level;
	}
	
	@Override
	public void insert() {
		runtime = (int) (new Date().getTime() - startTime);
		super.insert();
	}
	
	public static Query<JobResult> all() {
		return Model.all(JobResult.class);
	}
	
	public static List<JobResult> getByJobId(int jobId) {
		return all().filter("jobId", jobId).order("startTime").fetch(HISTORY_LENGTH);
	}
}

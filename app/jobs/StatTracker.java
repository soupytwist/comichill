package jobs;

import play.jobs.Every;
import models.siena.BasicAuthentication;
import models.siena.BasicPasswordReset;
import models.siena.Comic;
import models.siena.Strip;
import models.siena.StripQueue;
import models.siena.Subscription;
import models.siena.User;
import siena.Model;

@Every("1d")
public class StatTracker extends TrackedJob {

	public static final int JOB_ID = 5;
	
	public void doTrackedJob() {
		int users = User.all().count();
		track("Total users: ", "users", users);
		
		int comics = Comic.all().count();
		track("Total comics: ", "comics", comics);
		
		int strips = Strip.all().count();
		track("Total strips: ", "strips", strips);
		
		int subscriptions = Subscription.all().count();
		track("Total subscriptions: ", "subscriptions", subscriptions);
		
		int queues = StripQueue.all().count();
		track("Total queues: ", "queues", queues);
		
		int basic_auths = BasicAuthentication.all().count();
		track("Total basic_auths: ", "basic_auths", basic_auths);
		
		int basic_password_resets = BasicPasswordReset.all().count();
		track("Total basic_password_resets: ", "basic_password_resets", basic_password_resets);
	}
	
	@Override
	public int getJobId() {
		return JOB_ID;
	}

}

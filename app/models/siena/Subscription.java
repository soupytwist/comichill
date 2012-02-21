package models.siena;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;

import siena.Generator;
import siena.Id;
import siena.Model;
import util.Serializers;


public class Subscription extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Required
	public Long cid;
	
	@Required
	@Min(value = 0)
	public int bookmark, latest;
	
	@Required
	public User owner;
	
	// Default no-arguments constructor
	public Subscription() {	
	}
	
	public Subscription(User owner, Long cid, int sid) {
		this.owner = owner;
		this.cid = cid;
		this.bookmark = sid;
		this.latest = sid;
	}
	
	public void visit(int sid) {
		this.bookmark = sid;
		save();
	}
	
	public void updateWith(Subscription sub) {
		this.bookmark = sub.bookmark;
		if (sub.latest > this.latest)
			this.latest = sub.latest;
	}
	
	public Subscription(User owner, Long cid) { 
		this(owner, cid, 0);
	}
	
	public Comic getComic() {
		return Comic.getById(cid);
	}
	
	public static Subscription getById(Long id) {
		return Model.all(Subscription.class).getByKey(id);
	}
	
	public static List<Subscription> getByUser(User user) {
		return (List<Subscription>) Model.all(Subscription.class).filter("owner", user).fetch();
	}
	
	public static Subscription getByUserAndCid(User user, Long cid) {
		return Model.all(Subscription.class).filter("owner", user).filter("cid", cid).get();
	}
	
	public void save() {
		if (bookmark > latest)
			latest = bookmark;
		super.save();
	}
	
	public String toString() {
		// TODO Serialize toString for Subscription
		return Serializers.subscriptionSerializer.serialize(this);
	}
}
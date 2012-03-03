package models.siena;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import util.Serializers;


public class Subscription extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Required
	public Long cid;
	
	@Required
	@Min(value = 0)
	public int bookmark, latest, hits;
	
	@Required
	public User owner;
	
	// Default no-arguments constructor
	public Subscription() {	
	}
	
	public Subscription(User owner, Long cid) { 
		this(owner, cid, 0);
	}
	
	public Subscription(User owner, Long cid, int sid) {
		this.owner = owner;
		this.cid = cid;
		this.bookmark = sid;
		this.latest = sid;
	}
	
	/**
	 * Updates the bookmark to the given sid, updates latest if applicable
	 * @param sid
	 */
	public void visit(int sid) {
		this.bookmark = sid;
		save();
	}
	
	/**
	 * Updates the bookmark to match the given subscription
	 * @param sub
	 */
	public void updateWith(Subscription sub) {
		this.bookmark = sub.bookmark;
		if (sub.latest > this.latest)
			this.latest = sub.latest;
	}

	/**
	 * Get the Comic subscribed to
	 * @return Comic model object
	 */
	public Comic getComic() {
		return Comic.getById(cid);
	}
	
	/**
	 * Get a Subscription by id
	 */
	public static Subscription getById(Long id) {
		return all().getByKey(id);
	}
	
	/**
	 * Gets all subscriptions in the database for a given user
	 * @param user
	 */
	public static List<Subscription> getByUser(User user) {
		return (List<Subscription>) all().filter("owner", user).fetch();
	}
	
	/**
	 * Get a user's subscription to a particular comic if it exists
	 * @param user The User to lookup
	 * @param cid The comic subscribed to
	 */
	public static Subscription getByUserAndCid(User user, Long cid) {
		return all().filter("owner", user).filter("cid", cid).get();
	}
	
	/**
	 * Convenience method for fetching models with Siena
	 * @return An unfiltered Query
	 */
	public static Query<Subscription> all() {
		return Model.all(Subscription.class);
	}
	
	/**
	 * Persists this model in the database and updates latest so that bookmark <= latest
	 */
	public void save() {
		if (bookmark > latest)
			latest = bookmark;
		hits++;
		super.save();
	}
	
	public String toString() {
		return Serializers.subscriptionSerializer.serialize(this);
	}

	public int unreadCount() {
		return getComic().numStrips - this.latest;
	}
}
package models.siena;

import java.util.List;

import play.data.validation.Required;
import play.data.validation.Validation;

import siena.Generator;
import siena.Id;
import siena.Model;

public class User extends Model {

	public static final int PASSWORD_LENGTH_MIN = 6, PASSWORD_LENGTH_MAX = 40;
	
	public static final int REGULAR_USER = 0, ADMIN_USER = 1, DUMMY_USER = 2;
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Required
	public String email;
	
	@Required
	public int role;
	
	public StripQueue queue;
	
	// Default no-arguments constructor
	public User() {
		super();
		this.role = REGULAR_USER;
		this.id = -1L;
	}
	
	public User(String email) {
		this();
		this.email = email;
	}
	
	public List<Subscription> getSubscriptions() {
		return Subscription.getByUser(this);
	}
	
	public Subscription getComicSubscription(Comic comic) {
		return Subscription.getByUserAndCid(this, comic.id);
	}
	
	public boolean isGuest() {
		return email==null;
	}

	public boolean isAdmin() {
		return this.role == ADMIN_USER;
	}
	
	public static User getById(Long id) {
		return Model.getByKey(User.class, id);
	}
	
	public static User getByEmail(String email) {
		return Model.all(User.class).filter("email", email).get();
	}
	
	public static User guest() {
		return new User(null);
	}
	
	public String toString() {
		return "[User ID="+id+" email="+email+" role="+roleAsString()+"]";
	}
	
	public String roleAsString() {
		switch(role) {
		case REGULAR_USER:
			return "regular";
		case ADMIN_USER:
			return "admin";
		case DUMMY_USER:
			return "dummy";
		default:
			return "UNCATEGORIZED";
		}
	}
}
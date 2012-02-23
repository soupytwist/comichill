package models.siena;

import java.util.List;

import play.data.validation.Required;
import play.data.validation.Validation;

import siena.Generator;
import siena.Id;
import siena.Model;

public class User extends Model {

	public static final int PASSWORD_LENGTH_MIN = 6, PASSWORD_LENGTH_MAX = 40;
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Required
	public String email;
	
	@Required
	public boolean adminPrivelege;
	
	@Required
	public UserAuthentication auth;
	
	public StripQueue queue;
	
	// Default no-arguments constructor
	public User() {
		super();
		this.adminPrivelege = false;
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
		return adminPrivelege;
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
	
	public void generateAuthentication(String password) {
		this.auth = new UserAuthentication(this.id, password);
	}
	
	public void fetchUserAuth() {
		auth = Model.getByKey(UserAuthentication.class, id);
	}
	
	public void insert() {
		this.auth.insert();
		super.insert();
	}
	
	public String toString() {
		return "[User ID="+id+" email="+email+" adminPrivilege="+adminPrivelege+"]";
	}
}
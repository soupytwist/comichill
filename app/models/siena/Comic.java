package models.siena;

import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;

import play.data.validation.Required;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class Comic extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Required
	public String title, label, author, homepage, tags;
	
	@GeneratedValue
	public int numStrips;
	
	@GeneratedValue
	public Date created, updated;
	
	// Default no-arguments constructor
	public Comic() {
		this.created = new Date();
		this.updated = new Date();
		this.numStrips = 0;
		this.id = -1L;
	}
	
	public Comic(String acronym, String title, String author, String homepage) {
		super();
		updateFields(label, title, author, homepage);
	}
	
	public void updateFields(String label, String title, String author, String homepage) {
		this.title = title;
		this.label = label;
		this.author = author;
		this.homepage = homepage;
	}
	
	public void updateWith(Comic comic) {
		updateFields(comic.label, comic.title, comic.author, comic.homepage);
	}
	
	public int useNextSid() {
		return ++numStrips;
	}
	
	public Strip newStrip(String url, String title) {
		return new Strip(this, url, title);
	}
	
	public static Comic getById(Long id) {
		return Model.getByKey(Comic.class, id);
	}
	
	public static Comic getByLabel(String label) {
		return all().filter("label", label).get();
	}
	
	public static Query<Comic> all() {
		return Model.all(Comic.class);
	}
	
	public void save() {
		this.updated = new Date();
		super.save();
	}
	
	public String toString() {
		return "[Comic ID="+id+" label="+label+" title="+title+" author="+author+" homepage="+homepage+" numStrips="+numStrips+" created="+created+"]";
	}
	
}
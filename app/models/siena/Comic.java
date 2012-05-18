package models.siena;

import java.util.Date;

import javax.persistence.GeneratedValue;

import play.data.validation.Required;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import util.Serializers;

public class Comic extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	@Required
	public String title, label, author, homepage;

	public String tags;

	@GeneratedValue
	public int numStrips;

	@GeneratedValue
	public int rankPop, rankHits;

	@GeneratedValue
	public Long created, updated;

	public boolean enabled;

	// Default no-arguments constructor
	public Comic() {
		this.created = new Date().getTime();
		this.updated = new Date().getTime();
		this.numStrips = 0;
		this.id = -1L;
		this.rankPop = 0;
		this.rankHits = 0;
		this.enabled = false;
	}

	public Comic(String acronym, String title, String author, String homepage, String tags) {
		super();
		updateFields(label, title, author, homepage, tags, false);
	}

	public void updateFields(String label, String title, String author, String homepage, String tags, boolean enabled) {
		this.title = title;
		this.label = label;
		this.author = author;
		this.homepage = homepage;
		this.tags = tags;
		this.enabled = enabled;
	}

	public void updateWith(Comic comic) {
		updateFields(comic.label, comic.title, comic.author, comic.homepage, comic.tags, comic.enabled);
	}

	public int useNextSid() {
		return ++numStrips;
	}

	public String[] tagsAsList() {
		if (tags == null)
			return new String[0];

		// Remove the first and last pipe and then split
		return tags.substring(1, tags.length()-1).split("\\|");
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

	public static Query<Comic> allEnabled() {
		return all().filter("enabled", true);
	}

	@Override
	public void save() {
		this.updated = new Date().getTime();
		super.save();
	}

	@Override
	public String toString() {
		return Serializers.gson.toJson(this);
	}
}
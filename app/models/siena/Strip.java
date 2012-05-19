package models.siena;

import play.data.validation.Required;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import util.Serializers;

public class Strip extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public Long cid;

	public int sid;

	@Required
	public String url, title;

	// Default no-arguments constructor
	public Strip() {
	}

	public Strip(Comic comic, String url, String title) {
		init(comic, url, title);
	}

	public void init(Comic comic, String url, String title) {
		this.cid = comic.id;
		this.url = url;
		this.title = title;
		this.sid = comic.useNextSid();
	}

	public Comic getComic() {
		return Comic.getById(this.cid);
	}

	public static Strip get(Long cid, int sid) {
		return getStripsByCid(cid).filter("sid", sid).get();
	}

	public static Strip getById(Long id) {
		return Model.getByKey(Strip.class, id);
	}

	public static Query<Strip> getStripsByCid(Long cid) {
		return all().filter("cid", cid).order("sid");
	}

	public static Strip getWithUrl(Long cid, String url) {
		return all().filter("cid", cid).filter("url", url).get();
	}

	public static Query<Strip> all() {
		return Model.all(Strip.class);
	}

	@Override
	public String toString() {
		return Serializers.stripSerializer.serialize(this);
	}

}

package models.siena;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.*;

import javax.persistence.GeneratedValue;

import play.Logger;
import play.data.validation.Required;
import pojo.StripNode;

import siena.Id;
import siena.Model;
import siena.Query;
import util.Serializers;

public abstract class StripSource extends Model {

	@Id
	public Long id;
	
	@Required
	public Long cid;
	
	@Required
	public String src, titlePattern, urlPattern;
	
	@GeneratedValue
	public Date created, updated;
	
	public StripSource() {
		this.created = new Date();
		this.updated = new Date();
	}
	
	public void init(Long cid) {
		this.cid = cid;
		this.created = new Date();
	}
	
	public Comic getComic() {
		return Comic.getById(this.cid);
	}
	
	public abstract List<StripNode> load() throws Exception;
	
	public static StripSource getById(Long id) {
		StripSource ss = Model.all(RssStripSource.class).getByKey(id);
		if (ss != null)
			return ss;
		ss = Model.all(ArchiveStripSource.class).getByKey(id);
		return ss;
	}
	
	public void update() {
		updated = new Date();
		super.update();
	}
	
	protected String match(String str, String pattern) {
		String matched = "";
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		
		if (m.find()) {
			if (m.groupCount() == 0) {
				matched = m.group();
			} else {
				for (int i = 0; i < m.groupCount(); i++)
					matched += m.group(i+1)+"\n";
			}
		}
		
		return matched;
	}
	
	public String toString() {
		//return "ID="+id+" comic="+comic+" src="+src+" titlePattern="+titlePattern+" urlPattern="+urlPattern+" created="+created;
		return Serializers.sourceSerializer.serialize(this);
	}
}

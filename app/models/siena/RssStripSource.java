package models.siena;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import pojo.StripNode;
import siena.Id;
import siena.Model;
import siena.Query;
import util.CacheUtil;
import util.Serializers;
import util.WebUtil;
import play.libs.WS;
import play.libs.WS.HttpResponse;

public class RssStripSource extends StripSource {
	
	@Required
	public String linkTag, titleTag;
	
	public boolean enabled;

	// Default no-arguments constructor
	public RssStripSource() {
		super();
	}
	
	public void updateWith(RssStripSource rss) {
		this.src = rss.src;
		this.titleTag = rss.titleTag;
		this.titlePattern = rss.titlePattern;
		this.linkTag = rss.linkTag;
		this.urlPattern = rss.urlPattern;
		this.enabled = rss.enabled;
	}
	
	public List<StripNode> load() throws Exception {
		List<StripNode> result = new ArrayList<StripNode>();
		String xml = CacheUtil.getURL(this.src);
		Document doc = WebUtil.loadXMLFromString(xml);
		
		NodeList items = doc.getElementsByTagName("item");
		for (int i = 0; i < items.getLength(); i++) {
			String url = "";
			String title = "";
			
			Node item = items.item(i);
			
			NodeList children = item.getChildNodes();
			
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				String childName = child.getNodeName();
				
				if (childName.equals(this.linkTag)) {
					url = match(child.getTextContent(), urlPattern);
				}
				if (childName.equals(this.titleTag)) {
					title = match(child.getTextContent(), titlePattern);
				}
			}
			
			if (url.length() > 0) {
				StripNode strip = new StripNode(url, title);
				result.add(0, strip);
			}
		}
		
		return result;
	}
	
	public String toString() {
		return Serializers.rssSerializer.serialize(this);
	}
	
	public static RssStripSource getById(Long id) {
		return Model.all(RssStripSource.class).getByKey(id);
	}
	
	public static List<RssStripSource> getAllEnabled() {
		return Model.all(RssStripSource.class).filter("enabled", true).fetch();
	}
	
	public static List<RssStripSource> getByComic(Long cid) {
		List<RssStripSource> res = Model.all(RssStripSource.class).filter("cid", cid).fetch();
		Logger.debug("RssStripSource getByComic for cid=%d returned %d results", cid, res.size());
		return res;
	}
}

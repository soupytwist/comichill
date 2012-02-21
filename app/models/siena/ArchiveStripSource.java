package models.siena;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import play.Logger;
import play.cache.Cache;
import play.cache.CacheFor;
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

public class ArchiveStripSource extends StripSource {

	public boolean newestFirst;
	
	// Default no-arguments constructor
	public ArchiveStripSource() {
		super();
	}
	
	public void updateWith(ArchiveStripSource ass) {
		this.src = ass.src;
		this.titlePattern = ass.titlePattern;
		this.urlPattern = ass.urlPattern;
		this.newestFirst = ass.newestFirst;
	}
	
	public List<StripNode> load() throws Exception {
		List<StripNode> items = new ArrayList<StripNode>();
		String html = CacheUtil.getURL(this.src);
		Comic comic = getComic();
		String prefix = WebUtil.getDomainOnly(comic.homepage);
		
		NodeFilter linkFilter = new AndFilter( new TagNameFilter("a"), new HasAttributeFilter("href") );
		Parser parser = new Parser();
		parser.setInputHTML(html);
		NodeList nodes = parser.parse(linkFilter);
		Logger.debug("Successfully parsed HTML document at %s - Found %d links", this.src, nodes.size());
		
		for (int i = 0; i < nodes.size(); i++) {
			Node itemNode = nodes.elementAt(i);
			if (itemNode instanceof LinkTag) {
				LinkTag link = (LinkTag)itemNode;
				String title =  match(link.getLinkText(), titlePattern);
				String url = match(link.getLink(), urlPattern);
				
				if (url.length() > 0) {
					StripNode item = new StripNode(prefix + url, title);
					if (this.newestFirst) items.add(item); else items.add(0, item);
				}
			}
		}
		
		return items;
	}
	
	public String toString() {
		return Serializers.archiveSerializer.serialize(this);
	}
	
	public static ArchiveStripSource getById(Long id) {
		return Model.all(ArchiveStripSource.class).getByKey(id);
	}
	
	public static List<ArchiveStripSource> getAllEnabled() {
		return Model.all(ArchiveStripSource.class).filter("enabled", true).fetch();
	}
	
	public static List<ArchiveStripSource> getByComic(Long cid) {
		List<ArchiveStripSource> res = new ArrayList<ArchiveStripSource>(); 
		res.addAll(Model.all(ArchiveStripSource.class).filter("cid", cid).fetch());
		Logger.debug("ArchiveStripSource getByComic cid=%d returned %d results", cid, res.size());
		return res;
	}
}

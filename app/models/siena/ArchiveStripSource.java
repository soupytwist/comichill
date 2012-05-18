package models.siena;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.Logger;
import pojo.StripNode;
import siena.Model;
import util.CacheUtil;
import util.Serializers;
import util.WebUtil;

public class ArchiveStripSource extends StripSource {

	// Default no-arguments constructor
	public ArchiveStripSource() {
		super();
	}

	public void updateWith(ArchiveStripSource ass) {
		this.src = ass.src;
		this.titlePattern = ass.titlePattern;
		this.urlPattern = ass.urlPattern;
	}

	@Override
	public List<StripNode> load() throws Exception {
		List<StripNode> items = new ArrayList<StripNode>();
		String html = CacheUtil.getURL(this.src);
		Comic comic = getComic();
		String prefix = WebUtil.getDomainOnly(comic.homepage);

		// Update: Switched libraries to use JSoup instead of HtmlParser due to better functionality
		// but more importantly better documentation.
		/*
		NodeFilter linkFilter = new AndFilter( new TagNameFilter("a"), new HasAttributeFilter("href") );
		Parser parser = new Parser();
		parser.setInputHTML(html);
		NodeList nodes = parser.parse(linkFilter);
		Logger.debug("Successfully parsed HTML document at %s - Found %d links", this.src, nodes.size());
		 */

		Document doc = Jsoup.parse(html, "UTF-8");
		Elements elms = doc.getElementsByAttribute("href");

		for (Element elm : elms) {
			String title =  match(elm.text(), titlePattern);
			String url = match(elm.attr("href"), urlPattern);

			if (url.length() > 0) {
				StripNode item = new StripNode(prefix + url, title);
				items.add(0, item);
			}
		}

		return items;
	}

	@Override
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

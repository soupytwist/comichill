package util;

import play.Logger;
import play.cache.Cache;
import play.libs.WS;
import play.libs.WS.HttpResponse;

public class CacheUtil {

	public static String getURL(String url) {
		String xml = (String)Cache.get("url_"+url);
		
		if (xml == null) {
			Logger.debug("Cache miss; no data cached for: %s", url);
			HttpResponse response = WS.url(url).get();
			xml = response.getString();
			Cache.set("url_" + url, xml, "30mn");
		} else {
			Logger.debug("Cache hit!; data cached for: %s", url);
		}
		
		return xml;
	}
	
}

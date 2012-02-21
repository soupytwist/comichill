package util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import play.Logger;

public class WebUtil {
	public static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();

	    return builder.parse(new ByteArrayInputStream(xml.getBytes()));
	}
	
	/**
	 * Extracts the domain portion of a URL
	 * @param url The full URL to parse
	 * @return The 'domain' portion of the URL;
	 * 	ex: http://www.comichill.com, www.comichill.com, comichill.com, etc.
	 */
	public static String getDomainOnly(String url) {
		// TODO Test getDomainOnly thoroughly!
		int lastDot = 0, pos = 0;
		
		if (url == null) {
			Logger.error("WebUtil.getDomainOnly: null value received");
			return "";
		}
		
		do {
			lastDot += pos;
			pos = url.substring(lastDot).indexOf('.')+1;
		}while (pos != 0);
		
		if (lastDot == 0){
			Logger.warn("WebUtil.getDomainOnly: The url '%s' is invalid", url);
			return "";
		} else {
			pos = url.substring(lastDot).indexOf("/");
			if (pos != -1)
				return url.substring(0, lastDot+pos+1);
			else
				return url+"/";
		}
	}
}

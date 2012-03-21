package util;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;

public class My {

	public static Map<Integer, Object> map(Object... args) {
		Map<Integer, Object> map = new HashMap<Integer, Object>();
		for (int i = 0; i < args.length; i++) {
			if (args[i] != null)
				map.put(i, args[i]);
			else
				map.put(i, new Empty());
		}
		
		return map;
	}
	
	public static Map<Object, Object> mapListByKey(String key, List<? extends Object> objs) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		try {
			for (Object obj : objs) {
				if (obj != null) {
					Field fkey = obj.getClass().getField(key);
					map.put(fkey.get(obj), obj);
				}
			}
		} catch (Exception e) {
			Logger.error("Mapping by key failed!");
		}
		return map;
	}
	
	public static Map<Object, Object> mapByKey(String key, Object... args) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		try {
			for (Object arg : args) {
				if (arg != null) {
					Field fkey = arg.getClass().getField(key);
					map.put(fkey.get(arg), arg);
				}
			}
		} catch (Exception e) {
			Logger.error("Mapping by key failed!");
		}
		return map;
	}
	
	public static class Empty {}
	
	public static String timeSince(Calendar then) {
		Calendar now = Calendar.getInstance();
		
		if (then == null) {
			return "never";
		}
		
		if (now.before(then)) {
			return "In the future? " + then.toString();
		}
		
		Long millis = now.getTimeInMillis() - then.getTimeInMillis();
		Long minutes = (millis / 60000L);
		Long hours = minutes / 60;
		Long days = hours / 24;
		hours = hours % 24;
		minutes = minutes % 60;
		
		if (days > 0) {
			return days + " day(s) " + hours + " hours ago"; 
		} else if (hours > 0) {
			return hours + " hours " + minutes + " minutes ago";
		} else {
			return minutes + " minutes ago";
		}
	}
}

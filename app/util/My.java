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
}

package util;

import java.util.Date;

import play.Play;
import play.Play.Mode;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import flexjson.JSONSerializer;

public class Serializers {
	public static final JSONSerializer comicSerializer;
	public static final JSONSerializer stripSerializer;
	public static final JSONSerializer subscriptionSerializer;
	public static final JSONSerializer stripNodeSerializer;
	public static final JSONSerializer sourceSerializer;
	public static final JSONSerializer rssSerializer;
	public static final JSONSerializer archiveSerializer;

	public static final Gson gson;

	static {
		boolean prettyPrint = Play.mode == Mode.DEV;

		comicSerializer = new JSONSerializer().include(
				"id", "label", "title", "author", "homepage", "tags", "enabled").exclude("*").prettyPrint(prettyPrint);

		stripSerializer = new JSONSerializer().include(
				"id", "cid", "sid", "url", "title").exclude("*").prettyPrint(prettyPrint);

		sourceSerializer = new JSONSerializer().include(
				"src", "id", "created", "updated").exclude("*").prettyPrint(prettyPrint);

		rssSerializer = new JSONSerializer().include(
				"src", "id", "titleTag", "titlePattern", "linkTag", "urlPattern", "enabled", "created", "updated", "comic.id").exclude("*").prettyPrint(prettyPrint);

		archiveSerializer = new JSONSerializer().include(
				"src", "id", "titlePattern", "urlPattern", "enabled", "created", "updated", "newestFirst", "comic.id").exclude("*").prettyPrint(prettyPrint);

		subscriptionSerializer = new JSONSerializer().include(
				"bookmark", "latest", "cid", "id").exclude("*").prettyPrint(prettyPrint);

		stripNodeSerializer = new JSONSerializer().include(
				"title", "url").exclude("*").prettyPrint(prettyPrint);

		gson = builder().create();
	}

	public static GsonBuilder builder() {
		GsonBuilder gb = new GsonBuilder();
		if (Play.mode == Mode.DEV)
			gb.setPrettyPrinting();
		gb.setDateFormat("yyyyMMddHHMMss");
		gb.addDeserializationExclusionStrategy(new DateExclusionStategy());
		return gb;
	}

	static class DateExclusionStategy implements ExclusionStrategy {
		@Override
		public boolean shouldSkipClass(Class<?> c) {
			return c == Date.class;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return false;
		}
	}
}
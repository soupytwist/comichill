package models.siena;

import play.data.validation.Required;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import util.Serializers;

public class TagCategory extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	@Required
	public String name;

	public String tags;

	// Default no-arguments constructor
	public TagCategory() {
	}

	public String[] tagsAsList() {
		if (tags == null)
			return new String[0];

		// Remove the first and last pipe and then split
		return tags.substring(1, tags.length()-1).split("\\|");
	}

	public static TagCategory getById(Long id) {
		return Model.getByKey(TagCategory.class, id);
	}

	public static TagCategory getByName(String name) {
		return all().filter("name", name).get();
	}

	public static Query<TagCategory> all() {
		return Model.all(TagCategory.class);
	}

	@Override
	public String toString() {
		return Serializers.gson.toJson(this);
	}
}
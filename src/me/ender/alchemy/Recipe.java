package me.ender.alchemy;

import com.google.gson.annotations.SerializedName;
import haven.*;
import haven.res.ui.tt.alch.recipe.Recipe.Spec;
import me.ender.ClientUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Recipe {
    final String res;
    @SerializedName("sub")
    final List<Recipe> ingredients;
    private transient String name;

    public Recipe(Spec input) {
	this(input.res.res.get().name, input.inputs.isEmpty() ? null : from(input.inputs));
    }

    public Recipe(String res, List<Recipe> ingredients) {
	this.res = res;
	this.ingredients = ingredients;
    }

    public static Recipe from(String res, haven.res.ui.tt.alch.recipe.Recipe recipe) {
	List<Spec> inputs = recipe.inputs;
	if(res.contains("/jar-elixir")) {
	    if(inputs.size() == 2) {
		res = "paginae/craft/herbalswill";
	    } else if(inputs.size() == 3) {
		String last = inputs.get(2).res.res.get().name;
		if(AlchemyData.isMineral(last)) {
		    res = "paginae/craft/mercurialelixir";
		} else {
		    res = "paginae/craft/mushroomdecoction";
		}
	    }
	}

	return new Recipe(res, from(inputs));
    }

    public static List<Recipe> from(List<Spec> inputs) {
	return inputs.stream().map(Recipe::new).collect(Collectors.toList());
    }

    public String name() {
	if(name == null) {
	    name = ClientUtils.loadPrettyResName(res);
	}
	return name;
    }

    public List<ItemInfo> info(UI ui) {
	return Collections.singletonList(new haven.res.ui.tt.alch.recipe.Recipe(ui.infoOwner, spec()));
    }

    private List<Spec> spec() {
	return ingredients == null
	    ? Collections.emptyList()
	    : ingredients.stream()
	    .map(i -> new Spec(new ResData(Resource.remote().load(i.res), Message.nil), i.spec()))
	    .collect(Collectors.toList());
    }

    @Override
    public String toString() {
	String name = Resource.remote().loadwait(res).layer(Resource.tooltip).t;
	if(ingredients == null || ingredients.isEmpty()) {
	    return String.format("<%s>", name);
	}

	return String.format("<%s:[%s]>", name, ingredients.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /**
     * Returns recipe string for <a href="https://yoda-magic.github.io/alchemygraph">Yoda's Alchemy Graph</a> site.
     */
    public String toAlchemy() {
	String name = name();
	if(ingredients == null || ingredients.isEmpty()) {
	    return name;
	}

	String sub = ingredients.stream().map(Recipe::toAlchemy).collect(Collectors.joining(","));

	String method = name;
	if(res.contains("/herbalswill")) {
	    method = "herb";
	} else if(res.contains("/mushroomdecoction")) {
	    method = "mush";
	} else if(res.contains("/mercurialelixir")) {
	    method = "merc";
	} else if(res.contains(AlchemyData.HERBAL_GRIND)) {
	    method = "hg";
	} else if(res.contains(AlchemyData.LYE_ABLUTION)) {
	    method = "la";
	} else if(res.contains(AlchemyData.MINERAL_CALCINATION)) {
	    method = "mc";
	} else if(res.contains(AlchemyData.MEASURED_DISTILLATE)) {
	    method = "md";
	} else if(res.contains(AlchemyData.FIERY_COMBUSTION)) {
	    method = "fc";
	}

	return String.format("%s(%s)", method, sub);
    }

    @Override
    public boolean equals(Object obj) {
	if(!(obj instanceof Recipe)) {return super.equals(obj);}

	Recipe other = (Recipe) obj;
	return Objects.equals(res, other.res)
	    && Objects.equals(ingredients, other.ingredients);
    }

    @Override
    public int hashCode() {
	return 13 * res.hashCode() + 7 * (ingredients == null ? 0 : ingredients.hashCode());
    }

    public boolean matches(String filter) {
	String[] parts = filter.split(":", 2);
	if(parts.length < 2) {return false;}
	if(!parts[0].startsWith("use")) {return false;}

	final String f = parts[1];
	if(f.isEmpty()) {return false;}

	return doMatch(f);
    }

    private boolean doMatch(String filter) {
	return name().toLowerCase().contains(filter)
	    || (ingredients != null && ingredients.stream().anyMatch(r -> r.doMatch(filter)));
    }
}

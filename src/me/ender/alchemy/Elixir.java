package me.ender.alchemy;

import me.ender.ClientUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class Elixir implements Comparable<Elixir> {
    private String name;
    final public Recipe recipe;
    final public Collection<Effect> effects;
    private transient String _name;

    public Elixir(Recipe recipe, Collection<Effect> effects) {
	this.recipe = recipe;
	this.effects = effects;
    }

    public String name() {
	if(_name == null) {_name = name;}
	if(_name == null) {
	    _name = ClientUtils.loadPrettyResName(recipe.res);
	}
	return _name;
    }

    public void name(String name) {
	if(name != null) {
	    name = name.trim();
	    if(name.isEmpty()) {
		name = null;
	    }
	}
	_name = this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
	if(!(obj instanceof Elixir)) {return false;}
	Elixir other = (Elixir) obj;
	return Objects.equals(recipe, other.recipe);
    }

    @Override
    public int hashCode() {
	return recipe.hashCode();
    }

    private String alchemyEffects() {
	return effects.stream().map(Effect::format).collect(Collectors.joining("\\n"));
    }

    /**
     * Returns formatted URL for <a href="https://yoda-magic.github.io/alchemygraph">Yoda's Alchemy Graph</a> site.
     */
    public String toAlchemyUrl() {
	//TODO: escape " symbols in the alchemy info?
	String json = String.format("{\"f\":\"%s\", \"e\":\"%s\",\"ver\":1}", recipe.toAlchemy(), alchemyEffects());
	String data = new String(Base64.getEncoder().encode(json.getBytes(StandardCharsets.UTF_16LE)));
	return String.format("https://yoda-magic.github.io/alchemygraph/#%s", data);
    }

    @Override
    public int compareTo(Elixir o) {
	return name().compareToIgnoreCase(o.name());
    }
}

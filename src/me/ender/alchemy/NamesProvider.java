package me.ender.alchemy;

import haven.*;
import me.ender.ClientUtils;

import java.util.HashMap;
import java.util.Map;

public class NamesProvider implements Disposable {
    private static final Tex LOADING = Text.render("???").tex();
    private final Map<String, Tex> cache = new HashMap<>();
    private final Map<String, String> names = new HashMap<>();
    private final int width;

    public NamesProvider(int width) {
	this.width = width;
    }

    public String name(String res) {
	return names.computeIfAbsent(new Effect(res).res, ClientUtils::loadPrettyResName);
    }

    public Tex tex(String res) {
	try {
	    return cache.computeIfAbsent(res, this::render);
	} catch (Loading e) {
	    return LOADING;
	}
    }

    public Tex tex(Effect item) {
	try {
	    Tex tex = cache.getOrDefault(item.raw, null);
	    if(tex == null) {
		tex = new TexI(item.ingredientInfo().alchtip());
		cache.put(item.raw, tex);
	    }
	    return tex;
	} catch (Loading e) {
	    return LOADING;
	}
    }

    private Tex render(String res) {
	Effect effect = new Effect(res);
	if(effect.type != null) {
	    return new TexI(effect.ingredientInfo().alchtip());
	}
	String name = name(res);
	try {
	    return RichText.stdfrem.render(String.format("$img[%s,h=16,c] %s", res, name), width).tex();
	} catch (Exception ignore) {
	}
	return RichText.stdfrem.render(String.format("$img[gfx/invobjs/missing,h=16,c] %s", name), width).tex();
    }

    public int compare(String o1, String o2) {
	return name(o1).compareTo(name(o2));
    }

    @Override
    public void dispose() {
	names.clear();
	cache.values().forEach(Tex::dispose);
	cache.clear();
    }
}

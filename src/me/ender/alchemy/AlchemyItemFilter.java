package me.ender.alchemy;

import haven.GItem;

import java.util.Set;

public abstract class AlchemyItemFilter {

    protected final Ingredient tracked;
    protected final Set<Effect> testedEffects;

    AlchemyItemFilter(Ingredient tracked, Set<Effect> testedEffects) {
	this.tracked = tracked;
	this.testedEffects = testedEffects;
    }

    public abstract boolean matches(GItem item);

    public Ingredient tracked() {
	return tracked;
    }

    public Set<Effect> testedEffects() {
	return testedEffects;
    }
}

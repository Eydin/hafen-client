package me.ender.alchemy;

import haven.GItem;

import java.util.Set;

public class ComboFilter extends AlchemyItemFilter {
    private final Set<String> tested;

    public ComboFilter(Ingredient ingredient, Set<Effect> testedEffects, Set<String> testedIngredients) {
	super(ingredient, testedEffects);
	this.tested = testedIngredients;
    }

    public boolean matches(GItem item) {
	String res = item.resname();
	return AlchemyData.allIngredients().contains(res) && !tested.contains(res);
    }
}

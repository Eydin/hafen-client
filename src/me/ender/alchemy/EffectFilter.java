package me.ender.alchemy;

import haven.CFG;
import haven.GItem;
import haven.ItemInfo;

import java.util.HashSet;
import java.util.Set;

public class EffectFilter extends AlchemyItemFilter {

    private final Set<String> testedIngredients;

    public EffectFilter(Ingredient ingredient, Set<Effect> effects, Set<String> ingredients) {
	super(ingredient, effects);
	this.testedIngredients = ingredients;
    }

    @Override
    public boolean matches(GItem item) {
	String res = item.resname();
	if(testedIngredients.contains(res)) {return false;}

	Set<Effect> effects = new HashSet<>();

	for (ItemInfo info : item.info()) {
	    AlchemyData.tryAddIngredientEffect(effects, info);
	}

	if(!testedEffects.containsAll(effects)) {return true;}

	if(!CFG.ALCHEMY_DEEP_EFFECT_TRACK.get()) {return false;}

	if(effects.size() >= AlchemyData.MAX_EFFECTS) {return false;}

	return !testedEffects.containsAll(AlchemyData.untestedEffects(res));
    }
}

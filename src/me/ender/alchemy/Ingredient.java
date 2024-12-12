package me.ender.alchemy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class Ingredient {
    final public Collection<Effect> effects;

    public Ingredient(Collection<Effect> effects, Ingredient base) {
	if(base == null) {
	    this.effects = effects;
	    return;
	}
	this.effects = new HashSet<>();

	//add new effects with position data
	for (Effect effect : effects) {
	    if(effect.opt != null) {this.effects.add(effect);}
	}

	//add effects from base that have position data
	for (Effect effect : base.effects) {
	    if(effect.opt != null && effects.contains(effect)) {this.effects.add(effect);}
	}

	//add remaining effects
	this.effects.addAll(effects);
    }

    @Override
    public boolean equals(Object obj) {
	if(!(obj instanceof Ingredient)) {return false;}
	Ingredient other = (Ingredient) obj;

	if(effects.size() != other.effects.size()) {
	    return false;
	}

	for (Effect thisEff : effects) {
	    boolean found = false;

	    for (Effect otherEff : other.effects) {
		//compare raw instead of using equals() to make sure any changes to opt are detected
		if(Objects.equals(thisEff.raw, otherEff.raw)) {
		    found = true;
		    break;
		}
	    }

	    if(!found) {return false;}
	}

	return true;
    }
}

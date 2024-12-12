package me.ender;

import haven.Gob;
import haven.res.lib.vmat.Materials;
import haven.res.lib.vmat.VarSprite;

import java.util.stream.Collectors;

import static haven.CFG.*;

public class CustomizeVarMat {
    public static boolean NoMat(VarSprite sprite) {
	if(ResName.CUPBOARD.equals(sprite.res.name)) {
	    return DISPLAY_NO_MAT_CUPBOARDS.get();
	}
	return false;
    }
    
    public static String formatMaterials(Gob gob) {
	Materials mats = gob.getattr(Materials.class);
	if(mats == null || mats.res.isEmpty()) {return null;}
	
	return String.format("Materials:\n- %s", mats.res.stream().map(ClientUtils::prettyResName).collect(Collectors.joining("\n- ")));
    }
}

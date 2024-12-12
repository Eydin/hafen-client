package me.ender.gob;

import haven.*;
import haven.res.ui.obj.buddy.Buddy;

import java.util.HashMap;
import java.util.Map;

public class KinInfo {
    private static final Map<Long, KinInfo> cache = new HashMap<>(); 
    
    public static final String VILLAGE_MATE_NAME = "haven.res.ui.obj.buddy_v.Vilmate";
    
    private static Class<? extends GAttrib> VILLAGER;
    
    public final int group;
    public final String name;
    public final Tex rname;
    public final boolean isVillager;
    
    private KinInfo(Gob gob, int id, boolean isVillager) {
	this.isVillager = isVillager;
	GameUI gui = gob.context(GameUI.class);
	BuddyWnd.Buddy b;
	
	if(gui != null && gui.buddies != null && (b = gui.buddies.find(id)) != null) {
	    this.group = b.group;
	    this.name = b.name;
	    this.rname = b.rname().tex();
	} else {
	    this.group = 0;
	    this.name = "";
	    this.rname = null;
	}
    }
    
    public static KinInfo from(Gob gob, Map<Class<? extends GAttrib>, GAttrib> attrs) {
	Buddy buddy = null;
	GAttrib villager = null;
	if(VILLAGER == null) {
	    for (Map.Entry<Class<? extends GAttrib>, GAttrib> entry : attrs.entrySet()) {
		Class<? extends GAttrib> key = entry.getKey();
		GAttrib value = entry.getValue();
		if(value instanceof Buddy) {
		    buddy = (Buddy) value;
		}
		if(VILLAGER == null && VILLAGE_MATE_NAME.equals(key.getName())) {
		    VILLAGER = key;
		    villager = value;
		}
	    }
	}
	if(buddy == null) {
	    buddy = gob.getattr(Buddy.class);
	}
	if(villager == null && VILLAGER != null) {
	    villager = gob.getattr(VILLAGER);
	}
	if(buddy != null || villager != null) {
	    KinInfo info = from(gob, buddy, villager);
	    cache.put(gob.id, info);
	    return info;
	}
	return null;
    }
    
    public static KinInfo cached(long gobId) {
	return cache.get(gobId);
    }
    
    private static KinInfo from(Gob gob, Buddy buddy, GAttrib villager) {
	int id = buddy != null ? buddy.id : -1;
	return new KinInfo(gob, id, villager != null);
    }
    
    public static boolean isFoe(Gob gob) {
	KinInfo ki = gob.kin();
	if(ki != null) {
	    //mark as foe if in RED(2) group or WHITE(0) and not villager
	    return ki.group == 2 || ki.group == 0 && !ki.isVillager;
	}
	return true;
    }
    
    public static boolean isKinInfo(Class<? extends GAttrib> cls) {
	if(Buddy.class == cls) {return true;}
	return VILLAGE_MATE_NAME.equals(cls.getName());
    }
}

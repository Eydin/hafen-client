package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class Radar {
    public static final String CONFIG_JSON = "radar.json";
    private static final Map<String, String> gob2icon = new HashMap<>();
    
    public static boolean process(GobIcon icon) {
	try {
	    String gres = icon.gob.resid();
	    String ires = icon.res.get().name;
	    if(gres != null && ires != null) {
		if(!ires.equals(gob2icon.get(gres))) {
		    gob2icon.put(gres, ires);
		    //if(gres.contains("kritter")) Debug.log.printf("gob2icon.put(\"%s\", \"%s\");%n", gres, ires);
		}
		return true;
	    }
	} catch (Loading ignored) {}
	return false;
    }
    
    public static GobIcon getIcon(Gob gob) {
	String resname = gob2icon.get(gob.resid());
	if(resname != null) {
	    return new GobIcon(gob, Resource.remote().load(resname), true);
	}
	return null;
    }
    
    public static void addCustomSettings(GobIcon.Settings.Loader loader, UI ui) {
	List<RadarItemVO> items = load(Config.loadJarFile(CONFIG_JSON));
	items.addAll(load(Config.loadFSFile(CONFIG_JSON)));
	for (RadarItemVO item : items) {
	    gob2icon.put(item.match, item.icon);
	    addSetting(loader, item.icon, item.visible);
	}
	ui.sess.glob.oc.gobAction(Gob::iconUpdated);
    }
    
    private static void addSetting(GobIcon.Settings.Loader loader, String res, boolean def) {
	if(loader.load.stream().noneMatch(q -> Objects.equals(q.res.name, res))) {
	    Resource.Saved spec = new Resource.Saved(Resource.remote(), res, -1);
	    GobIcon.Settings.ResID id = new GobIcon.Settings.ResID(spec, new byte[0]);
	    GobIcon.Setting cfg = new GobIcon.Setting(spec, GobIcon.Icon.nilid);
	    cfg.show = cfg.defshow = def;
	    
	    Collection<GobIcon.Setting> sets = new ArrayList<>();
	    sets.add(cfg);
	    loader.load.add(id);
	    loader.resolve.put(id, sets);
	}
    }
    
    private static List<RadarItemVO> load(String json) {
	if(json != null) {
	    Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
	    try {
		return gson.fromJson(json, new TypeToken<List<RadarItemVO>>() {
		}.getType());
	    } catch (Exception ignored) {}
	}
	return new LinkedList<>();
    }
    
    enum Symbols {
	$circle("gfx/hud/mmap/symbols/circle"),
	$diamond("gfx/hud/mmap/symbols/diamond"),
	$dot("gfx/hud/mmap/symbols/dot"),
	$down("gfx/hud/mmap/symbols/down"),
	$pentagon("gfx/hud/mmap/symbols/pentagon"),
	$square("gfx/hud/mmap/symbols/square"),
	$up("gfx/hud/mmap/symbols/up");

	public final Tex tex;
	public static final Symbols DEFAULT = $circle;

	Symbols(String res) {
	    tex = Resource.loadtex(res);
	}
    }
    
    private static class RadarItemVO {
	String match, icon;
	boolean visible;
    }
}

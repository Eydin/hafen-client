package me.ender.gob;

import haven.*;
import haven.render.Homo3D;
import haven.render.Pipe;
import haven.render.RenderTree;
import me.ender.RichUText;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static haven.Buff.*;

public class GobCombatInfo extends GAttrib implements RenderTree.Node, PView.Render2D {
    public static final Coord STANCE_SZ = UI.scale(32, 32);
    public static final Coord PROGRESS_SZ = Coord.of(STANCE_SZ.x, UI.scale(6));
    public static final Coord PROGRESS_SZ2 = PROGRESS_SZ.div(2);
    public static final int IP_HX = STANCE_SZ.x / 2 + UI.scale(3);
    public static final int OPENING_DY = UI.scale(8);
    private final Fightview.Relation rel;
    private static final Coord3f pos = new Coord3f(0, 0, CFG.SHOW_COMBAT_INFO_HEIGHT.get());
    private static final Text.Furnace opIp = new PUtils.BlurFurn((new Text.Foundry(Text.monobold, 16, new Color(255, 150, 80))).aa(false), 2, 2, Color.DARK_GRAY);
    private static final Text.Furnace myIp = new PUtils.BlurFurn((new Text.Foundry(Text.monobold, 16, new Color(100, 220, 220))).aa(false), 2, 2, Color.DARK_GRAY);
    private static final RichText.Foundry fndOpen;
    
    //Slightly tweaked colors for better visibility
    private static final Map<String, Color> OPENINGS = new HashMap<String, Color>() {{
	put(OPEN_GREEN, new Color(0, 128, 0));
	put(OPEN_YELLOW, new Color(195, 159, 19));
	put(OPEN_BLUE, new Color(32, 72, 160));
	put(OPEN_RED, new Color(168, 25, 25));
    }};
    
    private static final Map<Long, Fightview.Relation> map = new HashMap<>();
    
    static {
	Map<AttributedCharacterIterator.Attribute, Object> a = new HashMap<>();
	a.put(TextAttribute.FAMILY, "monospaced");
	a.put(TextAttribute.SIZE, UI.scale(16.0f));
	a.put(TextAttribute.FOREGROUND, Color.WHITE);
	a.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
	fndOpen = new RichText.Foundry(new RichText.Parser(a));
	
	CFG.SHOW_COMBAT_INFO_HEIGHT.observe(cfg -> pos.z = cfg.get());
    }
    
    private final Text.UText<?> oip;
    private final Text.UText<?> mip;
    private final RichUText<String> openings;
    
    public GobCombatInfo(Gob owner, Fightview.Relation rel) {
	super(owner);
	this.rel = rel;
	
	this.oip = new Text.UText<Integer>(opIp) {
	    public String text(Integer v) {
		return v.toString();
	    }
	    
	    public Integer value() {
		if(rel == null || rel.invalid)
		    return 0;
		return rel.oip;
	    }
	};
	
	
	this.mip = new Text.UText<Integer>(myIp) {
	    public String text(Integer v) {
		return v.toString();
	    }
	    
	    public Integer value() {
		if(rel == null || rel.invalid)
		    return 0;
		return rel.ip;
	    }
	};
	
	this.openings = new RichUText<String>(fndOpen, new Color(32, 32, 32, 160)) {
	    @Override
	    public String value() {
		return getOpeningText();
	    }
	};
    }
    
    @Override
    public void ctick(double dt) {
	super.ctick(dt);
    }
    
    protected boolean enabled() {
	return CFG.SHOW_COMBAT_INFO.get() && rel != null;
    }
    
    @Override
    public void draw(GOut g, Pipe state) {
	if(!enabled() || rel.invalid) {return;}
	
	Coord sc = Homo3D.obj2sc(pos, state, Area.sized(g.sz()));
	if(sc == null) {return;}
	if(!sc.isect(Coord.z, g.sz())) {return;}
	
	g.aimage(mip.get().tex(), sc.addx(-IP_HX), 1f, 1f);
	g.aimage(oip.get().tex(), sc.addx(IP_HX), 0f, 1f);
	
	TexI tex = openings.get();
	if(tex != null) {g.aimage(tex, sc.addy(OPENING_DY), 0.5f, 0f);}
	
	drawStance(g, sc);
    }
    
    private void drawStance(GOut g, Coord sc) {
	//TODO: add setting to enable?
	if(!gob.is(GobTag.PLAYER)) {return;}
	Set<Buff> buffs = rel.buffs.children(Buff.class);
	Buff stance = buffs.stream().findFirst().orElse(null);
	if(stance == null) {return;}
	try {
	    g.aimage(resize(stance.res.get()), sc, 0.5f, 1f);
	    int meter = stance.ameter();
	    
	    if(meter >= 0) {
		g.chcolor(Color.BLACK);
		g.frect(sc.sub(PROGRESS_SZ2), PROGRESS_SZ);
		g.chcolor();
		if(meter > 0) {
		    g.frect(sc.sub(PROGRESS_SZ2), PROGRESS_SZ.mul(meter / 100f, 1));
		}
	    }
	} catch (Resource.Loading ignore) {}
	
    }
    
    private static final Map<String, TexI> sizedCache = new HashMap<>();
    
    private static TexI resize(Resource res) {
	if(sizedCache.containsKey(res.name)) {
	    return sizedCache.get(res.name);
	}
	
	TexI tex = new TexI(PUtils.convolvedown(res.layer(Resource.imgc).img, STANCE_SZ, CharWnd.iconfilter));
	sizedCache.put(res.name, tex);
	return tex;
    }
    
    private String getOpeningText() {
	String y = null, g = null, b = null, r = null;
	
	for (Buff buff : rel.buffs.children(Buff.class)) {
	    int value = buff.getNMeter();
	    if(value <= 0) {continue;}
	    try {
		String name = buff.res.get().name;
		String format = "%2d";
		if(OPEN_YELLOW.equals(name)) {
		    y = String.format(format, value);
		} else if(OPEN_GREEN.equals(name)) {
		    g = String.format(format, value);
		} else if(OPEN_BLUE.equals(name)) {
		    b = String.format(format, value);
		} else if(OPEN_RED.equals(name)) {
		    r = String.format(format, value);
		}
	    } catch (Loading ignore) {}
	}
	
	String result = null;
	
	result = add(result, RichText.bgcolor(y, OPENINGS.get(OPEN_YELLOW)));
	result = add(result, RichText.bgcolor(g, OPENINGS.get(OPEN_GREEN)));
	result = add(result, RichText.bgcolor(b, OPENINGS.get(OPEN_BLUE)));
	result = add(result, RichText.bgcolor(r, OPENINGS.get(OPEN_RED)));
	
	return result;
    }
    
    private static String add(String current, String value) {
	if(value == null) {return current;}
	if(current == null || current.isEmpty()) {return value;}
	return current + "$col[180,180,180]{|}" + value;
    }
    
    public static void check(Gob gob) {
	Fightview.Relation rel = map.getOrDefault(gob.id, null);
	if(rel != null) {
	    gob.addCombatInfo(rel);
	}
    }
    
    public static void add(Fightview.Relation rel, UI ui) {
	map.put(rel.gobid, rel);
	if(ui == null) {return;}
	Gob gob = ui.sess.glob.oc.getgob(rel.gobid);
	if(gob != null) {gob.addCombatInfo(rel);}
    }
    
    public static void del(Fightview.Relation rel, UI ui) {
	map.remove(rel.gobid);
	if(ui == null) {return;}
	Gob gob = ui.sess.glob.oc.getgob(rel.gobid);
	if(gob != null) {
	    gob.delattr(GobCombatInfo.class);
	}
    }
    
    public static void clear() {
	map.clear();
    }
    
}

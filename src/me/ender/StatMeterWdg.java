package me.ender;

import haven.*;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatMeterWdg extends DraggableWidget {
    public static final String HP = "hp";
    public static final String STAMINA = "stam";
    private static final Coord BAR_SZ = UI.scale(150, 20);
    private static final Color FRAME_COL = new Color(255, 255, 255, 111);
    private static final Text.Foundry TEXT_FND = new Text.Foundry(Text.sansbold, 12);
    
    private final String name;
    protected boolean hover = false;
    private boolean inCombat = false;
    
    public StatMeterWdg(String name) {
	super("StatMeterWdg:" + name);
	this.name = name;
	sz = BAR_SZ;
	
	disposables.add(CFG.SHOW_FLOATING_STAT_WDGS.observe(this::updateState));
	disposables.add(CFG.SHOW_FLOATING_STATS_COMBAT.observe(this::updateState));
	disposables.add(CFG.LOCK_FLOATING_STAT_WDGS.observe(this::updateState));
	updateState(null);
    }
    
    @Override
    public void tick(double dt) {
	super.tick(dt);
	if(ui == null || ui.gui == null) {return;}
	boolean inCombat = ui.gui.isInCombat();
	if(this.inCombat != inCombat) {
	    this.inCombat = inCombat;
	    updateState(null);
	}
    }
    
    private void updateState(CFG<Boolean> cfg) {
	show(CFG.SHOW_FLOATING_STAT_WDGS.get() && (!CFG.SHOW_FLOATING_STATS_COMBAT.get() || inCombat));
	boolean unlocked = !CFG.LOCK_FLOATING_STAT_WDGS.get();
	draggable(unlocked);
	hover = hover && unlocked;
    }
    
    protected IMeter imeter() {
	if(ui == null || ui.gui == null) {return null;}
	return ui.gui.getIMeter(name);
    }
    
    protected double meter(int idx) {
	IMeter imeter = imeter();
	if(imeter == null) {return -1;}
	return imeter.meter(idx);
    }
    
    @Override
    public boolean checkhit(Coord c) {
	if(!draggable()) {return false;}
	return c.isect(Coord.z, sz);
    }
    
    @Override
    public void mousemove(MouseMoveEvent ev) {
	hover = checkhit(ev.c); 
	super.mousemove(ev);
    }
    
    public static class HPMeterWdg extends StatMeterWdg {
	private static final Color SHP_COL = new Color(216, 0, 0, 255);
	private static final Color HHP_COL = new Color(205, 154, 0, 255);
	private static final Pattern PATTERN = Pattern.compile(".*?(\\d+)/(\\d+)/(\\d+).*");
	
	int SHP = 0, HHP = 0, MHP = 0;
	
	public HPMeterWdg() {
	    super(HP);
	}
	
	private void updateValues() {
	    IMeter imeter = imeter();
	    if(imeter == null) {return;}
	    String tip = imeter.tip;
	    if(tip == null) {return;}
	    
	    Matcher matcher = PATTERN.matcher(tip);
	    if(matcher.matches()) {
		try {
		    SHP = Integer.parseInt(matcher.group(1));
		    HHP = Integer.parseInt(matcher.group(2));
		    MHP = Integer.parseInt(matcher.group(3));
		} catch (Exception ignore) {}
	    }
	}
	
	@Override
	public void draw(GOut g) {
	    super.draw(g);
	    double hhp = meter(0);
	    double shp = meter(1);
	    if(shp < 0 || hhp < 0) {return;}
	    updateValues();
	    
	    Coord sc = Coord.of(0, 0);
	    
	    g.chcolor(HHP_COL);
	    g.frect(sc, BAR_SZ.mul(hhp, 1));
	    
	    g.chcolor(SHP_COL);
	    g.frect(sc, BAR_SZ.mul(shp, 1));
	    
	    g.chcolor(hover ? Color.WHITE : FRAME_COL);
	    g.rect(sc, BAR_SZ);
	    
	    String txt = String.format("%d/%d (%.0f%% HHP)", SHP, MHP, 100 * hhp);
	    
	    g.chcolor();
	    g.aimage(Text.renderstroked(txt, TEXT_FND).tex(), sc.add(BAR_SZ.div(2)), 0.5, 0.5);
	}
    }
    
    public static class StaminaMeterWdg extends StatMeterWdg {
	private static final Color SEG1_COL = new Color(3, 3, 80, 160);
	private static final Color SEG2_COL = new Color(16, 16, 128, 160);
	private static final Color SEG3_COL = new Color(16, 16, 255, 160);
	private static final Color DRINK_COL = new Color(100, 255, 100);
	
	private static final float TH1 = 0.25f;
	private static final float TH2 = 0.5f;
	private static final int W1 = (int) (TH1 * BAR_SZ.x);
	private static final int W2 = (int) (TH2 * BAR_SZ.x) - W1;
	
	public StaminaMeterWdg() {
	    super(STAMINA);
	}
	
	@Override
	public void draw(GOut g) {
	    super.draw(g);
	    double value = meter(0);
	    if(value < 0) {return;}
	    Coord sc = Coord.of(0, 0);
	    Coord p = Coord.of(0, 0);
	    int W = (int) Math.ceil(BAR_SZ.x * value);
	    
	    int seg = Math.min(W, W1);
	    g.chcolor(SEG1_COL);
	    g.frect(sc, Coord.of(seg, BAR_SZ.y));
	    p.x += seg;
	    
	    seg = Math.min(W - W1, W2);
	    if(seg > 0) {
		g.chcolor(SEG2_COL);
		g.frect(p, Coord.of(seg, BAR_SZ.y));
		
		p.x += seg;
		seg = W - W1 - W2;
		if(seg > 0) {
		    g.chcolor(SEG3_COL);
		    
		    g.frect(p, Coord.of(seg, BAR_SZ.y));
		}
	    }
	    g.chcolor(hover ? Color.WHITE : FRAME_COL);
	    g.rect(sc, BAR_SZ);
	    
	    String txt = String.format("%.1f", value * 100.0);
	    Gob player = this.ui.gui.map.player();
	    if(player != null && player.is(GobTag.DRINKING)) {
		g.chcolor(DRINK_COL);
		txt = "↑ " + txt + " ↑";
	    } else {
		g.chcolor();
	    }
	    g.aimage(Text.renderstroked(txt, TEXT_FND).tex(), sc.add(BAR_SZ.div(2)), 0.5, 0.5);
	}
    }
}

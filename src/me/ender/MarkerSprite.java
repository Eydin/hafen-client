package me.ender;

import haven.*;
import haven.render.BaseColor;
import haven.render.Render;
import haven.render.RenderTree;

import java.awt.*;
import java.util.function.Supplier;

public class MarkerSprite extends Sprite {
    private static final Supplier<FastMesh> COMBAT = MeshUtils.Ring(4f, 1.75f);
    private static final Supplier<FastMesh> CIRCLE_6 = MeshUtils.Circle(6f);
    private static final Supplier<FastMesh> CIRCLE_10 = MeshUtils.Circle(10f);
    public static Id SELF = Combat(CFG.COLOR_GOB_SELF);
    public static Id PARTY = Combat(CFG.COLOR_GOB_PARTY);
    public static Id LEADER = Combat(CFG.COLOR_GOB_LEADER);
    public static Id ENEMY = Combat(CFG.COLOR_GOB_IN_COMBAT);
    public static Id TARGET = Combat(CFG.COLOR_GOB_COMBAT_TARGET);
    public static Id RABBIT = new Id(CFG.COLOR_GOB_RABBIT.get(), CIRCLE_10);
    public static Id CRITTER = new Id(CFG.COLOR_GOB_CRITTERS.get(), CIRCLE_10);
    public static Id SPEED_BUFF = new Id(CFG.COLOR_GOB_SPEED_BUFF.get(), CIRCLE_6);
    
    static {
	CFG.COLOR_GOB_SELF.observe(cfg -> SELF = Combat(cfg));
	CFG.COLOR_GOB_PARTY.observe(cfg -> PARTY = Combat(cfg));
	CFG.COLOR_GOB_LEADER.observe(cfg -> LEADER = Combat(cfg));
	CFG.COLOR_GOB_IN_COMBAT.observe(cfg -> ENEMY = Combat(cfg));
	CFG.COLOR_GOB_COMBAT_TARGET.observe(cfg -> TARGET = Combat(cfg));
	CFG.COLOR_GOB_RABBIT.observe(cfg -> RABBIT = new Id(cfg.get(), CIRCLE_10));
	CFG.COLOR_GOB_CRITTERS.observe(cfg -> CRITTER = new Id(cfg.get(), CIRCLE_10));
	CFG.COLOR_GOB_SPEED_BUFF.observe(cfg -> SPEED_BUFF = new Id(cfg.get(), CIRCLE_6));
    }
    
    private final Gob gob;
    final FastMesh mesh;
    final RenderTree.Node model;
    Coord2d lc;
    
    public MarkerSprite(Gob owner, Id id) {
	super(owner, null);
	gob = owner;
	mesh = id.provider.get();
	model = new BaseColor(id.color).apply(mesh);
    }
    
    @Override
    public void added(RenderTree.Slot slot) {
	slot.add(model);
    }
    
    @Override
    public void dispose() {
	mesh.dispose();
	super.dispose();
    }
    
    @Override
    public void gtick(Render g) {
	if(!(mesh instanceof MeshUtils.IUpdatingMesh)) {return;}
	MeshUtils.IUpdatingMesh updater = (MeshUtils.IUpdatingMesh) mesh;
	Coord2d cc = gob.rc;
	if(this.lc != null && this.lc.equals(cc)) {return;}
	if(updater.update(g, gob)) {
	    this.lc = cc;
	}
    }
    
    private static Id Combat(CFG<Color> cfg) {
	return new Id(cfg.get(), COMBAT);
    }
    
    public static class Id {
	private final Color color;
	private final Supplier<FastMesh> provider;
	
	public Id(Color color, Supplier<FastMesh> provider) {
	    this.color = color;
	    this.provider = provider;
	}
    }
}

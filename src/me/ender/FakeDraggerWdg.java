package me.ender;

import haven.*;

import java.awt.*;

public class FakeDraggerWdg extends DraggableWidget {
    
    private boolean dbg = false;
    private Coord o = Coord.z;
    
    public FakeDraggerWdg(String name, CFG<Boolean> cfg) {
	super(name);
	if(cfg != null) {
	    draggable(cfg.get());
	    disposables.add(cfg.observe(c -> draggable(c.get())));
	}
    }
    
    public FakeDraggerWdg(String name) {
	this(name, null);
    }
    
    public void origin(Coord o) {
	if(this.o.equals(o)) {return;}
	c = c.sub(this.o).add(o);
	this.o = o;
    }
    
    @Override
    public void draw(GOut g) {
	super.draw(g);
	if(!dbg) {return;}
	g.chcolor(Color.MAGENTA);
	g.rect(Coord.z, sz);
	g.chcolor();
    }
    
    @Override
    protected void initCfg() {
	super.initCfg();
	c = c.add(o);
    }
    
    @Override
    protected void updateCfg() {
	Coord tmp = c;
	c = c.sub(o);
	super.updateCfg();
	c = tmp;
    }
    
    public void reset() {
	c = o;
	updateCfg();
    }
}

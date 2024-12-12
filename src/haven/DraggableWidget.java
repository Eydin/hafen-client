package haven;

public class DraggableWidget extends Widget {
    
    private final String name;
    private UI.Grab dm;
    private Coord doff;
    protected WidgetCfg cfg;
    private boolean draggable = true;
    
    public DraggableWidget(String name) {
	this.name = name;
    }
    
    public void draggable(boolean draggable) {
	this.draggable = draggable;
	if(!draggable) {stop_dragging();}
    }
    
    public boolean draggable() {return draggable;}
    
    private void stop_dragging() {
	if(dm != null) {
	    dm.remove();
	    dm = null;
	    updateCfg();
	}
    }
    
    @Override
    public boolean mousedown(MouseDownEvent ev) {
	if(ev.propagate(this)) {
	    parent.setfocus(this);
	    return true;
	}
	if(checkhit(ev.c) && draggable) {
	    if(ev.b == 1) {
		dm = ui.grabmouse(this);
		doff = ev.c;
	    }
	    parent.setfocus(this);
	    return true;
	}
	return false;
    }
    
    @Override
    public boolean mouseup(MouseUpEvent ev) {
	if(dm != null) {
	    stop_dragging();
	} else {
	    return super.mouseup(ev);
	}
	return (true);
    }
    
    @Override
    public void mousemove(MouseMoveEvent ev) {
	if(dm != null) {
	    this.c = this.c.add(ev.c.add(doff.inv()));
	} else {
	    super.mousemove(ev);
	}
    }
    
    protected void added() {
	initCfg();
    }
    
    protected void initCfg() {
	cfg = WidgetCfg.get(name);
	if(cfg != null) {
	    c = cfg.c == null ? c : cfg.c;
	} else {
	    updateCfg();
	}
    }
    
    protected void updateCfg() {
	setCfg();
	storeCfg();
    }
    
    protected void setCfg() {
	if(cfg == null) {
	    cfg = new WidgetCfg();
	}
	cfg.c = c;
    }
    
    protected void storeCfg() {
	WidgetCfg.set(name, cfg);
    }
}

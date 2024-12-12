package me.ender.ui;

import haven.CFG;
import haven.CheckBox;
import haven.RichText;
import haven.UI;

public class CFGBox extends CheckBox implements CFG.Observer<Boolean> {

    public static final int TT_WIDTH = UI.scale(200);
    protected final CFG<Boolean> cfg;

    public CFGBox(String lbl, CFG<Boolean> cfg) {
	this(lbl, cfg, null, false);
    }

    public CFGBox(String lbl, CFG<Boolean> cfg, String tip) {
	this(lbl, cfg, tip, false);
    }

    public CFGBox(String lbl, CFG<Boolean> cfg, String tip, boolean observe) {
	super(lbl);
	set = null;
	this.cfg = cfg;
	defval();
	if(tip != null) {
	    tooltip = RichText.render(tip, TT_WIDTH).tex();
	}
	if(observe) {cfg.observe(this);}
    }

    protected void defval() {
	a = cfg.get();
    }

    @Override
    public void set(boolean a) {
	this.a = a;
	cfg.set(a);
	if(set != null) {set.accept(a);}
    }

    @Override
    public void destroy() {
	cfg.unobserve(this);
	super.destroy();
    }

    @Override
    public void updated(CFG<Boolean> cfg) {
	a = cfg.get();
    }
}

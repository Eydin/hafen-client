package me.ender;

import haven.*;

import java.util.function.Supplier;

public class CustomPagButton extends MenuGrid.PagButton {
    private static final Resource toggle = Resource.remote().loadwait("ui/pag/toggle");
    private static final Audio.Clip sfx_on = Audio.resclip(Resource.remote().loadwait("sfx/hud/on"));
    private static final Audio.Clip sfx_off = Audio.resclip(Resource.remote().loadwait("sfx/hud/off"));
    private static final Resource.Image img_on = toggle.layer(Resource.imgc, 0);
    private static final Resource.Image img_off = toggle.layer(Resource.imgc, 1);
    
    private final CustomPaginaAction action;
    private final Supplier<Boolean> toggleState;
    
    public CustomPagButton(MenuGrid.Pagina pag, CustomPaginaAction action, Supplier<Boolean> toggleState) {
	super(pag);
	this.action = action;
	this.toggleState = toggleState;
    }
    
    @Override
    public void drawmain(GOut g, GSprite spr) {
	super.drawmain(g, spr);
	if(toggleState != null) {
	    g.image(toggleState.get() ? img_on : img_off, Coord.z);
	}
    }
    
    @Override
    public void use() {use(null);}
    
    @Override
    public void use(MenuGrid.Interaction iact) {
	if(action.perform(pag.button(), iact) && toggleState != null) {
	    pag.scm.ui.sfxrl(toggleState.get() ? sfx_on : sfx_off);
	}
    }
}

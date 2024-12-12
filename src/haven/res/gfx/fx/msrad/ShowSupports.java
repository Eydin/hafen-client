/* Preprocessed source code */
/* $use: gfx/fx/bprad */
/* $use: ui/pag/toggle */

package haven.res.gfx.fx.msrad;

import haven.*;
import haven.MenuGrid.Pagina;
import haven.res.ui.pag.toggle.Toggle;

/* >spr: MSRad */
@haven.FromResource(name = "gfx/fx/msrad", version = 16)
public class ShowSupports extends MenuGrid.PagButton {
    public ShowSupports(Pagina pag) {
	super(pag);
    }

    public static class Fac implements Factory {
	public MenuGrid.PagButton make(Pagina pag) {
	    return(new ShowSupports(pag));
	}
    }

    public void use(MenuGrid.Interaction iact) {
	CFG.SHOW_GOB_RADIUS.set(!CFG.SHOW_GOB_RADIUS.get());
	pag.scm.ui.msg("Mine-support display is now turned " + (MSRad.show ? "on" : "off") + ".", null,
		       Audio.resclip(MSRad.show ? Toggle.sfxon : Toggle.sfxoff));
    }

    public void drawmain(GOut g, GSprite spr) {
	super.drawmain(g, spr);
	g.image(MSRad.show ? Toggle.on : Toggle.off, Coord.z);
    }
}

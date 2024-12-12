package haven;

import java.awt.*;

public class FEPMeter extends Widget {
    private static final Tex bg = Resource.loadtex("gfx/hud/meter/custom/fep");

    private final BAttrWnd.FoodMeter food;

    public FEPMeter(BAttrWnd.FoodMeter food) {
	super(IMeter.fsz);
	this.food = food;
    }

    @Override
    public void draw(GOut g) {
	Coord isz = IMeter.msz;
	Coord off = IMeter.off;
	g.chcolor(0, 0, 0, 255);
	g.frect(off, isz);
	g.chcolor();
	double x = 0;
	int w = isz.x;
	for (BAttrWnd.FoodMeter.El el : food.els) {
	    int l = (int) Math.floor((x / food.cap) * w);
	    int r = (int) Math.floor(((x += el.a) / food.cap) * w);
	    try {
		Color col = el.ev().col;
		g.chcolor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 255));
		g.frect(off.add(l, 0), new Coord(r - l, isz.y));
	    } catch (Loading ignored) {}
	}
	g.chcolor();
	g.image(bg, Coord.z);
    }

    @Override
    public Object tooltip(Coord c, Widget prev) {
	return food.tooltip(c, prev);
    }

    public static void add(UI ui) {
	if(ui.gui == null || ui.gui.chrwdg == null) {return;}
	ui.gui.addcmeter(new FEPMeter(ui.gui.chrwdg.battr.feps));
    }

    public static void rem(UI ui) {
	if(ui.gui == null) {return;}
	ui.gui.delcmeter(FEPMeter.class);
    }
}
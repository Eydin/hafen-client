package haven;

public class HungerMeter extends Widget {
    private static final Tex bg = Resource.loadtex("gfx/hud/meter/custom/hunger");

    private final BAttrWnd.GlutMeter glut;

    public HungerMeter(BAttrWnd.GlutMeter glut) {
	super(IMeter.fsz);
	this.glut = glut;
    }

    @Override
    public void draw(GOut g) {
	if (glut.bg == null)
	    return;
	Coord isz = IMeter.msz;
	Coord off = IMeter.off;
	g.chcolor(glut.bg);
	g.frect(off, isz);
	g.chcolor(glut.fg);
	g.frect(off, new Coord((int) Math.round(isz.x * (glut.glut - Math.floor(glut.glut))), isz.y));
	g.chcolor();
	g.image(bg, Coord.z);
    }

    @Override
    public Object tooltip(Coord c, Widget prev) {
	return glut.tooltip(c, prev);
    }

    public static void add(UI ui) {
	if(ui.gui == null || ui.gui.chrwdg == null) {return;}
	ui.gui.addcmeter(new HungerMeter(ui.gui.chrwdg.battr.glut));
    }

    public static void rem(UI ui) {
	if(ui.gui == null) {return;}
	ui.gui.delcmeter(HungerMeter.class);
    }
}
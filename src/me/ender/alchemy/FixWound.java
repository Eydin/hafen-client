package me.ender.alchemy;

import haven.*;

import java.awt.image.BufferedImage;

public class FixWound extends ItemInfo.Tip {
    public final Indir<Resource> res, repl;
    public final int a;

    public FixWound(Owner owner, Indir<Resource> res, Indir<Resource> repl, int a) {
	super(owner);
	this.res = res;
	this.repl = repl;
	this.a = a;
    }

    public BufferedImage tipimg() {
	BufferedImage t1 = Text.render(String.format("Heal %d points of ", this.a)).img;
	BufferedImage t2 = Text.render(res.get().layer(Resource.tooltip).t).img;
	int h = t1.getHeight();
	BufferedImage icon = PUtils.convolvedown(res.get().layer(Resource.imgc).img, new Coord(h, h), CharWnd.iconfilter);
	BufferedImage ret = catimgsh(0, t1, icon, t2);
	if(repl != null) {
	    ret = catimgsh(0, ret,
		Text.render(" into ").img,
		PUtils.convolvedown(repl.get().layer(Resource.imgc).img, new Coord(h, h), CharWnd.iconfilter),
		Text.render(repl.get().layer(Resource.tooltip).t).img);
	}
	return(ret);
    }
}
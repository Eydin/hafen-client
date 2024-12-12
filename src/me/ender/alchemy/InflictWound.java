package me.ender.alchemy;

import haven.*;

import java.awt.image.BufferedImage;

public class InflictWound extends ItemInfo.Tip {
    public final Indir<Resource> res;
    public final int a;

    public InflictWound(ItemInfo.Owner owner, Indir<Resource> res, int a) {
	super(owner);
	this.res = res;
	this.a = a;
    }

    public BufferedImage tipimg() {
	BufferedImage t1 = Text.render(String.format("Causes %d points of ", this.a)).img;
	BufferedImage t2 = Text.render(res.get().layer(Resource.tooltip).t).img;
	int h = t1.getHeight();
	BufferedImage icon = PUtils.convolvedown(res.get().layer(Resource.imgc).img, new Coord(h, h), CharWnd.iconfilter);
	return (catimgsh(0, t1, icon, t2));
    }
}
package me.ender;

import haven.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CFGColorBtn extends IButton implements CFG.Observer<Color> {
    private static final BufferedImage box = Resource.loadsimg("gfx/hud/color/box");
    private static final Coord isz = Coord.of(box.getWidth(), box.getHeight());
    private static final Coord pad = UI.scale(Coord.of(2));
    private static final Coord csz = isz.sub(pad.mul(2));
    private final CFG<Color> cfg;
    private final String title;
    private final boolean hasAlpha;
    private boolean dis;
    
    public CFGColorBtn(CFG<Color> cfg, boolean hasAlpha) {
	this(cfg, null, hasAlpha);
    }
    
    public CFGColorBtn(CFG<Color> cfg, String title, boolean hasAlpha) {
	super(drawUp(cfg, title), drawDown(cfg, title), drawHover(cfg, title));
	this.title = title;
	this.hasAlpha = hasAlpha;
	recthit = true;
	
	this.cfg = cfg;
	cfg.observe(this);
    }
    
    @Override
    public boolean mousedown(MouseDownEvent ev) {
	if(dis) {return true;}
	return super.mousedown(ev);
    }
    
    @Override
    public boolean mouseup(MouseUpEvent ev) {
	if(dis) {return true;}
	return super.mouseup(ev);
    }
    
    @Override
    public void mousemove(MouseMoveEvent ev) {
	if(dis) {return;}
	super.mousemove(ev);
    }
    
    @Override
    public void click() {
	if(dis) {return;}
	Widget root = ui.gui;
	if(root == null) {
	    root = ui.root;
	}
	dis = true;
	root.add(CFGColorWnd.open(cfg, hasAlpha), ui.mc.add(10, -45)).onDestroyed(widget -> dis = false);
    }
    
    @Override
    public void destroy() {
	cfg.unobserve(this);
	super.destroy();
    }
    
    private static BufferedImage draw(CFG<Color> cfg, String title, Color textColor) {
	BufferedImage ret = TexI.mkbuf(isz);
	Graphics g = ret.getGraphics();
	
	Color col = cfg.get();
	g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 255));
	g.fillRect(pad.x, pad.y, csz.x, csz.y);
	
	g.setColor(textColor);
	g.drawImage(box, 0,0, null);
	
	g.dispose();
	
	BufferedImage img = title != null ? Text.render(title, textColor).img : null;
	return ItemInfo.catimgsh(UI.scale(5), ret, img);
    }
    
    private static BufferedImage drawUp(CFG<Color> cfg, String title) {
	return draw(cfg, title, Color.WHITE);
    }
    
    private static BufferedImage drawDown(CFG<Color> cfg, String title) {
	return draw(cfg, title, Color.LIGHT_GRAY);
    }
    
    private static BufferedImage drawHover(CFG<Color> cfg, String title) {
	return draw(cfg, title, Color.YELLOW);
    }
    
    @Override
    public void updated(CFG<Color> cfg) {
	up = drawUp(cfg, title);
	down = drawDown(cfg, title);
	hover = drawHover(cfg, title);
	redraw();
    }
}

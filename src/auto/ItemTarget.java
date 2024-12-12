package auto;

import haven.Coord;
import haven.WItem;

public class ItemTarget implements ITarget {
    final WItem item;
    
    public ItemTarget(WItem item) {
	this.item = item;
    }
    
    
    @Override
    public void rclick(int modflags) {
	if(!disposed()) {
	    if(item != null) {item.rclick(modflags);}
	}
    }
    
    @Override
    public void click(int button, int modflags) {
	if(disposed()) {return;}
	item.mouseclick(Coord.z, button, modflags);
    }
    
    @Override
    public void interact() {
	if(disposed()) {return;}
	//TODO: implement
    }
    
    @Override
    public void highlight() {
    }
    
    @Override
    public void take() {
    }
    
    @Override
    public void putBack() {
    }
    
    @Override
    public boolean hasMenu() {
	return !disposed();
    }
    
    @Override
    public boolean disposed() {
	return item == null || item.disposed();
    }
}

package auto;

import haven.Coord;

public class ContainedTarget implements ITarget {
    public final InvHelper.ContainedItem contained;
    
    public ContainedTarget(InvHelper.ContainedItem contained) {
	this.contained = contained;
    }
    
    
    @Override
    public void rclick(int modflags) {
	if(contained != null) {contained.item.rclick(modflags);}
    }
    
    @Override
    public void click(int button, int modflags) {
	if(contained != null) {contained.item.mouseclick(Coord.z, button, modflags);}
    }
    
    @Override
    public void interact() {
    }
    
    @Override
    public void highlight() {
    }
    
    @Override
    public void take() {
	if(contained != null && !contained.itemDisposed()) {
	    contained.take();
	}
    }
    
    @Override
    public void putBack() {
	if(contained != null && !contained.containerDisposed()) {
	    contained.putBack();
	}
    }
    
    @Override
    public boolean hasMenu() {
	//TODO: we can return true if item itself has menu?
	return false;
    }
    
    @Override
    public boolean disposed() {
	return false;
    }
}

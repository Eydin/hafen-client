package auto;

import haven.Gob;
import haven.GobTag;

public class GobTarget implements ITarget {
    
    final Gob gob;
    
    public GobTarget(Gob gob) {
	this.gob = gob;
    }
    
    @Override
    public void rclick(int modflags) {
	if(disposed()) {return;}
	gob.rclick(modflags);
    }
    
    @Override
    public void click(int button, int modflags) {
	if(disposed()) {return;}
	gob.click(button, modflags);
    }
    
    @Override
    public void interact() {
	if(disposed()) {return;}
	if(gob != null) {gob.itemact();}
    }
    
    @Override
    public void highlight() {
	if(!disposed()) {
	    gob.highlight();
	}
    }
    
    @Override
    public void take() {
    }
    
    @Override
    public void putBack() {
    }
    
    @Override
    public boolean hasMenu() {
	return !disposed() && gob.is(GobTag.MENU);
    }
    
    @Override
    public boolean disposed() {
	return gob == null || gob.disposed();
    }
}

package auto;

import haven.Gob;
import haven.WItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Targets {
    public static final List<ITarget> EMPTY = Collections.singletonList(new EmptyTarget());
    
    public static List<ITarget> of(WItem... items) {
	return Arrays.stream(items).map(ItemTarget::new).collect(Collectors.toList());
    }

    public static List<ITarget> of(Gob... gobs) {
	return Arrays.stream(gobs).map(GobTarget::new).collect(Collectors.toList());
    }
    
    public static Gob gob(ITarget target) {
	if(target instanceof GobTarget) {
	    return target.disposed() ? null : ((GobTarget) target).gob;
	}
	return null;
    }
    
    public static WItem item(ITarget target) {
	if(target instanceof ItemTarget) {
	    return target.disposed() ? null : ((ItemTarget) target).item;
	}
	return null;
    }
    
    
    private static class EmptyTarget implements ITarget {
	
	
	private EmptyTarget() {
	}
	
	
	@Override
	public void rclick(int modflags) {
	    
	}
	
	@Override
	public void click(int button, int modflags) {
	    
	}
	
	@Override
	public void interact() {
	    
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
	    return false;
	}
	
	@Override
	public boolean disposed() {
	    return false;
	}
    }
}

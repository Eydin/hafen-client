package auto;

import haven.Coord2d;
import haven.GameUI;
import haven.Gob;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public class PositionHelper {
    public static Comparator<Gob> byDistanceToPlayer = (o1, o2) -> {
	try {
	    Gob p = o1.glob.oc.getgob(o1.glob.sess.ui.gui.plid);
	    return Double.compare(p.rc.dist(o1.rc), p.rc.dist(o2.rc));
	} catch (Exception ignored) {}
	return Long.compare(o1.id, o2.id);
    };
    
    static CompletableFuture<Coord2d> mapPosOfMouse(GameUI gui) {
	return gui.map.hit(gui.ui.mc);
    }
    
    static double distanceToPlayer(Gob gob) {
	Gob p = gob.glob.oc.getgob(gob.glob.sess.ui.gui.plid);
	return p.rc.dist(gob.rc);
    }
    
    static double distanceToCoord(Coord2d c, Gob gob) {
	if(c == null) {return Double.MAX_VALUE;}
	return c.dist(gob.rc);
    }
}

package me.ender;

import haven.*;
import me.ender.minimap.Minesweeper;

import static haven.MCache.*;

public class CustomCursors {
    public static final Resource.Named INSPECT = Resource.local().loadwait("gfx/hud/curs/studyx").indir();
    public static final Resource.Named TRACK = Resource.local().loadwait("gfx/hud/curs/track").indir();
    public static final Resource.Named SWEEPER = Resource.local().loadwait("gfx/hud/curs/minesweep").indir();


    public static boolean processHit(MapView map, Coord2d mc, ClickData inf) {
	UI ui = map.ui;

	if(isTracking(map)) {
	    if(inf == null) {return false;}
	    Gob gob = Gob.from(inf.ci);
	    if(gob == null) {return false;}

	    ui.gui.mapfile.track(gob);
	    stopTracking(map);
	    return true;
	} else if(isSweeping(map)) {
	    int modflags = ui.modflags();
	    byte value;

	    if(modflags == 0) {
		value = Minesweeper.FLAG_DANGER;
	    } else if(modflags == UI.MOD_CTRL) {
		value = Minesweeper.CLEAR_FLAGS;
	    } else if(modflags == UI.MOD_META) {
		value = Minesweeper.FLAG_SAFE;
	    } else if(modflags == UI.MOD_SHIFT) {
		value = Minesweeper.FLAG_MAYBE;
	    } else {
		return true;
	    }

	    Minesweeper.markFlagAtPoint(mc, value, ui.gui);
	    return true;
	}

	return false;
    }

    public static boolean processDown(MapView map, Widget.MouseDownEvent ev) {
	if(ev.b == 3) {
	    if(isInspecting(map)) {
		stopInspecting(map);
		return true;
	    } else if(isTracking(map)) {
		stopTracking(map);
		return true;
	    } else if(isSweeping(map)) {
		stopSweeping(map);
		return true;
	    }

	}
	return false;
    }

    public static void inspect(MapView map, Coord c) {
	if(map.cursor == INSPECT || map.cursor == TRACK) {
	    map.new Hittest(c) {
		@Override
		protected void hit(Coord pc, Coord2d mc, ClickData inf) {
		    map.ttip(null);
		    if(inf != null) {
			Gob gob = Gob.from(inf.ci);
			if(gob != null) {
			    map.ttip(map.cursor == INSPECT ? gob.inspect(map.fullTip) : gob.tooltip());
			}
		    } else if(map.cursor == INSPECT) {
			MCache mCache = map.ui.sess.glob.map;
			int tile = mCache.gettile(mc.div(tilesz).floor());
			Resource res = mCache.tilesetr(tile);
			if(res != null) {
			    map.ttip(res.name);
			}
		    }
		}

		@Override
		protected void nohit(Coord pc) {
		    map.ttip(null);
		}
	    }.run();
	} else {
	    map.ttip(null);
	}
    }

    private static void stopCustomModes(MapView map) {
	stopInspecting(map);
	stopTracking(map);
	stopSweeping(map);
    }

    //INSPECTING

    public static boolean isInspecting(MapView map) {
	return map.cursor == INSPECT;
    }

    public static void toggleInspectMode(MapView map) {
	if(isInspecting(map)) {
	    stopInspecting(map);
	} else {
	    startInspecting(map);
	}
    }

    private static void startInspecting(MapView map) {
	stopCustomModes(map);
	if(map.cursor == null) {
	    map.cursor = INSPECT;
	    inspect(map, map.rootxlate(map.ui.mc));
	}
    }

    private static void stopInspecting(MapView map) {
	if(map.cursor == INSPECT) {
	    map.cursor = null;
	    map.ttip(null);
	}
    }

    //TRACKING

    public static boolean isTracking(MapView map) {
	return map.cursor == TRACK;
    }

    public static void toggleTrackingMode(MapView map) {
	if(isTracking(map)) {
	    stopTracking(map);
	} else {
	    startTracking(map);
	}
    }

    private static void startTracking(MapView map) {
	stopCustomModes(map);
	if(map.cursor == null) {
	    map.cursor = TRACK;
	    inspect(map, map.rootxlate(map.ui.mc));
	}
    }

    private static void stopTracking(MapView map) {
	if(map.cursor == TRACK) {
	    map.cursor = null;
	    map.ttip(null);
	}
    }

    //Mine SWEEPER
    public static boolean isSweeping(MapView map) {
	return map.cursor == SWEEPER;
    }

    public static void toggleSweeperMode(MapView map) {
	if(isSweeping(map)) {
	    stopSweeping(map);
	} else {
	    startSweeping(map);
	}
    }

    private static void startSweeping(MapView map) {
	stopCustomModes(map);
	if(map.cursor == null) {
	    map.cursor = SWEEPER;
	    map.ttip(null);
	}
    }

    private static void stopSweeping(MapView map) {
	if(map.cursor == SWEEPER) {
	    map.cursor = null;
	    map.ttip(null);
	}
    }

}

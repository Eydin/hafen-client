package me.ender.minimap;

import haven.MapFile;
import haven.Message;
import haven.StreamMessage;

import java.io.IOException;
import java.io.InputStream;

import static haven.MapFile.*;

public class MapFileUtils {
    public interface ILoader {
	boolean Load(StreamMessage data);
    }
    
    public static boolean load(MapFile file, ILoader loader, String name, Object... args) {
	InputStream fp;
	try {
	    fp = file.sfetch(name, args);
	} catch (IOException e) {
	    return false;
	}
	String fname = String.format(name, args);
	try (StreamMessage data = new StreamMessage(fp)) {
	    return loader.Load(data);
	} catch (Message.BinError e) {
	    warn(e, "error when loading '%s': %s", fname, e);
	    return false;
	}
    }
}

package me.ender;

import haven.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatCommands {
    public static void sendGobHighlight(UI ui, long gobId) {
	ChatUI.EntryChannel channel = findChannelForCommand(ui);
	if(channel != null) {
	    channel.send(String.format("@%d", gobId));
	}
    }
    
    public static void sendPointHighlight(UI ui, long gridId, Coord offset) {
	ChatUI.EntryChannel channel = findChannelForCommand(ui);
	if(channel != null) {
	    channel.send(String.format("@%d;%d;%d", gridId, offset.x, offset.y));
	}
    }
    
    private static ChatUI.EntryChannel findChannelForCommand(UI ui) {
	ChatUI chat = ui.gui.chat;
	ChatUI.Channel selected = chat.sel;
	
	if(selected instanceof ChatUI.PrivChat) {
	    return (ChatUI.EntryChannel) selected;
	}
	
	ChatUI.EntryChannel area = null;
	ChatUI.EntryChannel party = null;
	
	for (ChatUI.Selector.DarkChannel chl : chat.chansel.chls) {
	    if(chl.chan instanceof ChatUI.PartyChat) {
		party = (ChatUI.EntryChannel) chl.chan;
	    } else if(chl.chan instanceof ChatUI.MultiChat && "Area Chat".equals(chl.rname.text)) {
		area = (ChatUI.EntryChannel) chl.chan;
	    }
	}
	
	return party != null ? party : area;
    }
    
    private static final Pattern HIGHLIGHT = Pattern.compile("^@((-?\\d+;-?\\d+;)?-?\\d+)$");
    
    public static boolean matchesCommand(String msg) {
	return HIGHLIGHT.matcher(msg).matches();
    }
    
    public static boolean processCommand(UI ui, String msg) {
	Matcher matcher = HIGHLIGHT.matcher(msg);
	if(matcher.matches()) {
	    try {
		String[] parts = matcher.group(1).split(";");
		if(parts.length == 1) {
		    Gob gob = ui.gui.map.glob.oc.getgob(Long.parseLong(parts[0]));
		    if(gob != null) {
			gob.highlight();
			ui.root.effects.markGob(gob);
			return true;
		    }
		} else if(parts.length == 3) {
		    Coord offset = Coord.of(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		    MCache.Grid grid = ui.sess.glob.map.getgrid(Long.parseLong(parts[0]));
		    if(grid != null) {
			ui.root.effects.markPoint(grid, offset);
			return true;
		    }
		}
	    } catch (Exception ignored) {}
	}
	return false;
    }
}

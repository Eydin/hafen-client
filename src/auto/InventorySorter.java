package auto;

import haven.*;
import me.ender.WindowDetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InventorySorter implements Defer.Callable<Void> {
    public static final String[] EXCLUDE = new String[]{
	"Character Sheet",
	"Study",

	"Chicken Coop",
	"Belt",
	"Pouch",
	"Purse",

	"Cauldron",
	"Finery Forge",
	"Fireplace",
	"Frame",
	"Herbalist Table",
	"Kiln",
	"Ore Smelter",
	"Smith's Smelter",
	"Oven",
	"Pane mold",
	"Rack",
	"Smoke shed",
	"Stack Furnace",
	"Steelbox",
	"Tub"
    };

    private static final Object lock = new Object();
    public static final Comparator<WItem> ITEM_COMPARATOR = Comparator.comparing(WItem::sortName)
	.thenComparing(w -> w.item.resname())
	.thenComparing(WItem::sortValue)
	.thenComparing(WItem::quality, Comparator.reverseOrder());
    private static InventorySorter current;
    private Defer.Future<Void> task;

    private final List<Inventory> inventories;

    private InventorySorter(List<Inventory> inv) {
	this.inventories = inv;
    }

    public static void sort(Inventory inv) {
	if(invalidCursor(inv.ui)) {return;}
	start(new InventorySorter(Collections.singletonList(inv)), inv.ui.gui);
    }

    public static void sortAll(GameUI gui) {
	if(invalidCursor(gui.ui)) {return;}
	List<Inventory> targets = new ArrayList<>();
	for (ExtInventory w : gui.ui.root.children(ExtInventory.class)) {
	    if(w == null) {continue;}
	    WindowX window = w.getparent(WindowX.class);
	    if(window == null || WindowDetector.isWindowType(window, EXCLUDE)) {continue;}
	    if(w.inv != null) {
		targets.add(w.inv);
	    }
	}
	if(!targets.isEmpty()) {
	    start(new InventorySorter(targets), gui);
	}
    }

    private static boolean invalidCursor(UI ui) {
	if(ui.isDefaultCursor()) {
	    return false;
	}
	ui.message("Need to have default cursor active to sort inventory!", GameUI.MsgType.ERROR);
	return true;
    }
    
    @Override
    public Void call() throws InterruptedException {
	for (Inventory inv : inventories) {
	    if(inv.disposed()) {
		cancel();
		break;
	    }
	    doSort(inv);
	}
	synchronized (lock) {
	    if(current == this) {current = null;}
	}
	return null;
    }

    private void doSort(Inventory inv) {
	boolean[][] grid = new boolean[inv.isz.x][inv.isz.y];
	List<WItem> items = new ArrayList<>();
	for (Widget wdg = inv.lchild; wdg != null; wdg = wdg.prev) {
	    if(wdg.visible && wdg instanceof WItem) {
		WItem wItem = (WItem) wdg;
		Coord sz = wItem.lsz;
		Coord loc = wItem.c.sub(1, 1).div(Inventory.sqsz);
		if(sz.x * sz.y == 1) {
		    items.add(wItem);
		} else {
		    for (int x = 0; x < sz.x; x++) {
			for (int y = 0; y < sz.y; y++) {
			    grid[loc.x + x][loc.y + y] = true;
			}
		    }
		}
	    }
	}
	List<Object[]> sorted = items.stream()
	    .filter(witem -> witem.lsz.x * witem.lsz.y == 1)
	    .sorted(ITEM_COMPARATOR)
	    .map(witem -> new Object[]{witem, witem.c.sub(1, 1).div(Inventory.sqsz), new Coord(0, 0)})
	    .collect(Collectors.toList());

	int cur_x = -1, cur_y = 0;
	for (Object[] a : sorted) {
	    while (true) {
		cur_x += 1;
		if(cur_x == inv.isz.x) {
		    cur_x = 0;
		    cur_y += 1;
		    if(cur_y == inv.isz.y) {break;}
		}
		if(!grid[cur_x][cur_y]) {
		    a[2] = new Coord(cur_x, cur_y);
		    break;
		}
	    }
	    if(cur_y == inv.isz.y) {break;}
	}

	Object[] handu;
	for (Object[] a : sorted) {
	    if(a[1].equals(a[2])) // item in right place
	    {
		continue;
	    }
	    ((WItem) a[0]).take(); // item in wrong place, take it
	    handu = a;
	    while (handu != null) {
		inv.wdgmsg("drop", handu[2]); // place item in right place
		// find item in new pos
		Object[] b = null;
		for (Object[] x : sorted) {
		    if(x[1].equals(handu[2])) {
			b = x;
			break;
		    }
		}
		handu[1] = handu[2]; // update item position
		handu = b;
	    }
	}
    }

    private void run(Consumer<String> callback) {
	task = Defer.later(this);
	task.callback(() -> callback.accept(task.cancelled() ? "cancelled" : "complete"));
    }

    public static void cancel() {
	synchronized (lock) {
	    if(current != null) {
		current.task.cancel();
		current = null;
	    }
	}
    }

    private static final Audio.Clip sfx_done = Audio.resclip(Resource.remote().loadwait("sfx/hud/on"));

    private static void start(InventorySorter inventorySorter, GameUI gui) {
	cancel();
	synchronized (lock) {current = inventorySorter;}
	inventorySorter.run((result) -> {
	    if(result.equals("complete")) {
		gui.ui.sfxrl(sfx_done);
	    } else {
		gui.ui.message(String.format("Sort is %s.", result), GameUI.MsgType.INFO);
	    }
	});
    }
}
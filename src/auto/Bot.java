package auto;

import haven.*;
import haven.rx.Reactor;
import rx.functions.Action1;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;



import static haven.Inventory.*;
import static haven.OCache.*;


public class Bot implements Defer.Callable<Void> {
    private static final Object lock = new Object();
    private static Bot current;
    private final List<Target> targets;
    private final BotAction[] actions;
    private Defer.Future<Void> task;
    private boolean cancelled = false;
    private static final Object waiter = new Object();
    
    public Bot(List<Target> targets, BotAction... actions) {
	this.targets = targets;
	this.actions = actions;
    }
    
    @Override
    public Void call() throws InterruptedException {
	targets.forEach(Target::highlight);
	for (Target target : targets) {
	    for (BotAction action : actions) {
		if(target.disposed()) {break;}
		action.call(target);
		checkCancelled();
	    }
	}
	synchronized (lock) {
	    if(current == this) {current = null;}
	}
	return null;
    }
    
    private void run(Action1<String> callback) {
	task = Defer.later(this);
	task.callback(() -> callback.call(task.cancelled() ? "cancelled" : "complete"));
    }
    
    private void checkCancelled() throws InterruptedException {
	if(cancelled) {
	    throw new InterruptedException();
	}
    }
    
    private void markCancelled() {
	cancelled = true;
	task.cancel();
    }
    
    public static void cancel() {
	synchronized (lock) {
	    if(current != null) {
		current.markCancelled();
		current = null;
	    }
	}
    }
    
    public static void start(Bot bot, UI ui) {
	cancel();
	synchronized (lock) { current = bot; }
	bot.run((result) -> {
	    if (CFG.SHOW_BOT_MESSAGES.get())
	    	ui.message(String.format("Task is %s.", result), GameUI.MsgType.INFO);
	});
    }
    
    public static void pickup(GameUI gui, String filter) {
	pickup(gui, filter, Integer.MAX_VALUE);
    }
    
    public static void pickup(GameUI gui, String filter, int limit) {
	pickup(gui, startsWith(filter), limit);
    }
    
    public static void pickup(GameUI gui, Predicate<Gob> filter) {
	pickup(gui, filter, Integer.MAX_VALUE);
    }
    
    public static void pickup(GameUI gui, Predicate<Gob> filter, int limit) {
	List<Target> targets = gui.ui.sess.glob.oc.stream()
	    .filter(filter)
	    .filter(gob -> distanceToPlayer(gob) <= CFG.AUTO_PICK_RADIUS.get())
	    .filter(Bot::isOnRadar)
	    .sorted(byDistance)
	    .limit(limit)
	    .map(Target::new)
	    .collect(Collectors.toList());
	
	start(new Bot(targets,
	    Target::rclick,
	    selectFlower("Pick"),
	    target -> target.gob.waitRemoval()
	), gui.ui);
    }
    
    public static void pickup(GameUI gui) {
	pickup(gui, has(GobTag.PICKUP));
    }
    
    public static void selectFlower(GameUI gui, long gobid, String option) {
	List<Target> targets = gui.ui.sess.glob.oc.stream()
	    .filter(gob -> gob.id == gobid)
	    .map(Target::new)
	    .collect(Collectors.toList());
	
	selectFlower(gui, option, targets);
    }
    
    public static void selectFlowerOnItems(GameUI gui, String option, List<WItem> items) {
	List<Target> targets = items.stream()
	    .map(Target::new)
	    .collect(Collectors.toList());
    
	selectFlower(gui, option, targets);
    }
    
    public static void selectFlower(GameUI gui, String option, List<Target> targets) {
	start(new Bot(targets, Target::rclick, selectFlower(option)), gui.ui);
    }
    
    public static void drink(GameUI gui) {
	Collection<Supplier<List<WItem>>> everywhere = Arrays.asList(HANDS(gui), INVENTORY(gui), BELT(gui));
	Utils.chainOptionals(
	    () -> findFirstThatContains("Tea", everywhere),
	    () -> findFirstThatContains("Water", everywhere)
	).ifPresent(Bot::drink);
    }
    
    public static void drink(WItem item) {
	start(new Bot(Collections.singletonList(new Target(item)), Target::rclick, selectFlower("Drink")), item.ui);
    }
    
    public static void fuelGob(GameUI gui, String name, String fuel, int count) {
	List<Target> targets = getNearestTargets(gui, name, 1);
	
	if(!targets.isEmpty()) {
	    start(new Bot(targets, fuelWith(gui, fuel, count)), gui.ui);
	}
    }
    
    public static void spitroast(GameUI gui) {
	List<Target> targets = getNearestTargets(gui, "terobjs/pow", 1);
	
	gui.ui.message(String.format("Found Spitroast %i", !targets.isEmpty()), GameUI.MsgType.INFO);
	
	if(!targets.isEmpty()) {
	    //start(new Bot(targets, fuelWith(gui, fuel, count)), gui.ui);
	    start(new Bot(targets, Target::rclick, selectFlower("Take")), gui.ui);
	}
    }
    public static BotAction test(GameUI gui, String seedname, List<Target> barrels) {
	return target -> {
	    Supplier<List<WItem>> inventory = INVENTORY(gui);
	    float has = countItems(seedname, inventory);
	    if(has >= 1) {
		Optional<WItem> w = findFirstItem(seedname, inventory);
		if(w.isPresent()) {
		    w.get().take();
		    if(!waitHeld(gui, seedname)) {
			cancel();
			return;
		    }
		    Thread.sleep(100);
		    
		    Coord whereToPlant = gui.map.player().rc.floor(OCache.posres);
		    Coord mc = gui.ui.mc;
		    gui.map.wdgmsg("itemact", Coord.z, whereToPlant, 0);
		    //gui.ui.mousedown(whereToPlant,3);
		    //gui.ui.mouseup(whereToPlant,3);
		    //gui.ui.mousemove(mc);
		    
		    Thread.sleep(100);
		    
		    //place seed back in inv
		    Coord c = gui.maininv.findPlaceFor(w.get().lsz);
		    if(c != null) {
			c = c.mul(sqsz).add(sqsz.div(2));
			gui.maininv.drop(c, c);
		    } else {
			gui.ui.message("Non enough space!", GameUI.MsgType.BAD);
		    }
		    
		} else {
		    cancel();
		    return;
		}
	    } else {
		cancel();
	    }
	};
    }
    
    
    
    public static BotAction replant(GameUI gui, String seedname, List<Target> barrels) {
	return target -> {
	    try {
		if(target.gob.getgrowthstage() != 1)
		    return;
		
		FlowerMenu.lastTarget(target);
		Reactor.FLOWER.first().subscribe(flowerMenu -> {
		    Reactor.FLOWER_CHOICE.first().subscribe(choice -> unpause());
		    flowerMenu.forceChoose("Harvest");
		});
		target.rclick();
		try {
		    target.gob.waitRemoval();
		}
		catch (InterruptedException ignored) {}
		
		Supplier<List<WItem>> inventory = INVENTORY(gui);
		float has = 0;
		int retries = 20;
		while (retries > 0) {
		    has = countItems(seedname, inventory);
		    if(has > 1)
			break;
		    
		    Thread.sleep(25);
		    retries--;
		}
		if(has >= 1) {
		    Optional<WItem> w = findFirstItem(seedname, inventory);
		    if(w.isPresent()) {
			w.get().take();
			if(!waitHeld(gui, seedname)) {
			    gui.ui.message("Could not take seeds from inv 1", GameUI.MsgType.BAD);
			    cancel();
			    return;
			}
			
			Supplier<List<WItem>> hands = HANDS(gui);
			float hasInHandsBeforePlanting = countItems(seedname, hands);
			
			if (false) {
			    Coord whereToPlant = gui.map.player().rc.floor(OCache.posres);
			    Coord mc = gui.ui.mc;
			    gui.map.wdgmsg("itemact", Coord.z, whereToPlant, 0);
			    
			    retries = 20;
			    while (retries > 0) {
				if(hasInHandsBeforePlanting > countItems(seedname, hands))
				    break;
				
				Thread.sleep(25);
				retries--;
			    }
			}
			//place seed back in inv
			Coord c = gui.maininv.findPlaceFor(w.get().lsz);
			if(c != null) {
			    c = c.mul(sqsz).add(sqsz.div(2));
			    gui.maininv.drop(c, c);
			} else {
			    gui.ui.message("Non enough space!", GameUI.MsgType.BAD);
			}
			
		    } else {
			gui.ui.message("Could not take seeds from inv 2", GameUI.MsgType.BAD);
			cancel();
			return;
		    }
		} else {
		    gui.ui.message("Did not find seeds in inv", GameUI.MsgType.BAD);
		    cancel();
		    return;
		}
		
		// Empty inventory
		if(has > 1300) {
		    Optional<WItem> w = findFirstItem(seedname, inventory);
		    if(w.isPresent()) {
			w.get().take();
			if(!waitHeld(gui, seedname)) {
			    gui.ui.message("Did not find seeds in inv 2", GameUI.MsgType.BAD);
			    cancel();
			    return;
			}
		    }
		    
		    while (!barrels.isEmpty()) {
			barrels.get(0).gob.itemact(UI.MOD_SHIFT | UI.MOD_CTRL);
			Thread.sleep(5000);
			if(!waitHeld(gui, null)) {
			    barrels.remove(0);
			} else {
			    break;
			}
		    }
		    
		    if(barrels.isEmpty()) {
			gui.ui.message("Ran out of barrels", GameUI.MsgType.BAD);
			cancel();
			return;
		    }
		    
		}
	    }
	    catch (Exception ignored){
		int i = 0;
		i++;
	    }
	};
    }
    
    public static void farmTurnip(GameUI gui) {
	List<Target> targets = getUnsortedNearestTargets(gui, "terobjs/plants/turnip", 10000, 500);
	
	List<Target> barrels = getNearestTargets(gui, "terobjs/barrel", 5);
	
	gui.ui.message("Found barrels" + barrels.stream().count(), GameUI.MsgType.INFO);
	
	if(!targets.isEmpty()) {
	    start(new Bot(targets, replant(gui, "Turnip", barrels)), gui.ui);
	}
    }
    private static List<Target> getUnsortedNearestTargets(GameUI gui, String name, int limit, int radius) {
	return gui.ui.sess.glob.oc.stream()
	    .filter(gobIs(name))
	    .filter(gob -> distanceToPlayer(gob) <= radius)
	    .sorted(byDistance)
	    .limit(limit)
	    .sorted(byY)
	    .map(Target::new)
	    .collect(Collectors.toList());
    }
    
    private static List<Target> getNearestTargets(GameUI gui, String name, int limit) {
	return gui.ui.sess.glob.oc.stream()
	    .filter(gobIs(name))
	    .filter(gob -> distanceToPlayer(gob) <= CFG.AUTO_PICK_RADIUS.get())
	    .sorted(byDistance)
	    .limit(limit)
	    .map(Target::new)
	    .collect(Collectors.toList());
    }
    
    private static BotAction fuelWith(GameUI gui, String fuel, int count) {
	return target -> {
	    Supplier<List<WItem>> inventory = INVENTORY(gui);
	    float has = countItems(fuel, inventory);
	    if(has >= count) {
		for (int i = 0; i < count; i++) {
		    Optional<WItem> w = findFirstItem(fuel, inventory);
		    if(w.isPresent()) {
			w.get().take();
			if(!waitHeld(gui, fuel)) {
			    cancel();
			    return;
			}
			target.interact();
			if(!waitHeld(gui, null)) {
			    cancel();
			    return;
			}
		    } else {
			cancel();
			return;
		    }
		}
	    } else {
		cancel();
	    }
	};
    }
    
    private static boolean isHeld(GameUI gui, String what) throws Loading {
	GameUI.DraggedItem drag = gui.hand();
	if(drag == null && what == null) {
	    return true;
	}
	if(drag != null && what != null) {
	    return drag.item.is2(what);
	}
	return false;
    }
    
    private static boolean waitHeld(GameUI gui, String what) {
	if(Boolean.TRUE.equals(doWaitLoad(() -> isHeld(gui, what)))) {
	    return true;
	}
	if(waitHeldChanged(gui)) {
	    return Boolean.TRUE.equals(doWaitLoad(() -> isHeld(gui, what)));
	}
	return false;
    }
    
    private static boolean waitHeldChanged(GameUI gui) {
	boolean result = true;
	try {
	    synchronized (gui.heldNotifier) {
		gui.heldNotifier.wait(5000);
	    }
	} catch (InterruptedException e) {
	    result = false;
	}
	return result;
    }
    
    private static <T> T doWaitLoad(Supplier<T> action) {
	T result = null;
	boolean ready = false;
	while (!ready) {
	    try {
		result = action.get();
		ready = true;
	    } catch (Loading e) {
		pause(100);
	    }
	}
	return result;
    }
    
    private static void pause(long ms) {
	synchronized (waiter) {
	    try {
		waiter.wait(ms);
	    } catch (InterruptedException ignore) {
	    }
	}
    }
    
    private static void unpause() {
	synchronized (waiter) { waiter.notifyAll(); }
    }
    
    private static List<WItem> items(Widget inv) {
	return inv != null ? new ArrayList<>(inv.children(WItem.class)) : new LinkedList<>();
    }
    
    private static Optional<WItem> findFirstThatContains(String what, Collection<Supplier<List<WItem>>> where) {
	for (Supplier<List<WItem>> place : where) {
	    Optional<WItem> w = place.get().stream()
		.filter(contains(what))
		.findFirst();
	    if(w.isPresent()) {
		return w;
	    }
	}
	return Optional.empty();
    }
    
    private static Predicate<WItem> contains(String what) {
	return w -> w.contains.get().is(what);
    }
    
    private static Predicate<Gob> gobIs(String what) {
	return g -> {
	    if(g == null) { return false; }
	    String id = g.resid();
	    if(id == null) {return false;}
	    return id.contains(what);
	};
    }
    
    private static float countItems(String what, Supplier<List<WItem>> where) {
	return where.get().stream()
	    .filter(wItem -> wItem.is(what))
	    .map(wItem -> wItem.quantity.get())
	    .reduce(0f, Float::sum);
    }
    
    private static Optional<WItem> findFirstItem(String what, Supplier<List<WItem>> where) {
	return where.get().stream()
	    .filter(wItem -> wItem.is(what))
	    .findFirst();
    }
    
    
    private static Supplier<List<WItem>> INVENTORY(GameUI gui) {
	return () -> items(gui.maininv);
    }
    
    private static Supplier<List<WItem>> BELT(GameUI gui) {
	return () -> {
	    Equipory e = gui.equipory;
	    if(e != null) {
		WItem w = e.slots[Equipory.SLOTS.BELT.idx];
		if(w != null) {
		    return items(w.item.contents);
		}
	    }
	    return new LinkedList<>();
	};
    }
    
    private static Supplier<List<WItem>> HANDS(GameUI gui) {
	return () -> {
	    List<WItem> items = new LinkedList<>();
	    if(gui.equipory != null) {
		WItem slot = gui.equipory.slots[Equipory.SLOTS.HAND_LEFT.idx];
		if(slot != null) {
		    items.add(slot);
		}
		slot = gui.equipory.slots[Equipory.SLOTS.HAND_RIGHT.idx];
		if(slot != null) {
		    items.add(slot);
		}
	    }
	    return items;
	};
    }
    
    private static boolean isOnRadar(Gob gob) {
	if(!CFG.AUTO_PICK_ONLY_RADAR.get()) {return true;}
	GobIcon icon = gob.getattr(GobIcon.class);
	GameUI gui = gob.glob.sess.ui.gui;
	if(icon != null && gui != null) {
	    try {
		GobIcon.Setting s = gui.iconconf.get(icon.res.get());
		return s.show;
	    } catch (Loading ignored) {}
	}
	return true;
    }
    
    private static double distanceToPlayer(Gob gob) {
	Gob p = gob.glob.oc.getgob(gob.glob.sess.ui.gui.plid);
	return p.rc.dist(gob.rc);
    }
    
    public static Comparator<Gob> byDistance = (o1, o2) -> {
	try {
	    Gob p = o1.glob.oc.getgob(o1.glob.sess.ui.gui.plid);
	    return Double.compare(p.rc.dist(o1.rc), p.rc.dist(o2.rc));
	} catch (Exception ignored) {}
	return Long.compare(o1.id, o2.id);
    };
    
    public static Comparator<Gob> byY = (o1, o2) -> {
	try {
	    return Double.compare(o2.rc.y, o1.rc.y);
	} catch (Exception ignored) {}
	return Long.compare(o1.id, o2.id);
    };
    
    private static BotAction selectFlower(String... options) {
	return target -> {
	    if(target.hasMenu()) {
		FlowerMenu.lastTarget(target);
		Reactor.FLOWER.first().subscribe(flowerMenu -> {
		    Reactor.FLOWER_CHOICE.first().subscribe(choice -> unpause());
		    flowerMenu.forceChoose(options);
		});
		pause(5000);
	    }
	};
    }
    
    private static Predicate<Gob> startsWith(String text) {
	return gob -> {
	    try {
		return gob.getres().name.startsWith(text);
	    } catch (Exception ignored) {}
	    return false;
	};
    }
    
    private static Predicate<Gob> has(GobTag tag) {
	return gob -> gob.is(tag);
    }
    
    public interface BotAction {
	void call(Target target) throws InterruptedException;
    }
    
    public static class Target {
	public final Gob gob;
	public final WItem item;
	
	public Target(Gob gob) {
	    this.gob = gob;
	    this.item = null;
	}
	
	public Target(WItem item) {
	    this.item = item;
	    this.gob = null;
	}
	
	public void rclick() {
	    if(!disposed()) {
		if(gob != null) {gob.rclick();}
		if(item != null) {item.rclick();}
	    }
	}
	
	public void shiftrclick() {
	    if(!disposed()) {
		if(gob != null) {gob.rclick(UI.MOD_SHIFT);}
		if(item != null) {item.rclick();}
	    }
	}
    
	public void interact() {
	    if(!disposed()) {
		if(gob != null) {gob.itemact();}
		if(item != null) {/*TODO: implement*/}
	    }
	}
    
	public void highlight() {
	    if(!disposed()) {
		if(gob != null) {gob.highlight();}
	    }
	}
    
	public boolean hasMenu() {
	    if(gob != null) {return gob.is(GobTag.MENU);}
	    return item != null;
	}
    
	public boolean disposed() {
	    return (item != null && item.disposed()) || (gob != null && gob.disposed());
	}
    }
}

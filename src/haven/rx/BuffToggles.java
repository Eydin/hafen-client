package haven.rx;

import haven.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuffToggles {
    public static final Collection<Toggle> toggles = new ArrayList<>();
    private static final Collection<String> filters;

    static {
	toggles.add(new Toggle("Tracking", "paginae/act/tracking", "tracking", "Tracking is now turned on.", "Tracking is now turned off."));
	toggles.add(new Toggle("Criminal Acts", "paginae/act/crime", "crime", "Criminal acts are now turned on.", "Criminal acts are now turned off."));
	toggles.add(new Toggle("Swimming", "paginae/act/swim", "swim", "Swimming is now turned on.", "Swimming is now turned off."));

	filters = toggles.stream()
	    .flatMap(toggle -> Stream.of(toggle.msgOn, toggle.msgOff))
	    .collect(Collectors.toCollection(HashSet::new));
    }

    public static void init(GameUI gameUI) {
	Reactor.IMSG.filter(filters::contains)
	    .subscribe(BuffToggles::toggle);

	toggles.forEach(toggle -> toggle.setup(gameUI));
    }
    
    public static void menuBound(MenuGrid menu) {
	toggles.forEach(toggle -> toggle.menuBound(menu));
    }

    private static void toggle(String msg) {
	toggles.stream().filter(t -> t.matches(msg)).findFirst().ifPresent(toggle -> toggle.update(msg));
    }

    public static class Toggle {

	public final String name;
	private final String resname;
	public final String action;
	private final String msgOn;
	private final String msgOff;
	private boolean state = false;
	private Buff buff;
	public CFG<Boolean> show;
	public CFG<Boolean> startup;
	private GameUI gui;
	private MenuGrid menu;
	private boolean toggled = false;
	private boolean init = false;

	public Toggle(String name, String resname, String action, String msgOn, String msgOff) {
	    this.name = L10N.tooltip(resname, name);
	    this.resname = resname;

	    this.action = action;
	    this.msgOn = msgOn;
	    this.msgOff = msgOff;
	}

	public boolean matches(String msg) {
	    return msg.equals(msgOn) || msg.equals(msgOff);
	}

	public void update(String msg) {
	    state = msg.equals(msgOn);
	    update();
	}

	private void update() {
	    if(gui == null) {return;}
	    if(state && show.get()) {
		if(buff == null) {
		    buff = new TBuff(this);
		    gui.buffs.addchild(buff);
		}
	    } else {
		if(buff != null) {
		    gui.ui.destroy(buff);
		    buff = null;
		}
	    }
	}

	public boolean act() {
	    if(gui != null && menu != null) {
		menu.paginafor(resname).button().use();
		return true;
	    } else {
		toggled = !toggled;
	    }
	    return false;
	}

	public void cfg(CFG<Boolean> show, CFG<Boolean> startup) {
	    this.show = show;
	    this.startup = startup;
	}

	public void setup(GameUI gameUI) {
	    gui = gameUI;
	    init();
	}

	public void menuBound(MenuGrid menu) {
	   this.menu = menu;
	   init();
	}
	
	private void init() {
	    if(init) {return;}
	    if(startup.get()) {
		init = act();
	    } else {
		init = true;
	    }
	}
    }

    private static class TBuff extends Buff {
	private final Toggle toggle;

	public TBuff(Toggle toggle) {
	    super(Resource.remote().load(toggle.resname));
	    this.toggle = toggle;
	}

	@Override
	public boolean mousedown(MouseDownEvent ev) {
	    toggle.act();
	    return true;
	}
    }
}

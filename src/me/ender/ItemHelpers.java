package me.ender;

import haven.*;
import haven.res.ui.tt.wear.Wear;

import java.util.Optional;
import java.util.Set;

import static me.ender.ResName.*;

public class ItemHelpers {
    public static final Coord DISHES_SZ = Coord.of(3, 3);
    public static final Coord TABLECLOTH_SZ = Coord.of(1, 2);
    private static final int DISH_HP_WARNING = 1;


    public static boolean canTake(WItem item) {
	UI ui = item.ui;
	if(ui == null) {return true;}
	String msg;
	if(CFG.PRESERVE_SYMBEL.get() && (msg = preserveDishes(item, ui)) != null) {
	    ui.message(msg, GameUI.MsgType.ERROR);
	    return false;
	}
	return true;
    }

    public static void invalidateFoodItemTooltips(UI ui) {
	Set<WItem> children = ui.root.children(WItem.class);
	children.forEach(w -> {if(ItemData.hasFoodInfo(w.item)) {w.clearLongTip();}});
    }

    public static void invalidateIngredientTooltips(UI ui) {
	Set<WItem> children = ui.root.children(WItem.class);
	children.forEach(w -> {if(ItemData.hasIngredientInfo(w.item)) {w.clearLongTip();}});
    }

    private static String preserveDishes(WItem item, UI ui) {
	if(!ItemData.hasFoodInfo(item.item)) {return null;}

	if(!ui.isCursor(CURSOR_EAT)) {return null;}

	Window wnd = item.getparent(Window.class);
	if(wnd == null) {return null;}

	Optional<WItem> atRisk = wnd.children(Inventory.class).stream()
	    .filter(i -> i.isz.equals(DISHES_SZ) || i.isz.equals(TABLECLOTH_SZ))
	    .flatMap(i -> i.children(WItem.class).stream())
	    .filter(w -> {
		Wear wear = ItemInfo.getWear(w.item.info());
		if(wear == null) {return false;}
		return wear.m - wear.d <= DISH_HP_WARNING;
	    })
	    .findFirst();

	return atRisk.map(wItem -> String.format("Cannot eat from this table: %s is almost broken!", wItem.name.get())).orElse(null);

    }
}

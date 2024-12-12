package me.ender.alchemy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.*;
import haven.res.ui.tt.attrmod.AttrMod;
import haven.rx.Reactor;
import me.ender.Reflect;

import java.util.*;
import java.util.stream.Collectors;

public class AlchemyData {
    private static final String INGREDIENTS_JSON = "ingredients.json";
    private static final String ELIXIRS_JSON = "elixirs.json";
    private static final String ALL_INGREDIENTS_JSON = "all_ingredients.json";
    private static final String COMBOS_JSON = "combos.json";
    private static final String EFFECTS_JSON = "all_effects.json";

    public static final String INGREDIENTS_UPDATED = "ALCHEMY:INGREDIENTS:UPDATED";
    public static final String ELIXIRS_UPDATED = "ALCHEMY:ELIXIRS:UPDATED";
    public static final String COMBOS_UPDATED = "ALCHEMY:COMBOS:UPDATED";
    public static final String EFFECTS_UPDATED = "ALCHEMY:EFFECTS:UPDATED";


    public static final String HERBAL_GRIND = "/herbalgrind";
    public static final String LYE_ABLUTION = "/lyeablution";
    public static final String MINERAL_CALCINATION = "/mineralcalcination";
    public static final String MEASURED_DISTILLATE = "/measureddistillate";
    public static final String FIERY_COMBUSTION = "/fierycombustion";

    private static final Gson GSON = new GsonBuilder()
	.registerTypeAdapter(Effect.class, new Effect.Adapter())
	.create();
    public static final int MAX_EFFECTS = 4;

    private static boolean initializedIngredients = false;
    private static boolean initializedElixirs = false;
    private static boolean initializedCombos = false;
    private static boolean initializedEffects = false;
    private static final Map<String, Ingredient> INGREDIENTS = new HashMap<>();
    private static final Set<Elixir> ELIXIRS = new HashSet<>();
    private static final Set<String> INGREDIENT_LIST = new HashSet<>();
    private static final Map<String, Set<String>> COMBOS = new HashMap<>();
    private static final HashSet<Effect> EFFECTS = new HashSet<>();


    private static void initIngredients() {
	if(initializedIngredients) {return;}
	initializedIngredients = true;
	loadIngredients(Config.loadJarFile(INGREDIENTS_JSON));
	loadIngredients(Config.loadFSFile(INGREDIENTS_JSON));
    }

    private static void initElixirs() {
	if(initializedElixirs) {return;}
	initializedElixirs = true;
	loadElixirs(Config.loadFile(ELIXIRS_JSON));
    }

    private static void initCombos() {
	if(initializedCombos) {return;}
	initializedCombos = true;
	loadIngredientList(Config.loadJarFile(ALL_INGREDIENTS_JSON));
	loadIngredientList(Config.loadFSFile(ALL_INGREDIENTS_JSON));
	loadCombos(Config.loadFile(COMBOS_JSON));
    }

    private static void initEffects() {
	if(initializedEffects) {return;}
	initializedEffects = true;
	loadEffectList(Config.loadJarFile(EFFECTS_JSON));
	loadEffectList(Config.loadFSFile(EFFECTS_JSON));

	boolean changed = false;

	initIngredients();
	for (Ingredient ingredient : INGREDIENTS.values()) {
	    changed = tryAddUnknownEffects(ingredient) || changed;
	}

	initElixirs();
	for (Elixir elixir : ELIXIRS) {
	    changed = tryAddUnknownEffects(elixir) || changed;
	}

	if(changed) {saveEffects();}
    }

    private static void loadIngredients(String json) {
	if(json == null) {return;}
	try {
	    Map<String, Ingredient> tmp = GSON.fromJson(json, new TypeToken<Map<String, Ingredient>>() {
	    }.getType());
	    for (Map.Entry<String, Ingredient> entry : tmp.entrySet()) {
		String res = entry.getKey();
		INGREDIENTS.put(res, new Ingredient(entry.getValue().effects, INGREDIENTS.get(res)));
	    }
	} catch (Exception ignore) {}
    }

    private static void loadElixirs(String json) {
	if(json == null) {return;}
	try {
	    Set<Elixir> tmp = GSON.fromJson(json, new TypeToken<Set<Elixir>>() {
	    }.getType());
	    ELIXIRS.addAll(tmp);
	} catch (Exception ignore) {}
    }

    private static void loadIngredientList(String json) {
	if(json == null) {return;}
	try {
	    Set<String> tmp = GSON.fromJson(json, new TypeToken<Set<String>>() {
	    }.getType());
	    INGREDIENT_LIST.addAll(tmp);
	} catch (Exception ignore) {}
    }

    private static void loadCombos(String json) {
	if(json == null) {return;}
	try {
	    Map<String, Set<String>> tmp = GSON.fromJson(json, new TypeToken<Map<String, Set<String>>>() {
	    }.getType());
	    for (Map.Entry<String, Set<String>> entry : tmp.entrySet()) {
		String key = entry.getKey();
		Set<String> combos = COMBOS.computeIfAbsent(key, k -> new HashSet<>());
		combos.addAll(entry.getValue());
	    }
	} catch (Exception ignore) {}
    }

    private static void loadEffectList(String json) {
	if(json == null) {return;}
	try {
	    Set<Effect> tmp = GSON.fromJson(json, new TypeToken<Set<Effect>>() {
	    }.getType());
	    EFFECTS.addAll(tmp);
	} catch (Exception ignore) {}
    }

    public static void saveIngredients() {
	Config.saveFile(INGREDIENTS_JSON, GSON.toJson(INGREDIENTS));
    }

    private static void saveElixirs() {
	Config.saveFile(ELIXIRS_JSON, GSON.toJson(ELIXIRS));
    }

    private static void saveIngredientList() {
	Config.saveFile(ALL_INGREDIENTS_JSON, GSON.toJson(INGREDIENT_LIST));
    }

    private static void saveCombos() {
	Config.saveFile(COMBOS_JSON, GSON.toJson(COMBOS));
    }

    private static void saveEffects() {
	Config.saveFile(EFFECTS_JSON, GSON.toJson(EFFECTS));
    }

    public static void autoProcess(GItem item) {
	if(!CFG.ALCHEMY_AUTO_PROCESS.get()) {return;}
	if(item.ui.gui.getchild(AlchemyWnd.class) != null || item.ui.gui.getchild(TrackWnd.class) != null) {
	    process(item, false);
	}
    }

    public static void process(GItem item, boolean storeRecipe) {
	String res = item.resname();
	List<ItemInfo> infos = item.info();
	double q = item.quality();
	double qc = q > 0 ? 1d / Math.sqrt(10 * q) : 1d;

	ItemInfo.Contents contents = ItemInfo.find(ItemInfo.Contents.class, infos);
	if(contents != null) {infos = contents.sub;}

	List<Effect> effects = new LinkedList<>();
	boolean isElixir = false;
	Recipe recipe = null;

	for (ItemInfo info : infos) {
	    if(Reflect.is(info, "Elixir")) {
		isElixir = true;
		//noinspection unchecked
		List<ItemInfo> effs = (List<ItemInfo>) Reflect.getFieldValue(info, "effs");
		for (ItemInfo eff : effs) {
		    tryAddElixirEffect(qc, effects, eff);
		}
	    } else if(info instanceof haven.res.ui.tt.alch.recipe.Recipe) {
		recipe = Recipe.from(res, (haven.res.ui.tt.alch.recipe.Recipe) info);
	    } else {
		tryAddIngredientEffect(effects, info);
	    }
	}

	boolean effectsChanged = false;
	if(isElixir && recipe != null) {
	    //TODO: option to ignore bad-only elixirs?
	    Elixir elixir = new Elixir(recipe, effects);
	    if(storeRecipe) {
		initElixirs();
		ELIXIRS.add(elixir);
		saveElixirs();
		Reactor.event(ELIXIRS_UPDATED);
	    }
	    effectsChanged = tryAddUnknownEffects(elixir);
	    updateCombos(elixir);
	} else if(!isElixir && !effects.isEmpty() && isNatural(res)) {
	    initIngredients();
	    Ingredient base = INGREDIENTS.get(res);
	    Ingredient ingredient = new Ingredient(effects, base);
	    if(base == null || !Objects.equals(ingredient, base)) {
		INGREDIENTS.put(res, ingredient);
		saveIngredients();
		updateIngredientList(res);
		Reactor.event(INGREDIENTS_UPDATED);
	    }
	    effectsChanged = tryAddUnknownEffects(ingredient);
	}

	if(effectsChanged) {
	    saveEffects();
	    Reactor.event(EFFECTS_UPDATED);
	}
    }

    private static void updateIngredientList(String ingredient) {
	initCombos();
	if(INGREDIENT_LIST.add(ingredient)) {
	    saveIngredientList();
	    Reactor.event(COMBOS_UPDATED);
	}
    }

    private static void updateCombos(Elixir elixir) {
	List<String> natural = elixir.recipe.ingredients.stream()
	    .map(i -> i.res)
	    .filter(AlchemyData::isNatural)
	    .collect(Collectors.toList());

	if(natural.isEmpty()) {return;}
	initCombos();
	boolean listUpdated = false;
	boolean combosUpdated = false;
	for (String ingredient : natural) {
	    if(INGREDIENT_LIST.add(ingredient)) {listUpdated = true;}
	    Set<String> combos = COMBOS.computeIfAbsent(ingredient, k -> new HashSet<>());
	    if(combos.addAll(natural)) {combosUpdated = true;}
	}

	if(listUpdated) {saveIngredientList();}
	if(combosUpdated) {saveCombos();}

	if(listUpdated || combosUpdated) {Reactor.event(COMBOS_UPDATED);}
    }

    public static List<String> ingredients() {
	initIngredients();
	return new ArrayList<>(INGREDIENTS.keySet());
    }

    public static Ingredient ingredient(String res) {
	initIngredients();
	return INGREDIENTS.getOrDefault(res, null);
    }

    public static List<Elixir> elixirs() {
	initElixirs();
	return ELIXIRS.stream().sorted().collect(Collectors.toList());
    }

    public static void rename(Elixir elixir, String name) {
	initElixirs();
	elixir.name(name);
	saveElixirs();
	Reactor.event(ELIXIRS_UPDATED);
    }

    public static void remove(Elixir elixir) {
	initElixirs();
	ELIXIRS.remove(elixir);
	saveElixirs();
	Reactor.event(ELIXIRS_UPDATED);
    }

    public static List<String> allIngredients() {
	initCombos();
	return new ArrayList<>(INGREDIENT_LIST);
    }

    public static Set<String> combos(String target) {
	initCombos();
	return COMBOS.getOrDefault(target, Collections.emptySet());
    }

    public static Set<Effect> effects() {
	initEffects();
	return EFFECTS;
    }

    public static Set<Effect> testedEffects(String res) {
	if(res == null) {return Collections.emptySet();}
	initEffects();
	Ingredient ingredient = ingredient(res);
	Set<Effect> tested;
	if(ingredient != null) {
	    if(ingredient.effects.size() == MAX_EFFECTS) {return effects();}
	    tested = new HashSet<>(ingredient.effects);
	} else {
	    tested = new HashSet<>();
	}
	for (String combo : AlchemyData.combos(res)) {
	    ingredient = AlchemyData.ingredient(combo);
	    if(ingredient == null) {continue;}
	    tested.addAll(ingredient.effects);
	}
	return tested;
    }

    public static Set<Effect> untestedEffects(String res) {
	Set<Effect> tested = testedEffects(res);
	if(tested.isEmpty()) {return Collections.emptySet();}

	HashSet<Effect> effects = new HashSet<>(effects());
	if(effects.removeAll(tested)) {
	    return effects;
	}
	return Collections.emptySet();
    }

    public static boolean tryAddUnknownEffects(Ingredient ingredient) {
	boolean changed = false;
	initEffects();
	for (Effect effect : ingredient.effects) {
	    changed = EFFECTS.add(new Effect(effect.type, effect.res)) || changed;
	}
	return changed;
    }

    public static boolean tryAddUnknownEffects(Elixir elixir) {
	boolean changed = false;
	initEffects();
	for (Effect effect : elixir.effects) {
	    if(Effect.WOUND.equals(effect.type)) {continue;}
	    changed = EFFECTS.add(new Effect(effect.type, effect.res)) || changed;
	}
	return changed;
    }

    public static Tex tex(Collection<Effect> effects) {
	try {
	    List<ItemInfo> tips = Effect.ingredientInfo(effects);
	    if(tips.isEmpty()) {return null;}
	    return new TexI(ItemInfo.longtip(tips));

	} catch (Loading ignore) {}
	return null;
    }

    public static void tryAddIngredientEffect(Collection<Effect> effects, ItemInfo info) {
	Effect effect = Effect.from(info);
	if(effect != null) {
	    effects.add(effect);
	}
    }

    public static void tryAddElixirEffect(double qc, Collection<Effect> effects, ItemInfo info) {
	if(info instanceof AttrMod) {
	    for (AttrMod.Mod mod : ((AttrMod) info).mods) {
		long a = Math.round(qc * mod.mod);
		effects.add(new Effect(Effect.BUFF, mod.attr.name, Long.toString(a)));
	    }
	} else if(Reflect.is(info, "HealWound")) {
	    //this is from elixir, it uses different resource and has value
	    //noinspection unchecked
	    Indir<Resource> res = (Indir<Resource>) Reflect.getFieldValue(info, "res");
	    long a = Math.round(qc * Reflect.getFieldValueInt(info, "a"));
	    effects.add(new Effect(Effect.HEAL, res, Long.toString(a)));
	} else if(Reflect.is(info, "AddWound")) {
	    //this is from elixir
	    //noinspection unchecked
	    Indir<Resource> res = (Indir<Resource>) Reflect.getFieldValue(info, "res");
	    //TODO: try to find base wound magnitude
	    int a = Reflect.getFieldValueInt(info, "a");
	    effects.add(new Effect(Effect.WOUND, res, Long.toString(a)));
	}
	//TODO: detect less/more time effects in elixirs?
    }

    public static boolean isNatural(String res) {
	return !res.contains(HERBAL_GRIND)
	    && !res.contains(LYE_ABLUTION)
	    && !res.contains(MINERAL_CALCINATION)
	    && !res.contains(MEASURED_DISTILLATE)
	    && !res.contains(FIERY_COMBUSTION);
    }

    public static boolean isMineral(String res) {
	return GobIconCategoryList.GobCategory.isRock(res) || GobIconCategoryList.GobCategory.isOre(res);
    }
}

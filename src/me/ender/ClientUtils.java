package me.ender;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import haven.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientUtils {
    private static final Pattern RESID = Pattern.compile(".*\\[([^,]*),?.*]");
    private static final Map<String, String> customNames = new HashMap<>();
    private static boolean customNamesInit = false;
    
    private static final DecimalFormat f2sfmt = new DecimalFormat("0");
    
    static String[] units = {"s", "m", "h", "d"};
    static int[] div = {60, 60, 24};
    
    public static String prettyResName(Indir<Resource> res) {
	if(res == null) {return "???";}
	try {
	    return prettyResName(res.get());
	} catch (Loading ignore) {}
	if(res instanceof Resource.Named) {
	    return prettyResName(((Resource.Named) res).name);
	}
	return "???";
    }

    public static String loadPrettyResName(String resname) {
	try {
	    return prettyResName(Resource.remote().loadwait(resname));
	} catch (Exception ignore) {}
	return prettyResName(resname);
    }
    
    public static String prettyResName(Resource res) {
	if(res == null) {return "???";}
	Resource.Tooltip tt = res.layer(Resource.tooltip);
	if(tt != null) {
	    return tt.t;
	} else {
	    return prettyResName(res.name);
	}
    }
    
    public static String prettyResName(String resname) {
	if(resname == null || resname.isEmpty()) {return "???";}
	tryInitCustomNames();
	if(customNames.containsKey(resname)) {
	    return customNames.get(resname);
	}
	String fullname = resname;
	Matcher m = RESID.matcher(resname);
	if(m.matches()) {
	    resname = m.group(1);
	}
	int k = resname.lastIndexOf("/");
	resname = resname.substring(k + 1);
	
	//handle logs
	if(fullname.contains("terobjs/trees/") && resname.endsWith("log")) {
	    resname = resname.substring(0, resname.length() - 3);
	    if(resname.endsWith("tree")) {
		resname = resname.substring(0, resname.length() - 4) + " Tree";
	    }
	    resname += " Log";
	} else if(fullname.startsWith(ResName.BARREL_WITH_CONTENTS)) {
	    resname = fullname.substring(fullname.lastIndexOf("-") + 1);
	} else if(fullname.contains(ResName.STOCKPILE)) {
	    k = resname.lastIndexOf(ResName.STOCKPILE);
	    resname = resname.substring(k + 11) + " Stockpile";
	}
	
	//handle flour
	if(resname.endsWith("flour")) {
	    resname = resname.substring(0, resname.length() - 5) + " Flour";
	}

	resname = resname.substring(0, 1).toUpperCase() + resname.substring(1);
	
	return resname;
    }
    
    private static void tryInitCustomNames() {
	if(customNamesInit) {return;}
	customNamesInit = true;
	try {
	    Gson gson = new GsonBuilder().create();
	    customNames.putAll(gson.fromJson(Config.loadJarFile("res_names.json"), new TypeToken<Map<String, String>>() {
	    }.getType()));
	} catch (Exception ignored) {}
    }
    
    public static String timestamp() {
	return new SimpleDateFormat("HH:mm").format(new Date());
    }
    
    public static String timestamp(String text) {
	return String.format("[%s] %s", timestamp(), text);
    }
    
    public static String stream2str(InputStream is) {
	StringBuilder buffer = new StringBuilder();
	BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	String line;
	boolean first = true;
	try {
	    while ((line = in.readLine()) != null) {
		if(!first) {buffer.append("\n");}
		buffer.append(line);
		first = false;
	    }
	} catch (IOException ignored) {
	}
	return buffer.toString();
    }
    
    public static Color hex2color(String hex, Color def) {
	Color c = def;
	if(hex != null) {
	    try {
		int col = (int) Long.parseLong(hex, 16);
		boolean hasAlpha = (0xff000000 & col) != 0;
		c = new Color(col, hasAlpha);
	    } catch (Exception ignored) {}
	}
	return c;
    }
    
    public static int clamp(int value, int min, int max) {
	return Math.max(Math.min(max, value), min);
    }
    
    public static int str2cc(String value) {
	return clamp(str2int(value), 0, 255);
    }
    
    public static int str2int(String value) {
	int result = 0;
	if(value != null) {
	    try {
		result = Integer.parseInt(value);
	    } catch (Exception ignored) {}
	}
	return result;
    }
    
    public static String color2hex(Color col) {
	return color2hex(col, true);
    }
    
    public static String color2hex(Color col, boolean hasAlpha) {
	if(col != null) {
	    int rgb = col.getRGB();
	    if(!hasAlpha) {rgb = rgb & 0xffffff;}
	    return Integer.toHexString(rgb);
	}
	return null;
    }
    
    public static String f2s(double f) {return f2s(f, 2);}
    
    public static String f2s(double f, int precision) {
	f2sfmt.setMaximumFractionDigits(precision);
	return f2sfmt.format(f);
    }
    
    //Liang-Barsky algorithm
    public static Pair<Coord, Coord> clipLine(Coord a, Coord b, Coord ul, Coord br) {
	// Define the x/y clipping values for the border.
	double edgeLeft = ul.x;
	double edgeRight = br.x;
	double edgeBottom = ul.y;
	double edgeTop = br.y;
	
	// Define the start and end points of the line.
	double x0src = a.x;
	double y0src = a.y;
	double x1src = b.x;
	double y1src = b.y;
	
	double t0 = 0.0;
	double t1 = 1.0;
	double xdelta = x1src - x0src;
	double ydelta = y1src - y0src;
	double p = 0, q = 0, r;
	
	for (int edge = 0; edge < 4; edge++) {   // Traverse through left, right, bottom, top edges.
	    if(edge == 0) {
		p = -xdelta;
		q = -(edgeLeft - x0src);
	    }
	    if(edge == 1) {
		p = xdelta;
		q = (edgeRight - x0src);
	    }
	    if(edge == 2) {
		p = -ydelta;
		q = -(edgeBottom - y0src);
	    }
	    if(edge == 3) {
		p = ydelta;
		q = (edgeTop - y0src);
	    }
	    if(p == 0 && q < 0) return null;   // Don't draw line at all. (parallel line outside)
	    r = q / p;
	    
	    if(p < 0) {
		if(r > t1) return null;         // Don't draw line at all.
		else if(r > t0) t0 = r;         // Line is clipped!
	    } else if(p > 0) {
		if(r < t0) return null;      // Don't draw line at all.
		else if(r < t1) t1 = r;      // Line is clipped!
	    }
	}
	
	return new Pair<>(new Coord((int) (x0src + t0 * xdelta), (int) (y0src + t0 * ydelta)), new Coord((int) (x0src + t1 * xdelta), (int) (y0src + t1 * ydelta)));
    }
    
    public static Optional<Coord2d> intersect(Pair<Coord2d, Coord2d> lineA, Pair<Coord2d, Coord2d> lineB) {
	double a1 = lineA.b.y - lineA.a.y;
	double b1 = lineA.a.x - lineA.b.x;
	double c1 = a1 * lineA.a.x + b1 * lineA.a.y;
	
	double a2 = lineB.b.y - lineB.a.y;
	double b2 = lineB.a.x - lineB.b.x;
	double c2 = a2 * lineB.a.x + b2 * lineB.a.y;
	
	double delta = a1 * b2 - a2 * b1;
	if(delta == 0) {
	    return Optional.empty();
	}
	return Optional.of(new Coord2d((float) ((b2 * c1 - b1 * c2) / delta), (float) ((a1 * c2 - a2 * c1) / delta)));
    }
    
    public static boolean checkbit(int target, int index) {
	return (target & (1 << index)) != 0;
    }
    
    public static int setbit(int target, int index, boolean value) {
	if(value) {
	    return target | (1 << index);
	} else {
	    return target & ~(1 << index);
	}
    }
    
    public static double round(double a, int order) {
	double o = Math.pow(10, order);
	return Math.round(o * a) / o;
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Number> T num2value(Number n, Class<T> type) {
	if(Integer.class.equals(type)) {
	    return (T) (Integer) (n.intValue());
	} else if(Long.class.equals(type)) {
	    return (T) (Long) (n.longValue());
	}
	return (T) (Float) (n.floatValue());
    }
    
    @SafeVarargs
    public static <T> Optional<T> chainOptionals(Supplier<Optional<T>>... items) {
	return Arrays.stream(items).map(Supplier::get).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
    
    public static String formatTimeLong(int time) {
	int[] vals = new int[units.length];
	vals[0] = time;
	for (int i = 0; i < div.length; i++) {
	    vals[i + 1] = vals[i] / div[i];
	    vals[i] = vals[i] % div[i];
	}
	StringBuilder buf = new StringBuilder();
	for (int i = units.length - 1; i >= 0; i--) {
	    if(vals[i] > 0) {
		if(buf.length() > 0) {
		    buf.append(String.format(" %02d", vals[i]));
		} else {
		    buf.append(vals[i]);
		}
		buf.append(units[i]);
	    }
	}
	return (buf.toString());
    }
    
    public static String formatTimeShort(int time) {
	if(time >= 60) {
	    if(time > 3600) {
		time = time / 60;
	    }
	    return String.format("%d:%02d", time / 60, time % 60);
	} else {
	    return String.format("%02d", time);
	}
    }
    
    public static class ColorSerializer implements JsonSerializer<Color> {
	@Override
	public JsonElement serialize(Color color, Type type, JsonSerializationContext serializer) {
	    return new JsonPrimitive(color2hex(color));
	}
    }
    
    public static Coord getScreenCenter(UI ui) {
	return ui.root.sz.div(2);
    }
}

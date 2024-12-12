/* Preprocessed source code */

package haven.res.gfx.invobjs.gems.gemstone;
import java.awt.image.*;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import haven.*;
import static haven.PUtils.*;

/* >ispr: Gemstone */
@haven.FromResource(name = "gfx/invobjs/gems/gemstone", version = 53)
public class Gemstone extends GSprite implements GSprite.ImageSprite, ItemInfo.Name.Dynamic {
    public final BufferedImage img;
    public final Tex tex;
    public final String name;
    private final String sortName;
    private final int sortValue;

    public Gemstone(Owner owner, Resource res, Message sdt) {
	super(owner);
	Resource.Resolver rr = owner.context(Resource.Resolver.class);
	if(!sdt.eom()) {
	    Resource cut = rr.getres(sdt.uint16()).get();
	    int texid = sdt.uint16();
	    String gemName;
	    if(texid != 65535) {
		Resource tex = rr.getres(texid).get();
		this.tex = new TexI(this.img = construct(cut, tex));
		gemName = tex.layer(Resource.tooltip).t;
	    } else {
		this.tex = new TexI(this.img = construct(cut, null));
		gemName = "Gemstone";
	    }
	    String cutName = cut.layer(Resource.tooltip).t;
	    name = cutName + " " + gemName;
	    sortName = "Gem:" + gemName;
	    sortValue = sortValue(cutName);
	} else {
	    this.tex = new TexI(this.img = TexI.mkbuf(new Coord(32, 32)));
	    sortName = name = "Broken gem";
	    sortValue = 0;
	}
    }

    public static BufferedImage convert(BufferedImage img) {
	WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, img.getWidth(), img.getHeight(), 4, null);
	BufferedImage tgt = new BufferedImage(TexI.glcm, buf, false, null);
	Graphics g = tgt.createGraphics();
	g.drawImage(img, 0, 0, null);
	g.dispose();
	return(tgt);
    }

    public static WritableRaster alphamod(WritableRaster dst) {
	int w = dst.getWidth(), h = dst.getHeight();
	for(int y = 0; y < h; y++) {
	    for(int x = 0; x < w; x++) {
		dst.setSample(x, y, 3, (dst.getSample(x, y, 3) * 3) / 4);
	    }
	}
	return(dst);
    }

    public static WritableRaster alphasq(WritableRaster dst) {
	int w = dst.getWidth(), h = dst.getHeight();
	for(int y = 0; y < h; y++) {
	    for(int x = 0; x < w; x++) {
		int a = dst.getSample(x, y, 3);
		dst.setSample(x, y, 3, (a * a) / 255);
	    }
	}
	return(dst);
    }

    public static Coord so(Resource.Image img) {
	return(UI.scale(img.o).min(UI.scale(img.tsz).sub(img.ssz)));
    }

    public static BufferedImage construct(Resource cut, Resource tex) {
	Resource.Image outl, body, hili;
	BufferedImage outli, bodyi, hilii;
	try {
	    outl = cut.layer(Resource.imgc, 0);
	    body = cut.layer(Resource.imgc, 1);
	    hili = cut.layer(Resource.imgc, 2);
	    outli = convert(outl.scaled());
	    bodyi = convert(body.scaled());
	    hilii = convert(hili.scaled());
	    Coord sz = UI.scale(body.tsz);
	    WritableRaster buf = imgraster(sz);
	    blit(buf, outli.getRaster(), so(outl));
	    WritableRaster buf2 = imgraster(sz);
	    blit(buf2, bodyi.getRaster(), so(body));
	    alphablit(buf2, hilii.getRaster(), so(hili));
	    if(tex != null) {
		BufferedImage texi = ((TexL)tex.layer(TexR.class).tex()).fill();
		texi = convolvedown(texi, sz.mul(2), new Lanczos(3));
		tilemod(buf2, texi.getRaster(), Coord.z);
	    }
	    // alphamod(buf2);
	    alphablit(buf2, alphasq(blit(imgraster(imgsz(hilii)), hilii.getRaster(), Coord.z)), so(hili));
	    alphablit(buf, buf2, Coord.z);
	    return(rasterimg(buf));
	} catch(RuntimeException e) {
	    throw(new RuntimeException(String.format("invalid gemstone in %s (using %s)", (cut == null) ? "null" : cut.name, (tex == null) ? null : tex.name), e));
	}
    }

    public Coord sz() {
	return(imgsz(img));
    }

    public void draw(GOut g) {
	g.image(tex, Coord.z);
    }

    public String name() {
	return(name);
    }

    public BufferedImage image() {
	return(img);
    }

    public String sortName() {return sortName;}

    public int sortValue() {return sortValue;}

    private static final Map<String, Integer> CUTS = new HashMap<>();
    private static final Map<String, Integer> SIZES = new HashMap<>();

    static {
	CUTS.put("Brilliant", 1);
	CUTS.put("Heart", 2);
	CUTS.put("Pear", 3);
	CUTS.put("Cabochon", 4);
	CUTS.put("Smooth", 5);
	CUTS.put("Rough", 6);

	SIZES.put("Jotun", 100);
	SIZES.put("Grand", 200);
	SIZES.put("Large", 300);
	SIZES.put("Fair", 400);
	SIZES.put("Small", 500);
	SIZES.put("Tiny", 600);
    }

    private int sortValue(String cut) {
	String[] parts = cut.split(" ");
	int value = 0;
	if(parts.length > 0) {
	    value += SIZES.getOrDefault(parts[0], 0);
	}
	if(parts.length > 1) {
	    value += CUTS.getOrDefault(parts[1], 0);
	}
	return value;
    }
}

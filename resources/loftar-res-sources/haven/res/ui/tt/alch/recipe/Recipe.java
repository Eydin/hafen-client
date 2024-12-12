/* Preprocessed source code */
package haven.res.ui.tt.alch.recipe;

import haven.*;
import java.util.*;
import java.awt.image.BufferedImage;

/* >tt: Recipe */
@haven.FromResource(name = "ui/tt/alch/recipe", version = 1)
public class Recipe extends ItemInfo.Tip {
    public final List<Spec> inputs;

    public static class Spec {
	public final ResData res;
	public final List<Spec> inputs;

	public Spec(ResData res, List<Spec> inputs) {
	    this.res = res;
	    this.inputs = inputs;
	}

	public static int parse(List<Spec> buf, Owner owner, Object[] args, int a) {
	    ResData res = new ResData(owner.context(Resource.Resolver.class).getresv(args[a++]), Message.nil);
	    if((args.length > a) && (args[a] instanceof byte[]))
		res.sdt = new MessageBuf((byte[])args[a++]);
	    List<Spec> sub = Collections.emptyList();
	    if((args.length > a) && (args[a] instanceof Object[])) {
		Object[] sspec = (Object[])args[a++];
		sub = new ArrayList<>();
		for(int sa = 0; sa < sspec.length;)
		    sa = parse(sub, owner, sspec, sa);
		((ArrayList)sub).trimToSize();
	    }
	    buf.add(new Spec(res, sub));
	    return(a);
	}
    }

    public Recipe(Owner owner, List<Spec> inputs) {
	super(owner);
	this.inputs = inputs;
    }

    public static ItemInfo mkinfo(Owner owner, Raw raw, Object... args) {
	ArrayList<Spec> inputs = new ArrayList<>();
	for(int a = 1; a < args.length;)
	    a = Spec.parse(inputs, owner, args, a);
	inputs.trimToSize();
	return(new Recipe(owner, inputs));
    }

    private void layout(Layout l, Spec input, int indent) {
	ItemSpec spec = new ItemSpec(owner, input.res, null);
	BufferedImage nm = Text.render(spec.name()).img;
	BufferedImage icon = PUtils.convolvedown(spec.image(), Coord.of(nm.getHeight()), CharWnd.iconfilter);
	Coord c = Coord.of(UI.scale(10) * indent, l.cmp.sz.y);
	l.cmp.add(icon, c);
	l.cmp.add(nm, c.add(icon.getWidth() + UI.scale(2), 0));
	for(Spec sub : input.inputs)
	    layout(l, sub, indent + 1);
    }

    public void layout(Layout l) {
	l.cmp.add(Text.render("Made from:").img, Coord.of(0, l.cmp.sz.y));
	for(Spec input : inputs)
	    layout(l, input, 1);
    }

    public int order() {return(2000);}
}

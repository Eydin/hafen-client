package me.ender;

import haven.MenuGrid;
import haven.Resource;

import java.util.function.Supplier;

public class CustomPagina extends MenuGrid.Pagina {
    public final String resName;
    
    public CustomPagina(MenuGrid scm, Resource.Named res, CustomPaginaAction action, Supplier<Boolean> toggleState) {
	super(scm, res, res);
	this.resName = res.name;
	button(new CustomPagButton(this, action, toggleState));
    }
    
    public static boolean isLocalPagina(MenuGrid.Pagina p) {
	if(p == null) {return false;}
	if(p instanceof CustomPagina) {
	    return true;
	}
	String resname = MenuGrid.Pagina.resname(p);
	if(resname.isEmpty()) {
	    return false;
	}
	if(resname.equals("paginae/act/add")) {
	    return true;
	}
	
	return isLocalPagina(p.parent());
    }
}

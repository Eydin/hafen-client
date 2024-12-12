package me.ender;

import haven.MenuGrid;
import haven.OwnerContext;

public interface CustomPaginaAction {
    /**Should return true if this pagina should be toggled*/
    boolean perform(OwnerContext ctx, MenuGrid.Interaction iact);
}

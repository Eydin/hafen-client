package auto;

import haven.Gob;
import haven.UI;
import haven.WItem;

public interface ITarget {
    
    default void rclick(Bot b) {rclick();}
    
    default void rclick() {rclick(0);}
    
    default void rclick_shift(Bot b) {rclick_shift();}
    
    default void rclick_shift() {
	rclick(UI.MOD_SHIFT);
    }
    
    void rclick(int modflags);
    
    void click(int button, int modflags);
    
    void interact();
    
    void highlight();
    
    default void take(Bot b) {take();}
    
    void take();
    
    default void putBack(Bot b) {putBack();}
    
    void putBack();
    
    boolean hasMenu();
    
    boolean disposed();
}

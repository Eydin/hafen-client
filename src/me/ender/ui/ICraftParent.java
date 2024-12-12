package me.ender.ui;

import java.util.HashMap;
import java.util.Map;

public interface ICraftParent {
    Map<String, Integer> CraftAmounts = new HashMap<>();

    void setCraftAmount(int amount);

    default int getCraftAmount() {return -1;}
}

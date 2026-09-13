package com.arsenal.item;

import net.minecraft.world.item.Item;

/**
 * Marker item for casting molds (casing, bullet, shot). A mold is placed in the Munitions Bench's
 * mold slot to select what the machine casts; it is not consumed. The distinct type lets the menu's
 * mold slot accept only molds without needing an item tag.
 */
public class MoldItem extends Item {
    public MoldItem(Properties properties) {
        super(properties);
    }
}

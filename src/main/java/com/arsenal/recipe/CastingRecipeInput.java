package com.arsenal.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * The two-slot input the Munitions Bench presents to a {@link CastingRecipe}: a metal to melt down
 * and the mold that shapes it.
 */
public record CastingRecipeInput(ItemStack metal, ItemStack mold) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> metal;
            case 1 -> mold;
            default -> throw new IllegalArgumentException("No slot " + index + " in a casting input");
        };
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return metal.isEmpty() && mold.isEmpty();
    }
}

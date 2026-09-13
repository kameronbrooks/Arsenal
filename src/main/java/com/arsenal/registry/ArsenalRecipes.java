package com.arsenal.registry;

import com.arsenal.Arsenal;
import com.arsenal.recipe.CastingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Registration for the custom {@code arsenal:casting} recipe type and its serializer. */
public final class ArsenalRecipes {
    private ArsenalRecipes() {}

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Arsenal.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Arsenal.MOD_ID);

    public static final Supplier<RecipeType<CastingRecipe>> CASTING_TYPE =
            RECIPE_TYPES.register("casting",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Arsenal.MOD_ID, "casting")));

    public static final Supplier<RecipeSerializer<CastingRecipe>> CASTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("casting", CastingRecipe.Serializer::new);
}

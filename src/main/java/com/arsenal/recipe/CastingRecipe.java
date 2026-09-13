package com.arsenal.recipe;

import com.arsenal.registry.ArsenalRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * A recipe for the Munitions Bench: a metal ingredient plus a mold ingredient are melted and cast
 * into the result over {@code cookTime} ticks. The mold is a selector, not a reagent — the bench
 * consumes the metal and the result stays; the mold is left in place (handled by the block entity).
 */
public class CastingRecipe implements Recipe<CastingRecipeInput> {
    private final Ingredient metal;
    private final Ingredient mold;
    private final ItemStack result;
    private final int cookTime;

    public CastingRecipe(Ingredient metal, Ingredient mold, ItemStack result, int cookTime) {
        this.metal = metal;
        this.mold = mold;
        this.result = result;
        this.cookTime = cookTime;
    }

    public Ingredient metal() {
        return metal;
    }

    public Ingredient mold() {
        return mold;
    }

    public int cookTime() {
        return cookTime;
    }

    @Override
    public boolean matches(CastingRecipeInput input, Level level) {
        return metal.test(input.metal()) && mold.test(input.mold());
    }

    @Override
    public ItemStack assemble(CastingRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ArsenalRecipes.CASTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ArsenalRecipes.CASTING_TYPE.get();
    }

    /** Codec + network serialization for casting recipes. */
    public static final class Serializer implements RecipeSerializer<CastingRecipe> {
        public static final MapCodec<CastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("metal").forGetter(r -> r.metal),
                Ingredient.CODEC_NONEMPTY.fieldOf("mold").forGetter(r -> r.mold),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                com.mojang.serialization.Codec.INT.optionalFieldOf("cookTime", 200).forGetter(r -> r.cookTime)
        ).apply(instance, CastingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.metal,
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.mold,
                ItemStack.STREAM_CODEC, r -> r.result,
                ByteBufCodecs.VAR_INT, r -> r.cookTime,
                CastingRecipe::new);

        @Override
        public MapCodec<CastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

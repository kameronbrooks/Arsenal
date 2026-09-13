package com.arsenal.registry;

import com.arsenal.Arsenal;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Custom {@link DataComponentType}s stored on item stacks.
 *
 * <p>{@link #LOADED_ROUNDS} records how many rounds are currently chambered in a gun. Each gun is a
 * single stack (max size 1), so a plain integer is all the ammo state a gun needs.
 */
public final class ArsenalDataComponents {
    private ArsenalDataComponents() {}

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, Arsenal.MOD_ID);

    /** Rounds currently loaded in a gun's magazine. */
    public static final Supplier<DataComponentType<Integer>> LOADED_ROUNDS =
            DATA_COMPONENTS.register("loaded_rounds", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .cacheEncoding()
                    .build());
}

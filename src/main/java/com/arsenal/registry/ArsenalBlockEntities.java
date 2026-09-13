package com.arsenal.registry;

import com.arsenal.Arsenal;
import com.arsenal.block.entity.MunitionsBenchBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Block-entity type registration. */
public final class ArsenalBlockEntities {
    private ArsenalBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Arsenal.MOD_ID);

    public static final Supplier<BlockEntityType<MunitionsBenchBlockEntity>> MUNITIONS_BENCH =
            BLOCK_ENTITIES.register("munitions_bench", () -> BlockEntityType.Builder
                    .of(MunitionsBenchBlockEntity::new, ArsenalBlocks.MUNITIONS_BENCH.get())
                    .build(null));
}

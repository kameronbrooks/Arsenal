package com.arsenal.registry;

import com.arsenal.Arsenal;
import com.arsenal.block.MunitionsBenchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Blocks added by the mod. Currently just the Munitions Bench machine. */
public final class ArsenalBlocks {
    private ArsenalBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Arsenal.MOD_ID);

    public static final DeferredBlock<MunitionsBenchBlock> MUNITIONS_BENCH = BLOCKS.register("munitions_bench",
            () -> new MunitionsBenchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .lightLevel(state -> state.getValue(MunitionsBenchBlock.LIT) ? 13 : 0)));
}

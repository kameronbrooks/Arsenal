package com.arsenal;

import com.arsenal.registry.ArsenalBlockEntities;
import com.arsenal.registry.ArsenalBlocks;
import com.arsenal.registry.ArsenalCreativeTabs;
import com.arsenal.registry.ArsenalDataComponents;
import com.arsenal.registry.ArsenalItems;
import com.arsenal.registry.ArsenalMenus;
import com.arsenal.registry.ArsenalRecipes;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

/**
 * Arsenal — early-1900s firearms for Minecraft.
 *
 * <p>Adds four guns (revolver, bolt-action rifle, pump shotgun, submachine gun), each with its own
 * caliber, magazine capacity, and reload. Guns are hitscan: a shot instantly traces from the muzzle
 * and damages the first entity or block it meets. Ammunition is manufactured in two stages —
 * casings, bullets, and shot are cast from metal at the {@code Munitions Bench} (a custom
 * furnace-like machine), then assembled into live cartridges at the crafting table.
 *
 * <p>This class only wires registration onto the mod event bus; the actual content lives in the
 * {@code com.arsenal.registry} classes.
 */
@Mod(Arsenal.MOD_ID)
public final class Arsenal {
    public static final String MOD_ID = "arsenal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Arsenal(IEventBus modBus, ModContainer container) {
        ArsenalDataComponents.DATA_COMPONENTS.register(modBus);
        ArsenalBlocks.BLOCKS.register(modBus);
        ArsenalItems.ITEMS.register(modBus);
        ArsenalBlockEntities.BLOCK_ENTITIES.register(modBus);
        ArsenalMenus.MENUS.register(modBus);
        ArsenalRecipes.RECIPE_TYPES.register(modBus);
        ArsenalRecipes.RECIPE_SERIALIZERS.register(modBus);
        ArsenalCreativeTabs.TABS.register(modBus);

        modBus.addListener(this::registerCapabilities);

        LOGGER.info("Arsenal initialising — loading the armoury.");
    }

    /** Expose the Munitions Bench inventory so hoppers and other automation can feed and empty it. */
    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ArsenalBlockEntities.MUNITIONS_BENCH.get(),
                (be, side) -> be.getItems());
    }
}

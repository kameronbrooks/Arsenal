package com.arsenal.registry;

import com.arsenal.Arsenal;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Creative-mode tabs. A dedicated "Arsenal" tab holds everything in build order (bench → molds →
 * components → cartridges → guns), and the items are also injected into the matching vanilla tabs.
 */
@EventBusSubscriber(modid = Arsenal.MOD_ID)
public final class ArsenalCreativeTabs {
    private ArsenalCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Arsenal.MOD_ID);

    public static final Supplier<CreativeModeTab> ARSENAL = TABS.register("arsenal", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.arsenal"))
            .icon(() -> new ItemStack(ArsenalItems.REVOLVER.get()))
            .displayItems((parameters, output) -> {
                output.accept(ArsenalItems.MUNITIONS_BENCH.get());
                // Molds
                output.accept(ArsenalItems.CASING_MOLD.get());
                output.accept(ArsenalItems.BULLET_MOLD.get());
                output.accept(ArsenalItems.SHOT_MOLD.get());
                // Cast components
                output.accept(ArsenalItems.BRASS_CASING.get());
                output.accept(ArsenalItems.SHOTGUN_HULL.get());
                output.accept(ArsenalItems.BULLET.get());
                output.accept(ArsenalItems.LEAD_SHOT.get());
                // Cartridges
                output.accept(ArsenalItems.CARTRIDGE_38.get());
                output.accept(ArsenalItems.CARTRIDGE_45.get());
                output.accept(ArsenalItems.CARTRIDGE_3006.get());
                output.accept(ArsenalItems.SHELL_12GA.get());
                // Guns
                output.accept(ArsenalItems.REVOLVER.get());
                output.accept(ArsenalItems.SUBMACHINE_GUN.get());
                output.accept(ArsenalItems.BOLT_ACTION_RIFLE.get());
                output.accept(ArsenalItems.PUMP_SHOTGUN.get());
            })
            .build());

    @SubscribeEvent
    static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tab = event.getTabKey();
        if (tab == CreativeModeTabs.COMBAT) {
            event.accept(ArsenalItems.REVOLVER.get());
            event.accept(ArsenalItems.SUBMACHINE_GUN.get());
            event.accept(ArsenalItems.BOLT_ACTION_RIFLE.get());
            event.accept(ArsenalItems.PUMP_SHOTGUN.get());
            event.accept(ArsenalItems.CARTRIDGE_38.get());
            event.accept(ArsenalItems.CARTRIDGE_45.get());
            event.accept(ArsenalItems.CARTRIDGE_3006.get());
            event.accept(ArsenalItems.SHELL_12GA.get());
        } else if (tab == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ArsenalItems.MUNITIONS_BENCH.get());
        } else if (tab == CreativeModeTabs.INGREDIENTS) {
            event.accept(ArsenalItems.CASING_MOLD.get());
            event.accept(ArsenalItems.BULLET_MOLD.get());
            event.accept(ArsenalItems.SHOT_MOLD.get());
            event.accept(ArsenalItems.BRASS_CASING.get());
            event.accept(ArsenalItems.SHOTGUN_HULL.get());
            event.accept(ArsenalItems.BULLET.get());
            event.accept(ArsenalItems.LEAD_SHOT.get());
        }
    }
}

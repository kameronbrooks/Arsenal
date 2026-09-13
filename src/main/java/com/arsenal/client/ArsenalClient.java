package com.arsenal.client;

import com.arsenal.Arsenal;
import com.arsenal.registry.ArsenalMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only setup: binds the Munitions Bench menu to its screen. */
@EventBusSubscriber(modid = Arsenal.MOD_ID, value = Dist.CLIENT)
public final class ArsenalClient {
    private ArsenalClient() {}

    @SubscribeEvent
    static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ArsenalMenus.MUNITIONS_BENCH.get(), MunitionsBenchScreen::new);
    }
}

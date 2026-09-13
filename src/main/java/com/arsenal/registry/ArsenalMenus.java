package com.arsenal.registry;

import com.arsenal.Arsenal;
import com.arsenal.menu.MunitionsBenchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Container menu registration. */
public final class ArsenalMenus {
    private ArsenalMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Arsenal.MOD_ID);

    public static final Supplier<MenuType<MunitionsBenchMenu>> MUNITIONS_BENCH =
            MENUS.register("munitions_bench",
                    () -> IMenuTypeExtension.create(MunitionsBenchMenu::new));
}

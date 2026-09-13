package com.arsenal.registry;

import com.arsenal.Arsenal;
import com.arsenal.item.GunItem;
import com.arsenal.item.GunSettings;
import com.arsenal.item.MoldItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every item Arsenal adds, and the tuning for each gun.
 *
 * <p>Declaration order matters: the ammo items are declared before the {@link GunSettings} that
 * reference them, and those settings before the guns that use them.
 */
public final class ArsenalItems {
    private ArsenalItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Arsenal.MOD_ID);

    // ---------------------------------------------------------------- components
    // Cast from metal at the Munitions Bench, then assembled into cartridges.

    /** Brass casing — the body of pistol, SMG, and rifle cartridges (cast from copper). */
    public static final DeferredItem<Item> BRASS_CASING = ITEMS.registerSimpleItem("brass_casing");
    /** Shotgun hull — the body of a shell (cast from copper). */
    public static final DeferredItem<Item> SHOTGUN_HULL = ITEMS.registerSimpleItem("shotgun_hull");
    /** Bullet — the projectile for pistol, SMG, and rifle rounds (cast from iron). */
    public static final DeferredItem<Item> BULLET = ITEMS.registerSimpleItem("bullet");
    /** Lead shot — the pellets packed into a shotgun shell (cast from iron). */
    public static final DeferredItem<Item> LEAD_SHOT = ITEMS.registerSimpleItem("lead_shot");

    // --------------------------------------------------------------------- molds
    // Reusable templates that tell the Munitions Bench what to cast.

    public static final DeferredItem<MoldItem> CASING_MOLD = ITEMS.registerItem("casing_mold", MoldItem::new);
    public static final DeferredItem<MoldItem> BULLET_MOLD = ITEMS.registerItem("bullet_mold", MoldItem::new);
    public static final DeferredItem<MoldItem> SHOT_MOLD = ITEMS.registerItem("shot_mold", MoldItem::new);

    // ------------------------------------------------------------------ cartridges
    // Assembled at the crafting table from a casing/hull + projectile + gunpowder.

    /** .38 Special — the revolver's round. */
    public static final DeferredItem<Item> CARTRIDGE_38 = ITEMS.registerSimpleItem("cartridge_38");
    /** .45 ACP — the submachine gun's round. */
    public static final DeferredItem<Item> CARTRIDGE_45 = ITEMS.registerSimpleItem("cartridge_45");
    /** .30-06 — the bolt-action rifle's round. */
    public static final DeferredItem<Item> CARTRIDGE_3006 = ITEMS.registerSimpleItem("cartridge_3006");
    /** 12 gauge — the pump shotgun's shell. */
    public static final DeferredItem<Item> SHELL_12GA = ITEMS.registerSimpleItem("shell_12ga");

    // -------------------------------------------------------------- gun definitions

    private static final GunSettings REVOLVER_SETTINGS = GunSettings.builder(CARTRIDGE_38)
            .capacity(6).damage(6.0F).range(32.0D).pellets(1).spread(1.2F)
            .fireDelay(7).reloadTime(36).recoil(1.2F)
            .fireSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.4F)
            .reloadSound(SoundEvents.DISPENSER_DISPENSE)
            .build();

    private static final GunSettings RIFLE_SETTINGS = GunSettings.builder(CARTRIDGE_3006)
            .capacity(5).damage(15.0F).range(64.0D).pellets(1).spread(0.3F)
            .fireDelay(20).reloadTime(60).recoil(3.5F)
            .fireSound(SoundEvents.FIREWORK_ROCKET_BLAST, 0.7F)
            .reloadSound(SoundEvents.DISPENSER_DISPENSE)
            .build();

    private static final GunSettings SHOTGUN_SETTINGS = GunSettings.builder(SHELL_12GA)
            .capacity(5).damage(3.0F).range(18.0D).pellets(8).spread(6.0F)
            .fireDelay(16).reloadTime(55).recoil(3.5F)
            .fireSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F)
            .reloadSound(SoundEvents.DISPENSER_DISPENSE)
            .build();

    private static final GunSettings SMG_SETTINGS = GunSettings.builder(CARTRIDGE_45)
            .capacity(20).damage(4.0F).range(30.0D).pellets(1).spread(3.0F)
            .fireDelay(3).reloadTime(55).recoil(0.6F).automatic(true)
            .fireSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.6F)
            .reloadSound(SoundEvents.DISPENSER_DISPENSE)
            .build();

    // ---------------------------------------------------------------------- guns

    public static final DeferredItem<GunItem> REVOLVER =
            ITEMS.registerItem("revolver", props -> new GunItem(props, REVOLVER_SETTINGS));
    public static final DeferredItem<GunItem> BOLT_ACTION_RIFLE =
            ITEMS.registerItem("bolt_action_rifle", props -> new GunItem(props, RIFLE_SETTINGS));
    public static final DeferredItem<GunItem> PUMP_SHOTGUN =
            ITEMS.registerItem("pump_shotgun", props -> new GunItem(props, SHOTGUN_SETTINGS));
    public static final DeferredItem<GunItem> SUBMACHINE_GUN =
            ITEMS.registerItem("submachine_gun", props -> new GunItem(props, SMG_SETTINGS));

    // ------------------------------------------------------------------ block item

    public static final DeferredItem<BlockItem> MUNITIONS_BENCH =
            ITEMS.registerSimpleBlockItem(ArsenalBlocks.MUNITIONS_BENCH);
}

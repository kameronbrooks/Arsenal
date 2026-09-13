package com.arsenal.registry;

import com.arsenal.Arsenal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code arsenal:bullet} damage type. The {@link DamageType} itself is defined by a data file
 * ({@code data/arsenal/damage_type/bullet.json}); this class just holds the {@link ResourceKey} and
 * a helper to build a {@link DamageSource} attributed to the shooter.
 */
public final class ArsenalDamageTypes {
    private ArsenalDamageTypes() {}

    public static final ResourceKey<DamageType> BULLET = ResourceKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Arsenal.MOD_ID, "bullet"));

    /** Build a bullet damage source caused by {@code shooter}. */
    public static DamageSource bullet(Level level, @Nullable Entity shooter) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BULLET), shooter);
    }
}

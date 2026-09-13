package com.arsenal.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * Immutable configuration for a single {@link GunItem}. Bundling every tunable value into one record
 * keeps {@code GunItem} generic — a new gun is just a new set of numbers, not a new class.
 *
 * @param ammo         the cartridge item this gun chambers and reloads from
 * @param capacity     magazine size (rounds held when fully loaded)
 * @param damage       damage dealt per bullet — per pellet for multi-pellet guns like the shotgun
 * @param range        hitscan reach in blocks
 * @param pellets      bullets fired per trigger pull (1 for rifled guns, several for the shotgun)
 * @param spreadDegrees random cone half-angle applied to each pellet, in degrees (0 = pinpoint)
 * @param fireDelayTicks cooldown between shots, in ticks (caps the rate of fire)
 * @param reloadTicks  time the gun is unusable while reloading, in ticks
 * @param recoil       upward camera kick applied to the shooter, in degrees
 * @param automatic    if true the gun keeps firing while the trigger is held; if false it fires once per pull
 * @param fireSound    sound played when firing
 * @param firePitch    pitch of the fire sound (lets the same sound read as different calibers)
 * @param reloadSound  sound played when reloading
 */
public record GunSettings(
        Supplier<? extends Item> ammo,
        int capacity,
        float damage,
        double range,
        int pellets,
        float spreadDegrees,
        int fireDelayTicks,
        int reloadTicks,
        float recoil,
        boolean automatic,
        SoundEvent fireSound,
        float firePitch,
        SoundEvent reloadSound
) {
    /** Fluent builder so gun definitions read as a labelled block rather than a long argument list. */
    public static Builder builder(Supplier<? extends Item> ammo) {
        return new Builder(ammo);
    }

    public static final class Builder {
        private final Supplier<? extends Item> ammo;
        private int capacity = 1;
        private float damage = 4.0F;
        private double range = 40.0D;
        private int pellets = 1;
        private float spreadDegrees = 0.0F;
        private int fireDelayTicks = 10;
        private int reloadTicks = 30;
        private float recoil = 1.0F;
        private boolean automatic = false;
        private SoundEvent fireSound;
        private float firePitch = 1.0F;
        private SoundEvent reloadSound;

        private Builder(Supplier<? extends Item> ammo) {
            this.ammo = ammo;
        }

        public Builder capacity(int v) { this.capacity = v; return this; }
        public Builder damage(float v) { this.damage = v; return this; }
        public Builder range(double v) { this.range = v; return this; }
        public Builder pellets(int v) { this.pellets = v; return this; }
        public Builder spread(float degrees) { this.spreadDegrees = degrees; return this; }
        public Builder fireDelay(int ticks) { this.fireDelayTicks = ticks; return this; }
        public Builder reloadTime(int ticks) { this.reloadTicks = ticks; return this; }
        public Builder recoil(float degrees) { this.recoil = degrees; return this; }
        public Builder automatic(boolean value) { this.automatic = value; return this; }
        public Builder fireSound(SoundEvent sound, float pitch) { this.fireSound = sound; this.firePitch = pitch; return this; }
        public Builder reloadSound(SoundEvent sound) { this.reloadSound = sound; return this; }

        public GunSettings build() {
            return new GunSettings(ammo, capacity, damage, range, pellets, spreadDegrees,
                    fireDelayTicks, reloadTicks, recoil, automatic, fireSound, firePitch, reloadSound);
        }
    }
}

package com.arsenal.item;

import com.arsenal.registry.ArsenalDamageTypes;
import com.arsenal.registry.ArsenalDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

import java.util.List;

/**
 * A hitscan firearm. All four Arsenal guns are instances of this one class, differing only by their
 * {@link GunSettings}.
 *
 * <p><b>Controls.</b> Right-click raises the gun into the crossbow-style aiming pose and fires; if
 * the magazine is empty it reloads instead. Sneak + right-click always reloads (tops the magazine up
 * early). Automatic guns keep firing while the trigger is held; the rest fire once per pull. An item
 * cooldown enforces the fire rate and locks the gun mid-reload.
 *
 * <p><b>Animation.</b> The gun reports {@link UseAnim#CROSSBOW}, so aiming shows the two-handed
 * crossbow pose instead of the arm-swing "hitting" animation. Shots are dealt from
 * {@link #onUseTick} while that pose plays.
 *
 * <p><b>Firing.</b> Shots are instantaneous ray traces from the shooter's eyes: the first entity or
 * block along the ray is hit. Guns that fire multiple pellets (the shotgun) trace one ray per pellet
 * with random spread but consume a single shell.
 *
 * <p>Loaded-round count lives in the {@link ArsenalDataComponents#LOADED_ROUNDS} data component and
 * is shown both as the item's durability-style bar and in the tooltip.
 */
public class GunItem extends Item {
    private final GunSettings settings;

    public GunItem(Properties properties, GunSettings settings) {
        super(properties.stacksTo(1));
        this.settings = settings;
    }

    public GunSettings settings() {
        return settings;
    }

    public int getLoaded(ItemStack stack) {
        return stack.getOrDefault(ArsenalDataComponents.LOADED_ROUNDS.get(), 0);
    }

    private void setLoaded(ItemStack stack, int rounds) {
        stack.set(ArsenalDataComponents.LOADED_ROUNDS.get(), rounds);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // Long enough that "using" (aiming) lasts as long as the trigger is held.
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        int loaded = getLoaded(stack);
        if (player.isShiftKeyDown() || loaded <= 0) {
            return reload(level, player, hand, stack, loaded);
        }

        // Raise the gun into the crossbow aim pose; the actual shot is fired from onUseTick.
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    // ------------------------------------------------------------------ firing

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (getLoaded(stack) <= 0) {
            player.stopUsingItem();
            return;
        }

        int elapsed = getUseDuration(stack, entity) - remainingUseDuration; // 1 on the first tick
        boolean shouldFire = settings.automatic()
                ? (elapsed - 1) % Math.max(1, settings.fireDelayTicks()) == 0
                : elapsed == 1;

        if (shouldFire) {
            fireRound(level, player, stack);
        }
    }

    private void fireRound(Level level, Player player, ItemStack stack) {
        int loaded = getLoaded(stack);
        if (loaded <= 0) {
            return;
        }
        if (level instanceof ServerLevel server) {
            for (int i = 0; i < settings.pellets(); i++) {
                traceShot(server, player);
            }
            setLoaded(stack, loaded - 1);
            server.playSound(null, player.getX(), player.getY(), player.getZ(),
                    settings.fireSound(), SoundSource.PLAYERS, 1.2F, settings.firePitch() + (server.random.nextFloat() - 0.5F) * 0.1F);
            spawnMuzzleFlash(server, player);
        } else {
            // Kick the local camera up for immediate feedback; the server copy stays authoritative.
            player.setXRot(player.getXRot() - settings.recoil());
        }
        player.getCooldowns().addCooldown(this, settings.fireDelayTicks());
    }

    /** Trace a single pellet and apply its effect to whatever it hits first. */
    private void traceShot(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        float pitch = player.getXRot() + spread(level) ;
        float yaw = player.getYRot() + spread(level);
        Vec3 look = Vec3.directionFromRotation(pitch, yaw);
        Vec3 end = eye.add(look.scale(settings.range()));

        // Stop the ray at the first solid block.
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 limit = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : end;

        // Look for an entity between the muzzle and that block (or the max range).
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, eye, limit,
                new AABB(eye, limit).inflate(1.0D),
                e -> e != player && e.isPickable() && !e.isSpectator());

        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            target.hurt(ArsenalDamageTypes.bullet(level, player), settings.damage());
            if (target instanceof LivingEntity living) {
                // Per-pellet knockback is lighter for multi-pellet guns so a point-blank shotgun
                // (all 8 pellets landing now that bullets bypass the hurt cooldown) shoves rather
                // than launches the target.
                float knockback = settings.pellets() > 1 ? 0.08F : 0.2F;
                living.knockback(knockback, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
            Vec3 hit = entityHit.getLocation();
            level.sendParticles(ParticleTypes.CRIT, hit.x, hit.y, hit.z, 6, 0.05, 0.05, 0.05, 0.05);
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            Vec3 hit = blockHit.getLocation();
            BlockState state = level.getBlockState(blockHit.getBlockPos());
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), hit.x, hit.y, hit.z, 6, 0.1, 0.1, 0.1, 0.05);
            level.sendParticles(ParticleTypes.SMOKE, hit.x, hit.y, hit.z, 2, 0.0, 0.0, 0.0, 0.01);
        }
    }

    /** One random spread offset in degrees, within the gun's cone. */
    private float spread(ServerLevel level) {
        if (settings.spreadDegrees() <= 0.0F) {
            return 0.0F;
        }
        return (level.random.nextFloat() - level.random.nextFloat()) * settings.spreadDegrees();
    }

    private void spawnMuzzleFlash(ServerLevel level, Player player) {
        Vec3 muzzle = player.getEyePosition().add(player.getViewVector(1.0F).scale(1.1D)).subtract(0, 0.1, 0);
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 3, 0.03, 0.03, 0.03, 0.01);
        level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    // ----------------------------------------------------------------- reloading

    private InteractionResultHolder<ItemStack> reload(Level level, Player player, InteractionHand hand, ItemStack stack, int loaded) {
        int need = settings.capacity() - loaded;
        if (need <= 0) {
            return InteractionResultHolder.pass(stack);
        }

        boolean creative = player.getAbilities().instabuild;
        int available = creative ? need : countAmmo(player);
        int toLoad = Math.min(need, available);

        if (toLoad <= 0) {
            // Nothing to chamber — a dry click for feedback.
            if (level.isClientSide) {
                level.playSound(player, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.6F, 1.6F);
            }
            player.getCooldowns().addCooldown(this, 6);
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            if (!creative) {
                consumeAmmo(player, toLoad);
            }
            setLoaded(stack, loaded + toLoad);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    settings.reloadSound(), SoundSource.PLAYERS, 0.9F, 1.0F);
        }

        player.getCooldowns().addCooldown(this, settings.reloadTicks());
        return InteractionResultHolder.consume(stack);
    }

    private int countAmmo(Player player) {
        Item ammo = settings.ammo().get();
        int count = 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.is(ammo)) {
                count += s.getCount();
            }
        }
        return count;
    }

    private void consumeAmmo(Player player, int amount) {
        Item ammo = settings.ammo().get();
        int remaining = amount;
        for (ItemStack s : player.getInventory().items) {
            if (remaining <= 0) {
                break;
            }
            if (s.is(ammo)) {
                int take = Math.min(remaining, s.getCount());
                s.shrink(take);
                remaining -= take;
            }
        }
    }

    // ------------------------------------------------------------------- display

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getLoaded(stack) / (float) settings.capacity());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFB000; // amber, evocative of brass
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arsenal.rounds", getLoaded(stack), settings.capacity())
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.arsenal.ammo", settings.ammo().get().getDescription())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.arsenal.reload_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}

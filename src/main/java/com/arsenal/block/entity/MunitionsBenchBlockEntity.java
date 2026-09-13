package com.arsenal.block.entity;

import com.arsenal.block.MunitionsBenchBlock;
import com.arsenal.item.MoldItem;
import com.arsenal.menu.MunitionsBenchMenu;
import com.arsenal.recipe.CastingRecipe;
import com.arsenal.recipe.CastingRecipeInput;
import com.arsenal.registry.ArsenalBlockEntities;
import com.arsenal.registry.ArsenalRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * The Munitions Bench machine. Behaves like a themed furnace: given a metal in the input slot, a
 * mold in the mold slot, and fuel, it burns fuel to melt the metal and cast it into the mold's
 * product over time. The mold is a selector and is never consumed; only the metal and fuel are.
 */
public class MunitionsBenchBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler items = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case MunitionsBenchMenu.SLOT_MOLD -> stack.getItem() instanceof MoldItem;
                case MunitionsBenchMenu.SLOT_FUEL -> stack.getBurnTime(null) > 0;
                case MunitionsBenchMenu.SLOT_OUTPUT -> false;
                default -> true;
            };
        }
    };

    private int litTime;
    private int litDuration;
    private int cookProgress;
    private int cookTotalTime;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> litTime;
                case 1 -> litDuration;
                case 2 -> cookProgress;
                case 3 -> cookTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> litTime = value;
                case 1 -> litDuration = value;
                case 2 -> cookProgress = value;
                case 3 -> cookTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    private final RecipeManager.CachedCheck<CastingRecipeInput, CastingRecipe> quickCheck =
            RecipeManager.createCheck(ArsenalRecipes.CASTING_TYPE.get());

    public MunitionsBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ArsenalBlockEntities.MUNITIONS_BENCH.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    private boolean isLit() {
        return litTime > 0;
    }

    // -------------------------------------------------------------------- ticking

    public static void serverTick(Level level, BlockPos pos, BlockState state, MunitionsBenchBlockEntity be) {
        boolean wasLit = be.isLit();
        boolean changed = false;

        if (be.isLit()) {
            be.litTime--;
        }

        ItemStack input = be.items.getStackInSlot(MunitionsBenchMenu.SLOT_INPUT);
        ItemStack mold = be.items.getStackInSlot(MunitionsBenchMenu.SLOT_MOLD);
        ItemStack fuel = be.items.getStackInSlot(MunitionsBenchMenu.SLOT_FUEL);

        CastingRecipe recipe = null;
        if (!input.isEmpty() && !mold.isEmpty()) {
            recipe = be.quickCheck.getRecipeFor(new CastingRecipeInput(input, mold), level)
                    .map(RecipeHolder::value)
                    .orElse(null);
        }

        boolean canProcess = recipe != null && be.canOutput(recipe, level);
        if (canProcess) {
            be.cookTotalTime = recipe.cookTime();
        }

        // Light the burner if we have work to do and fuel to spend.
        if (!be.isLit() && canProcess && !fuel.isEmpty()) {
            int burn = fuel.getBurnTime(null);
            if (burn > 0) {
                be.litTime = burn;
                be.litDuration = burn;
                fuel.shrink(1);
                changed = true;
            }
        }

        // Advance the cast while lit; otherwise cool down.
        if (be.isLit() && canProcess) {
            be.cookProgress++;
            if (be.cookProgress >= be.cookTotalTime) {
                be.cookProgress = 0;
                be.craft(recipe, level);
                changed = true;
            }
        } else if (be.cookProgress > 0) {
            be.cookProgress = Math.max(0, be.cookProgress - 2);
        }

        if (wasLit != be.isLit()) {
            changed = true;
            state = state.setValue(MunitionsBenchBlock.LIT, be.isLit());
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    private boolean canOutput(CastingRecipe recipe, Level level) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }
        ItemStack out = items.getStackInSlot(MunitionsBenchMenu.SLOT_OUTPUT);
        if (out.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(out, result)) {
            return false;
        }
        int total = out.getCount() + result.getCount();
        return total <= out.getMaxStackSize() && total <= items.getSlotLimit(MunitionsBenchMenu.SLOT_OUTPUT);
    }

    private void craft(CastingRecipe recipe, Level level) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack out = items.getStackInSlot(MunitionsBenchMenu.SLOT_OUTPUT);
        if (out.isEmpty()) {
            items.setStackInSlot(MunitionsBenchMenu.SLOT_OUTPUT, result.copy());
        } else {
            out.grow(result.getCount());
        }
        // Consume one unit of metal; the mold stays in place.
        items.getStackInSlot(MunitionsBenchMenu.SLOT_INPUT).shrink(1);
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items.getStackInSlot(i));
        }
    }

    // --------------------------------------------------------------- menu provider

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.arsenal.munitions_bench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MunitionsBenchMenu(id, inventory, items, dataAccess,
                ContainerLevelAccess.create(this.level, this.worldPosition));
    }

    // ---------------------------------------------------------------------- saving

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
        tag.putInt("CookProgress", cookProgress);
        tag.putInt("CookTotalTime", cookTotalTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Inventory"));
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
        cookProgress = tag.getInt("CookProgress");
        cookTotalTime = tag.getInt("CookTotalTime");
    }
}

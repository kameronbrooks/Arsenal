package com.arsenal.menu;

import com.arsenal.block.entity.MunitionsBenchBlockEntity;
import com.arsenal.item.MoldItem;
import com.arsenal.registry.ArsenalBlocks;
import com.arsenal.registry.ArsenalMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Container menu for the {@link MunitionsBenchBlockEntity}. Four machine slots — metal input, mold,
 * fuel, and output — plus the player's inventory.
 */
public class MunitionsBenchMenu extends AbstractContainerMenu {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_MOLD = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_OUTPUT = 3;
    private static final int MACHINE_SLOTS = 4;
    private static final int PLAYER_INV_START = MACHINE_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Client-side constructor, invoked by the menu type from the opening packet. */
    public MunitionsBenchMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, handlerFromClient(playerInv, buf.readBlockPos()), new SimpleContainerData(4), ContainerLevelAccess.NULL);
    }

    /** Server-side constructor, invoked by the block entity. */
    public MunitionsBenchMenu(int id, Inventory playerInv, IItemHandler handler, ContainerData data, ContainerLevelAccess access) {
        super(ArsenalMenus.MUNITIONS_BENCH.get(), id);
        this.data = data;
        this.access = access;

        this.addSlot(new SlotItemHandler(handler, SLOT_INPUT, 56, 17));
        this.addSlot(new SlotItemHandler(handler, SLOT_MOLD, 26, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof MoldItem;
            }
        });
        this.addSlot(new SlotItemHandler(handler, SLOT_FUEL, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getBurnTime(null) > 0;
            }
        });
        this.addSlot(new SlotItemHandler(handler, SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    private static IItemHandler handlerFromClient(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof MunitionsBenchBlockEntity bench) {
            return bench.getItems();
        }
        return new ItemStackHandler(MACHINE_SLOTS);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ArsenalBlocks.MUNITIONS_BENCH.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return moved;
        }
        ItemStack stack = slot.getItem();
        moved = stack.copy();

        if (index < MACHINE_SLOTS) {
            // Machine -> player inventory.
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, moved);
        } else {
            // Player inventory -> the appropriate machine slot.
            if (stack.getItem() instanceof MoldItem) {
                if (!this.moveItemStackTo(stack, SLOT_MOLD, SLOT_MOLD + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getBurnTime(null) > 0) {
                if (!this.moveItemStackTo(stack, SLOT_FUEL, SLOT_FUEL + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, SLOT_INPUT, SLOT_INPUT + 1, false)) {
                // Not fuel or mold: try the metal input, else shuffle within the inventory.
                if (index < PLAYER_INV_START + 27) {
                    if (!this.moveItemStackTo(stack, PLAYER_INV_START + 27, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_START + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == moved.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return moved;
    }

    // Accessors used by the screen to draw progress.

    public boolean isLit() {
        return data.get(0) > 0;
    }

    /** Flame height in pixels, 0..14. */
    public int getLitHeight() {
        int duration = data.get(1);
        if (duration == 0) {
            duration = 200;
        }
        return data.get(0) * 14 / duration;
    }

    /** Progress-arrow width in pixels, 0..24. */
    public int getCookArrowWidth() {
        int total = data.get(3);
        int progress = data.get(2);
        return (total != 0 && progress != 0) ? progress * 24 / total : 0;
    }
}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Jamalam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.jamalam360.tool.belt.mixin;

import io.github.jamalam360.tool.belt.Ducks;
import io.github.jamalam360.tool.belt.ToolBeltClientInit;
import io.github.jamalam360.tool.belt.ToolBeltInit;
import io.github.jamalam360.tool.belt.item.ToolBeltItem;
import io.github.jamalam360.tool.belt.util.SimplerInventory;
import io.github.jamalam360.tool.belt.util.TrinketsUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Jamalam
 */

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements Ducks.LivingEntity {
    @Shadow
    protected abstract void detectEquipmentUpdates();

    @Inject(
            method = "sendEquipmentBreakStatus",
            at = @At("HEAD")
    )
    private void toolbelt$updateToolBeltNbt(EquipmentSlot slot, CallbackInfo ci) {
        if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
            if (TrinketsUtil.hasToolBelt(player)) {
                ItemStack toolBelt = TrinketsUtil.getToolBelt(player);
                SimplerInventory inv = ToolBeltItem.getInventory(toolBelt);

                if (player.world.isClient) {
                    inv.setStack(ToolBeltClientInit.toolBeltSelectedSlot, ItemStack.EMPTY);
                } else {
                    inv.setStack(ToolBeltInit.TOOL_BELT_SELECTED_SLOTS.getOrDefault(player, 0), ItemStack.EMPTY);
                }

                ToolBeltItem.update(toolBelt, inv);
            }
        }
    }

    @Inject(
            method = "getStackInHand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void toolbelt$useToolBeltStack(Hand hand, CallbackInfoReturnable<ItemStack> cir) {
        if (hand == Hand.MAIN_HAND && ((LivingEntity) (Object) this) instanceof PlayerEntity player) {
            ItemStack stack = ToolBeltItem.getSelectedToolBeltStack(player);

            if (stack != null) {
                cir.setReturnValue(stack);
            }
        }
    }

    @Inject(
            method = "getMainHandStack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void toolbelt$useToolBeltStack2(CallbackInfoReturnable<ItemStack> cir) {
        if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
            ItemStack stack = ToolBeltItem.getSelectedToolBeltStack(player);

            if (stack != null) {
                cir.setReturnValue(stack);
            }
        }
    }

    @Inject(
            method = "setStackInHand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void toolbelt$setStackInHandToolBelt(Hand hand, ItemStack stack, CallbackInfo ci) {
        if (hand == Hand.MAIN_HAND && ((LivingEntity) (Object) this) instanceof PlayerEntity player) {
            boolean selected = false;
            int selectedSlot = 0;

            if (player.world.isClient) {
                if (ToolBeltClientInit.hasSwappedToToolBelt) {
                    selected = true;
                    selectedSlot = ToolBeltClientInit.toolBeltSelectedSlot;
                }
            } else {
                if (ToolBeltInit.TOOL_BELT_SELECTED.getOrDefault(player, false)) {
                    selected = true;
                    selectedSlot = ToolBeltInit.TOOL_BELT_SELECTED_SLOTS.getOrDefault(player, 0);
                }
            }

            if (selected) {
                ItemStack toolBelt = TrinketsUtil.getToolBelt(player);
                SimplerInventory inv = ToolBeltItem.getInventory(toolBelt);
                inv.setStack(selectedSlot, stack);
                ToolBeltItem.update(toolBelt, inv);
                ci.cancel();
            }
        }
    }

    @Override
    public void updateEquipment() {
        this.detectEquipmentUpdates();
    }
}

/*
 * MIT License
 *
 * Copyright (c) NyliumMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder.mixin.datapacks;

import io.github.gunpowder.gunpowder.api.GunpowderMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin_Datapacks_Voodoo extends AbstractDecorationEntity {
    protected ItemFrameEntityMixin_Datapacks_Voodoo(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract void setHeldItemStack(ItemStack stack);

    @Inject(method = "setHeldItemStack(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    void makeInvisible(ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() == Items.POTION && !isInvisible()) {
            if (GunpowderMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getInvisibleItemFrames() && (PotionUtil.getPotion(stack) == Potions.INVISIBILITY || PotionUtil.getPotion(stack) == Potions.LONG_INVISIBILITY)) {
                setInvisible(true);
                setHeldItemStack(new ItemStack(Items.GLASS_BOTTLE));
                ci.cancel();
            }
        }
    }
}

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

import com.mojang.authlib.GameProfile;
import io.github.gunpowder.gunpowder.api.GunpowderMod;
import io.github.nyliummc.essentials.configs.DatapacksConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Arrays;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Datapacks_Voodoo extends PlayerEntity {
    public ServerPlayerEntityMixin_Datapacks_Voodoo(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Override
    public boolean isFireImmune() {
        if (GunpowderMod.getInstance().getRegistry().getConfig(DatapacksConfig.class).getVoodooBeard().getNetheriteFireImmune()) {
            int netheriteItems = 0;
            Item[] items = new Item[] {Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS};
            for (ItemStack it : getArmorItems()) {
                if (Arrays.stream(items).anyMatch((a) -> a == it.getItem())) {
                    netheriteItems += 1;
                }
            }

            if (netheriteItems == 4) {
                return false;
            }
        }
        return super.isFireImmune();
    }
}

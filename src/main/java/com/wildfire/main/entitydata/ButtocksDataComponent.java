/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.main.entitydata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.Configuration;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * <p>Data component-like class for storing player buttocks settings on armor equipped onto armor stands</p>
 *
 * <p>Note that while this is treated similarly to any other {@link DataComponentTypes data component} for performance reasons,
 * this is never written as its own component on item stacks, but instead uses the {@link DataComponentTypes#CUSTOM_DATA custom NBT data component}
 * (under the {@code WildfireGender} key) for compatibility with vanilla clients on servers.</p>
 */
public record ButtocksDataComponent(float buttocksSize, float cleavage, Vector3f offsets, boolean jacket, @Nullable NbtComponent component2) {

    private static final String KEY = "WildfireGender";
    private static final Codec<ButtocksDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WildfireHelper.boundedFloat(Configuration.BUTTOCKS_SIZE)
                    .optionalFieldOf("ButtocksSize", 0f)
                    .forGetter(ButtocksDataComponent::buttocksSize),
            WildfireHelper.boundedFloat(Configuration.BUTTOCKS_CLEAVAGE)
                    .optionalFieldOf("Cleavage", Configuration.BUTTOCKS_CLEAVAGE.getDefault())
                    .forGetter(ButtocksDataComponent::cleavage),
            Codec.BOOL
                    .optionalFieldOf("Jacket", true)
                    .forGetter(ButtocksDataComponent::jacket),
            WildfireHelper.boundedFloat(Configuration.BUTTOCKS_OFFSET_X)
                    .optionalFieldOf("XOffset", 0f)
                    .forGetter(component2 -> component2.offsets.x),
            WildfireHelper.boundedFloat(Configuration.BUTTOCKS_OFFSET_Y)
                    .optionalFieldOf("YOffset", 0f)
                    .forGetter(component2 -> component2.offsets.y),
            WildfireHelper.boundedFloat(Configuration.BUTTOCKS_OFFSET_Z)
                    .optionalFieldOf("ZOffset", 0f)
                    .forGetter(component2 -> component2.offsets.z)
        ).apply(instance, (buttocksSize, cleavage, jacket, x, y, z) -> new ButtocksDataComponent(buttocksSize, cleavage, new Vector3f(x, y, z), jacket, null))
    );
    private static final MapCodec<ButtocksDataComponent> MAP_CODEC = CODEC.fieldOf(KEY);

    public static @Nullable ButtocksDataComponent fromPlayer(@NotNull PlayerEntity player, @NotNull PlayerConfig config) {
        if(!config.getGender().canHaveButtocks() || !config.showButtocksInArmor()) {
            return null;
        }

        return new ButtocksDataComponent(config.getButtocksSize(), config.getButtocks().getCleavage(), config.getButtocks().getOffsets(),
                player.isPartVisible(PlayerModelPart.JACKET), null);
    }

    public static @Nullable ButtocksDataComponent fromComponent(@Nullable NbtComponent component2) {
        if(component2 == null) {
            return null;
        }

        DataResult<ButtocksDataComponent> result = component2.get(MAP_CODEC);
        if(result.isError()) {
            return null;
        }

        return result.getOrThrow().withComponent(component2);
    }

    public void write(RegistryWrapper.WrapperLookup lookup, ItemStack stack) {
        if(stack.isEmpty()) {
            throw new IllegalArgumentException("The provided ItemStack must not be empty");
        }

        RegistryOps<NbtElement> op = lookup.getOps(NbtOps.INSTANCE);
        DataResult<NbtComponent> result = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).with(op, MAP_CODEC, this);
        if(result.isSuccess()) {
            stack.set(DataComponentTypes.CUSTOM_DATA, result.getOrThrow());
        }
    }

    public static void removeFromStack(ItemStack stack) {
        if(stack.isEmpty()) return;
        NbtComponent component2 = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(component2 != null && component2.contains(KEY)) {
            NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.remove(KEY));
        }
    }

    private ButtocksDataComponent withComponent(NbtComponent component2) {
        return new ButtocksDataComponent(buttocksSize, cleavage, offsets, jacket, component2);
    }
}

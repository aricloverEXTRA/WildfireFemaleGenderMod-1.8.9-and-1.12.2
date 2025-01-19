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

package com.wildfire.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.api.impl.ButtocksArmorTexture;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the texture data for a given armor piece when covering an entity's buttocks
 */
public interface IButtocksArmorTexture {

    Vec2i DEFAULT_TEXTURE_SIZE = new Vec2i(64, 32);
    Vec2i DEFAULT_DIMENSIONS = new Vec2i(4, 5);
    Vec2i DEFAULT_LEFT_UV = new Vec2i(16, 17);
    Vec2i DEFAULT_RIGHT_UV = DEFAULT_LEFT_UV.add(DEFAULT_DIMENSIONS.x(), 0);

    Codec<IButtocksArmorTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec2i.CODEC
                    .optionalFieldOf("texture_size", DEFAULT_TEXTURE_SIZE)
                    .forGetter(IButtocksArmorTexture::textureSize),
            Vec2i.CODEC
                    .optionalFieldOf("left_uv", DEFAULT_LEFT_UV)
                    .forGetter(IButtocksArmorTexture::leftUv),
            Vec2i.CODEC
                    .optionalFieldOf("right_uv", new Vec2i(-1, -1))
                    .forGetter(IButtocksArmorTexture::rightUv),
            Vec2i.CODEC
                    .optionalFieldOf("dimensions", DEFAULT_DIMENSIONS)
                    .forGetter(IButtocksArmorTexture::dimensions)
    ).apply(instance, (size, leftUv, rightUv, dimensions) -> {
        var right = rightUv;
        if (right.x() == -1 && right.y() == -1) {
            right = leftUv.add(dimensions.x(), 0);
        }
        return new ButtocksArmorTexture(size, leftUv, right, dimensions);
    }));

    /**
     * The size of the armor sprite in pixels
     *
     * @implNote Defaults to {@code Vec2i(64, 32)}
     *
     * @return A {@link Vec2i} indicating how large the texture file is
     */
    default @NotNull Vec2i textureSize() {
        return DEFAULT_TEXTURE_SIZE;
    }

    /**
     * How large of an area from the sprite should be used for each buttock
     *
     * @apiNote The X value of this should be halved from the total hip size to account for each buttock side
     *          rendering independently of each other.
     *
     * @implNote Defaults to {@code Vec2i(4, 5)}
     *
     * @return A {@link Vec2i} indicating how large of an area should be grabbed from the texture sprite to display over
     *         the wearer's buttocks
     */
    default @NotNull Vec2i dimensions() {
        return DEFAULT_DIMENSIONS;
    }

    /**
     * Where the left buttock should grab the texture from on the sprite
     *
     * @implNote Defaults to {@code Vec2i(16, 17)}
     *
     * @return A {@link Vec2i} indicating the UV to use for the left buttock
     */
    default @NotNull Vec2i leftUv() {
        return DEFAULT_LEFT_UV;
    }

    /**
     * Where the right buttock should grab the texture from on the sprite
     *
     * @implNote Defaults to {@code Vec2i(20, 17)}
     *
     * @return A {@link Vec2i} indicating the UV to use for the right buttock
     */
    default @NotNull Vec2i rightUv() {
        return DEFAULT_RIGHT_UV;
    }
}

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
import com.wildfire.api.impl.BreastArmorTexture;
import com.wildfire.api.impl.ButtocksArmorTexture;
import com.wildfire.api.impl.GenderArmor;
import com.wildfire.main.WildfireHelper;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.NotNull;

/**
 * Implement this on a custom class for your chestplates or items that go in the chest slot to configure how it interacts with breast and buttocks rendering.
 */
public interface IGenderArmor {

    Codec<IGenderArmor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WildfireHelper.boundedFloat(0f, 1f)
                    .optionalFieldOf("resistance", 0.5f)
                    .forGetter(IGenderArmor::physicsResistance),
            WildfireHelper.boundedFloat(0f, 1f)
                    .optionalFieldOf("tightness", 0f)
                    .forGetter(IGenderArmor::tightness),
            Codec.BOOL
                    .optionalFieldOf("covers_breasts", true)
                    .forGetter(IGenderArmor::coversBreasts),
            Codec.BOOL
                    .optionalFieldOf("hide_breasts", false)
                    .forGetter(IGenderArmor::alwaysHidesBreasts),
            Codec.BOOL
                    .optionalFieldOf("covers_buttocks", true)
                    .forGetter(IGenderArmor::coversButtocks),
            Codec.BOOL
                    .optionalFieldOf("hide_buttocks", false)
                    .forGetter(IGenderArmor::alwaysHidesButtocks),
            WildfireHelper.TRISTATE
                    .optionalFieldOf("render_on_armor_stands", TriState.DEFAULT)
                    .forGetter(armor -> armor.armorStandsCopySettings() ? TriState.TRUE : TriState.FALSE),
            IBreastArmorTexture.CODEC
                    .optionalFieldOf("breast_texture", BreastArmorTexture.DEFAULT)
                    .forGetter(IGenderArmor::breastTexture),
            IButtocksArmorTexture.CODEC
                    .optionalFieldOf("buttocks_texture", ButtocksArmorTexture.DEFAULT)
                    .forGetter(IGenderArmor::buttocksTexture)
    ).apply(instance, (resistance, tightness, coversBreasts, hideBreasts, coversButtocks, hideButtocks, armorStands, breastTexture, buttocksTexture) -> {
        if (!coversBreasts && !coversButtocks) {
            return GenderArmor.EMPTY;
        }
        return new GenderArmor(resistance, tightness, coversBreasts, hideBreasts, coversButtocks, hideButtocks, armorStands.asBoolean(resistance == 1f), breastTexture, buttocksTexture);
    }));

    default boolean coversBreasts() {
        return true;
    }

    default boolean alwaysHidesBreasts() {
        return false;
    }

    default boolean coversButtocks() {
        return true;
    }

    default boolean alwaysHidesButtocks() {
        return false;
    }

    default float physicsResistance() {
        return 0.5f;
    }

    default float tightness() {
        return 0;
    }

    default boolean armorStandsCopySettings() {
        return !alwaysHidesBreasts() && coversBreasts() && physicsResistance() == 1f;
    }

    default @NotNull IBreastArmorTexture breastTexture() {
        return BreastArmorTexture.DEFAULT;
    }

    default @NotNull IButtocksArmorTexture buttocksTexture() {
        return ButtocksArmorTexture.DEFAULT;
    }
}

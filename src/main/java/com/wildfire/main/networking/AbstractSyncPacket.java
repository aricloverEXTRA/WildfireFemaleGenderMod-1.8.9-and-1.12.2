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

package com.wildfire.main.networking;

import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.Buttocks;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.Gender;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

import java.util.UUID;

abstract class AbstractSyncPacket {

    protected static <T extends AbstractSyncPacket> PacketCodec<ByteBuf, T> codec(SyncPacketConstructor<T> constructor) {
        return PacketCodec.tuple(
            Uuids.PACKET_CODEC, p -> p.uuid,
            Gender.CODEC, p -> p.gender,
            PacketCodecs.FLOAT, p -> p.bustSize,
            PacketCodecs.FLOAT, p -> p.buttocksSize,
            PacketCodecs.BOOLEAN, p -> p.hurtSounds,
            PacketCodecs.FLOAT, p -> p.voicePitch,
            BreastPhysics.CODEC, p -> p.physics,
            ButtocksPhysics.CODEC, p -> p.buttocksPhysics,
            Breasts.CODEC, p -> p.breasts,
            Buttocks.CODEC, p -> p.buttocks,
            constructor
        );
    }

    protected final UUID uuid;
    protected final Gender gender;
    protected final float bustSize;
    protected final float buttocksSize;
    protected final boolean hurtSounds;
    protected final float voicePitch;
    protected final BreastPhysics physics;
    protected final ButtocksPhysics buttocksPhysics;
    protected final Breasts breasts;
    protected final Buttocks buttocks;

    protected AbstractSyncPacket(UUID uuid, Gender gender, float bustSize, float buttocksSize, boolean hurtSounds, float voicePitch, BreastPhysics physics, ButtocksPhysics buttocksPhysics, Breasts breasts, Buttocks buttocks) {
        this.uuid = uuid;
        this.gender = gender;
        this.bustSize = bustSize;
        this.buttocksSize = buttocksSize;
        this.hurtSounds = hurtSounds;
        this.voicePitch = voicePitch;
        this.physics = physics;
        this.buttocksPhysics = buttocksPhysics;
        this.breasts = breasts;
        this.buttocks = buttocks;
    }

    protected AbstractSyncPacket(PlayerConfig plr) {
        this(plr.uuid, plr.getGender(), plr.getBustSize(), plr.getButtocksSize(), plr.hasHurtSounds(), plr.getVoicePitch(), new BreastPhysics(plr), new ButtocksPhysics(plr), plr.getBreasts(), plr.getButtocks());
    }

    protected void updatePlayerFromPacket(PlayerConfig plr) {
        plr.updateGender(gender);
        plr.updateBustSize(bustSize);
        plr.updateButtocksSize(buttocksSize);
        plr.updateHurtSounds(hurtSounds);
        plr.updateVoicePitch(voicePitch);
        physics.applyTo(plr);
        buttocksPhysics.applyTo(plr);
        plr.getBreasts().copyFrom(breasts);
        plr.getButtocks().copyFrom(buttocks);
    }

    protected record BreastPhysics(boolean physics, boolean showInArmor, float bounceMultiplier, float floppyMultiplier) {

        public static final PacketCodec<ByteBuf, BreastPhysics> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, BreastPhysics::physics,
            PacketCodecs.BOOLEAN, BreastPhysics::showInArmor,
            PacketCodecs.FLOAT, BreastPhysics::bounceMultiplier,
            PacketCodecs.FLOAT, BreastPhysics::floppyMultiplier,
            BreastPhysics::new
        );

        private BreastPhysics(PlayerConfig plr) {
            this(plr.hasBreastPhysics(), plr.showBreastsInArmor(), plr.getBounceMultiplier(), plr.getFloppiness());
        }

        private void applyTo(PlayerConfig plr) {
            plr.updateBreastPhysics(physics);
            plr.updateShowBreastsInArmor(showInArmor);
            plr.updateBounceMultiplier(bounceMultiplier);
            plr.updateFloppiness(floppyMultiplier);
        }
    }

    protected record ButtocksPhysics(boolean physics, boolean showInArmor, float bounceMultiplier, float floppyMultiplier) {

        public static final PacketCodec<ByteBuf, ButtocksPhysics> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, ButtocksPhysics::physics,
            PacketCodecs.BOOLEAN, ButtocksPhysics::showInArmor,
            PacketCodecs.FLOAT, ButtocksPhysics::bounceMultiplier,
            PacketCodecs.FLOAT, ButtocksPhysics::floppyMultiplier,
            ButtocksPhysics::new
        );

        private ButtocksPhysics(PlayerConfig plr) {
            this(plr.hasButtocksPhysics(), plr.showButtocksInArmor(), plr.getBounceMultiplier(), plr.getFloppiness());
        }

        private void applyTo(PlayerConfig plr) {
            plr.updateButtocksPhysics(physics);
            plr.updateShowButtocksInArmor(showInArmor);
            plr.updateBounceMultiplier(bounceMultiplier);
            plr.updateFloppiness(floppyMultiplier);
        }
    }

    protected interface SyncPacketConstructor<T extends AbstractSyncPacket> {
        T construct(UUID uuid, Gender gender, float bustSize, float buttocksSize, boolean hurtSounds, float voicePitch, BreastPhysics physics, ButtocksPhysics buttocksPhysics, Breasts breasts, Buttocks buttocks);
    }
}


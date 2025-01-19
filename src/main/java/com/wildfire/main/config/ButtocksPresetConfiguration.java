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

package com.wildfire.main.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.ArrayList;

public class ButtocksPresetConfiguration extends AbstractConfiguration {

    private static final String PRESETS_DIR = "WildfireGender/presets";

    public static final StringConfigKey PRESET_NAME = new StringConfigKey("preset_name", "");
    public static final FloatConfigKey BUTTOCKS_SIZE = new FloatConfigKey("buttocks_size", 0.6F, 0, 0.8f);

    public static final FloatConfigKey BUTTOCKS_OFFSET_X = new FloatConfigKey("buttocks_xOffset", 0.0F, -1, 1);
    public static final FloatConfigKey BUTTOCKS_OFFSET_Y = new FloatConfigKey("buttocks_yOffset", 0.0F, -1, 1);
    public static final FloatConfigKey BUTTOCKS_OFFSET_Z = new FloatConfigKey("buttocks_zOffset", 0.0F, -1, 0);
    public static final BooleanConfigKey BUTTOCKS_UNIBUTT = new BooleanConfigKey("buttocks_unibutt", true);
    public static final FloatConfigKey BUTTOCKS_CLEAVAGE = new FloatConfigKey("buttocks_cleavage", 0, 0, 0.1F);

    public ButtocksPresetConfiguration(String cfgName) {
        super(PRESETS_DIR, cfgName);
    }

    public static ButtocksPresetConfiguration[] getButtocksPresetConfigurationFiles() {
        ArrayList<ButtocksPresetConfiguration> presets = new ArrayList<>();
        File saveDir = FabricLoader.getInstance().getConfigDir().resolve(PRESETS_DIR).toFile();

        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        File[] presetFiles = saveDir.listFiles();
        if (presetFiles != null) {
            for (File f : presetFiles) {
                // strip the trailing '.json'
                String name = f.getName().substring(0, f.getName().length() - 5);
                ButtocksPresetConfiguration cfg = new ButtocksPresetConfiguration(name);
                cfg.load(); // load from file
                presets.add(cfg);
            }
        }

        return presets.toArray(ButtocksPresetConfiguration[]::new);
    }
}

package com.wildfire.main.contributors;

/**
 * Contributor POJO for 1.8.9 port.
 * Restores role ordering and color values so the credits GUI can sort and tint correctly.
 */
public class Contributor {
    public enum Role {
        MOD_CREATOR(0xFFCC66FF),          // light purple
        FABRIC_MAINTAINER(0xFFA78FFF),    // purple-ish
        NEOFORGE_MAINTAINER(0xFFA78FFF),  // purple-ish
        DEVELOPER(0xFFFFD700),            // gold
        TRANSLATOR(0xFF66CCFF),           // light blue
        MASCOT(0xFFFFD700),               // gold
        VOICE_ACTOR_FEMALE(0xFFFFD700),   // gold
        GENERIC(0xFFFFD700);              // gold

        private final int color;

        Role(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        public String shortNameKey() {
            switch (this) {
                case MOD_CREATOR: return "wildfire_gender.contributor.role.mod_creator.short";
                case FABRIC_MAINTAINER: return "wildfire_gender.contributor.role.fabric_maintainer.short";
                case NEOFORGE_MAINTAINER: return "wildfire_gender.contributor.role.neoforge_maintainer.short";
                case DEVELOPER: return "wildfire_gender.contributor.role.developer.short";
                case TRANSLATOR: return "wildfire_gender.contributor.role.translator.short";
                case MASCOT: return "wildfire_gender.contributor.role.mascot.short";
                case VOICE_ACTOR_FEMALE: return "wildfire_gender.contributor.role.voice_actor_female.short";
                default: return "wildfire_gender.contributor.role.generic.short";
            }
        }

        public String nameKey() {
            return "wildfire_gender.contributor.role." + this.name().toLowerCase();
        }
    }

    private final Role role;
    private final String name;
    private final String description;
    private final boolean showInCredits;

    public Contributor(Role role, String name, String description, boolean showInCredits) {
        this.role = role;
        this.name = name;
        this.description = description;
        this.showInCredits = showInCredits;
    }

    public Role getRole() { return role; }
    public String name() { return name; }
    public String getDescription() { return description; }
    public Boolean showInCredits() { return showInCredits; }
    public int getColor() { return role.getColor(); }

    public static String getLegacyColorCode(int argb) {
        int rgb = argb & 0xFFFFFF;

        int[] mcColors = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
            0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
            0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
        };

        int bestIndex = 0;
        int bestDist = Integer.MAX_VALUE;

        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        for (int i = 0; i < mcColors.length; i++) {
            int c = mcColors[i];
            int cr = (c >> 16) & 0xFF;
            int cg = (c >> 8) & 0xFF;
            int cb = c & 0xFF;

            int dr = r - cr;
            int dg = g - cg;
            int db = b - cb;
            int dist = dr * dr + dg * dg + db * db;

            if (dist < bestDist) {
                bestDist = dist;
                bestIndex = i;
            }
        }

        return "§" + Integer.toHexString(bestIndex);
    }
}
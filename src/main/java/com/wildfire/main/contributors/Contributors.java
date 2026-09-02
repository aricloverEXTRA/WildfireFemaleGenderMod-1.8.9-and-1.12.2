package com.wildfire.main.contributors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class Contributors {

    private static final Map<UUID, Contributor> CONTRIBUTORS = new LinkedHashMap<>();

    static {
        add("23b6feed-2dfe-4f2e-9429-863fd4adb946", Contributor.Role.MOD_CREATOR, "WildfireFGM", true);
        add("70336328-0de7-430e-8cba-2779e2a05ab5", Contributor.Role.FABRIC_MAINTAINER, "celeste", true);
        add("64e57307-72e5-4f43-be9c-181e8e35cc9b", Contributor.Role.NEOFORGE_MAINTAINER, "pupnewfster", true);
        add("ad8ee68c-0aa1-47f9-b29f-f92fa1ef66dc", Contributor.Role.DEVELOPER, "DiaDemiEmi", true);
        add("3f36f7e9-7459-43fe-87ce-4e8a5d47da80", Contributor.Role.DEVELOPER, "IzzyBizzy45", true);
        add("ad3cb52d-524b-41b4-b9d6-b91ec440811d", Contributor.Role.DEVELOPER, "Crosby", true);
        add("618a8390-51b1-43b2-a53a-ab72c1bbd8bd", Contributor.Role.CI_MAINTAINER, "Kichura", true);
        add("9a60e979-c890-4b43-a4c0-32d8a9f6b6b9", Contributor.Role.VOICE_ACTOR_FEMALE, "SavLeftUs", true);
        add("525b0455-15e9-49b7-b61d-f291e8ee6c5b", Contributor.Role.GENERIC, "Powerless001", true);

        add("33feda66-c706-4725-8983-f62e5e6cbee7", Contributor.Role.TRANSLATOR, "Bluelight", true);
        add("8fb5e95d-7f41-4b4c-b8c5-4f15ea3fa2c1", Contributor.Role.TRANSLATOR, "ArcticWah", true);
        add("e31edb15-d8bd-44ac-8ec3-b54114e9d595", Contributor.Role.TRANSLATOR, "PinguinLars", true);
        add("242c1a3a-83ee-4aa6-a3de-568cdac082a4", Contributor.Role.TRANSLATOR, "le0n_lol", true);
        add("4c3e3225-aec0-499c-b563-2b17cdb017f8", Contributor.Role.TRANSLATOR, "Betawolfy", true);
		add("07ee0495-90ae-4138-9343-9c270020196b", Contributor.Role.TRANSLATOR, "vyxiepie_", true);

        add("372271ab-28f2-44bd-b585-95f43e010c22", Contributor.Role.MASCOT, "KeiraFGM", false);
    }

    private Contributors() {}

    private static void add(String uuidStr, Contributor.Role role, String name, boolean visible) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            CONTRIBUTORS.put(uuid, new Contributor(role, name, null, visible));
        } catch (IllegalArgumentException ex) {
            System.err.println("[FGM] Invalid UUID in Contributors: " + uuidStr);
        }
    }

    public static Map<UUID, Contributor> getContributors() {
        return Collections.unmodifiableMap(CONTRIBUTORS);
    }
}

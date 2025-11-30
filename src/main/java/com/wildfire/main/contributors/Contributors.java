package com.wildfire.main.contributors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contributors - simple static registry for 1.8.9 port.
 * CloudSync/remote merging removed for the Forge 1.8.9 port.
 */
public final class Contributors {
    private static final Map<UUID, Contributor> CONTRIBUTORS = new LinkedHashMap<>();

    static {
        // Fill with the canonical hardcoded contributor list (UUIDs are the exact ones from upstream)
        CONTRIBUTORS.put(UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946"),
                new Contributor(Contributor.Role.MOD_CREATOR, "WildfireFGM", null, true));
        CONTRIBUTORS.put(UUID.fromString("70336328-0de7-430e-8cba-2779e2a05ab5"),
                new Contributor(Contributor.Role.FABRIC_MAINTAINER, "celeste", null, true));
        CONTRIBUTORS.put(UUID.fromString("64e57307-72e5-4f43-be9c-181e8e35cc9b"),
                new Contributor(Contributor.Role.NEOFORGE_MAINTAINER, "pupnewfster", null, true));
        CONTRIBUTORS.put(UUID.fromString("9a60e979-c890-4b43-a4c0-32d8a9f6b6b9"),
                new Contributor(Contributor.Role.VOICE_ACTOR_FEMALE, "SavLeftUs", null, true));
        CONTRIBUTORS.put(UUID.fromString("618a8390-51b1-43b2-a53a-ab72c1bbd8bd"),
                new Contributor(Contributor.Role.DEVELOPER, "Kichura", null, true));
        CONTRIBUTORS.put(UUID.fromString("ad8ee68c-0aa1-47f9-b29f-f92fa1ef66dc"),
                new Contributor(Contributor.Role.DEVELOPER, "DiaDemiEmi", null, true));
        CONTRIBUTORS.put(UUID.fromString("3f36f7e9-7459-43fe-87ce-4e8a5d47da80"),
                new Contributor(Contributor.Role.DEVELOPER, "IzzyBizzy45", null, true));
        CONTRIBUTORS.put(UUID.fromString("ad3cb52d-524b-41b4-b9d6-b91ec440811d"),
                new Contributor(Contributor.Role.DEVELOPER, "RacoonDog", null, true));
        CONTRIBUTORS.put(UUID.fromString("525b0455-15e9-49b7-b61d-f291e8ee6c5b"),
                new Contributor(Contributor.Role.GENERIC, "Powerless001", null, true));

        // translators
        CONTRIBUTORS.put(UUID.fromString("33feda66-c706-4725-8983-f62e5e6cbee7"),
                new Contributor(Contributor.Role.TRANSLATOR, "Bluelight", null, true));
        CONTRIBUTORS.put(UUID.fromString("8fb5e95d-7f41-4b4c-b8c5-4f15ea3fa2c1"),
                new Contributor(Contributor.Role.TRANSLATOR, "ArcticWah", null, true));
        CONTRIBUTORS.put(UUID.fromString("e31edb15-d8bd-44ac-8ec3-b54114e9d595"),
                new Contributor(Contributor.Role.TRANSLATOR, "PinguinLars", null, true));
        CONTRIBUTORS.put(UUID.fromString("242c1a3a-83ee-4aa6-a3de-568cdac082a4"),
                new Contributor(Contributor.Role.TRANSLATOR, "le0n_lol", null, true));
        CONTRIBUTORS.put(UUID.fromString("4c3e3225-aec0-499c-b563-2b17cdb017f8"),
                new Contributor(Contributor.Role.TRANSLATOR, "Betawolfy", null, true));

        // mascot (not shown by default)
        CONTRIBUTORS.put(UUID.fromString("372271ab-28f2-44bd-b585-95f43e010c22"),
                new Contributor(Contributor.Role.MASCOT, "KeiraFGM", null, false));
    }

    private Contributors() {}

    public static Map<UUID, Contributor> getContributors() {
        return Collections.unmodifiableMap(CONTRIBUTORS);
    }
}
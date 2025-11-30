package com.wildfire.main.contributors;

/**
 * Minimal Contributor POJO for 1.8.9 port.
 * Keeps the role enum and provides simple accessors.
 */
public class Contributor {
    public enum Role {
        MOD_CREATOR,
        FABRIC_MAINTAINER,
        NEOFORGE_MAINTAINER,
        DEVELOPER,
        TRANSLATOR,
        MASCOT,
        VOICE_ACTOR_FEMALE,
        GENERIC;

        public String shortNameKey() {
            switch (this) {
                case MOD_CREATOR: return "wildfire_gender.contributor.role.mod_creator.short";
                case FABRIC_MAINTAINER: return "wildfire_gender.contributor.role.developer.short";
                case NEOFORGE_MAINTAINER: return "wildfire_gender.contributor.role.developer.short";
                case DEVELOPER: return "wildfire_gender.contributor.role.developer.short";
                case TRANSLATOR: return "wildfire_gender.contributor.role.translator.short";
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
}
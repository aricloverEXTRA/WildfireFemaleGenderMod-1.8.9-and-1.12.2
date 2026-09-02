package com.wildfire.main.entitydata;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.wildfire.api.IGenderArmor;
import com.wildfire.render.armor.EmptyGenderArmor;
import com.wildfire.render.armor.SimpleGenderArmor;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EntityConfig {
    public static final LoadingCache<UUID, EntityConfig> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, EntityConfig>() {
                @Override
                public EntityConfig load(UUID key) {
                    return new EntityConfig(key);
                }
            });

    public final UUID uuid;

    protected String gender = "Male";
    protected float pBustSize = 0.6f;
    protected boolean breastPhysics = true;
    protected float bounceMultiplier = 0.3328767f;
    protected float floppyMultiplier = 0.75f;

    protected UVLayout leftBreastUVLayout = UVLayout.defaultsForLeft();
    protected UVLayout rightBreastUVLayout = UVLayout.defaultsForRight();

    protected UVLayout leftBreastOverlayUVLayout = UVLayout.leftOverlayDefaults();
    protected UVLayout rightBreastOverlayUVLayout = UVLayout.rightOverlayDefaults();

    protected UVLayout leftBreastArmorUVLayout = UVLayout.defaultsForLeft();
    protected UVLayout rightBreastArmorUVLayout = UVLayout.defaultsForRight();

    protected float voicePitch = 1.0f;

    protected final BreastPhysics lBreastPhysics;
    protected final BreastPhysics rBreastPhysics;

    protected final Breasts breasts;
    protected boolean jacketLayer = true;
    protected Object fromComponent = null;

    public boolean forceSimplifiedPhysics = false;

    protected EntityConfig(UUID uuid) {
        this.uuid = uuid;
        this.breasts = new Breasts();
        this.lBreastPhysics = new BreastPhysics();
        this.rBreastPhysics = new BreastPhysics();
    }

    public void readFromStack(ItemStack chestplate) {
        if (chestplate == null) {
            this.fromComponent = null;
            this.gender = "Male";
            return;
        }
    }

    public static boolean isSupportedEntity(EntityLivingBase entity) {
        return (entity instanceof EntityPlayer) || (entity instanceof EntityArmorStand);
    }

    public static EntityConfig getEntity(EntityLivingBase entity) {
        if (entity == null) return null;
        @Nonnull UUID id = entity.getUniqueID();

        if (entity instanceof EntityPlayer) {
            try {
                Class<?> cls = Class.forName("com.wildfire.main.WildfireGender");
                Method m = cls.getMethod("getOrAddPlayerById", UUID.class);
                Object result = m.invoke(null, id);
                if (result instanceof EntityConfig) {
                    return (EntityConfig) result;
                }
            } catch (Throwable ignored) {
            }
        }

        return CACHE.getUnchecked(id);
    }

    public String getGender() {
        return this.gender;
    }

    public Breasts getBreasts() {
        return this.breasts;
    }

    public float getBustSize() {
        return this.pBustSize;
    }

    public boolean hasBreastPhysics() {
        return this.breastPhysics;
    }

    public boolean getArmorPhysicsOverride() {
        return false;
    }

    public boolean showBreastsInArmor() {
        return true;
    }

    public float getBounceMultiplier() {
        return this.bounceMultiplier;
    }

    public float getFloppiness() {
        return this.floppyMultiplier;
    }

    public float getVoicePitch() {
        return this.voicePitch;
    }

    public BreastPhysics getLeftBreastPhysics() {
        return lBreastPhysics;
    }

    public BreastPhysics getRightBreastPhysics() {
        return rBreastPhysics;
    }

    public UVLayout getLeftBreastUVLayout() {
        return this.leftBreastUVLayout;
    }

    public boolean updateLeftBreastUVLayout(UVLayout layout) {
        if (layout == null) return false;
        this.leftBreastUVLayout = layout.copy();
        return true;
    }

    public UVLayout getRightBreastUVLayout() {
        return this.rightBreastUVLayout;
    }

    public boolean updateRightBreastUVLayout(UVLayout layout) {
        if (layout == null) return false;
        this.rightBreastUVLayout = layout.copy();
        return true;
    }

    public UVLayout getLeftBreastOverlayUVLayout() {
        return this.leftBreastOverlayUVLayout;
    }

    public boolean updateLeftBreastOverlayUVLayout(UVLayout layout) {
        if (layout == null) return false;
        this.leftBreastOverlayUVLayout = layout.copy();
        return true;
    }

    public UVLayout getRightBreastOverlayUVLayout() {
        return this.rightBreastOverlayUVLayout;
    }

    public boolean updateRightBreastOverlayUVLayout(UVLayout layout) {
        if (layout == null) return false;
        this.rightBreastOverlayUVLayout = layout.copy();
        return true;
    }

    @Deprecated
    public UVLayout getLeftBreastArmorUVLayout() {
        return this.leftBreastArmorUVLayout;
    }

    @Deprecated
    public UVLayout getRightBreastArmorUVLayout() {
        return this.rightBreastArmorUVLayout;
    }

    public void tickBreastPhysics(EntityLivingBase entity) {
        ItemStack chest = null;
        try {
            if (entity instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) entity;
                chest = p.inventory.armorInventory[2];
            }
        } catch (Throwable ignored) {}

        IGenderArmor armor;
        if (chest == null || chest.getItem() == null) {
            armor = EmptyGenderArmor.INSTANCE;
        } else {
            if (chest.getItem() == Items.leather_chestplate) {
                armor = SimpleGenderArmor.LEATHER;
            } else if (chest.getItem() == Items.chainmail_chestplate) {
                armor = SimpleGenderArmor.CHAINMAIL;
            } else if (chest.getItem() == Items.golden_chestplate) {
                armor = SimpleGenderArmor.GOLD;
            } else if (chest.getItem() == Items.iron_chestplate) {
                armor = SimpleGenderArmor.IRON;
            } else if (chest.getItem() == Items.diamond_chestplate) {
                armor = SimpleGenderArmor.DIAMOND;
            } else {
                armor = SimpleGenderArmor.FALLBACK;
            }
        }

        try {
            this.getLeftBreastPhysics().update(entity, armor);
            this.getRightBreastPhysics().update(entity, armor);
        } catch (Throwable t) {
            System.err.println("[WFG] tickBreastPhysics error: " + t.getMessage());
        }
    }

    @Override
    public String toString() {
        return String.format("%s(uuid=%s, gender=%s)", getClass().getCanonicalName(), uuid, gender);
    }

    public List<String> getDebugInfo() {
        List<String> info = new ArrayList<>();
        info.add("Gender: " + getGender());
        info.add("Breast size: " + getBustSize());
        info.add("Physics enabled: " + hasBreastPhysics());
        Breasts bs = getBreasts();
        info.add("Uniboob: " + bs.isUniboob());
        info.add("Cleavage: " + bs.getCleavage());
        info.add("Offsets: (" + bs.getXOffset() + ", " + bs.getYOffset() + ", " + bs.getZOffset() + ")");
        return info;
    }

    protected <T> boolean updateValue(Object key, T value, Consumer<T> setter) {
        setter.accept(value);
        return true;
    }
}

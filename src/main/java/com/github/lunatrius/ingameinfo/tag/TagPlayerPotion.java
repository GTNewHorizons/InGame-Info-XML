package com.github.lunatrius.ingameinfo.tag;

import java.util.Collection;

import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import org.jetbrains.annotations.NotNull;

import com.github.lunatrius.ingameinfo.client.gui.Info;
import com.github.lunatrius.ingameinfo.client.gui.InfoIcon;
import com.github.lunatrius.ingameinfo.client.gui.InfoText;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;

public abstract class TagPlayerPotion extends Tag {

    public static final int MAXIMUM_INDEX = Potion.potionTypes.length;

    protected static PotionEffect[] potionEffects = null;
    protected final int index;

    public TagPlayerPotion(int index) {
        this.index = index;
    }

    @Override
    public String getName() {
        return super.getName() + this.index;
    }

    @Override
    public String[] getAliases() {
        String[] aliases = super.getAliases();
        String[] aliasesIndexed = new String[aliases.length];
        for (int i = 0; i < aliases.length; i++) {
            aliasesIndexed[i] = aliases[i] + this.index;
        }
        return aliasesIndexed;
    }

    @Override
    public boolean isIndexed() {
        return true;
    }

    @Override
    public int getMaximumIndex() {
        return MAXIMUM_INDEX - 1;
    }

    @Override
    public String getCategory() {
        return "playerpotion";
    }

    protected void updatePotionEffects() {
        if (potionEffects == null) {
            Collection<PotionEffect> potionEffectCollection = player.getActivePotionEffects();
            potionEffects = new PotionEffect[potionEffectCollection.size()];
            if (!potionEffectCollection.isEmpty()) {
                int index = 0;

                for (PotionEffect potionEffect : potionEffectCollection) {
                    potionEffects[index++] = potionEffect;
                }
            }
        }
    }

    public static class Effect extends TagPlayerPotion {

        public Effect(int index) {
            super(index);
        }

        @Override
        public String getValue() {
            updatePotionEffects();
            if (potionEffects.length > this.index) {
                String str = I18n.format(potionEffects[this.index].getEffectName());
                switch (potionEffects[this.index].getAmplifier()) {
                    case 1:
                        str += " II";
                        break;
                    case 2:
                        str += " III";
                        break;
                    case 3:
                        str += " IV";
                        break;
                }
                return str;
            }
            return "";
        }
    }

    public static class Duration extends TagPlayerPotion {

        public Duration(int index) {
            super(index);
        }

        @Override
        public String getValue() {
            updatePotionEffects();
            if (potionEffects.length > this.index) {
                return Potion.getDurationString(potionEffects[this.index]);
            }
            return "0:00";
        }
    }

    public static class DurationTicks extends TagPlayerPotion {

        public DurationTicks(int index) {
            super(index);
        }

        @Override
        public String getValue() {
            updatePotionEffects();
            if (potionEffects.length > this.index) {
                return String.valueOf(potionEffects[this.index].getDuration());
            }
            return "0";
        }
    }

    public static class Negative extends TagPlayerPotion {

        public Negative(int index) {
            super(index);
        }

        @Override
        public String getValue() {
            updatePotionEffects();
            if (potionEffects.length > this.index) {
                final Potion potion = Potion.potionTypes[potionEffects[this.index].getPotionID()];
                return String.valueOf(potion.isBadEffect());
            }
            return "false";
        }
    }

    public static class Icon extends TagPlayerPotion {

        private final boolean large;

        public Icon(int index, boolean large) {
            super(index);
            this.large = large;
        }

        @Override
        public @NotNull String getValue(@NotNull InfoText parent) {
            updatePotionEffects();
            Info value = parent.getAttachedValue(getName());
            if (potionEffects.length > this.index) {
                Potion potion = Potion.potionTypes[potionEffects[this.index].getPotionID()];
                if (potion.hasStatusIcon() && shouldUpdate(value, potion.id)) {
                    InfoIcon icon = new InfoIcon("textures/gui/container/inventory.png");
                    int i = potion.getStatusIconIndex();
                    if (this.large) {
                        icon.setDisplayDimensions(1, -5, 18, 18);
                    } else {
                        icon.setDisplayDimensions(1, -1, 18 / 2, 18 / 2);
                    }

                    icon.setIdentifier(String.valueOf(potion.id));
                    icon.setTextureData((i % 8) * 18, 198 + (i / 8) * 18, 18, 18, 256, 256);
                    parent.attachValue(getName(), icon);
                    return getIconTag(icon);
                }
            } else if (value != null) {
                parent.removeAttachedValue(getName());
            }

            return "";
        }

        @Override
        public String getValue() {
            return "";
        }

        private boolean shouldUpdate(Info value, int potionId) {
            return value == null || !value.getIdentifier().equals(String.valueOf(potionId));
        }
    }

    public static void register() {
        for (int i = 0; i < MAXIMUM_INDEX; i++) {
            TagRegistry.INSTANCE.register(new Effect(i).setName("potioneffect"));
            TagRegistry.INSTANCE.register(new Duration(i).setName("potionduration"));
            TagRegistry.INSTANCE.register(new DurationTicks(i).setName("potiondurationticks"));
            TagRegistry.INSTANCE.register(new Negative(i).setName("potionnegative"));
            TagRegistry.INSTANCE.register(new Icon(i, false).setName("potionicon"));
            TagRegistry.INSTANCE.register(new Icon(i, true).setName("potionlargeicon"));
        }
    }

    public static void releaseResources() {
        potionEffects = null;
    }
}

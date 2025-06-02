package com.github.lunatrius.ingameinfo.tag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.ingameinfo.client.gui.Info;
import com.github.lunatrius.ingameinfo.client.gui.InfoText;
import com.github.lunatrius.ingameinfo.reference.Reference;

public abstract class Tag {

    protected static final Minecraft minecraft = Minecraft.getMinecraft();
    protected static final Vector3i playerPosition = new Vector3i();
    protected static final Vector3f playerMotion = new Vector3f();
    protected static MinecraftServer server;
    protected static World world;
    protected static EntityClientPlayerMP player;
    protected static boolean hasSeed = false;
    protected static long seed = 0;
    protected static boolean hasNextRainTime = false;
    protected static int nextRainTime = 0;

    private String name = null;
    private String[] aliases = new String[0];

    public Tag setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Tag setAliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public boolean isIndexed() {
        return false;
    }

    public int getMaximumIndex() {
        return -1;
    }

    public String getRawName() {
        return this.name;
    }

    public String getFormattedName() {
        return this.name + (isIndexed() ? String.format("[0..%d]", getMaximumIndex()) : "");
    }

    public String getLocalizedCategory() {
        return I18n.format(Reference.MODID.toLowerCase() + ".tag.category." + getCategory() + ".name");
    }

    public String getLocalizedDescription() {
        return I18n.format(Reference.MODID.toLowerCase() + ".tag." + getRawName() + ".desc");
    }

    public abstract String getCategory();

    public abstract String getValue();

    public @NotNull String getValue(@NotNull InfoText caller) {
        return "";
    }

    public static void setServer(MinecraftServer server) {
        Tag.server = server;
        if (Tag.server == null) {
            unsetSeed();
        } else {
            try {
                setSeed(Tag.server.worldServerForDimension(0).getSeed());
            } catch (Exception e) {
                unsetSeed();
            }
        }
    }

    public static void setSeed(long seed) {
        Tag.hasSeed = true;
        Tag.seed = seed;
    }

    public static void unsetSeed() {
        Tag.hasSeed = false;
        Tag.seed = 0;
    }

    public static void setNextRain(int rain) {
        Tag.hasNextRainTime = true;
        Tag.nextRainTime = rain;
    }

    public static void unsetNextRain() {
        Tag.hasNextRainTime = false;
        Tag.nextRainTime = 0;
    }

    public static void update() {
        world = minecraft.theWorld;
        player = minecraft.thePlayer;

        if (player != null) {
            playerPosition
                    .set((int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
            playerMotion.set(
                    (float) (player.posX - player.prevPosX),
                    (float) (player.posY - player.prevPosY),
                    (float) (player.posZ - player.prevPosZ));
        }
    }

    public static void releaseResources() {
        TagNearbyPlayer.releaseResources();
        TagPlayerPotion.releaseResources();
    }

    public static void onClientDisconnect() {
        world = null;
        player = null;
    }

    public static String getIconTag(Info info) {
        return String.format("{ICON|%s}", info.getIconSpacing());
    }
}

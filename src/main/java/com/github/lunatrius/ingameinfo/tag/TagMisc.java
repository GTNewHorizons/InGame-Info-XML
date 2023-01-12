package com.github.lunatrius.ingameinfo.tag;

import com.github.lunatrius.ingameinfo.client.gui.InfoIcon;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;
import java.util.List;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.EnumChatFormatting;

public abstract class TagMisc extends Tag {
    protected static final ResourcePackRepository resourcePackRepository = minecraft.getResourcePackRepository();

    @Override
    public String getCategory() {
        return "misc";
    }

    public static class MemoryMaximum extends TagMisc {
        @Override
        public String getValue() {
            return String.valueOf(Runtime.getRuntime().maxMemory());
        }
    }

    public static class MemoryTotal extends TagMisc {
        @Override
        public String getValue() {
            return String.valueOf(Runtime.getRuntime().totalMemory());
        }
    }

    public static class MemoryFree extends TagMisc {
        @Override
        public String getValue() {
            return String.valueOf(Runtime.getRuntime().freeMemory());
        }
    }

    public static class MemoryUsed extends TagMisc {
        @Override
        public String getValue() {
            return String.valueOf(
                    Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        }
    }

    public static class FPS extends TagMisc {
        @Override
        public String getValue() {
            return minecraft.debug.substring(0, minecraft.debug.indexOf(" fps"));
        }
    }

    public static class ResourcePack extends TagMisc {
        @Override
        public String getValue() {
            List<ResourcePackRepository.Entry> repositoryEntries = resourcePackRepository.getRepositoryEntries();
            if (repositoryEntries.size() > 0) {
                return repositoryEntries.get(0).getResourcePackName();
            }
            return resourcePackRepository.rprDefaultResourcePack.getPackName();
        }
    }

    public static class EntitiesRendered extends TagMisc {
        @Override
        public String getValue() {
            String str = minecraft.getEntityDebug();
            return str.substring(str.indexOf(' ') + 1, str.indexOf('/'));
        }
    }

    public static class EntitiesTotal extends TagMisc {
        @Override
        public String getValue() {
            String str = minecraft.getEntityDebug();
            return str.substring(str.indexOf('/') + 1, str.indexOf('.'));
        }
    }

    public static class LoadedChunks extends TagMisc {
        @Override
        public String getValue() {
            return String.valueOf(world.getChunkProvider().getLoadedChunkCount());
        }
    }

    public static class Server extends TagMisc {
        @Override
        public String getValue() {
            String str = player.sendQueue.getNetworkManager().getSocketAddress().toString();
            int i = str.indexOf("/");
            int j = str.indexOf(":");
            if (i < 0) {
                return "localhost";
            }

            String name = (i == 0) ? str.substring(i + 1, j) : str.substring(0, i);
            String port = str.substring(j + 1);
            return name + (port.equals("25565") ? "" : ":" + port);
        }
    }

    public static class ServerName extends TagMisc {
        @Override
        public String getValue() {
            String str = player.sendQueue.getNetworkManager().getSocketAddress().toString();
            int i = str.indexOf("/");
            if (i < 0) {
                return "localhost";
            } else if (i == 0) {
                return str.substring(i + 1, str.indexOf(":"));
            }
            return str.substring(0, i);
        }
    }

    public static class ServerIP extends TagMisc {
        @Override
        public String getValue() {
            String str = player.sendQueue.getNetworkManager().getSocketAddress().toString();
            int i = str.indexOf("/");
            if (i < 0) {
                return "127.0.0.1";
            }
            return str.substring(i + 1, str.indexOf(":"));
        }
    }

    public static class ServerPort extends TagMisc {
        @Override
        public String getValue() {
            String str = player.sendQueue.getNetworkManager().getSocketAddress().toString();
            int i = str.indexOf("/");
            if (i < 0) {
                return "-1";
            }
            return str.substring(str.indexOf(":") + 1);
        }
    }

    public static class Ping extends TagMisc {
        @Override
        public String getValue() {
            List<GuiPlayerInfo> list = player.sendQueue.playerInfoList;
            for (GuiPlayerInfo playerInfo : list) {
                if (player.getGameProfile().getName().equals(playerInfo.name)) {
                    return String.valueOf(playerInfo.responseTime);
                }
            }
            return "-1";
        }
    }

    public static class PingIcon extends TagMisc {
        @Override
        public String getValue() {
            List<GuiPlayerInfo> list = player.sendQueue.playerInfoList;
            for (GuiPlayerInfo playerInfo : list) {
                if (player.getGameProfile()
                        .getName()
                        .equals(EnumChatFormatting.getTextWithoutFormattingCodes(playerInfo.name))) {
                    int pingIndex = 4;
                    if (playerInfo.responseTime < 0) {
                        pingIndex = 5;
                    } else if (playerInfo.responseTime < 150) {
                        pingIndex = 0;
                    } else if (playerInfo.responseTime < 300) {
                        pingIndex = 1;
                    } else if (playerInfo.responseTime < 600) {
                        pingIndex = 2;
                    } else if (playerInfo.responseTime < 1000) {
                        pingIndex = 3;
                    }

                    InfoIcon icon = new InfoIcon("textures/gui/icons.png");
                    icon.setDisplayDimensions(0, 0, 10, 8);
                    icon.setTextureData(0, 176 + pingIndex * 8, 10, 8, 256, 256);
                    info.add(icon);
                    return getIconTag(icon);
                }
            }
            return "-1";
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new MemoryMaximum().setName("memmax"));
        TagRegistry.INSTANCE.register(new MemoryTotal().setName("memtotal"));
        TagRegistry.INSTANCE.register(new MemoryFree().setName("memfree"));
        TagRegistry.INSTANCE.register(new MemoryUsed().setName("memused"));
        TagRegistry.INSTANCE.register(new FPS().setName("fps"));
        TagRegistry.INSTANCE.register(new ResourcePack().setName("resourcepack"));
        TagRegistry.INSTANCE.register(new EntitiesRendered().setName("entitiesrendered"));
        TagRegistry.INSTANCE.register(new EntitiesTotal().setName("entitiestotal"));
        TagRegistry.INSTANCE.register(new LoadedChunks().setName("loadedchunks"));
        TagRegistry.INSTANCE.register(new Server().setName("server"));
        TagRegistry.INSTANCE.register(new ServerName().setName("servername"));
        TagRegistry.INSTANCE.register(new ServerIP().setName("serverip"));
        TagRegistry.INSTANCE.register(new ServerPort().setName("serverport"));
        TagRegistry.INSTANCE.register(new Ping().setName("ping"));
        TagRegistry.INSTANCE.register(new PingIcon().setName("pingicon"));
    }
}

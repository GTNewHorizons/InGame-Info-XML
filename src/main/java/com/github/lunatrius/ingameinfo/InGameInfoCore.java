package com.github.lunatrius.ingameinfo;

import com.github.lunatrius.ingameinfo.client.gui.Info;
import com.github.lunatrius.ingameinfo.client.gui.InfoText;
import com.github.lunatrius.ingameinfo.handler.ConfigurationHandler;
import com.github.lunatrius.ingameinfo.parser.IParser;
import com.github.lunatrius.ingameinfo.parser.json.JsonParser;
import com.github.lunatrius.ingameinfo.parser.text.TextParser;
import com.github.lunatrius.ingameinfo.parser.xml.XmlParser;
import com.github.lunatrius.ingameinfo.printer.IPrinter;
import com.github.lunatrius.ingameinfo.printer.json.JsonPrinter;
import com.github.lunatrius.ingameinfo.printer.text.TextPrinter;
import com.github.lunatrius.ingameinfo.printer.xml.XmlPrinter;
import com.github.lunatrius.ingameinfo.reference.Names;
import com.github.lunatrius.ingameinfo.reference.Reference;
import com.github.lunatrius.ingameinfo.tag.Tag;
import com.github.lunatrius.ingameinfo.value.Value;
import com.github.lunatrius.ingameinfo.value.ValueComplex;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResource;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class InGameInfoCore {
    private static final Pattern PATTERN = Pattern.compile("\\{ICON\\|( *)\\}", Pattern.CASE_INSENSITIVE);
    private static final Matcher MATCHER = PATTERN.matcher("");
    public static final InGameInfoCore INSTANCE = new InGameInfoCore();

    private IParser parser;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Profiler profiler = this.minecraft.mcProfiler;
    private File configDirectory = null;
    private File configFile = null;
    private final Map<Alignment, List<List<Value>>> format = new HashMap<>();
    private final List<Info> info = new ArrayList<>();
    private final List<Info> infoItemQueue = new ArrayList<>();
    private ScaledResolution scaledResolution =
            new ScaledResolution(this.minecraft, this.minecraft.displayWidth, this.minecraft.displayHeight);

    private InGameInfoCore() {
        Tag.setInfo(this.infoItemQueue);
        Value.setInfo(this.infoItemQueue);
    }

    public boolean setConfigDirectory(File directory) {
        this.configDirectory = directory;
        return true;
    }

    public File getConfigDirectory() {
        return this.configDirectory;
    }

    public boolean setConfigFile(String filename) {
        File file = new File(this.configDirectory, filename);
        if (file.exists()) {
            if (filename.endsWith(Names.Files.EXT_XML)) {
                this.configFile = file;
                this.parser = new XmlParser();
                return true;
            } else if (filename.endsWith(Names.Files.EXT_JSON)) {
                this.configFile = file;
                this.parser = new JsonParser();
                return true;
            } else if (filename.endsWith(Names.Files.EXT_TXT)) {
                this.configFile = file;
                this.parser = new TextParser();
                return true;
            }
        }

        this.configFile = null;
        this.parser = new XmlParser();
        return false;
    }

    public void onTickClient() {
        float scale = ConfigurationHandler.Scale / 10;
        int scaledWidth = (int) (scaledResolution.getScaledWidth() / scale);
        int scaledHeight = (int) (scaledResolution.getScaledHeight() / scale);

        World world = this.minecraft.theWorld;
        if (world == null) {
            return;
        }
        Tag.setWorld(world);

        EntityClientPlayerMP player = this.minecraft.thePlayer;
        if (player == null) {
            return;
        }
        Tag.setPlayer(player);

        this.info.clear();
        int x, y;

        this.profiler.startSection("alignment");
        this.profiler.startSection("none");
        for (Alignment alignment : Alignment.values()) {
            this.profiler.endStartSection(alignment.toString().toLowerCase());
            List<List<Value>> lines = this.format.get(alignment);

            if (lines == null) {
                continue;
            }

            FontRenderer fontRenderer = this.minecraft.fontRenderer;
            List<Info> queue = new ArrayList<>();

            for (List<Value> line : lines) {
                StringBuilder str = new StringBuilder();

                this.infoItemQueue.clear();
                this.profiler.startSection("taggathering");
                for (Value value : line) {
                    str.append(getValue(value));
                }
                this.profiler.endSection();

                if (str.length() > 0) {
                    String processed = str.toString().replaceAll("\\{ICON\\|( *)\\}", "$1");

                    x = alignment.getX(scaledWidth, fontRenderer.getStringWidth(processed));
                    InfoText text = new InfoText(fontRenderer, processed, x, 0);

                    if (this.infoItemQueue.size() > 0) {
                        MATCHER.reset(str.toString());

                        for (int i = 0; i < this.infoItemQueue.size() && MATCHER.find(); i++) {
                            Info item = this.infoItemQueue.get(i);
                            item.x = fontRenderer.getStringWidth(str.substring(0, MATCHER.start()));
                            text.children.add(item);

                            str = new StringBuilder(
                                    str.toString().replaceFirst(Pattern.quote(MATCHER.group(0)), MATCHER.group(1)));
                            MATCHER.reset(str.toString());
                        }
                    }
                    queue.add(text);
                }
            }

            y = alignment.getY(scaledHeight, queue.size() * (fontRenderer.FONT_HEIGHT + 1));
            for (Info item : queue) {
                item.y = y;
                this.info.add(item);
                y += fontRenderer.FONT_HEIGHT + 1;
            }

            this.info.addAll(queue);
        }
        this.profiler.endSection();
        this.profiler.endSection();

        Tag.releaseResources();
        ValueComplex.ValueFile.tick();
    }

    public void onTickRender(ScaledResolution resolution) {
        this.scaledResolution = resolution;
        GL11.glPushMatrix();
        float scale = ConfigurationHandler.Scale / 10;
        GL11.glScalef(scale, scale, scale);
        for (Info info : this.info) {
            info.draw();
        }
        GL11.glPopMatrix();
    }

    public boolean loadConfig(String filename) {
        return setConfigFile(filename) && reloadConfig();
    }

    public boolean reloadConfig() {
        this.info.clear();
        this.infoItemQueue.clear();
        this.format.clear();

        if (this.parser == null) {
            return false;
        }

        final InputStream inputStream = getInputStream();
        if (inputStream == null) {
            return false;
        }

        if (this.parser.load(inputStream) && this.parser.parse(this.format)) {
            return true;
        }

        this.format.clear();
        return false;
    }

    private InputStream getInputStream() {
        InputStream inputStream = null;

        try {
            if (this.configFile != null && this.configFile.exists()) {
                Reference.logger.debug("Loading file config...");
                inputStream = new FileInputStream(this.configFile);
            } else {
                Reference.logger.debug("Loading default config...");
                ResourceLocation resourceLocation = new ResourceLocation("ingameinfo", Names.Files.FILE_XML);
                IResource resource = this.minecraft.getResourceManager().getResource(resourceLocation);
                inputStream = resource.getInputStream();
            }
        } catch (Exception e) {
            Reference.logger.error("", e);
        }

        return inputStream;
    }

    public boolean saveConfig(String filename) {
        IPrinter printer = null;
        File file = new File(this.configDirectory, filename);
        if (filename.endsWith(Names.Files.EXT_XML)) {
            printer = new XmlPrinter();
        } else if (filename.endsWith(Names.Files.EXT_JSON)) {
            printer = new JsonPrinter();
        } else if (filename.endsWith(Names.Files.EXT_TXT)) {
            printer = new TextPrinter();
        }

        return printer != null && printer.print(file, this.format);
    }

    private String getValue(Value value) {
        try {
            if (value.isValidSize()) {
                return value.getReplacedValue();
            }
        } catch (Exception e) {
            Reference.logger.debug("Failed to get value!", e);
            return "null";
        }

        return "";
    }
}

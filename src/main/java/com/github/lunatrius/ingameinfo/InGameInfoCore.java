package com.github.lunatrius.ingameinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResource;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.github.lunatrius.ingameinfo.client.gui.InfoText;
import com.github.lunatrius.ingameinfo.handler.ClientConfigurationHandler;
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

public class InGameInfoCore {

    public static final InGameInfoCore INSTANCE = new InGameInfoCore();

    private IParser parser;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Profiler profiler = minecraft.mcProfiler;
    private File configDirectory = null;
    private File configFile = null;
    /**
     * Config file name without locale code suffix.
     */
    private String baseConfigFileName;
    private final Map<Alignment, List<List<Value>>> format = new HashMap<>();
    public ScaledResolution scaledResolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
    private final Set<InfoText> infoTexts = new HashSet<>();
    public int scaledWidth;
    public int scaledHeight;

    private InGameInfoCore() {}

    public void setConfigDirectory(File directory) {
        configDirectory = directory;
    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public void setConfigFileWithLocale() {
        if (baseConfigFileName != null) {
            setConfigFileWithLocale(baseConfigFileName);
        }
    }

    public void setConfigFileWithLocale(String filename) {
        String userLang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        String baseName = filename.split("\\.")[0];
        String extension = filename.split("\\.").length > 1 ? filename.split("\\.")[1] : "";
        String localeAwareFileName = baseName + "_" + userLang + "." + extension;
        if (new File(configDirectory, localeAwareFileName).isFile()) {
            setConfigFile(localeAwareFileName, filename);
        } else {
            setConfigFile(filename);
        }
    }

    public boolean setConfigFile(String filename) {
        return setConfigFile(filename, filename);
    }

    public boolean setConfigFile(String filename, String baseName) {
        File file = new File(configDirectory, filename);
        if (file.exists()) {
            configFile = file;
            baseConfigFileName = baseName;
            if (filename.endsWith(Names.Files.EXT_XML)) {
                parser = new XmlParser();
            } else if (filename.endsWith(Names.Files.EXT_JSON)) {
                parser = new JsonParser();
            } else if (filename.endsWith(Names.Files.EXT_TXT)) {
                parser = new TextParser();
            }
            return true;
        }

        configFile = null;
        parser = new XmlParser();
        baseConfigFileName = null;
        return false;
    }

    public void onTickClient() {
        float scale = ClientConfigurationHandler.Scale / 10;
        scaledWidth = (int) (scaledResolution.getScaledWidth() / scale);
        scaledHeight = (int) (scaledResolution.getScaledHeight() / scale);
        World world = minecraft.theWorld;
        EntityClientPlayerMP player = minecraft.thePlayer;
        if (world == null || player == null) {
            return;
        }
        Tag.update();

        if (infoTexts.isEmpty()) {
            refreshInfoTexts();
        }

        for (InfoText infoText : infoTexts) {
            infoText.update();
        }

        Tag.releaseResources();
        ValueComplex.ValueFile.tick();
    }

    public void onTickRender(ScaledResolution resolution) {
        scaledResolution = resolution;
        GL11.glPushMatrix();
        float scale = ClientConfigurationHandler.Scale / 10;
        GL11.glScalef(scale, scale, scale);
        for (InfoText info : infoTexts) {
            info.draw();
        }
        GL11.glPopMatrix();
    }

    public boolean loadConfig(String filename) {
        return setConfigFile(filename) && reloadConfig();
    }

    public boolean reloadConfig() {
        infoTexts.clear();
        format.clear();

        if (parser == null) {
            return false;
        }

        final InputStream inputStream = getInputStream();
        if (inputStream == null) {
            return false;
        }

        if (parser.load(inputStream) && parser.parse(format)) {
            return true;
        }

        format.clear();
        return false;
    }

    private InputStream getInputStream() {
        InputStream inputStream = null;

        try {
            if (configFile != null && configFile.exists()) {
                Reference.logger.debug("Loading file config...");
                inputStream = new FileInputStream(configFile);
            } else {
                Reference.logger.debug("Loading default config...");
                ResourceLocation resourceLocation = new ResourceLocation("ingameinfo", Names.Files.FILE_XML);
                IResource resource = minecraft.getResourceManager().getResource(resourceLocation);
                inputStream = resource.getInputStream();
            }
        } catch (Exception e) {
            Reference.logger.error("", e);
        }

        return inputStream;
    }

    public void refreshInfoTexts() {
        for (Alignment alignment : Alignment.values()) {
            List<List<Value>> lines = format.get(alignment);

            if (lines == null) {
                continue;
            }

            for (int i = 0; i < lines.size(); i++) {
                infoTexts.add(new InfoText(i, alignment, lines.get(i)));
            }
        }
    }

    public boolean saveConfig(String filename) {
        IPrinter printer = null;
        File file = new File(configDirectory, filename);
        if (filename.endsWith(Names.Files.EXT_XML)) {
            printer = new XmlPrinter();
        } else if (filename.endsWith(Names.Files.EXT_JSON)) {
            printer = new JsonPrinter();
        } else if (filename.endsWith(Names.Files.EXT_TXT)) {
            printer = new TextPrinter();
        }

        return printer != null && printer.print(file, format);
    }

    public void moveConfig(File directory, String fileName) {
        File originalFile = new File(directory, fileName);
        if (!originalFile.isFile()) return;
        Path source = originalFile.toPath();
        Path subdirectory = directory.toPath().resolve(Names.Files.SUBDIRECTORY);
        Path destination = subdirectory.resolve(fileName).normalize();
        if (!destination.startsWith(directory.toPath())) {
            // Maybe we don't need but just in case
            throw new RuntimeException("Failed to move file: " + destination);
        }
        try {
            if (!subdirectory.toFile().isDirectory()) {
                Files.createDirectory(subdirectory);
            }
            // noinspection ResultOfMethodCallIgnored
            destination.toFile().createNewFile();
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

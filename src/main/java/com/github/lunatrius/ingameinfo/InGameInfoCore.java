package com.github.lunatrius.ingameinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

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
    public int scaledWidth;
    public int scaledHeight;
    private boolean needsRefresh = true;

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

    public String getBaseConfigFileName() {
        return baseConfigFileName;
    }

    public boolean hasConfigFileWithLocale(String filename) {
        if (configDirectory == null || filename == null) {
            return false;
        }

        String localeAwareFileName = getLocaleAwareFilename(filename);
        if (new File(configDirectory, localeAwareFileName).isFile()) {
            return true;
        }

        return new File(configDirectory, filename).isFile();
    }

    public void setConfigFileWithLocale(String filename) {
        String localeAwareFileName = getLocaleAwareFilename(filename);
        if (new File(configDirectory, localeAwareFileName).isFile()) {
            setConfigFile(localeAwareFileName, filename);
        } else {
            setConfigFile(filename);
        }
    }

    public String getLocaleAwareFilename(String filename) {
        String userLang = minecraft.getLanguageManager().getCurrentLanguage().getLanguageCode();
        String[] parts = filename.split("\\.", 2);
        String baseName = parts[0];
        String extension = parts.length > 1 ? "." + parts[1] : "";
        return baseName + "_" + userLang + extension;
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
        Tag.update();

        if (needsRefresh) {
            refreshInfoTexts();
            needsRefresh = false;
        }

        for (Alignment alignment : Alignment.VALUES) {
            int lastActiveIndex = 0;
            for (InfoText infoText : alignment.getLines()) {
                infoText.update(lastActiveIndex);
                if (infoText.isActive()) {
                    lastActiveIndex++;
                }
            }
        }

        ValueComplex.ValueFile.tick();
    }

    public void onTickRender(ScaledResolution resolution) {
        scaledResolution = resolution;
        GL11.glPushMatrix();
        float scale = ClientConfigurationHandler.Scale / 10;
        GL11.glScalef(scale, scale, scale);
        for (Alignment alignment : Alignment.VALUES) {
            for (InfoText info : alignment.getLines()) {
                info.draw();
            }
        }
        GL11.glPopMatrix();
    }

    public boolean loadConfig(String filename) {
        return setConfigFile(filename) && reloadConfig();
    }

    public boolean reloadConfig() {
        needsRefresh = true;
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
        for (Alignment alignment : Alignment.VALUES) {
            List<List<Value>> lines = format.get(alignment);
            alignment.getLines().clear();

            if (lines == null) {
                continue;
            }

            for (List<Value> line : lines) {
                alignment.getLines().add(new InfoText(alignment, line));
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

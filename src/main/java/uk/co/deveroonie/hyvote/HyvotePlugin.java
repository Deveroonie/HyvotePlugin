package uk.co.deveroonie.hyvote;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import tools.jackson.databind.ObjectMapper;
import uk.co.deveroonie.hyvote.models.Settings;
import uk.co.deveroonie.hyvote.server.HyvoteServer;
import uk.co.deveroonie.hyvote.util.Keys;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class HyvotePlugin extends JavaPlugin {
    public static Path dataDir;
    public static Settings settings;
    public static HytaleLogger logger;

    public HyvotePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        try {
            if(!Files.exists(getDataDirectory())) {
                Files.createDirectories(getDataDirectory());
            }
            dataDir = getDataDirectory();

            if(Files.notExists(getDataDirectory().resolve("settings.json"))) {
                try (InputStream in = getClass().getClassLoader()
                        .getResourceAsStream("settings.json")) {

                    if (in == null) {
                        getLogger().at(Level.SEVERE)
                                .log("settings.json not found in JAR file.");
                    } else {
                        Files.copy(in, getDataDirectory().resolve("settings.json"));
                    }
                }
            }

            if(
                    Files.notExists(getDataDirectory().resolve("private.key"))
                    || Files.notExists(getDataDirectory().resolve("public.key"))
            ) {
                Keys.saveKeys();
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try (InputStream in = Files.newInputStream(dataDir.resolve("settings.json"))) {
                settings = objectMapper.readValue(in, Settings.class);
            } catch (IOException e) {
                getLogger().at(Level.SEVERE).log("Failed to load settings.");
                throw new RuntimeException(e);
            }

            logger = getLogger();
            new HyvoteServer().start();

        } catch (IOException e) {
            getLogger().at(Level.SEVERE).log("Failed to extract settings.json.");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            getLogger().atSevere().log("Failed to start the Hyvote server.");
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    public static Path getDataDir() { return dataDir; }

    public static Settings getSettings() { return settings; }

    public static HytaleLogger getLog() { return logger; }
}
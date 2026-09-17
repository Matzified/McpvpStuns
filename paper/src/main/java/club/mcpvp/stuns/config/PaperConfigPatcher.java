package club.mcpvp.stuns.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles detection, backup, and safe mutation of paper-global.yml.
 */
public final class PaperConfigPatcher {

    private static final String TARGET_KEY = "skip-vanilla-damage-tick-when-shield-blocked";
    private static final String SECTION_UNSUPPORTED = "unsupported-settings";
    private static final String SECTION_COLLISIONS = "collisions";

    private final Logger logger;
    private final Yaml yaml;

    public PaperConfigPatcher(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        this.yaml = new Yaml(options);
    }

    public boolean applyPatch() {
        File configFile = resolveConfigFile();
        if (configFile == null || !configFile.isFile()) {
            logger.warning("[McPvpStuns] paper-global.yml was not found. Skipping auto-configuration.");
            return false;
        }

        Map<String, Object> root;
        try (FileInputStream in = new FileInputStream(configFile)) {
            root = yaml.load(in);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "[McPvpStuns] Failed to read paper-global.yml: " + ex.getMessage(), ex);
            return false;
        }

        if (root == null) {
            root = new LinkedHashMap<>();
        }

        boolean changed = false;

        // 1. Primary path: unsupported-settings
        Map<String, Object> unsupported = getOrCreateSection(root, SECTION_UNSUPPORTED);
        if (!Boolean.TRUE.equals(unsupported.get(TARGET_KEY))) {
            unsupported.put(TARGET_KEY, true);
            changed = true;
        }

        // 2. Compatibility check: collisions section if present
        Object collisionsObj = root.get(SECTION_COLLISIONS);
        if (collisionsObj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> collisions = (Map<String, Object>) collisionsObj;
            if (!Boolean.TRUE.equals(collisions.get(TARGET_KEY))) {
                collisions.put(TARGET_KEY, true);
                changed = true;
            }
        }

        if (!changed) {
            logger.info("[McPvpStuns] paper-global.yml already has '" + TARGET_KEY + "' enabled.");
            return false;
        }

        // Create timestamped or overwrite .bak
        File backupFile = new File(configFile.getParentFile(), configFile.getName() + ".bak");
        try {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("[McPvpStuns] Backup written to " + backupFile.getName());

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                yaml.dump(root, writer);
            }

            logger.info("[McPvpStuns] Successfully updated paper-global.yml with " + TARGET_KEY + "=true");
            logger.info("[McPvpStuns] Server restart recommended for Paper native kernel changes to take effect.");
            return true;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "[McPvpStuns] Failed to write changes to paper-global.yml: " + ex.getMessage(), ex);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getOrCreateSection(Map<String, Object> parent, String sectionName) {
        Object section = parent.get(sectionName);
        if (section instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        Map<String, Object> newSection = new LinkedHashMap<>();
        parent.put(sectionName, newSection);
        return newSection;
    }

    private static File resolveConfigFile() {
        File standard = new File("config", "paper-global.yml");
        if (standard.isFile()) return standard;

        File root = new File("paper-global.yml");
        if (root.isFile()) return root;

        File parentConfig = new File("..", "config/paper-global.yml");
        if (parentConfig.isFile()) return parentConfig;

        return null;
    }
}

package net.mcreator.lostinfog.config;

import net.neoforged.fml.loading.FMLPaths;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShaderConfig {
    
    private static boolean shaderEnabled = true;
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("lostinfog/gameplay");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("shader.toml").toFile();
    private static long lastModified = 0;

    public static void update() {
        try {
            if (!CONFIG_FILE.exists()) {
                Files.createDirectories(CONFIG_DIR);
                String content = "shader_enabled = true\n";
                Files.writeString(CONFIG_FILE.toPath(), content);
                shaderEnabled = true;
                lastModified = CONFIG_FILE.lastModified();
            } else if (CONFIG_FILE.lastModified() != lastModified) {
                String content = Files.readString(CONFIG_FILE.toPath());
                shaderEnabled = !content.contains("shader_enabled = false") && !content.contains("shader_enabled=false");
                lastModified = CONFIG_FILE.lastModified();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isShaderEnabled() {
        return shaderEnabled;
    }
}
package net.mcreator.lostinfog.client;

import net.neoforged.fml.loading.FMLPaths;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CameraConfig {
    public static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("lostinfog/gameplay/camera_bobbing.toml");
    public static boolean enabled = true;
    public static double idleAmp = 1.5, sprintAmp = 3.0, walkAmp = 2.0;
    public static double idleSpeed = 1.0, sprintSpeed = 3.5, walkSpeed = 2.0;

    public static void load() {
        try {
            if (!Files.exists(PATH.getParent())) Files.createDirectories(PATH.getParent());
            if (!Files.exists(PATH)) save();
            
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(PATH)) { props.load(in); }
            
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            idleAmp = Double.parseDouble(props.getProperty("idle_amplitude", "1.5"));
            sprintAmp = Double.parseDouble(props.getProperty("sprint_amplitude", "3.0"));
            walkAmp = Double.parseDouble(props.getProperty("walk_amplitude", "2.0"));
            idleSpeed = Double.parseDouble(props.getProperty("idle_speed", "1.0"));
            sprintSpeed = Double.parseDouble(props.getProperty("sprint_speed", "3.5"));
            walkSpeed = Double.parseDouble(props.getProperty("walk_speed", "2.0"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try {
            Properties props = new Properties();
            props.setProperty("enabled", String.valueOf(enabled));
            props.setProperty("idle_amplitude", String.valueOf(idleAmp));
            props.setProperty("sprint_amplitude", String.valueOf(sprintAmp));
            props.setProperty("walk_amplitude", String.valueOf(walkAmp));
            props.setProperty("idle_speed", String.valueOf(idleSpeed));
            props.setProperty("sprint_speed", String.valueOf(sprintSpeed));
            props.setProperty("walk_speed", String.valueOf(walkSpeed));
            
            try (OutputStream out = Files.newOutputStream(PATH)) { props.store(out, null); }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
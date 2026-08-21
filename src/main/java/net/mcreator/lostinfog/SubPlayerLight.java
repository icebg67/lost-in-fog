package net.mcreator.lostinfog.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@EventBusSubscriber(modid = "lostinfog", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class SubPlayerLight {

    private static Object light = null;
    private static Object lightRenderer = null;
    private static boolean initialized = false;
    private static boolean veilAvailable = true;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            if (light != null && lightRenderer != null) {
                invokeAny(lightRenderer, new String[]{"removeLight", "remove"}, light);
                light = null;
            }
            return;
        }

        if (!veilAvailable) return;

        if (!initialized) {
            initVeil();
        }

        if (!veilAvailable) return;

        try {
            if (light == null) {
                Class<?> pointLightClass = findClass(new String[]{
                    "foundry.veil.api.client.render.light.PointLight",
                    "foundry.veil.api.client.render.light.type.PointLight",
                    "foundry.veil.client.render.light.PointLight"
                });

                if (pointLightClass == null) {
                    veilAvailable = false;
                    return;
                }

                Constructor<?> ctor = pointLightClass.getConstructor();
                light = ctor.newInstance();

                setLightColor(light, 1.0f, 0.9f, 0.8f);
                setLightRadius(light, 3.0f);
                setLightBrightness(light, 0.15f);

                invokeAny(lightRenderer, new String[]{"addLight", "add"}, light);
            }

            setLightPos(light, player.getX(), player.getY() - 10.0D, player.getZ());
        } catch (Exception e) {
            veilAvailable = false;
        }
    }

    private static void setLightPos(Object lightObj, double x, double y, double z) {
        if (invokeAny(lightObj, new String[]{"setPos", "setPosition"}, x, y, z)) return;
        if (invokeAny(lightObj, new String[]{"setPos", "setPosition"}, (float) x, (float) y, (float) z)) return;
        try {
            Class<?> vec3f = Class.forName("org.joml.Vector3f");
            Constructor<?> ctor = vec3f.getConstructor(float.class, float.class, float.class);
            Object vec = ctor.newInstance((float) x, (float) y, (float) z);
            invokeAny(lightObj, new String[]{"setPos", "setPosition"}, vec);
        } catch (Exception ignored) {}
    }

    private static void setLightColor(Object lightObj, float r, float g, float b) {
        if (invokeAny(lightObj, new String[]{"setColor", "color"}, r, g, b)) return;
        try {
            Class<?> vec3f = Class.forName("org.joml.Vector3f");
            Constructor<?> ctor = vec3f.getConstructor(float.class, float.class, float.class);
            Object vec = ctor.newInstance(r, g, b);
            invokeAny(lightObj, new String[]{"setColor", "color"}, vec);
        } catch (Exception ignored) {}
    }

    private static void setLightRadius(Object lightObj, float radius) {
        invokeAny(lightObj, new String[]{"setRadius", "radius"}, radius);
    }

    private static void setLightBrightness(Object lightObj, float brightness) {
        invokeAny(lightObj, new String[]{"setBrightness", "brightness"}, brightness);
    }

    private static void initVeil() {
        try {
            Class<?> veilRenderSystem = Class.forName("foundry.veil.api.client.render.VeilRenderSystem");
            Method rendererMethod = veilRenderSystem.getMethod("renderer");
            Object renderer = rendererMethod.invoke(null);

            if (renderer == null) return;

            Method getLightRenderer = renderer.getClass().getMethod("getLightRenderer");
            lightRenderer = getLightRenderer.invoke(renderer);

            if (lightRenderer != null) {
                initialized = true;
            } else {
                veilAvailable = false;
            }
        } catch (Exception e) {
            veilAvailable = false;
        }
    }

    private static Class<?> findClass(String[] classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    private static boolean invokeAny(Object target, String[] methodNames, Object... args) {
        if (target == null) return false;
        Class<?> clazz = target.getClass();
        for (String name : methodNames) {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                    try {
                        m.invoke(target, args);
                        return true;
                    } catch (Exception ignored) {}
                }
            }
        }
        return false;
    }
}
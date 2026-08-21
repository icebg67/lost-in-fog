package net.mcreator.lostinfog;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import java.util.function.Supplier;

@EventBusSubscriber(modid = "lostinfog", bus = EventBusSubscriber.Bus.MOD)
public class LostinfogModAttachments {
    public static final AttachmentType<FrostbiteData> FROSTBITE_TYPE = AttachmentType.serializable(FrostbiteData::new).build();
    public static final AttachmentType<CampfireData> CAMPFIRES_TYPE = AttachmentType.serializable(CampfireData::new).build();

    public static final Supplier<AttachmentType<FrostbiteData>> FROSTBITE = () -> FROSTBITE_TYPE;
    public static final Supplier<AttachmentType<CampfireData>> CAMPFIRES = () -> CAMPFIRES_TYPE;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, helper -> {
            helper.register(ResourceLocation.fromNamespaceAndPath("lostinfog", "frostbite"), FROSTBITE_TYPE);
            helper.register(ResourceLocation.fromNamespaceAndPath("lostinfog", "campfires"), CAMPFIRES_TYPE);
        });
    }
}
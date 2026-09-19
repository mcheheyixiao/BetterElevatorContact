package org.stellarvan.betterelevatorcontact.client;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.stellarvan.betterelevatorcontact.Betterelevatorcontact;
import org.stellarvan.betterelevatorcontact.ClientConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Per-screen translations: honours resource packs without replacing Minecraft's global language. */
public final class ContactLanguage {
    private final boolean automatic;
    private final Map<String, String> translations = new HashMap<>();

    public ContactLanguage() {
        String selected = ClientConfig.LANGUAGE.get();
        automatic = "auto".equals(selected);
        if (!automatic) {
            load("create", "en_us");
            load(Betterelevatorcontact.MODID, "en_us");
            if (!"en_us".equals(selected)) {
                load("create", selected);
                load(Betterelevatorcontact.MODID, selected);
            }
        }
    }

    private void load(String namespace, String language) {
        var location = Objects.requireNonNull(ResourceLocation.tryBuild(namespace, "lang/" + language + ".json"));
        for (var resource : Minecraft.getInstance().getResourceManager().getResourceStack(location)) {
            try (var reader = resource.openAsReader()) {
                JsonParser.parseReader(reader).getAsJsonObject().entrySet().forEach(entry -> {
                    if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString())
                        translations.put(entry.getKey(), entry.getValue().getAsString());
                });
            } catch (IOException | RuntimeException exception) {
                LogUtils.getLogger().warn("Could not read contact UI language {}", location, exception);
            }
        }
    }

    public MutableComponent text(String key, Object... arguments) {
        if (automatic)
            return Component.translatable(key, arguments);
        String pattern = translations.getOrDefault(key, key);
        Object[] values = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++)
            values[i] = arguments[i] instanceof Component component ? component.getString() : arguments[i];
        try {
            return Component.literal(String.format(Locale.ROOT, pattern, values));
        } catch (java.util.IllegalFormatException exception) {
            return Component.literal(pattern);
        }
    }
}

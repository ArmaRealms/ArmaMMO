package com.gmail.nossr50.util.text;

import static com.gmail.nossr50.util.text.StringUtils.getCapitalized;
import com.gmail.nossr50.datatypes.party.PartyFeature;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Utility class for String operations, including formatting and caching deterministic results to
 * improve performance.
 */
public class ConfigStringUtils {
    public static final String UNDERSCORE = "_";
    public static final String SPACE = " ";

    // Using concurrent hash maps to avoid concurrency issues (Folia)
    private static final Map<EntityType, String> configEntityStrings = new ConcurrentHashMap<>();
    private static final Map<Material, String> configMaterialStrings = new ConcurrentHashMap<>();
    private static final Map<PartyFeature, String> configPartyFeatureStrings = new ConcurrentHashMap<>();
    private static final Function<String, String> CONFIG_FRIENDLY_STRING_FORMATTER = baseString -> {
        if (baseString.contains(UNDERSCORE) && !baseString.contains(SPACE)) {
            return asConfigFormat(baseString.split(UNDERSCORE));
        } else {
            if (baseString.contains(SPACE)) {
                return asConfigFormat(baseString.split(SPACE));
            } else {
                return getCapitalized(baseString);
            }
        }
    };

    public static String getMaterialConfigString(final Material material) {
        return configMaterialStrings.computeIfAbsent(material,
                ConfigStringUtils::createConfigFriendlyString);
    }

    public static String getConfigEntityTypeString(final EntityType entityType) {
        return configEntityStrings.computeIfAbsent(entityType,
                ConfigStringUtils::createConfigFriendlyString);
    }

    public static String getConfigPartyFeatureString(final PartyFeature partyFeature) {
        return configPartyFeatureStrings.computeIfAbsent(partyFeature,
                // For whatever dumb reason, party feature enums got formatted like this...
                pf -> createConfigFriendlyString(pf.name()).replace(UNDERSCORE, ""));
    }

    private static String createConfigFriendlyString(final String baseString) {
        return CONFIG_FRIENDLY_STRING_FORMATTER.apply(baseString);
    }

    private static @NotNull String asConfigFormat(final String[] substrings) {
        final StringBuilder configString = new StringBuilder();

        for (int i = 0; i < substrings.length; i++) {
            configString.append(getCapitalized(substrings[i]));
            if (i < substrings.length - 1) {
                configString.append(UNDERSCORE);
            }
        }

        return configString.toString();
    }

    private static String createConfigFriendlyString(final Object object) {
        return createConfigFriendlyString(object.toString());
    }
}

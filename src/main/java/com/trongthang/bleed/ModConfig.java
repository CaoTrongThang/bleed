package com.trongthang.bleed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.joml.Vector3f;

public class ModConfig {
    private static final String CONFIG_FILE_NAME = "bleed_config.json";
    private static ModConfig INSTANCE;

    @Expose
    @SerializedName("modEnable")
    public boolean modEnable = true;

    @Expose
    @SerializedName("minDamageToGetBleed")
    public int minDamageToGetBleed = 1;

    @Expose
    @SerializedName("bleedingDamage")
    public float bleedingDamage = 2.0F;

    @Expose
    @SerializedName("bleedingDamageScaleWithHostileMobsHealth")
    public boolean bleedingDamageScaleWithHostileMobsHealth = true;

    @Expose
    @SerializedName("bleedingDamageScaleByPercentOfHostileMobsHealth")
    public float bleedingDamageScaleByPercentOfHostileMobsHealth = 1;

    @Expose
    @SerializedName("maxBleedingDamageForHostileMobs")
    public float maxBleedingDamageForHostileMobs = 20F;

    @Expose
    @SerializedName("bleedingDamageDecreaseByArmor")
    public boolean bleedingDamageDecreaseByArmor = false;

    @Expose
    @SerializedName("doesSneakingReduceBleedingDamage")
    public boolean doesSneakingReduceBleedingDamage = true;

    @Expose
    @SerializedName("bleedingDamageReducedWhileSneaking")
    public float bleedingDamageReducedWhileSneaking = 0.5f;

    @Expose
    @SerializedName("bleedingChance")
    public float bleedingChance = 0.5F;

    @Expose
    @SerializedName("bleedingChanceDecreaseByArmor")
    public boolean bleedingChanceDecreaseByArmor = true;

    @Expose
    @SerializedName("bleedingDurationInTick")
    public int bleedingDurationInTick = 200;

    @Expose
    @SerializedName("bleedingCheckInterval")
    public int bleedingCheckInterval = 40;

    @Expose
    @SerializedName("bandageConsumeTimeInTick")
    public int bandageConsumeTimeInTick = 15;

    @Expose
    @SerializedName("healingNeedleConsumeTimeInTick")
    public int healingNeedleConsumeTimeInTick = 40;

    @Expose
    @SerializedName("blacklistMobs")
    public HashSet<String> blacklistMobs = new HashSet<>(Arrays.asList(
            "minecraft:iron_golem",
            "minecraft:skeleton",
            "caligo:strow"));

    @Expose
    @SerializedName("customBloodColors")
    public HashMap<String, String> customBloodColors = createDefaultColors();

    @Expose
    @SerializedName("defaultBloodColor")
    public String defaultBloodColor = "red";

    private transient Map<String, Vector3f> colorCache = new HashMap<>();
    private transient Vector3f defaultColorCache = new Vector3f(1.0f, 0.0f, 0.0f);

    private static HashMap<String, String> createDefaultColors() {
        HashMap<String, String> map = new HashMap<>();
        map.put("minecraft:zombie", "#691010");
        map.put("minecraft:skeleton", "#ffffff");
        map.put("minecraft:creeper", "#13942b");
        map.put("minecraft:spider", "#6e1199");
        map.put("minecraft:piglin", "#d97f21");
        map.put("minecraft:slime", "#13942b");
        return map;
    }

    public void updateCache() {
        colorCache.clear();
        customBloodColors.forEach((id, color) -> colorCache.put(id, parseColor(color)));
        defaultColorCache = parseColor(defaultBloodColor);
    }

    public Vector3f getEntityBloodColor(String entityId) {
        return colorCache.getOrDefault(entityId, defaultColorCache);
    }

    private static Vector3f parseColor(String colorStr) {
        if (colorStr == null)
            return new Vector3f(1.0f, 0.0f, 0.0f);
        colorStr = colorStr.trim().toLowerCase();

        if (colorStr.startsWith("#") && colorStr.length() >= 7) {
            try {
                int r = Integer.parseInt(colorStr.substring(1, 3), 16);
                int g = Integer.parseInt(colorStr.substring(3, 5), 16);
                int b = Integer.parseInt(colorStr.substring(5, 7), 16);
                return new Vector3f(r / 255.0f, g / 255.0f, b / 255.0f);
            } catch (NumberFormatException ignored) {
            }
        }
        return new Vector3f(1.0f, 0.0f, 0.0f);
    }

    public static void loadConfig() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), CONFIG_FILE_NAME);
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                INSTANCE = gson.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig(); // Fallback to default if JSON was empty or malformed
                }
                INSTANCE.updateCache();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            INSTANCE = new ModConfig();
            INSTANCE.updateCache();
        }

        saveConfig(gson, configFile); // Save current config, including defaults if they were missing
    }

    private static void saveConfig(Gson gson, File configFile) {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ModConfig getInstance() {
        return INSTANCE;
    }
}

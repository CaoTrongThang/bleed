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
import java.util.HashSet;

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
    public boolean bleedingDamageScaleForHostielMobs = true;

    @Expose
    @SerializedName("bleedingDamageScaleByPercentOfHostileMobsHealth")
    public float bleedingDamageScaleByPercentOfHostileMobs = 1;

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
    @SerializedName("bandageConsumeTimeInTick")
    public int healingNeedleConsumeTimeInTick = 40;

    @Expose
    @SerializedName("blacklistMobs")
    public HashSet<String> blacklistMobs = new HashSet<>(Arrays.asList(
            "minecraft:iron_golem",
            "minecraft:skeleton",
            "caligo:strow"
    ));

    public static void loadConfig() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), CONFIG_FILE_NAME);
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                INSTANCE = gson.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig(); // Fallback to default if JSON was empty or malformed
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            INSTANCE = new ModConfig();
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

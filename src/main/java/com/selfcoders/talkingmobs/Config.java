package com.selfcoders.talkingmobs;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

class Config {
    private final TalkingMobs plugin;
    private final File file;
    private YamlConfiguration config;

    Config(TalkingMobs plugin, String filename) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), filename);

        reload();
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);

        InputStream defaultConfigStream = this.plugin.getResource(this.file.getName());
        if (defaultConfigStream != null) {
            this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, Charsets.UTF_8)));

            if (!this.file.exists()) {
                this.plugin.saveResource(this.file.getName(), false);
            }
        }
    }

    public void save() {
        try {
            this.getConfig().save(this.file);
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.file, exception);
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
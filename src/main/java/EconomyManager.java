package com.example.economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final EconomyPlugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public EconomyManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "balances.yml");
        loadAll();
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, getStartingBalance());
    }

    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
    }

    public boolean deposit(UUID uuid, double amount) {
        if (amount <= 0) return false;
        balances.put(uuid, getBalance(uuid) + amount);
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(uuid);
        if (current < amount) return false;
        balances.put(uuid, current - amount);
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public String getCurrencyName() {
        return plugin.getConfig().getString("currency-name", "Coins");
    }

    public String format(double amount) {
        return String.format("%.2f %s", amount, getCurrencyName());
    }

    private double getStartingBalance() {
        return plugin.getConfig().getDouble("starting-balance", 100.0);
    }

    public void loadAll() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create balances.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains("balances")) {
            for (String key : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double balance = dataConfig.getDouble("balances." + key);
                    balances.put(uuid, balance);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("Loaded " + balances.size() + " player balance(s).");
    }

    public void saveAll() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            dataConfig.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save balances.yml: " + e.getMessage());
        }
    }
}

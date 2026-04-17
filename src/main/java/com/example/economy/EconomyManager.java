package com.example.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
    private final Economy externalEconomy; // null if using internal storage

    // Internal storage (used only if no external economy)
    private final Map<UUID, Double> balances = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    // Constructor for internal storage
    public EconomyManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        this.externalEconomy = null;
        this.dataFile = new File(plugin.getDataFolder(), "balances.yml");
        loadAll();
    }

    // Constructor for external Vault economy (e.g. EssentialsX)
    public EconomyManager(EconomyPlugin plugin, Economy externalEconomy) {
        this.plugin = plugin;
        this.externalEconomy = externalEconomy;
        this.dataFile = null;
        this.dataConfig = null;
        plugin.getLogger().info("Using " + externalEconomy.getName() + " as economy backend.");
    }

    // ── Balance methods ───────────────────────────────────────────

    public double getBalance(UUID uuid) {
        if (externalEconomy != null) {
            return externalEconomy.getBalance(Bukkit.getOfflinePlayer(uuid));
        }
        return balances.getOrDefault(uuid, getStartingBalance());
    }

    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public void setBalance(UUID uuid, double amount) {
        if (externalEconomy != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            double current = externalEconomy.getBalance(player);
            if (amount > current) {
                externalEconomy.depositPlayer(player, amount - current);
            } else {
                externalEconomy.withdrawPlayer(player, current - amount);
            }
            return;
        }
        balances.put(uuid, Math.max(0, amount));
    }

    public boolean deposit(UUID uuid, double amount) {
        if (amount <= 0) return false;
        if (externalEconomy != null) {
            externalEconomy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
            return true;
        }
        double max = plugin.getConfig().getDouble("max-balance", -1);
        double newBal = getBalance(uuid) + amount;
        if (max >= 0 && newBal > max) newBal = max;
        balances.put(uuid, newBal);
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        if (externalEconomy != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (!externalEconomy.has(player, amount)) return false;
            externalEconomy.withdrawPlayer(player, amount);
            return true;
        }
        double current = getBalance(uuid);
        if (current < amount) return false;
        balances.put(uuid, current - amount);
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    // ── Formatting ────────────────────────────────────────────────

    public String getCurrencyName() {
        if (externalEconomy != null) return externalEconomy.currencyNamePlural();
        return plugin.getConfig().getString("currency-name", "Dollars");
    }

    public String getCurrencySymbol() {
        return plugin.getConfig().getString("currency-symbol", "$");
    }

    public String format(double amount) {
        if (externalEconomy != null) return externalEconomy.format(amount);
        int decimals = plugin.getConfig().getInt("currency-decimals", 2);
        String symbol = getCurrencySymbol();
        return symbol + String.format("%." + decimals + "f", amount);
    }

    public double getMaxBalance() {
        return plugin.getConfig().getDouble("max-balance", -1);
    }

    public double getMinPay() {
        return plugin.getConfig().getDouble("pay.min-amount", 1.0);
    }

    public double getMaxPay() {
        return plugin.getConfig().getDouble("pay.max-amount", -1);
    }

    public String getMessage(String key) {
        String msg = plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        return msg.replace("&", "§");
    }

    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    private double getStartingBalance() {
        return plugin.getConfig().getDouble("starting-balance", 100.0);
    }

    // ── Persistence (internal only) ───────────────────────────────

    public void loadAll() {
        if (dataFile == null) return;
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Could not create balances.yml: " + e.getMessage()); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains("balances")) {
            for (String key : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                try {
                    balances.put(UUID.fromString(key), dataConfig.getDouble("balances." + key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("Loaded " + balances.size() + " player balance(s).");
    }

    public void saveAll() {
        if (dataFile == null || externalEconomy != null) return;
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            dataConfig.set("balances." + entry.getKey(), entry.getValue());
        }
        try { dataConfig.save(dataFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save balances.yml: " + e.getMessage()); }
    }
}

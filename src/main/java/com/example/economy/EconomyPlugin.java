package com.example.economy;

import com.example.economy.commands.BalanceCommand;
import com.example.economy.commands.EcoCommand;
import com.example.economy.commands.PayCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyPlugin extends JavaPlugin {

    private EconomyManager economyManager;
    private boolean usingExternalEconomy = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Check if Vault is installed
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found! EconomyPlugin will use internal storage only.");
            economyManager = new EconomyManager(this);
            setupCommands();
            startPassiveIncome();
            return;
        }

        // Check if EssentialsX or another economy is already registered
        RegisteredServiceProvider<Economy> existing =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (existing != null && !existing.getProvider().getName().equals("EconomyPlugin")) {
            // Another economy plugin (e.g. EssentialsX) is already registered — use it!
            getLogger().info("Found existing economy: " + existing.getProvider().getName() +
                    " — EconomyPlugin will sync with it via Vault!");
            economyManager = new EconomyManager(this, existing.getProvider());
            usingExternalEconomy = true;
        } else {
            // No other economy found — register ourselves as the Vault economy
            economyManager = new EconomyManager(this);
            VaultEconomyProvider provider = new VaultEconomyProvider(this, economyManager);
            Bukkit.getServicesManager().register(Economy.class, provider, this, ServicePriority.Normal);
            getLogger().info("Registered EconomyPlugin as Vault economy provider!");
        }

        setupCommands();
        startPassiveIncome();
        getLogger().info("EconomyPlugin enabled!");
    }

    private void setupCommands() {
        getCommand("balance").setExecutor(new BalanceCommand(economyManager));
        getCommand("pay").setExecutor(new PayCommand(economyManager));
        getCommand("eco").setExecutor(new EcoCommand(economyManager));
    }

    private void startPassiveIncome() {
        if (!getConfig().getBoolean("passive-income.enabled", true)) return;

        double reward = getConfig().getDouble("passive-income.amount", 100.0);
        double minutes = getConfig().getDouble("passive-income.interval-minutes", 5.0);
        long intervalTicks = (long) (minutes * 60 * 20);
        boolean notify = getConfig().getBoolean("passive-income.notify-player", true);
        String rawMsg = getConfig().getString("passive-income.notify-message",
                "&a[Economy] &rYou received &f{amount}&a as passive income!");

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                economyManager.deposit(player.getUniqueId(), reward);
                if (notify) {
                    String msg = rawMsg
                            .replace("{amount}", economyManager.format(reward))
                            .replace("&", "§");
                    player.sendMessage(msg);
                }
            }
        }, intervalTicks, intervalTicks);
    }

    @Override
    public void onDisable() {
        if (economyManager != null && !usingExternalEconomy) {
            economyManager.saveAll();
        }
        Bukkit.getServicesManager().unregisterAll(this);
        getLogger().info("EconomyPlugin disabled.");
    }

    public EconomyManager getEconomyManager() { return economyManager; }
    public boolean isUsingExternalEconomy() { return usingExternalEconomy; }
}

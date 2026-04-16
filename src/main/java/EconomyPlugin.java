package com.example.economy;

import com.example.economy.commands.BalanceCommand;
import com.example.economy.commands.EcoCommand;
import com.example.economy.commands.PayCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyPlugin extends JavaPlugin {

    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        economyManager = new EconomyManager(this);

        getCommand("balance").setExecutor(new BalanceCommand(economyManager));
        getCommand("pay").setExecutor(new PayCommand(economyManager));
        getCommand("eco").setExecutor(new EcoCommand(economyManager));

        startPassiveIncome();

        getLogger().info("EconomyPlugin enabled!");
    }

    private void startPassiveIncome() {
        double reward = getConfig().getDouble("passive-income.amount", 100.0);
        // 5 minutes = 5 * 60 * 20 = 6000 ticks
        long intervalTicks = getConfig().getLong("passive-income.interval-ticks", 6000L);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                economyManager.deposit(player.getUniqueId(), reward);
                player.sendMessage("§a§l[Economy] §r§aYou received §f"
                        + economyManager.format(reward)
                        + " §aas passive income!");
            }
        }, intervalTicks, intervalTicks);
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.saveAll();
        }
        getLogger().info("EconomyPlugin disabled. Data saved.");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}

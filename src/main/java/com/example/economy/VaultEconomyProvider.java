package com.example.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

public class VaultEconomyProvider implements Economy {

    private final EconomyPlugin plugin;
    private final EconomyManager economyManager;

    public VaultEconomyProvider(EconomyPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @Override
    public boolean isEnabled() { return plugin.isEnabled(); }

    @Override
    public String getName() { return "EconomyPlugin"; }

    @Override
    public boolean hasBankSupport() { return false; }

    @Override
    public int fractionalDigits() { return 2; }

    @Override
    public String format(double amount) { return economyManager.format(amount); }

    @Override
    public String currencyNamePlural() { return economyManager.getCurrencyName(); }

    @Override
    public String currencyNameSingular() { return economyManager.getCurrencyName(); }

    @Override
    public boolean hasAccount(OfflinePlayer player) { return true; }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) { return true; }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economyManager.getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return economyManager.has(player.getUniqueId(), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, getBalance(player),
                EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount.");
        boolean success = economyManager.withdraw(player.getUniqueId(), amount);
        if (success) {
            return new EconomyResponse(amount, getBalance(player),
                    EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(0, getBalance(player),
                    EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, getBalance(player),
                EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount.");
        economyManager.deposit(player.getUniqueId(), amount);
        return new EconomyResponse(amount, getBalance(player),
                EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported.");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported.");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported.");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported.");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported.");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported.");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported.");
    }

    @Override
    public List<String> getBanks() { return new ArrayList<>(); }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) { return true; }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return true; }

    // Deprecated string-based methods — delegate to OfflinePlayer versions
    @Override @Deprecated
    public boolean hasAccount(String playerName) { return true; }
    @Override @Deprecated
    public boolean hasAccount(String playerName, String worldName) { return true; }
    @Override @Deprecated
    public double getBalance(String playerName) { return 0; }
    @Override @Deprecated
    public double getBalance(String playerName, String world) { return 0; }
    @Override @Deprecated
    public boolean has(String playerName, double amount) { return false; }
    @Override @Deprecated
    public boolean has(String playerName, String worldName, double amount) { return false; }
    @Override @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Use OfflinePlayer method.");
    }
    @Override @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }
    @Override @Deprecated
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Use OfflinePlayer method.");
    }
    @Override @Deprecated
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }
    @Override @Deprecated
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }
    @Override @Deprecated
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }
    @Override @Deprecated
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }
    @Override @Deprecated
    public boolean createPlayerAccount(String playerName) { return true; }
    @Override @Deprecated
    public boolean createPlayerAccount(String playerName, String worldName) { return true; }
}

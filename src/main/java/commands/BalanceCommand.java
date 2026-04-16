package com.example.economy.commands;

import com.example.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final EconomyManager economy;

    public BalanceCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cConsole must specify a player: /balance <player>");
                return true;
            }
            sender.sendMessage("§aYour balance: §f" + economy.format(economy.getBalance(player.getUniqueId())));
        } else {
            // Check another player's balance
            if (!sender.hasPermission("economy.balance.others")) {
                sender.sendMessage("§cYou don't have permission to check others' balances.");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage("§cPlayer §f" + args[0] + " §cnot found.");
                return true;
            }
            sender.sendMessage("§f" + target.getName() + "§a's balance: §f" + economy.format(economy.getBalance(target.getUniqueId())));
        }
        return true;
    }
}

package com.example.economy.commands;

import com.example.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EcoCommand implements CommandExecutor {

    private final EconomyManager economy;

    public EcoCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("economy.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eco <give|take|set> <player> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer §f" + args[1] + " §cnot found.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: §f" + args[2]);
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("§cAmount must be greater than 0.");
            return true;
        }

        switch (action) {
            case "give" -> {
                economy.deposit(target.getUniqueId(), amount);
                sender.sendMessage("§aGave §f" + economy.format(amount) + " §ato §f" + target.getName() + "§a.");
                if (target.isOnline()) {
                    target.getPlayer().sendMessage("§aAn admin gave you §f" + economy.format(amount) + "§a.");
                }
            }
            case "take" -> {
                boolean success = economy.withdraw(target.getUniqueId(), amount);
                if (!success) {
                    sender.sendMessage("§f" + target.getName() + " §cdoesn't have enough money.");
                } else {
                    sender.sendMessage("§aTook §f" + economy.format(amount) + " §afrom §f" + target.getName() + "§a.");
                    if (target.isOnline()) {
                        target.getPlayer().sendMessage("§cAn admin took §f" + economy.format(amount) + " §cfrom you.");
                    }
                }
            }
            case "set" -> {
                economy.setBalance(target.getUniqueId(), amount);
                sender.sendMessage("§aSet §f" + target.getName() + "§a's balance to §f" + economy.format(amount) + "§a.");
                if (target.isOnline()) {
                    target.getPlayer().sendMessage("§aYour balance was set to §f" + economy.format(amount) + " §aby an admin.");
                }
            }
            default -> sender.sendMessage("§cUnknown action. Use: give, take, or set.");
        }

        return true;
    }
}

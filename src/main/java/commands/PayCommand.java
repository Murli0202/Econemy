package com.example.economy.commands;

import com.example.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final EconomyManager economy;

    public PayCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player payer)) {
            sender.sendMessage("§cOnly players can use /pay.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1].equals(args[1]) ? args[0] : args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer §f" + args[0] + " §cis not online.");
            return true;
        }

        if (target.equals(payer)) {
            sender.sendMessage("§cYou can't pay yourself.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: §f" + args[1]);
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("§cAmount must be greater than 0.");
            return true;
        }

        if (!economy.withdraw(payer.getUniqueId(), amount)) {
            sender.sendMessage("§cYou don't have enough money. Your balance: §f"
                    + economy.format(economy.getBalance(payer.getUniqueId())));
            return true;
        }

        economy.deposit(target.getUniqueId(), amount);

        payer.sendMessage("§aYou paid §f" + target.getName() + " §f" + economy.format(amount) + "§a.");
        target.sendMessage("§aYou received §f" + economy.format(amount) + " §afrom §f" + payer.getName() + "§a.");
        return true;
    }
}

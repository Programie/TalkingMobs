package com.selfcoders.talkingmobs;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TalkingMobs extends JavaPlugin {
    private final Message message = new Message(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new EventListener(message, this), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("talkingmobs")) {
            return false;
        }

        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0];

        switch (subCommand) {
            case "help":
                printHelp(sender);
                return true;
            case "reload":
                if (sender.hasPermission(Permission.RELOAD.permission())) {
                    sender.sendMessage("Reloading configuration...");
                    reloadConfig();
                    sender.sendMessage("Done");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have the required permissions for this command!");
                }
                return true;
            case "toggle":
                toggle(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            case "version":
                PluginDescriptionFile description = getDescription();

                sender.sendMessage(ChatColor.GOLD + description.getName() + " " + ChatColor.RED + description.getVersion());
                sender.sendMessage(ChatColor.DARK_GREEN + "by Programie");
                sender.sendMessage(ChatColor.DARK_GREEN + description.getWebsite());
                return true;
        }

        return false;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        message.reloadConfig();
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "----------" + ChatColor.WHITE + " Subcommands " + ChatColor.YELLOW + "----------");

        if (sender.hasPermission(Permission.RELOAD.permission())) {
            sender.sendMessage(ChatColor.GOLD + "/talkingmobs reload: " + ChatColor.WHITE + "Reload the configuration");
        }

        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.GOLD + "/talkingmobs toggle: " + ChatColor.WHITE + "Toggle messages sent by mob");
        }

        sender.sendMessage(ChatColor.GOLD + "/talkingmobs version: " + ChatColor.WHITE + " Show the version of this plugin");
    }

    private void toggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage("Usage: " + ChatColor.GOLD + "/talkingmobs toggle <type>");
            sender.sendMessage("");
            sender.sendMessage(messageTypesList());

            return;
        }

        String type = args[0];

        if (type.equalsIgnoreCase("all")) {
            if (message.isEnabled(player)) {
                message.setEnabled(player, false);
                sender.sendMessage(ChatColor.GREEN + "Mob messages disabled");
            } else {
                message.setEnabled(player, true);
                sender.sendMessage(ChatColor.GREEN + "Mob messages enabled");
            }

            return;
        }

        Message.EventType eventType;

        try {
            eventType = Message.EventType.fromString(type);
        } catch (IllegalArgumentException exception) {
            return;
        }

        if (eventType == null || !eventType.isToggleable()) {
            sender.sendMessage(ChatColor.RED + "Invalid type: " + type);
            return;
        }

        if (message.isEnabled(player, eventType)) {
            message.setEnabled(player, eventType, false);
            sender.sendMessage(ChatColor.GREEN + "Mob messages for type '" + ChatColor.BLUE + type + ChatColor.GREEN + "' disabled");
        } else {
            message.setEnabled(player, eventType, true);
            sender.sendMessage(ChatColor.GREEN + "Mob messages for type '" + ChatColor.BLUE + type + ChatColor.GREEN + "' enabled");
        }
    }

    private String messageTypesList() {
        List<String> eventTypes = new ArrayList<>();

        eventTypes.add("all");

        for (Message.EventType eventType : Message.EventType.values()) {
            if (!eventType.isToggleable()) {
                continue;
            }

            eventTypes.add(eventType.getType());
        }

        return "Message types: " + ChatColor.DARK_GREEN + StringUtils.join(eventTypes, ChatColor.WHITE + ", " + ChatColor.DARK_GREEN);
    }
}
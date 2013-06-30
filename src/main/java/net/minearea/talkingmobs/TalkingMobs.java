package net.minearea.talkingmobs;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TalkingMobs extends JavaPlugin
{
	private final Message message = new Message(this);

	@Override
	public void onEnable()
	{
		this.saveDefaultConfig();

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new EventListener(this, message), this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (command.getName().equalsIgnoreCase("talkingmobs"))
		{
			if (args.length >= 1)
			{
				switch (args[0])
				{
					case "help":// TODO: Messages are in wrong order
						if (sender.hasPermission("talkingmobs.reload"))
						{
							sender.sendMessage("/talkingmobs reload - Reload the configuration");
						}

						List<String> eventTypes = new ArrayList<>();
						for (Message.EventType eventType: Message.EventType.values())
						{
							eventTypes.add(eventType.toString());
						}

						sender.sendMessage("/talkingmobs toggle <type> - Toggle messages send by mobs (Type is optional and can be used to only toggle the specified message type)");
						sender.sendMessage("");
						sender.sendMessage("Message types: " + StringUtils.join(eventTypes, ", "));

						return true;
					case "reload":
						if (sender.hasPermission("talkingmobs.reload"))
						{
							sender.sendMessage("Reloading configuration...");
							this.reloadConfig();
							sender.sendMessage("Done");
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "You do not have the required permissions for this command!");
						}
						return true;
					case "toggle":
						if (sender instanceof Player)
						{
							Player player = (Player) sender;

							if (args.length >= 2)
							{
								try
								{
									Message.EventType eventType = Message.EventType.valueOf(args[1]);

									if (message.isEnabled(player, eventType))
									{
										message.setEnabled(player, eventType, false);
										sender.sendMessage(ChatColor.GREEN + "Mob messages for type '" + ChatColor.BLUE + args[1] + ChatColor.GREEN + "' disabled");
									}
									else
									{
										message.setEnabled(player, eventType, true);
										sender.sendMessage(ChatColor.GREEN + "Mob messages for type '" + ChatColor.BLUE + args[1] + ChatColor.GREEN + "' enabled");
									}
								}
								catch (IllegalArgumentException exception)
								{
									sender.sendMessage(ChatColor.RED + "Invalid type: " + args[1]);
								}
							}
							else
							{
								if (message.isEnabled(player))
								{
									message.setEnabled(player, false);
									sender.sendMessage(ChatColor.GREEN + "Mob messages disabled");
								}
								else
								{
									message.setEnabled(player, true);
									sender.sendMessage(ChatColor.GREEN + "Mob messages enabled");
								}
							}
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
						}
						return true;
				}
			}
		}
		return false;
	}
}
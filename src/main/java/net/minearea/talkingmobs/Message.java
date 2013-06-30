package net.minearea.talkingmobs;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Class to send mob messages to players
 */
public class Message
{
	private final TalkingMobs plugin;

	/**
	 * Possible event types
	 */
	public enum EventType
	{
		/**
		 * Mob is alive (Currently unused)
		 */
		alive,
		/**
		 * Mob has been attacked by a player
		 */
		attacked,
		/**
		 * Player interacted with the mob (Currently unused)
		 */
		interacted,
		/**
		 * Mob has been killed by a player
		 */
		killed,
		/**
		 * Mob has been spawned
		 */
		spawn
	}

	/**
	 * Initialize the message class
	 * @param pluginInstance The instance of this plugin ('this' in TalkingMobs class)
	 */
	public Message(TalkingMobs pluginInstance)
	{
		plugin = pluginInstance;
	}

	private String getMessage(Entity mob, EventType eventType)
	{
		List<String> messages;
		messages = plugin.getConfig().getStringList("messages." + mob.getType().getName() + "." + eventType.toString());
		if (messages.size() > 0)
		{
			Random randomGenerator = new Random();

			String message = plugin.getConfig().getString("messageformat." + eventType.toString());

			if (message == null)
			{
				message = "[&a%mobname%&r] %message%";
				plugin.getLogger().log(Level.WARNING, "Message format for event type '{0}' not defined!", eventType.toString());
			}

			message = message.replaceAll("%message%", messages.get(randomGenerator.nextInt(messages.size())));
			message = message.replaceAll("%mobname%", mob.getType().getName());

			return message;
		}

		return null;
	}

	private boolean isAllowed(Player player)
	{
		return player.hasPermission("talkingmobs.receive");
	}

	/**
	 * Check if talking mobs of the specified message type is enabled for the specified player
	 * @param player The player for which the state should be checked
	 * @param eventType The message type which should be checked
	 * @return True if talking mobs of the specified type is enabled, false otherwise
	 */
	public boolean isEnabled(Player player, EventType eventType)
	{
		// Check if all has been disabled
		if (!isEnabled(player))
		{
			return false;
		}

		String path = "players." + player.getName() + ".enabled." + eventType.toString();

		return plugin.getConfig().getBoolean(path, true);
	}

	/**
	 * Check if talking mobs is enabled for the specified player
	 * @param player The player for which the state should be checked
	 * @return True if talking mobs is enabled, false otherwise
	 */
	public boolean isEnabled(Player player)
	{
		return plugin.getConfig().getBoolean("players." + player.getName() + ".enabled.all", true);
	}

	private void sendFormattedMessage(Player player, String message)
	{
		String formattedMessage = message.replaceAll("%player%", player.getName());
		formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);
		player.sendMessage(formattedMessage);
	}

	/**
	 * Send a mob message to the player
	 * @param mob The mob which sends the message
	 * @param player The player which should receive the message
	 * @param messageType The type of the message
	 */
	public void sendMessage(Entity mob, Player player, EventType eventType)
	{
		if (isAllowed(player) && isEnabled(player, eventType))
		{
			String message = getMessage(mob, eventType);
			if (message != null)
			{
				sendFormattedMessage(player, message);
			}
		}
	}

	/**
	 * Send a mob message to all players
	 * @param mob The mob which sends the message
	 * @param messageType The type of the message
	 */
	public void sendMessage(Entity mob, EventType eventType)
	{
		String message = getMessage(mob, eventType);
		if (message != null)
		{
			for (Player player: plugin.getServer().getOnlinePlayers())
			{
				if (isAllowed(player) && isEnabled(player, eventType))
				{
					sendFormattedMessage(player, message);
				}
			}
		}
	}

	/**
	 * Enable or disable talking mobs of the specified message type for the specified player
	 * @param player The player for which the state should be changed
	 * @param messageType The message type of which the state should be set
	 * @param state The new state
	 */
	public void setEnabled(Player player, EventType eventType, Boolean state)
	{
		plugin.getConfig().set("players." + player.getName() + ".enabled." + eventType.toString(), state);
		plugin.getConfig().set("players." + player.getName() + ".enabled.all", true);

		plugin.saveConfig();
	}

	/**
	 * Enable or disable talking mobs for the specified player
	 * @param player The player for which the state should be changed
	 * @param state The new state
	 */
	public void setEnabled(Player player, Boolean state)
	{
		plugin.getConfig().set("players." + player.getName() + ".enabled.all", state);

		for (EventType eventType: EventType.values())
		{
			plugin.getConfig().set("players." + player.getName() + ".enabled." + eventType.toString(), state);
		}

		plugin.saveConfig();
	}
}
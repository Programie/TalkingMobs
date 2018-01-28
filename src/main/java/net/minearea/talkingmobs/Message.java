package net.minearea.talkingmobs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * Class providing methods to send mob messages to players
 */
public class Message {
    private final TalkingMobs plugin;

    /**
     * Possible event types
     */
    public enum EventType {
        /**
         * Mob has been attacked by a player
         */
        attacked,
        /**
         * Mob is idle (Is just standing/walking/running around) -> Unused
         */
        idle,
        /**
         * Player interacted with the mob (Right clicked on mob)
         */
        interacted,
        /**
         * Mob has been killed by a player
         */
        killed,
        /**
         * Mob has been spawned (Currently only mob spawner or egg)
         */
        spawned
    }

    /**
     * Initialize the message class
     *
     * @param pluginInstance The instance of this plugin ('this' in TalkingMobs class)
     */
    Message(TalkingMobs pluginInstance) {
        plugin = pluginInstance;
    }

    /**
     * Get a random message for the specified mob and event type
     *
     * @param mob       The entity of the mob for which the message should be fetched
     * @param eventType The event type of the message
     * @return A string containing the message
     */
    private String getMessage(Entity mob, EventType eventType) {
        List<String> messages;
        messages = plugin.getConfig().getStringList("messages." + mob.getType().getName() + "." + eventType.toString());
        if (messages.size() > 0) {
            Random randomGenerator = new Random();

            String message = plugin.getConfig().getString("messageFormat." + eventType.toString());

            if (message == null) {
                message = "[&a%mobname%&r] %message%";
                plugin.getLogger().log(Level.INFO, "Message format for event type ''{0}'' not defined!", eventType.toString());
            }

            message = message.replaceAll("%message%", messages.get(randomGenerator.nextInt(messages.size())));
            message = message.replaceAll("%mobname%", mob.getType().getName());

            return message;
        } else {
            plugin.getLogger().log(Level.INFO, "No messages for event ''{0}'' of mob ''{1}'' defined!", new Object[]{eventType.toString(), mob.getType().getName()});
        }

        return null;
    }

    /**
     * Check whether the player has the permission to receive mob messages
     *
     * @param player The player which should be checked
     * @return True if the player has the permission, false otherwise
     */
    private boolean isAllowed(Player player) {
        return player.hasPermission("talkingmobs.receive");
    }

    /**
     * Check if talking mobs of the specified message type is enabled for the specified player
     *
     * @param player    The player for which the state should be checked
     * @param eventType The message type which should be checked
     * @return True if talking mobs of the specified type is enabled, false otherwise
     */
    public boolean isEnabled(Player player, EventType eventType) {
        // Check if all has been disabled
        if (!isEnabled(player)) {
            return false;
        }

        String path = "players." + player.getName() + ".enabled." + eventType.toString();

        return plugin.getConfig().getBoolean(path, true);
    }

    /**
     * Check if talking mobs is enabled for the specified player
     *
     * @param player The player for which the state should be checked
     * @return True if talking mobs is enabled, false otherwise
     */
    public boolean isEnabled(Player player) {
        return plugin.getConfig().getBoolean("players." + player.getName() + ".enabled.all", true);
    }

    /**
     * Format the message and send it to the player
     * Formatting includes replacing %player% with the name of the player and translating color codes.
     *
     * @param player  The player which should receive the message
     * @param message The message to send
     */
    private void sendFormattedMessage(Player player, String message) {
        String formattedMessage = message.replaceAll("%player%", player.getName());
        formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);
        player.sendMessage(formattedMessage);
    }

    /**
     * Send a mob message to the player
     *
     * @param mob       The mob which sends the message
     * @param player    The player which should receive the message
     * @param eventType The event type
     */
    public void sendMessage(Entity mob, Player player, EventType eventType) {
        if (mob instanceof Player) {
            return;
        }

        if (!(mob instanceof LivingEntity)) {
            return;
        }

        double maxDistance = plugin.getConfig().getDouble("maxDistance");
        if (isAllowed(player) && isEnabled(player, eventType)) {
            String message = getMessage(mob, eventType);
            if (message != null) {
                if (maxDistance > 0) {
                    double distance = player.getLocation().distance(mob.getLocation());
                    if (distance != Double.NaN && distance <= maxDistance) {
                        sendFormattedMessage(player, message);
                    }
                } else {
                    sendFormattedMessage(player, message);
                }
            }
        }
    }

    /**
     * Send a mob message to all players
     *
     * @param mob       The mob which sends the message
     * @param eventType The event type
     */
    public void sendMessage(Entity mob, EventType eventType) {
        if (mob instanceof Player) {
            return;
        }

        if (!(mob instanceof LivingEntity)) {
            return;
        }

        double maxDistance = plugin.getConfig().getDouble("maxDistance");
        Location mobLocation = mob.getLocation();
        String message = getMessage(mob, eventType);
        if (message != null) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (isAllowed(player) && isEnabled(player, eventType)) {
                    if (maxDistance > 0) {
                        double distance = player.getLocation().distance(mobLocation);
                        if (distance != Double.NaN && distance <= maxDistance) {
                            sendFormattedMessage(player, message);
                        }
                    } else {
                        sendFormattedMessage(player, message);
                    }
                }
            }
        }
    }

    /**
     * Enable or disable talking mobs of the specified message type for the specified player
     *
     * @param player    The player for which the state should be changed
     * @param eventType The event type of which the state should be set
     * @param state     The new state
     */
    public void setEnabled(Player player, EventType eventType, Boolean state) {
        plugin.getConfig().set("players." + player.getName() + ".enabled." + eventType.toString(), state);
        plugin.getConfig().set("players." + player.getName() + ".enabled.all", true);

        plugin.saveConfig();
    }

    /**
     * Enable or disable talking mobs for the specified player
     *
     * @param player The player for which the state should be changed
     * @param state  The new state
     */
    public void setEnabled(Player player, Boolean state) {
        plugin.getConfig().set("players." + player.getName() + ".enabled.all", state);

        for (EventType eventType : EventType.values()) {
            plugin.getConfig().set("players." + player.getName() + ".enabled." + eventType.toString(), state);
        }

        plugin.saveConfig();
    }
}
package com.selfcoders.talkingmobs;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * Class providing methods to send mob messages to players
 */
public class Message {
    private final TalkingMobs plugin;
    private final Config messagesConfig;
    private final Config playersConfig;
    private HashMap<String, Long> lastMessage = new HashMap<>();

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
         * Mob is looking at the player (and player is looking at the mob)
         */
        looking,
        /**
         * Mob has been spawned (Currently only mob spawner or egg)
         */
        spawned,
        /**
         * Mob has been tamed
         */
        tamed
    }

    /**
     * Initialize the message class
     *
     * @param pluginInstance The instance of this plugin ('this' in TalkingMobs class)
     */
    Message(TalkingMobs pluginInstance) {
        plugin = pluginInstance;

        messagesConfig = new Config(plugin, "messages.yml");
        playersConfig = new Config(plugin, "players.yml");
    }

    /**
     * Reload the messages
     */
    public void reloadConfig() {
        messagesConfig.reload();
        playersConfig.reload();
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
        String mobTypeName = mob.getType().name().toLowerCase();

        messages = messagesConfig.getConfig().getStringList(mobTypeName + "." + eventType.toString());
        if (messages.size() == 0) {
            messages = messagesConfig.getConfig().getStringList("default." + eventType.toString());
        }

        if (messages.size() > 0) {
            Random randomGenerator = new Random();

            String message = plugin.getConfig().getString("messageFormat." + eventType.toString());

            if (message == null) {
                message = plugin.getConfig().getString("messageFormat.default");

                if (message == null) {
                    message = "[&a%mobname%&r] %message%";
                    plugin.getLogger().log(Level.INFO, "Message format for event type ''{0}'' not defined!", eventType.toString());
                }
            }

            message = message.replaceAll("%message%", messages.get(randomGenerator.nextInt(messages.size())));
            message = message.replaceAll("%mobname%", mobTypeName);
            message = message.replaceAll("%event%", eventType.name());

            return message;
        } else {
            plugin.getLogger().log(Level.INFO, "No messages for event ''{0}'' of mob ''{1}'' defined!", new Object[]{eventType.toString(), mobTypeName});
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

        return playersConfig.getConfig().getBoolean(player.getName() + ".enabled." + eventType.name(), true);
    }

    /**
     * Check if talking mobs is enabled for the specified player
     *
     * @param player The player for which the state should be checked
     * @return True if talking mobs is enabled, false otherwise
     */
    public boolean isEnabled(Player player) {
        return playersConfig.getConfig().getBoolean(player.getName() + ".enabled.all", true);
    }

    private boolean isSpamming(Entity mob, Player player, EventType eventType) {
        String key = String.valueOf(mob.getEntityId()) + "/" + String.valueOf(player.getEntityId()) + "/" + eventType.name();
        Long now = System.currentTimeMillis();
        Long spamTimeout = plugin.getConfig().getLong("spam-timeout");

        Long previousMessage = lastMessage.get(key);

        if (previousMessage == null || now - previousMessage > spamTimeout) {
            lastMessage.put(key, now);
            return false;
        } else {
            lastMessage.put(key, now);
            return true;
        }
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
        String message = getMessage(mob, eventType);

        if (message == null) {
            return;
        }

        sendMessage(mob, player, eventType, message);
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

        String message = getMessage(mob, eventType);
        if (message == null) {
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendMessage(mob, player, eventType, message);
        }
    }

    /**
     * Send the given mob message to the player
     *
     * @param mob       The mob which sends the message
     * @param player    The player which should receive the message
     * @param eventType The event type
     * @param message   The message to send
     */
    private void sendMessage(Entity mob, Player player, EventType eventType, String message) {
        if (mob instanceof Player) {
            return;
        }

        if (!(mob instanceof LivingEntity)) {
            return;
        }

        if (!isAllowed(player)) {
            return;
        }

        if (!isEnabled(player, eventType)) {
            return;
        }

        if (isSpamming(mob, player, eventType)) {
            return;
        }

        double maxDistance = plugin.getConfig().getDouble("maxDistance");

        if (maxDistance > 0) {
            double distance = player.getLocation().distance(mob.getLocation());
            if (distance != Double.NaN && distance <= maxDistance) {
                sendFormattedMessage(player, message);
            }
        } else {
            sendFormattedMessage(player, message);
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
        playersConfig.getConfig().set(player.getName() + ".enabled." + eventType.name(), state);
        playersConfig.getConfig().set(player.getName() + ".enabled.all", true);

        playersConfig.save();
    }

    /**
     * Enable or disable talking mobs for the specified player
     *
     * @param player The player for which the state should be changed
     * @param state  The new state
     */
    public void setEnabled(Player player, Boolean state) {
        playersConfig.getConfig().set(player.getName() + ".enabled.all", state);

        for (EventType eventType : EventType.values()) {
            playersConfig.getConfig().set(player.getName() + ".enabled." + eventType.name(), state);
        }

        playersConfig.save();
    }
}
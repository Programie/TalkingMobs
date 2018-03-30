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
        ATTACKED("attacked", true),
        /**
         * Mob is idle (Is just standing/walking/running around) -> Unused
         */
        IDLE("idle", true),
        /**
         * Player interacted with the mob (Right clicked on mob)
         */
        INTERACTED("interacted", true),
        /**
         * Mob has been killed
         */
        KILLED("killed", false),
        /**
         * Mob has been killed by a player
         */
        KILLED_PLAYER("killed_player", true),
        /**
         * Mob has been killed by something else (not a player)
         */
        KILLED_OTHER("killed_other", true),
        /**
         * Mob is looking at the player (and player is looking at the mob)
         */
        LOOKING("looking", true),
        /**
         * Mob has been spawned (Mob spawner, egg or another plugin by default, but can be configured in config.yml)
         */
        SPAWNED("spawned", true),
        /**
         * Mob has been tamed
         */
        TAMED("tamed", true);

        private String type;
        private boolean toggleable;

        EventType(String type, boolean toggleable) {
            this.type = type;
            this.toggleable = toggleable;
        }

        public String getType() {
            return type;
        }

        public boolean isToggleable() {
            return toggleable;
        }

        public static EventType fromString(String type) {
            for (EventType eventType : values()) {
                if (eventType.getType().equalsIgnoreCase(type)) {
                    return eventType;
                }
            }

            return null;
        }
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
     * Get a list of messages configured for the specified mob and event type
     *
     * @param mob       The entity of the mob for which the messages should be fetched
     * @param eventType The event type of the messages
     * @return A list of messages or null if there a none
     */
    private List<String> getMessages(Entity mob, EventType eventType) {
        List<String> messages;
        String mobTypeName = mob.getType().name().toLowerCase();

        messages = messagesConfig.getConfig().getStringList(mobTypeName + "." + eventType.getType());
        if (messages.size() == 0) {
            messages = messagesConfig.getConfig().getStringList("default." + eventType.getType());

            if (messages.size() == 0) {
                return null;
            }
        }

        return messages;
    }

    /**
     * Get a random message for the specified mob and event type
     *
     * @param mob       The entity of the mob for which the message should be fetched
     * @param eventType The event type of the message
     * @return A string containing the message
     */
    private String getMessage(Entity mob, EventType eventType) {
        String mobTypeName = mob.getType().name().toLowerCase();

        List<String> messages = getMessages(mob, eventType);

        if (messages == null) {
            plugin.getLogger().log(Level.INFO, "No messages for event ''{0}'' of mob ''{1}'' defined!", new Object[]{eventType.getType(), mobTypeName});
            return null;
        }

        Random randomGenerator = new Random();

        String message = plugin.getConfig().getString("messageFormat." + eventType.getType());

        if (message == null) {
            message = plugin.getConfig().getString("messageFormat.default");

            if (message == null) {
                message = "[&a%mobname%&r] %message%";
                plugin.getLogger().log(Level.INFO, "Message format for event type ''{0}'' not defined!", eventType.getType());
            }
        }

        message = message.replaceAll("%message%", messages.get(randomGenerator.nextInt(messages.size())));
        message = message.replaceAll("%mobname%", mobTypeName);
        message = message.replaceAll("%event%", eventType.getType());

        return message;
    }

    /**
     * Check whether the player has the permission to receive mob messages
     *
     * @param player The player which should be checked
     * @return True if the player has the permission, false otherwise
     */
    private boolean isAllowed(Player player) {
        return player.hasPermission(Permission.RECEIVE.permission());
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

        return playersConfig.getConfig().getBoolean(player.getName() + ".enabled." + eventType.getType(), true);
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
        String key = String.valueOf(mob.getEntityId()) + "/" + String.valueOf(player.getEntityId()) + "/" + eventType.getType();
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
     * Send a mob message to the player
     *
     * @param mob                The mob which sends the message
     * @param player             The player which should receive the message
     * @param originalEventType  The original event type which triggered this message
     * @param possibleEventTypes The event types of which to pick the first possible one for getting the message
     */
    public void sendMessage(Entity mob, Player player, EventType originalEventType, List<EventType> possibleEventTypes) {
        for (EventType eventType : possibleEventTypes) {
            if (getMessages(mob, eventType) == null) {
                continue;
            }

            String message = getMessage(mob, eventType);

            sendMessage(mob, player, originalEventType, message);
            return;
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

        String message = getMessage(mob, eventType);
        if (message == null) {
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendMessage(mob, player, eventType, message);
        }
    }

    /**
     * Send a mob message to all players
     *
     * @param mob                The mob which sends the message
     * @param originalEventType  The original event type which triggered this message
     * @param possibleEventTypes The event types of which to pick the first possible one for getting the message
     */
    public void sendMessage(Entity mob, EventType originalEventType, List<EventType> possibleEventTypes) {
        if (mob instanceof Player) {
            return;
        }

        if (!(mob instanceof LivingEntity)) {
            return;
        }

        for (EventType eventType : possibleEventTypes) {
            if (getMessages(mob, eventType) == null) {
                continue;
            }

            String message = getMessage(mob, eventType);

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                sendMessage(mob, player, originalEventType, message);
            }

            return;
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
        playersConfig.getConfig().set(player.getName() + ".enabled." + eventType.getType(), state);
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
            if (!eventType.isToggleable()) {
                continue;
            }

            playersConfig.getConfig().set(player.getName() + ".enabled." + eventType.getType(), state);
        }

        playersConfig.save();
    }
}
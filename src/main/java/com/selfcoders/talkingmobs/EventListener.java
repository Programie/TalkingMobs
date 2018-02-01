package com.selfcoders.talkingmobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.List;

/**
 * Class providing all event listeners required for the plugin
 */
class EventListener implements Listener {
    private final Message message;
    private final TalkingMobs plugin;

    /**
     * Constructor of the class
     *
     * @param messageInstance The instance of the Message class
     */
    EventListener(Message messageInstance, TalkingMobs pluginInstance) {
        message = messageInstance;
        plugin = pluginInstance;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

        List<String> allowedSpawnReasons = plugin.getConfig().getStringList("events.spawned");

        if (allowedSpawnReasons.contains(spawnReason.name())) {
            message.sendMessage(entity, Message.EventType.spawned);
        }
    }

    @EventHandler
    public void onEntityAttacked(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("events.attacked")) {
            return;
        }

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (damager instanceof Player && !entity.isDead()) {
            message.sendMessage(entity, (Player) damager, Message.EventType.attacked);
        }
    }

    @EventHandler
    public void onEntityKilled(EntityDeathEvent event) {
        if (!plugin.getConfig().getBoolean("events.killed")) {
            return;
        }

        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer != null) {
            message.sendMessage(entity, killer, Message.EventType.killed);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!plugin.getConfig().getBoolean("events.interacted")) {
            return;
        }

        message.sendMessage(event.getRightClicked(), event.getPlayer(), Message.EventType.interacted);
    }
}
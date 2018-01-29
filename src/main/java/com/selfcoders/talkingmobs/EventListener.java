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

/**
 * Class providing all event listeners required for the plugin
 */
class EventListener implements Listener {
    private final Message message;

    /**
     * Constructor of the class
     *
     * @param messageInstance The instance of the Message class
     */
    EventListener(Message messageInstance) {
        message = messageInstance;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

        if (spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER || spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || spawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            message.sendMessage(entity, Message.EventType.spawned);
        }
    }

    @EventHandler
    public void onEntityAttacked(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (damager instanceof Player && !entity.isDead()) {
            message.sendMessage(entity, (Player) damager, Message.EventType.attacked);
        }
    }

    @EventHandler
    public void onEntityKilled(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer != null) {
            message.sendMessage(entity, killer, Message.EventType.killed);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        message.sendMessage(event.getRightClicked(), event.getPlayer(), Message.EventType.interacted);
    }
}
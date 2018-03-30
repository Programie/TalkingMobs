package com.selfcoders.talkingmobs;

import org.bukkit.Location;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
            message.sendMessage(entity, Message.EventType.SPAWNED);
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
            message.sendMessage(entity, (Player) damager, Message.EventType.ATTACKED);
        }
    }

    @EventHandler
    public void onEntityKilled(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) {
            if (!plugin.getConfig().getBoolean("events.killed_other")) {
                return;
            }

            List<Message.EventType> eventTypes = new ArrayList<>();

            eventTypes.add(Message.EventType.KILLED_OTHER);
            eventTypes.add(Message.EventType.KILLED);

            message.sendMessage(entity, Message.EventType.KILLED_OTHER, eventTypes);
        } else {
            // events.killed is used till version 1.3 and is now deprecated
            if (!plugin.getConfig().getBoolean("events.killed_player") && !plugin.getConfig().getBoolean("events.killed")) {
                return;
            }

            List<Message.EventType> eventTypes = new ArrayList<>();

            eventTypes.add(Message.EventType.KILLED_PLAYER);
            eventTypes.add(Message.EventType.KILLED);

            message.sendMessage(entity, killer, Message.EventType.KILLED_PLAYER, eventTypes);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!plugin.getConfig().getBoolean("events.interacted")) {
            return;
        }

        message.sendMessage(event.getRightClicked(), event.getPlayer(), Message.EventType.INTERACTED);
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (!plugin.getConfig().getBoolean("events.tamed")) {
            return;
        }

        AnimalTamer owner = event.getOwner();

        if (!(owner instanceof Player)) {
            return;
        }

        Player player = (Player) owner;

        message.sendMessage(event.getEntity(), player, Message.EventType.TAMED);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("events.looking")) {
            return;
        }

        Location location = event.getTo().clone();
        Player player = event.getPlayer();

        List<Entity> nearbyEntities = event.getPlayer().getNearbyEntities(location.getX(), location.getY(), location.getZ());

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            LivingEntity livingEntity = (LivingEntity) entity;

            if (!livingEntity.hasLineOfSight(player)) {
                continue;
            }

            if (location.distance(entity.getLocation()) > plugin.getConfig().getDouble("max-looking-distance")) {
                continue;
            }

            if (isEntityLookingAtEntity(livingEntity, player) && isEntityLookingAtEntity(player, livingEntity)) {
                onEntityFacingPlayer(livingEntity, player);
            }
        }
    }

    private void onEntityFacingPlayer(LivingEntity entity, Player player) {
        message.sendMessage(entity, player, Message.EventType.LOOKING);
    }

    /**
     * Check whether the entity looks at the other entity
     * Thanks to Mr.Midnight (https://www.spigotmc.org/threads/how-to-detect-an-entity-the-player-is-looking-at.139310/#post-1476341)
     *
     * @param entity      The entity
     * @param otherEntity The other entity
     * @return Whether the entity looks at the other entity
     */
    private boolean isEntityLookingAtEntity(LivingEntity entity, LivingEntity otherEntity) {
        Location eye = entity.getEyeLocation();
        Vector toEntity = otherEntity.getEyeLocation().toVector().subtract(eye.toVector());
        double dot = toEntity.normalize().dot(eye.getDirection());

        return dot > 0.90D;
    }
}
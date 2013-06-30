package net.minearea.talkingmobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class EventListener implements Listener
{
	private final TalkingMobs plugin;
	private final Message message;

	public EventListener(TalkingMobs pluginInstance, Message messageInstance)
	{
		plugin = pluginInstance;
		message = messageInstance;
	}

	// TODO: Prevent spam
	/*@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		message.sendMessage(event.getEntity(), Message.MessageType.spawn);
	}*/

	@EventHandler
	public void onEntityAttacked(EntityDamageByEntityEvent event)
	{
		Entity damager = event.getDamager();
		Entity entity = event.getEntity();
		if (damager instanceof Player && !entity.isDead())
		{
			message.sendMessage(entity, (Player) damager, Message.EventType.attacked);
		}
	}

	@EventHandler
	public void onEntityKilled(EntityDeathEvent event)
	{
		LivingEntity entity = event.getEntity();
		Entity killer = entity.getKiller();

		if (killer instanceof Player)
		{
			message.sendMessage(entity, (Player) killer, Message.EventType.killed);
		}
	}
}
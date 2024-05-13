package com.festp.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.festp.messaging.Chatter;
import com.festp.messaging.SpigotMessageSender;

public class ChatHandler implements Listener
{
	private final Chatter chatter;
	
	public ChatHandler(Chatter chatter) {
		this.chatter = chatter;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnChat(AsyncPlayerChatEvent event)
	{
		String message = event.getMessage();

		boolean sent = chatter.sendFormatted(event.getRecipients(), event.getPlayer(), message, event.getFormat(), true);
		if (sent)
			event.setCancelled(true);
	}
}

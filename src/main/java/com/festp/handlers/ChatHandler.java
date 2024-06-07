package com.festp.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.festp.messaging.Chatter;

public class ChatHandler implements Listener
{
	private final Chatter chatter;
	
	public ChatHandler(Chatter chatter) {
		this.chatter = chatter;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnChat(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled())
			return;
		
		boolean sent = chatter.sendFormatted(event.getPlayer(), event.getRecipients(), event.getMessage(), event.getFormat(), true);
		if (sent)
			event.getRecipients().clear();
	}
}

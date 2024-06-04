package com.festp.handlers;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.common.collect.Lists;

public class MemoryChatHandler implements Listener, MessageInfoProvider
{
	private List<MessageInfo> prevMessages = Lists.newArrayList();
	private List<MessageInfo> messages = Lists.newArrayList();
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void OnChat(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled())
			return;
		
		messages.add(new MessageInfo(event.getPlayer(), event.getRecipients(), event.getFormat(), event.getMessage()));
	}
	
	public void onTick()
	{
		prevMessages = messages;
		messages = Lists.newArrayList();
		messages.clear();
	}
	
	public MessageInfo[] getRecentMessages()
	{
		List<MessageInfo> res = Lists.newArrayList(prevMessages);
		res.addAll(messages);
		return res.toArray(new MessageInfo[0]);
	}
	
	// Could not use when messages has delay (disabled for test purposes!)
	
	/*private List<MessageInfo> messages = Lists.newArrayList();
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void OnChat(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled())
			return;
		
		messages.add(new MessageInfo(event.getPlayer(), event.getRecipients(), event.getFormat(), event.getMessage()));
	}
	
	public void onTick()
	{
		messages.clear();
	}
	
	public MessageInfo[] getRecentMessages()
	{
		return messages.toArray(new MessageInfo[0]);
	}*/
}

package com.festp.handlers;

import java.util.Collection;

import org.bukkit.entity.Player;

public interface MessageInfoProvider
{
	public MessageInfo[] getRecentMessages();

	public static class MessageInfo
	{
		public final Player sender;
		public final Collection<? extends Player> recipients;
		public final String format;
		public final String content;
		
		public MessageInfo(Player sender, Collection<? extends Player> recipients, String format, String content) {
			this.sender = sender;
			this.recipients = recipients;
			this.format = format;
			this.content = content;
		}
	}
}

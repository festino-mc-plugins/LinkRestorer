package com.festp.messaging;

import org.bukkit.entity.Player;

public interface MessageSender
{
	public void sendRawJson(Player player, CharSequence rawJsonMessage);
}

package com.festp.messaging;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

public interface MessageSender
{
	public void sendChatComponents(Player player, BaseComponent... components);
}

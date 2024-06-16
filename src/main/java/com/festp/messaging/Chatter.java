package com.festp.messaging;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

public interface Chatter
{
	public boolean sendFormatted(CommandSender sender, Collection<? extends Player> recipients, String message, String format, boolean sendToConsole);

	public boolean sendWhisperMessage(CommandSender sender, Player[] recipients, String message);
	
	public boolean sendOnlyLinks(CommandSender sender, Player[] recipients, String message);

	public boolean sendIntercepted(
			CommandSender sender,
			Player recipient,
			List<BaseComponent> formatComponents,
			List<Integer> messagePositions,
			List<BaseComponent> message);
}

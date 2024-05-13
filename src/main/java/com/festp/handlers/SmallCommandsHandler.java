package com.festp.handlers;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.festp.messaging.Chatter;
import com.festp.messaging.SpigotMessageSender;

public class SmallCommandsHandler implements Listener
{
	private static final String ME_COMMAND = "me";
	private static final String SAY_COMMAND = "say";
	
	private Chatter chatter;
	
	public SmallCommandsHandler(Chatter chatter)
	{
		this.chatter = chatter;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerCommand(ServerCommandEvent event)
	{
		onCommand(event, event.getSender(), "/" + event.getCommand());
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		onCommand(event, event.getPlayer(), event.getMessage());
	}

	private void onCommand(Cancellable event, CommandSender sender, String command)
	{
		if (event.isCancelled())
			return;
		
		String cmd = CommandUtils.getCommand(command);
		String message = CommandUtils.getArgs(command);
		if (isMeCommand(cmd))
			onMeCommand(event, sender, message);
		else if (isSayCommand(cmd))
			onSayCommand(event, sender, message);
	}

	private void onMeCommand(Cancellable event, CommandSender sender, String message)
	{
		if (message == "")
			return;
		
		boolean sent = chatter.sendFormatted(null, sender, message, "* %1$s %2$s", false);
		if (sent)
			event.setCancelled(true);
	}

	private void onSayCommand(Cancellable event, CommandSender sender, String message)
	{
		if (message == "")
			return;

		boolean sent = chatter.sendFormatted(null, sender, message, "[%1$s] %2$s", false);
		if (sent)
			event.setCancelled(true);
	}

	private boolean isMeCommand(String command) {
		return command.equalsIgnoreCase(ME_COMMAND);
	}
	private boolean isSayCommand(String command) {
		return command.equalsIgnoreCase(SAY_COMMAND);
	}
}

package com.festp.handlers;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.festp.Logger;
import com.festp.config.Config;
import com.festp.messaging.Chatter;
import com.festp.styledmessage.components.TextStyle;

public class WhisperHandler implements Listener
{
	private Chatter chatter;
	private Config config;
	
	private static final String STYLE_CODES = ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
	
	public WhisperHandler(Chatter chatter, Config config)
	{
		this.chatter = chatter;
		this.config = config;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onServerCommand(ServerCommandEvent event)
	{
		onCommand(event, event.getSender(), "/" + event.getCommand());
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		onCommand(event, event.getPlayer(), event.getMessage());
	}
	
	private void onCommand(Cancellable event, CommandSender sender, String command)
	{
		if (!config.get(Config.Key.LISTEN_TO_WHISPER, true))
			return;
		if (event.isCancelled() && !config.get(Config.Key.WHISPER_NEW_MESSAGE, false))
			return;

		String cmd = CommandUtils.getCommand(command);
		if (!isWhisperCommand(cmd))
			return;
		boolean isLogging = config.get(Config.Key.LOG_DEBUG, false);
		if (isLogging) Logger.info("Handling whisper event... (event was " + (event.isCancelled() ? "" : "not ") + "cancelled)");

		int[] indices = CommandUtils.selectRecipientsFromArg0(command);
		if (indices == null)
			return;
		// if is not vanilla, recipients list may be invalid
		Player[] recipients = CommandUtils.getRecipients(command.substring(indices[0], indices[1]), sender);
		if (recipients == null || recipients.length == 0)
			return;
		if (isLogging) Logger.info("Got " + recipients.length + " recipients...");
		
		String message = command.substring(indices[1]).trim();
		if (message == "")
			return;
		
		TextStyle style = new TextStyle().update(STYLE_CODES);
		
		if (!config.get(Config.Key.WHISPER_NEW_MESSAGE, false))
		{
			boolean sent = chatter.sendWhisperMessage(sender, recipients, message, style);
			if (sent)
				event.setCancelled(true);
		}
		else
		{
			chatter.sendOnlyLinks(sender, recipients, message, style);
		}
	}

	private static boolean isWhisperCommand(String command)
	{
		// EssentialsX Chat: aliases: [w,m,t,pm,emsg,epm,tell,etell,whisper,ewhisper]
		// (https://github.com/EssentialsX/Essentials/blob/f7cbc7b0d37ea7a674955758da099524b009ad03/Essentials/src/main/resources/plugin.yml)
		// (https://github.com/EssentialsX/Essentials/blob/f7cbc7b0d37ea7a674955758da099524b009ad03/Essentials/src/main/resources/config.yml)
		return command.equalsIgnoreCase("w") || command.equalsIgnoreCase("msg") || command.equalsIgnoreCase("tell");
	}
}

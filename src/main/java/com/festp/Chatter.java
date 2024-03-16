package com.festp;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.config.Config;
import com.festp.parsing.Link;
import com.festp.parsing.StyledMessage;
import com.festp.parsing.StyledMessageParser;
import com.festp.parsing.TextStyle;
import com.festp.utils.RawJsonBuilder;

public class Chatter
{
	private static final String PLACEHOLDER_NAME = "%1$s";
	private static final String PLACEHOLDER_MESSAGE = "%2$s";
	
	private JavaPlugin plugin;
	private Config config;
	
	public Chatter(JavaPlugin plugin, Config config)
	{
		this.plugin = plugin;
		this.config = config;
	}

	/**
	 * Generates command for chat messages containing links<br>
	 * {@literal<FEST_Channel>} test https://www.netflix.com/browse extra text<br>
	 * like<br>
	 * /tellraw @a [<br>
	 * {"text":"<"},<br>
	 * {"text":"FEST_Channel",<br>
	 * "hoverEvent":{"action":"show_text","value":"FEST_Channel\nType: Player\n4a9b60fa-6c37-3673-b0ae-02ee83a6356d"},<br>
	 * "clickEvent":{"action":"suggest_command","value":"/tell FEST_Channel"}},<br>
	 * {"text":"> test "},<br>
	 * {"text":"https://www.netflix.com/browse","underlined":true,<br>
	 * "clickEvent":{"action":"open_url","value":"https://www.netflix.com/browse"}},<br>
	 * {"text":" extra text"}<br>
	 * ]
	 * 
	 * @param recipients is <i>null</i> if all the players will get the message; console will always get the message
	 * @param sender is message sender
	 * @param message is full message containing link(s)
	 * @param format use "%1$s" for the sender name and "%2$s" for the message, i.e. "<%1$s> %2$s"
	 * @param link is the first link found*/
	public boolean sendFormatted(Set<Player> recipients, CommandSender sender, String message, String format, boolean sendToConsole)
	{
		StyledMessage styledMessage = StyledMessageParser.parse(message);
		if (styledMessage == null || !styledMessage.hasLinks)
			return false;
		
		if (sendToConsole)
		{
			String consoleMessage = format.replace(PLACEHOLDER_NAME, getDisplayName(sender)).replace(PLACEHOLDER_MESSAGE, message);
			Bukkit.getConsoleSender().sendMessage(consoleMessage);
		}
		
		// check if actually no recipients
		if (recipients != null && recipients.isEmpty() || Bukkit.getOnlinePlayers().size() == 0)
			return true;
		
		if (recipients == null)
		{
			recipients = new HashSet<>(Bukkit.getOnlinePlayers());
		}
		
		final RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.startList();
		Pattern pattern = Pattern.compile("[%][\\d][$][s]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(format);
		int prevEnd = 0;
		TextStyle style = new TextStyle();
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();
			builder.tryWrap(format.substring(prevEnd, start), style);

			int colorStart = format.lastIndexOf(ChatColor.COLOR_CHAR, start);
			int colorEnd = colorStart + 2;
			if (0 <= colorStart && colorEnd <= end)
			{
				while (colorStart >= 2 && format.charAt(colorStart - 2) == ChatColor.COLOR_CHAR) {
					colorStart -= 2;
				}
				style.update(format, colorStart, colorEnd);
			}

			String placeholder = format.substring(start, end);
			if (placeholder.equals(PLACEHOLDER_NAME))
				builder.appendSender(sender, style, true);
			if (placeholder.equals(PLACEHOLDER_MESSAGE))
				builder.appendMessage(styledMessage, style);

			prevEnd = end;
		}
		builder.tryWrap(format.substring(prevEnd), style);
		builder.endList();

		String rawJson = builder.releaseStringBuilder().toString();
		for (Player p : recipients) {
			sendRawJson(p, rawJson);
		}
		return true;
	}
	
	public void sendWhisperMessage(CommandSender sender, Player[] recipients, StyledMessage styledMessage, TextStyle style)
	{
		String fromStr = "commands.message.display.outgoing"; // "You whisper to %s: %s"
		String toStr = "commands.message.display.incoming"; // "%s whispers to you: %s"
		
		RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.appendMessage(styledMessage, style);
		StringBuilder modifiedMessage = builder.releaseStringBuilder();

		builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.appendSender(sender, style, false);
		StringBuilder wrapNameFrom = builder.releaseStringBuilder();
		
		for (Player recipient : recipients)
		{
			if (sender instanceof Player)
			{
				builder = new RawJsonBuilder(config.getBuilderSettings());
				builder.appendPlayer(recipient, style, false);
				StringBuilder wrapNameTo = builder.releaseStringBuilder();
				
				RawJsonBuilder from = new RawJsonBuilder(config.getBuilderSettings());
				from.appendTranslated(fromStr, new CharSequence[] { wrapNameTo, modifiedMessage }, style);
				sendRawJson((Player)sender, from.build());
			}
			
			RawJsonBuilder to = new RawJsonBuilder(config.getBuilderSettings());
			to.appendTranslated(toStr, new CharSequence[] { wrapNameFrom, modifiedMessage }, style);
			sendRawJson(recipient, to.build());
		}
	}
	
	public void sendOnlyLinks(CommandSender sender, Player[] recipients, Iterable<Link> links, TextStyle style)
	{
		RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.appendJoinedLinks(links, style, ", ");
		String linkCommand = builder.build();
		
		if (sender instanceof Player)
			sendRawJson((Player)sender, linkCommand);
		
		for (Player recipient : recipients)
		{
			if (recipient == sender)
				continue;
			sendRawJson(recipient, linkCommand);
		}
	}
	
	public static String getDisplayName(CommandSender sender)
	{
		if (sender instanceof Player)
			return ((Player)sender).getDisplayName();
		if (sender instanceof ConsoleCommandSender)
			return "Server";
		return sender.getName();
	}
	
	private void sendRawJson(Player player, String rawJsonMessage)
	{
		boolean isLogging = config.get(Config.Key.LOG_DEBUG, false);
		if (isLogging) Logger.info("Sending raw JSON: " + rawJsonMessage);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				StringBuilder command = new StringBuilder("tellraw ");
				command.append(player.getName());
				command.append(' ');
				command.append(rawJsonMessage);
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.toString());
			}
		});
	}
}

package com.festp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.StyledMessageBuilder;
import com.festp.styledmessage.components.Link;
import com.festp.styledmessage.components.MentionedPlayer;
import com.festp.styledmessage.components.TextComponent;
import com.festp.styledmessage.components.TextStyle;
import com.festp.utils.RawJsonBuilder;

public class Chatter
{
	private static final String PLACEHOLDER_NAME = "%1$s";
	private static final String PLACEHOLDER_MESSAGE = "%2$s";
	
	private JavaPlugin plugin;
	private Config config;
	private final StyledMessageBuilder builder;
	private final StyledMessageBuilder textBuilder;
	
	public Chatter(JavaPlugin plugin, Config config, StyledMessageBuilder builder, StyledMessageBuilder textBuilder)
	{
		this.plugin = plugin;
		this.config = config;
		this.builder = builder;
		this.textBuilder = textBuilder;
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
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage))
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
		
		Pattern pattern = Pattern.compile("[%][\\d][$][s]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(format);
		int prevEnd = 0;
		StyledMessage fullStyledMessage = new StyledMessage();
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();
			fullStyledMessage.addAll(textBuilder.append(format.substring(prevEnd, start)).build().getStyledParts());

			String placeholder = format.substring(start, end);
			if (placeholder.equals(PLACEHOLDER_NAME))
				fullStyledMessage.addAll(getSender(sender, false).getStyledParts());
			if (placeholder.equals(PLACEHOLDER_MESSAGE))
				fullStyledMessage.addAll(styledMessage.getStyledParts());

			prevEnd = end;
		}
		fullStyledMessage.addAll(textBuilder.append(format.substring(prevEnd)).build().getStyledParts());

		RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.appendStyledMessage(fullStyledMessage);
		String rawJson = builder.toString();
		for (Player p : recipients) {
			sendRawJson(p, rawJson);
		}
		return true;
	}
	
	private StyledMessage getSender(CommandSender sender, boolean stripColors)
	{
		String name;
		if (stripColors)
			name = sender.getName();
		else
			name = Chatter.getDisplayName(sender);

		StyledMessage styledMessage = textBuilder.append(name).build();
		if (sender instanceof Player) {
			MentionedPlayer playerComponent = new MentionedPlayer((Player) sender, ChatColor.stripColor(name));
			for (SingleStyleMessage part : styledMessage.getStyledParts())
			{
				part.getComponents().add(playerComponent);
			}
		}
		return styledMessage;
	}
	
	public boolean sendWhisperMessage(CommandSender sender, Player[] recipients, String message, TextStyle baseStyle)
	{
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage))
			return false;
		
		String fromStr = "commands.message.display.outgoing"; // "You whisper to %s: %s"
		String toStr = "commands.message.display.incoming"; // "%s whispers to you: %s"
		
		TextStyle style = baseStyle;
		RawJsonBuilder messageBuilder = new RawJsonBuilder(config.getBuilderSettings());
		messageBuilder.appendStyledMessage(styledMessage);
		CharSequence messageJson = messageBuilder.toCharSequence();

		RawJsonBuilder nameFromBuilder = new RawJsonBuilder(config.getBuilderSettings());
		nameFromBuilder.appendStyledMessage(getSender(sender, true));
		CharSequence nameFromJson = nameFromBuilder.toCharSequence();
		
		for (Player recipient : recipients)
		{
			if (sender instanceof Player)
			{
				RawJsonBuilder nameToBuilder = new RawJsonBuilder(config.getBuilderSettings());
				nameToBuilder.appendStyledMessage(getSender(recipient, true));
				CharSequence nameToJson = nameFromBuilder.toCharSequence();
				
				RawJsonBuilder from = new RawJsonBuilder(config.getBuilderSettings());
				from.appendTranslated(fromStr, new CharSequence[] { nameToJson, messageJson }, style);
				sendRawJson((Player)sender, from.toString());
			}
			
			RawJsonBuilder to = new RawJsonBuilder(config.getBuilderSettings());
			to.appendTranslated(toStr, new CharSequence[] { nameFromJson, messageJson }, style);
			sendRawJson(recipient, to.toString());
		}
		return true;
	}
	
	public boolean sendOnlyLinks(CommandSender sender, Player[] recipients, String message, TextStyle style)
	{
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage))
			return false;
		
		Iterable<Link> links = getLinks(styledMessage);
		RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		builder.appendJoinedLinks(links, style, ", ");
		String linkCommand = builder.toString();
		
		if (sender instanceof Player)
			sendRawJson((Player)sender, linkCommand);
		
		for (Player recipient : recipients)
		{
			if (recipient == sender)
				continue;
			sendRawJson(recipient, linkCommand);
		}
		return true;
	}
	
	public static String getDisplayName(CommandSender sender)
	{
		if (sender instanceof Player)
			return ((Player)sender).getDisplayName();
		if (sender instanceof ConsoleCommandSender)
			return "Server";
		return sender.getName();
	}
	
	private void sendRawJson(Player player, CharSequence rawJsonMessage)
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
	
	private List<Link> getLinks(StyledMessage styledMessage)
	{
		List<Link> links = new ArrayList<>();
		for (SingleStyleMessage part : styledMessage.getStyledParts())
			for (TextComponent component : part.getComponents())
				if (component instanceof Link)
					links.add((Link)component);
		
		return links;
	}
	
	private boolean canSend(StyledMessage styledMessage)
	{
		if (styledMessage == null)
			return false;
		
		for (SingleStyleMessage part : styledMessage.getStyledParts())
			for (TextComponent component : part.getComponents())
				if (component instanceof Link)
					return true;
		
		return false;
	}
}

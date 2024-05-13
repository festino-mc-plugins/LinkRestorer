package com.festp.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.festp.config.Config;
import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.StyledMessageBuilder;
import com.festp.styledmessage.components.Link;
import com.festp.styledmessage.components.MentionedPlayer;
import com.festp.styledmessage.components.TextComponent;
import com.festp.styledmessage.components.TextStyle;
import com.google.common.collect.Lists;

public class RawJsonChatter implements Chatter
{
	private static final String PLACEHOLDER_NAME = "%1$s";
	private static final String PLACEHOLDER_MESSAGE = "%2$s";
	
	private static final String WHISPER_STYLE_CODES = ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
	
	private final Config config;
	private final StyledMessageBuilder builder;
	private final MessageSender messageSender;

	public RawJsonChatter(Config config, StyledMessageBuilder builder, MessageSender messageSender)
	{
		this.config = config;
		this.builder = builder;
		this.messageSender = messageSender;
	}
	
	public boolean sendFormatted(Collection<? extends Player> recipients, CommandSender sender, String message, String format, boolean sendToConsole)
	{
		builder.clear();
		StyledMessage styledMessage = builder.append(message).build();
		builder.clear();
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
			recipients = Bukkit.getOnlinePlayers();
		}
		
		Pattern pattern = Pattern.compile("[%][\\d][$][s]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(format);
		int prevEnd = 0;
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();
			builder.appendSplitting(format.substring(prevEnd, start));

			String placeholder = format.substring(start, end);
			if (placeholder.equals(PLACEHOLDER_NAME))
				appendSender(builder, sender, false);
			if (placeholder.equals(PLACEHOLDER_MESSAGE))
				builder.append(message);

			prevEnd = end;
		}
		builder.appendSplitting(format.substring(prevEnd));

		RawJsonBuilder jsonBuilder = new RawJsonBuilder(config.getBuilderSettings());
		jsonBuilder.appendStyledMessage(builder.build());
		String rawJson = jsonBuilder.toString();
		for (Player p : recipients) {
			messageSender.sendRawJson(p, rawJson);
		}
		return true;
	}
	
	public boolean sendWhisperMessage(CommandSender sender, Player[] recipients, String message)
	{
		builder.clear();
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage))
			return false;
		
		String fromStr = "commands.message.display.outgoing"; // "You whisper to %s: %s"
		String toStr = "commands.message.display.incoming"; // "%s whispers to you: %s"
		
		TextStyle style = new TextStyle().update(WHISPER_STYLE_CODES);
		RawJsonBuilder messageBuilder = new RawJsonBuilder(config.getBuilderSettings());
		messageBuilder.appendStyledMessage(styledMessage);
		CharSequence messageJson = messageBuilder.toCharSequence();

		RawJsonBuilder nameFromBuilder = new RawJsonBuilder(config.getBuilderSettings());
		builder.clear();
		appendSender(builder, sender, true);
		nameFromBuilder.appendStyledMessage(builder.build());
		CharSequence nameFromJson = nameFromBuilder.toCharSequence();
		
		for (Player recipient : recipients)
		{
			if (sender instanceof Player)
			{
				RawJsonBuilder nameToBuilder = new RawJsonBuilder(config.getBuilderSettings());
				builder.clear();
				appendSender(builder, recipient, true);
				nameToBuilder.appendStyledMessage(builder.build());
				CharSequence nameToJson = nameFromBuilder.toCharSequence();
				
				RawJsonBuilder from = new RawJsonBuilder(config.getBuilderSettings());
				from.appendTranslated(fromStr, new CharSequence[] { nameToJson, messageJson }, style);
				messageSender.sendRawJson((Player)sender, from.toString());
			}
			
			RawJsonBuilder to = new RawJsonBuilder(config.getBuilderSettings());
			to.appendTranslated(toStr, new CharSequence[] { nameFromJson, messageJson }, style);
			messageSender.sendRawJson(recipient, to.toString());
		}
		return true;
	}
	
	public boolean sendOnlyLinks(CommandSender sender, Player[] recipients, String message)
	{
		builder.clear();
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage))
			return false;
		
		Iterable<Link> links = getLinks(styledMessage);
		RawJsonBuilder builder = new RawJsonBuilder(config.getBuilderSettings());
		TextStyle style = new TextStyle().update(WHISPER_STYLE_CODES);
		builder.appendJoinedLinks(links, style, ", ");
		String linkCommand = builder.toString();
		
		if (sender instanceof Player)
			messageSender.sendRawJson((Player)sender, linkCommand);
		
		for (Player recipient : recipients)
		{
			if (recipient == sender)
				continue;
			messageSender.sendRawJson(recipient, linkCommand);
		}
		return true;
	}
	
	private static void appendSender(StyledMessageBuilder builder, CommandSender sender, boolean stripColors)
	{
		String name;
		if (stripColors)
			name = sender.getName();
		else
			name = getDisplayName(sender);

		if (sender instanceof Player) {
			MentionedPlayer playerComponent = new MentionedPlayer((Player) sender, ChatColor.stripColor(name));
			builder.appendSplitting(name, Lists.newArrayList(playerComponent));
		} else {
			builder.appendSplitting(name);
		}
	}
	
	private static String getDisplayName(CommandSender sender)
	{
		if (sender instanceof Player)
			return ((Player)sender).getDisplayName();
		if (sender instanceof ConsoleCommandSender)
			return "Server";
		return sender.getName();
	}

	
	private static List<Link> getLinks(StyledMessage styledMessage)
	{
		List<Link> links = Lists.newArrayList();
		List<String> urls = Lists.newArrayList();
		for (SingleStyleMessage part : styledMessage.getStyledParts())
			for (TextComponent component : part.getComponents())
				if (component instanceof Link) {
					Link link = (Link) component;
					if (!urls.contains(link.getUrl())) {
						links.add(link);
						urls.add(link.getUrl());
					}
				}
		
		return links;
	}
	
	private static boolean canSend(StyledMessage styledMessage)
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

package com.festp.messaging;

import java.util.Collection;
import java.util.List;
import java.util.Random;
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
import com.festp.styledmessage.StyledMessageBuilderFactory;
import com.festp.styledmessage.attributes.Formatting;
import com.festp.styledmessage.attributes.Link;
import com.festp.styledmessage.attributes.MentionedPlayer;
import com.festp.styledmessage.attributes.StyleAttribute;
import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class RawJsonChatter implements Chatter
{
	private static final String PLACEHOLDER_NAME = "%1$s";
	private static final String PLACEHOLDER_MESSAGE = "%2$s";
	
	private static final String WHISPER_STYLE_CODES = ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
	
	private final Config config;
	private final StyledMessageBuilderFactory factory;
	private final MessageSender messageSender;

	public RawJsonChatter(Config config, StyledMessageBuilderFactory factory, MessageSender messageSender)
	{
		this.config = config;
		this.factory = factory;
		this.messageSender = messageSender;
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
	 * @param format use "%1$s" for the sender name and "%2$s" for the message, e.g. "<%1$s> %2$s"
	 * @param link is the first link found*/
	public boolean sendFormatted(CommandSender sender, Collection<? extends Player> recipients, String message, String format, boolean sendToConsole)
	{
		StyledMessageBuilder builder = factory.create(sender);
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage, message))
			return false;
		
		if (sendToConsole)
		{
			String consoleMessage = format.replace(PLACEHOLDER_NAME, getDisplayName(sender)).replace(PLACEHOLDER_MESSAGE, message);
			Bukkit.getConsoleSender().sendMessage(consoleMessage);
		}
		
		// check if actually no recipients
		if (recipients != null && recipients.isEmpty() || Bukkit.getOnlinePlayers().size() == 0)
			return false;
		
		if (recipients == null)
		{
			recipients = Bukkit.getOnlinePlayers();
		}
		
		builder.clear();
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

		RawJsonBuilder jsonBuilder = newRawJsonBuilder();
		jsonBuilder.appendStyledMessage(builder.build());
		String rawJson = jsonBuilder.toString();
		for (Player p : recipients) {
			messageSender.sendRawJson(p, rawJson);
		}
		return true;
	}

	public boolean sendIntercepted(
			CommandSender sender,
			Player recipient,
			Collection<? extends BaseComponent> formatComponents,
			Collection<? extends Integer> messagePositions,
			Collection<? extends BaseComponent> message)
	{
		if (new Random().nextBoolean()) return false;
		// parse styled message
		// try merge events (our have less priority)
		// send only if any event has changed
		List<BaseComponent> components = Lists.newArrayList(formatComponents);
		List<Integer> indices = Lists.newArrayList(messagePositions);
		for (int i = indices.size() - 1; i >= 0; i--) {
			components.addAll(indices.get(i), message);
		}
		String rawJson = ComponentSerializer.toString(components);
		messageSender.sendRawJson(recipient, rawJson);
		return true;
	}
	
	public boolean sendWhisperMessage(CommandSender sender, Player[] recipients, String message)
	{
		StyledMessageBuilder builder = factory.create(sender);
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage, message))
			return false;
		
		String fromStr = "commands.message.display.outgoing"; // "You whisper to %s: %s"
		String toStr = "commands.message.display.incoming"; // "%s whispers to you: %s"
		
		Formatting baseFormatting = new Formatting().update(WHISPER_STYLE_CODES);
		RawJsonBuilder messageBuilder = newRawJsonBuilder();
		messageBuilder.appendStyledMessage(styledMessage);
		CharSequence messageJson = messageBuilder.toCharSequence();

		RawJsonBuilder nameFromBuilder = newRawJsonBuilder();
		builder.clear();
		appendSender(builder, sender, true);
		nameFromBuilder.appendStyledMessage(builder.build());
		CharSequence nameFromJson = nameFromBuilder.toCharSequence();
		
		for (Player recipient : recipients)
		{
			if (sender instanceof Player)
			{
				RawJsonBuilder nameToBuilder = newRawJsonBuilder();
				builder.clear();
				appendSender(builder, recipient, true);
				nameToBuilder.appendStyledMessage(builder.build());
				CharSequence nameToJson = nameFromBuilder.toCharSequence();
				
				RawJsonBuilder from = newRawJsonBuilder();
				from.appendTranslated(fromStr, new CharSequence[] { nameToJson, messageJson }, baseFormatting);
				messageSender.sendRawJson((Player)sender, from.toString());
			}
			
			RawJsonBuilder to = newRawJsonBuilder();
			to.appendTranslated(toStr, new CharSequence[] { nameFromJson, messageJson }, baseFormatting);
			messageSender.sendRawJson(recipient, to.toString());
		}
		return true;
	}
	
	public boolean sendOnlyLinks(CommandSender sender, Player[] recipients, String message)
	{
		StyledMessageBuilder builder = factory.create(sender);
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage, message))
			return false;
		
		Iterable<Link> links = getLinks(styledMessage);
		RawJsonBuilder jsonBuilder = newRawJsonBuilder();
		Formatting formatting = new Formatting().update(WHISPER_STYLE_CODES);
		jsonBuilder.appendJoinedLinks(links, formatting, ", ");
		String linkCommand = jsonBuilder.toString();
		
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
	
	private RawJsonBuilder newRawJsonBuilder()
	{
		return new RawJsonBuilder(config.getDisplaySettings());
	}
	
	private static void appendSender(StyledMessageBuilder builder, CommandSender sender, boolean stripColors)
	{
		String name;
		if (stripColors)
			name = sender.getName();
		else
			name = getDisplayName(sender);

		if (sender instanceof Player) {
			MentionedPlayer playerAttribute = new MentionedPlayer((Player) sender, ChatColor.stripColor(name));
			builder.appendSplitting(name, Lists.newArrayList(playerAttribute));
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
			for (StyleAttribute attribute : part.getStyle())
				if (attribute instanceof Link) {
					Link link = (Link) attribute;
					if (!urls.contains(link.getUrl())) {
						links.add(link);
						urls.add(link.getUrl());
					}
				}
		
		return links;
	}
	
	private static boolean canSend(StyledMessage styledMessage, String originalText)
	{
		if (styledMessage == null)
			return false;
		
		for (SingleStyleMessage part : styledMessage.getStyledParts())
			for (StyleAttribute attribute : part.getStyle())
				if (!(attribute instanceof Formatting))
					return true;
		
		StringBuilder text = new StringBuilder();
		for (SingleStyleMessage part : styledMessage.getStyledParts())
			text.append(part.getText());
		
		if (!text.toString().equals(ChatColor.stripColor(originalText)))
			return true;
		
		return false;
	}
}

package com.festp.messaging;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.festp.Logger;
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

public class ChatComponentChatter implements Chatter
{
	private static final String PLACEHOLDER_NAME = "%1$s";
	private static final String PLACEHOLDER_MESSAGE = "%2$s";
	
	private static final String WHISPER_STYLE_CODES = ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
	
	private final Config config;
	private final StyledMessageBuilderFactory factory;
	private final MessageSender messageSender;

	public ChatComponentChatter(Config config, StyledMessageBuilderFactory factory, MessageSender messageSender)
	{
		this.config = config;
		this.factory = factory;
		this.messageSender = messageSender;
	}

	/**
	 * Generates and sends chat components for messages containing StyleAttribute other than Formatting<br>
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

		ChatComponentBuilder componentBuilder = newComponentBuilder();
		componentBuilder.appendStyledMessage(builder.build());
		BaseComponent components = componentBuilder.build();
		for (Player p : recipients) {
			messageSender.sendChatComponents(p, components);
		}
		return true;
	}

	public boolean sendIntercepted(
			CommandSender sender,
			Player recipient,
			List<BaseComponent> formatComponents,
			List<Integer> messagePositions,
			List<BaseComponent> message)
	{
		boolean isLogging = config.get(Config.Key.LOG_DEBUG, false);
		boolean changed = false;
		List<BaseComponent> updatedMessage = Lists.newArrayList();
		// iterate groups without events imitating closed parts
		for (int i = 0; i < message.size(); i++) {
			StringBuilder text = new StringBuilder();
			// our events have less priority
			while (i < message.size() && message.get(i).getClickEvent() == null && message.get(i).getHoverEvent() == null) {
				BaseComponent component = message.get(i);
				String componentText = component.toLegacyText();
				// {"text":"§nabc§rdef"} is not equal to {"text":"abc§rdef", "underlined":true}
				String baseColorCodes = component.getColorRaw() != null ? component.getColorRaw().toString() : ""
						              + (component.isUnderlined() ? ChatColor.UNDERLINE.toString() : "")
						              + (component.isBold() ? ChatColor.BOLD.toString() : "")
						              + (component.isItalic() ? ChatColor.ITALIC.toString() : "")
						              + (component.isObfuscated() ? ChatColor.MAGIC.toString() : "")
						              + (component.isStrikethrough() ? ChatColor.STRIKETHROUGH.toString() : "");
				componentText.replace(ChatColor.RESET.toString(), ChatColor.RESET.toString() + baseColorCodes);
				text.append(componentText);
				i++;
			}
			
			if (text.length() > 0) {
				StyledMessageBuilder builder = factory.create(sender);
				StyledMessage styledMessage = builder.append(text.toString()).build();
				
				ChatComponentBuilder componentBuilder = newComponentBuilder();
				componentBuilder.appendStyledMessage(styledMessage);
				updatedMessage.add(componentBuilder.build());
				if (isLogging) Logger.info("Processing message part: "
						+ text.toString() + " -> " + ComponentSerializer.toString(componentBuilder.build())
						+ " (changed: " + componentBuilder.isChanged() + ")");
				changed |= componentBuilder.isChanged();
			}
			
			if (i < message.size()) {
				updatedMessage.add(message.get(i));
			}
		}
		
		if (!changed) {
			if (isLogging) Logger.info("Message has not been changed, will not resend: " + ComponentSerializer.toString(message));
			return false;
		}
		
		List<BaseComponent> components = Lists.newArrayList(formatComponents);
		List<Integer> indices = Lists.newArrayList(messagePositions);
		// inverse order to not update indices
		for (int i = indices.size() - 1; i >= 0; i--) {
			// TODO test if need to deep copy components if indices.size() > 1
			components.addAll(indices.get(i), updatedMessage);
		}
		messageSender.sendChatComponents(recipient, components.toArray(new BaseComponent[0]));
		return true;
	}
	
	public boolean sendWhisperMessage(CommandSender sender, Player[] recipients, String message)
	{
		StyledMessageBuilder builder = factory.create(sender);
		StyledMessage styledMessage = builder.append(message).build();
		if (!canSend(styledMessage, message))
			return false;
		
		String fromIdentifier = "commands.message.display.outgoing"; // "You whisper to %s: %s"
		String toIdentifier = "commands.message.display.incoming"; // "%s whispers to you: %s"
		
		Formatting baseFormatting = new Formatting().update(WHISPER_STYLE_CODES);
		ChatComponentBuilder messageBuilder = newComponentBuilder();
		messageBuilder.appendStyledMessage(styledMessage);
		BaseComponent messageComponent = messageBuilder.build();

		ChatComponentBuilder nameFromBuilder = newComponentBuilder();
		builder.clear();
		appendSender(builder, sender, true);
		nameFromBuilder.appendStyledMessage(builder.build());
		BaseComponent nameFromComponent = nameFromBuilder.build();
		
		for (Player recipient : recipients)
		{
			if (sender instanceof Player)
			{
				ChatComponentBuilder nameToBuilder = newComponentBuilder();
				builder.clear();
				appendSender(builder, recipient, true);
				nameToBuilder.appendStyledMessage(builder.build());
				BaseComponent nameToComponent = nameToBuilder.build();
				
				ChatComponentBuilder from = newComponentBuilder(baseFormatting);
				from.appendTranslated(fromIdentifier, Lists.newArrayList(nameToComponent, messageComponent));
				messageSender.sendChatComponents((Player)sender, from.build());
			}
			
			ChatComponentBuilder to = newComponentBuilder(baseFormatting);
			to.appendTranslated(toIdentifier, Lists.newArrayList(nameFromComponent, messageComponent));
			messageSender.sendChatComponents(recipient, to.build());
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
		ChatComponentBuilder componentBuilder = newComponentBuilder();
		Formatting formatting = new Formatting().update(WHISPER_STYLE_CODES);
		componentBuilder.appendJoinedLinks(links, formatting, ", ");
		BaseComponent components = componentBuilder.build();
		
		if (sender instanceof Player)
			messageSender.sendChatComponents((Player)sender, components);
		
		for (Player recipient : recipients)
		{
			if (recipient == sender)
				continue;
			messageSender.sendChatComponents(recipient, components);
		}
		return true;
	}
	
	private ChatComponentBuilder newComponentBuilder(Formatting baseFormatting)
	{
		return new ChatComponentBuilder(config.getDisplaySettings(), baseFormatting);
	}
	
	private ChatComponentBuilder newComponentBuilder()
	{
		return newComponentBuilder(new Formatting());
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

package com.festp.handlers;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.festp.handlers.MessageInfoProvider.MessageInfo;
import com.festp.messaging.Chatter;
import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPacketListener extends PacketAdapter
{
	private static final PacketType CHAT_PACKET = PacketType.Play.Server.CHAT;
	private static final PacketType SYSTEM_CHAT_PACKET = PacketType.Play.Server.SYSTEM_CHAT;

	private final Chatter chatter;
	private final MessageInfoProvider messageInfoProvider;
	
	public ChatPacketListener(JavaPlugin plugin, Chatter chatter, MessageInfoProvider messageInfoProvider) {
		super(plugin, ListenerPriority.HIGHEST, CHAT_PACKET, SYSTEM_CHAT_PACKET);
		this.chatter = chatter;
		this.messageInfoProvider = messageInfoProvider;
	}

	public void register() {
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	public void unregister() {
		ProtocolLibrary.getProtocolManager().removePacketListener(this);
	}
	
    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == SYSTEM_CHAT_PACKET)
        	onSystemChatMessage(event);
        else if (event.getPacketType() == CHAT_PACKET)
        	onChatMessage(event);
    }

	private void onChatMessage(PacketEvent event)
    {	
		if (event.isCancelled())
			return;
		
        UUID senderUuid = event.getPacket().getUUIDs().read(0);
        
        String messageContent = "";
		try {
			// according to https://www.wiki.vg/Protocol#Player_Chat_Message
			// a is String message, b is Instant timestamp, c is long salt, d is LastSeenMessages
	        Object signedMessageBody = event.getPacket().getModifier().readSafely(3);
			Field contentField = signedMessageBody.getClass().getDeclaredField("a");
			contentField.setAccessible(true);
	    	messageContent = contentField.get(signedMessageBody).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        /*WrappedChatComponent senderName;
		try {
			// according to https://www.wiki.vg/Protocol#Player_Chat_Message
			// a is int chat type, b is IChatBaseComponent sender name, c is IChatBaseComponent target name
	        Object chatMessageType = event.getPacket().getModifier().readSafely(6);
	        Field nameField = chatMessageType.getClass().getDeclaredField("b");
	        nameField.setAccessible(true);
	    	senderName = WrappedChatComponent.fromHandle(nameField.get(chatMessageType));
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		boolean sent = chatter.sendFormatted(Bukkit.getPlayer(senderUuid), Lists.newArrayList(event.getPlayer()), messageContent, "<%1$s> %2$s", false);
		if (sent)
			event.setCancelled(true);
    }
    
    private void onSystemChatMessage(PacketEvent event)
    {
    	if (event.isCancelled())
    		return;
    	
    	String rawJson = event.getPacket().getStrings().read(0);
    	ParseResult parseResult = parseMessage(rawJson);
    	if (parseResult == null)
    		return;
		
		boolean sent = chatter.sendIntercepted(parseResult.messageInfo.sender, event.getPlayer(),
											   parseResult.formatComponents, parseResult.messagePositions, parseResult.messageComponents);
		if (sent)
			event.setCancelled(true);
	}
	
	private ParseResult parseMessage(String rawJson)
	{ 
		BaseComponent[] components = ComponentSerializer.parse(rawJson);
		for (MessageInfo messageInfo : messageInfoProvider.getRecentMessages())
		{
			ParseResult result = tryParse(messageInfo, components);
			if (result != null)
				return result;
		}
		return null;
	}

	private ParseResult tryParse(MessageInfo messageInfo, BaseComponent[] components)
	{
		String colorlessMessage = BaseComponent.toPlainText(components);
		String colorlessSender = ChatColor.stripColor(messageInfo.sender.getDisplayName());
		String colorlessContent = ChatColor.stripColor(messageInfo.content);
		if (!colorlessMessage.contains(colorlessSender))
			return null;
		
		if (!colorlessMessage.contains(colorlessContent))
			return null;

		List<Integer> potentialPositions = Lists.newArrayList();
		int start = colorlessMessage.indexOf(colorlessContent);
		while (start >= 0)
		{
			potentialPositions.add(start);
			start = colorlessMessage.indexOf(colorlessContent, start + 1);
		}
		if (potentialPositions.isEmpty())
			return null;

		// BaseComponent can contain BaseComponents...
		BaseComponent[] flattenComponents = components;

		int componentStart = 0;
		int positionIndex = 0;
		start = potentialPositions.get(positionIndex);
		List<BaseComponent> formatComponents = Lists.newArrayList();
		List<BaseComponent> messageComponents = Lists.newArrayList();
		List<Integer> messagePositions = Lists.newArrayList();
		for (int i = 0; i < flattenComponents.length; i++)
		{
			BaseComponent component = flattenComponents[i];
			String componentText = component.toPlainText();
			int componentLength = componentText.length();
			int componentEnd = componentStart + componentLength;
			if (componentEnd < start) {
				componentStart = componentEnd;
				continue;
			}
			
			// check if current component is valid beginning
			while (componentStart <= start && start < componentEnd)
			{
				boolean isValid = component instanceof TextComponent;
				int sumLength = componentEnd - start;
				for (int j = i + 1; j < flattenComponents.length && sumLength < colorlessContent.length() && isValid; j++)
				{
					if (!(flattenComponents[j] instanceof TextComponent)) {
						isValid = false;
						break;
					}
					sumLength += flattenComponents[j].toPlainText().length();
				}
				if (!isValid)
				{
					positionIndex++;
					if (positionIndex < potentialPositions.size()) {
						start = potentialPositions.get(positionIndex);
					}
					else {
						start = colorlessMessage.length();
					}
				}
			}
			
			// add format component
			if (componentStart < start)
			{
				TextComponent newComponent;
				int textEnd = Math.min(componentLength, start - componentStart);
				if (textEnd == componentLength) {
					newComponent = (TextComponent) component;
				}
				else {
					String newText = componentText.substring(0, textEnd);
					newComponent = ((TextComponent) component).duplicate();
					newComponent.setText(newText);
				}
				formatComponents.add(newComponent);
			}
			if (componentStart <= start && start < componentEnd) // TODO test < or <=
			{
				messagePositions.add(formatComponents.size());
			}
			
			int end = start + colorlessContent.length();
			int textStart = Math.max(0, start - componentStart);
			int textEnd = Math.min(componentLength, end - componentStart);
			TextComponent newComponent;
			if (textStart == 0 && textEnd == componentLength) {
				newComponent = (TextComponent) component;
			}
			else {
				String newText = componentText.substring(textStart, textEnd);
				newComponent = ((TextComponent) component).duplicate();
				newComponent.setText(newText);
			}
			messageComponents.add(newComponent);

			componentStart = componentEnd;
		}
		
		return new ParseResult(messageInfo, formatComponents, messageComponents, messagePositions);
	}
	
	private static class ParseResult
	{
		public final MessageInfo messageInfo;
		public final Iterable<BaseComponent> formatComponents;
		public final Iterable<BaseComponent> messageComponents;
		public final Iterable<Integer> messagePositions;
		
		public ParseResult(
				MessageInfo messageInfo,
				Iterable<BaseComponent> formatComponents,
				Iterable<BaseComponent> messageComponents,
				Iterable<Integer> messagePositions)
		{
			this.messageInfo = messageInfo;
			this.formatComponents = formatComponents;
			this.messageComponents = messageComponents;
			this.messagePositions = messagePositions;
		}
	}
}

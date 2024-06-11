package com.festp.handlers;

import java.lang.reflect.Field;
import java.util.Collection;
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
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
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
		// assumption: sender name could not be a parsable component (no need to distinguish between sender and content)
		// assumption: format has only one content specifier
		String colorlessMessage = BaseComponent.toPlainText(components);
		String colorlessSender = ChatColor.stripColor(messageInfo.sender.getDisplayName());
		if (!colorlessMessage.contains(colorlessSender))
			return null;

		String colorlessContent = ChatColor.stripColor(messageInfo.content);
		if (!colorlessMessage.contains(colorlessContent))
			return null;

		// BaseComponents have tree structure
		BaseComponent[] flatComponents = flattenComponents(components);
		ComponentSubstringData substringData = getSubstringData(flatComponents, colorlessContent);
		if (substringData == null)
			return null;

		List<BaseComponent> formatComponents = Lists.newArrayList();
		List<BaseComponent> messageComponents = Lists.newArrayList();
		List<Integer> messagePositions = Lists.newArrayList();
		for (int i = 0; i < substringData.startComponentIndex; i++)
			formatComponents.add(flatComponents[i]);
		
		if (substringData.startInnerIndex == 0) {
			messageComponents.add(flatComponents[substringData.startComponentIndex]);
		}
		else {
			TextComponent formatComponent = (TextComponent) flatComponents[substringData.startComponentIndex];
			String componentText = formatComponent.getText();
			TextComponent messageComponent = formatComponent.duplicate();
			formatComponent.setText(componentText.substring(0, substringData.startInnerIndex));
			messageComponent.setText(componentText.substring(substringData.startInnerIndex));
			formatComponents.add(formatComponent);
			messageComponents.add(messageComponent);
		}
		
		messagePositions.add(formatComponents.size());

		for (int i = substringData.startComponentIndex + 1; i < substringData.endComponentIndex; i++)
			messageComponents.add(flatComponents[i]);

		if (substringData.endInnerIndex == flatComponents[substringData.endComponentIndex].toPlainText().length()) {
			messageComponents.add(flatComponents[substringData.startComponentIndex]);
		}
		else {
			TextComponent formatComponent = (TextComponent) flatComponents[substringData.startComponentIndex];
			String componentText = formatComponent.getText();
			TextComponent messageComponent = formatComponent.duplicate();
			messageComponent.setText(componentText.substring(0, substringData.startInnerIndex));
			formatComponent.setText(componentText.substring(substringData.startInnerIndex));
			formatComponents.add(formatComponent);
			messageComponents.add(messageComponent);
		}

		for (int i = substringData.endComponentIndex + 1; i < flatComponents.length; i++)
			formatComponents.add(flatComponents[i]);
		
		return new ParseResult(messageInfo, formatComponents, messageComponents, messagePositions);
	}
	
	private BaseComponent[] flattenComponents(BaseComponent[] components)
	{
		List<BaseComponent> result = Lists.newArrayList();
		for (BaseComponent component : components)
			for (BaseComponent newComponent : flattenComponent(component, new TextComponent()))
				result.add(newComponent);
		
		return result.toArray(new BaseComponent[0]);
	}
	
	private Collection<? extends BaseComponent> flattenComponent(BaseComponent component, BaseComponent parentFormatting)
	{
		BaseComponent componentCopy = component.duplicate();
		// TODO test formatting identity
		componentCopy.copyFormatting(parentFormatting, FormatRetention.ALL, true);
		componentCopy.copyFormatting(component, FormatRetention.ALL, false);
		
		Field extraField;
		try {
			extraField = BaseComponent.class.getDeclaredField("extra");
			extraField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return null;
		}

		// componentCopy.setExtra(Lists.newArrayList());
		// Invalid chat component: Unexpected empty array of components
		try {
			extraField.set(componentCopy, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		List<BaseComponent> result = Lists.newArrayList(componentCopy);
		if (component.getExtra() == null)
			return result;
		
		for (BaseComponent extra : component.getExtra())
			result.addAll(flattenComponent(extra, componentCopy));

		return result;
	}
	
	private ComponentSubstringData getSubstringData(BaseComponent[] flattenComponents, String substring)
	{
		// TODO color codes...
		// merge plain text from consecutive TextComponents
		// checking if plain text contains substring
		int startComponentIndex = -1;
		int startInnerIndex = -1;
		int endComponentIndex = -1;
		int endInnerIndex = -1;
		for (int i = 0; i < flattenComponents.length; i++)
		{
			if (!(flattenComponents[i] instanceof TextComponent)) {
				continue;
			}
			
			StringBuilder text = new StringBuilder();
			while (i < flattenComponents.length && flattenComponents[i] instanceof TextComponent) {
				String componentText = flattenComponents[i].toPlainText();
				text.append(componentText);
				int startIndex = text.indexOf(substring);
				if (startIndex >= 0) {
					endComponentIndex = i;
					endInnerIndex = componentText.length() - (text.length() - (startIndex + substring.length()));
					break;
				}
				i++;
			}
			if (endComponentIndex >= 0)
			{
				int remainingLength = substring.length() - endInnerIndex;
				if (remainingLength <= 0) {
					startComponentIndex = i;
					startInnerIndex = -remainingLength;
				}
				else {
					while (i >= 0) {
						i--;
						int componentLength = flattenComponents[i].toPlainText().length();
						remainingLength -= componentLength;
						if (remainingLength <= 0) {
							startComponentIndex = i;
							startInnerIndex = -remainingLength;
							break;
						}
					}
				}
				break;
			}
		}
		if (startComponentIndex < 0)
			return null;
		
		return new ComponentSubstringData(startComponentIndex, startInnerIndex, endComponentIndex, endInnerIndex);
	}
	
	private static class ParseResult
	{
		public final MessageInfo messageInfo;
		public final Collection<? extends BaseComponent> formatComponents;
		public final Collection<? extends BaseComponent> messageComponents;
		public final Collection<? extends Integer> messagePositions;
		
		public ParseResult(
				MessageInfo messageInfo,
				Collection<? extends BaseComponent> formatComponents,
				Collection<? extends BaseComponent> messageComponents,
				Collection<? extends Integer> messagePositions)
		{
			this.messageInfo = messageInfo;
			this.formatComponents = formatComponents;
			this.messageComponents = messageComponents;
			this.messagePositions = messagePositions;
		}
	}
	
	private static class ComponentSubstringData
	{
		public final int startComponentIndex;
		public final int startInnerIndex;
		public final int endComponentIndex;
		public final int endInnerIndex;
		
		public ComponentSubstringData(int startComponentIndex, int startInnerIndex,int endComponentIndex, int endInnerIndex) {
			this.startComponentIndex = startComponentIndex;
			this.startInnerIndex = startInnerIndex;
			this.endComponentIndex = endComponentIndex;
			this.endInnerIndex = endInnerIndex;
		}
	}
}

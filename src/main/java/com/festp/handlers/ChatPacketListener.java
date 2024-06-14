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
			// TODO optimize reflection
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

	private static ParseResult tryParse(MessageInfo messageInfo, BaseComponent[] components)
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

		int messageComponentLength = flatComponents[substringData.startComponentIndex].toPlainText().length();
		List<BaseComponent> formatComponents = Lists.newArrayList();
		List<BaseComponent> messageComponents = Lists.newArrayList();
		List<Integer> messagePositions = Lists.newArrayList();
		for (int i = 0; i < substringData.startComponentIndex; i++)
			formatComponents.add(flatComponents[i]);
		
		splitComponent(flatComponents[substringData.startComponentIndex], substringData.startInnerIndex, formatComponents, messageComponents);
		
		messagePositions.add(formatComponents.size());

		for (int i = substringData.startComponentIndex + 1; i < substringData.endComponentIndex; i++)
			messageComponents.add(flatComponents[i]);

		if (substringData.startComponentIndex == substringData.endComponentIndex) {
			BaseComponent componentPart = messageComponents.remove(messageComponents.size() - 1);
			// component text could be prefixed by color codes
			int index = componentPart.toPlainText().length() - (messageComponentLength - substringData.endInnerIndex);
			splitComponent(componentPart, index, messageComponents, formatComponents);
		}
		else {
			splitComponent(flatComponents[substringData.endComponentIndex], substringData.endInnerIndex, messageComponents, formatComponents);
		}

		for (int i = substringData.endComponentIndex + 1; i < flatComponents.length; i++)
			formatComponents.add(flatComponents[i]);
		
		return new ParseResult(messageInfo, formatComponents, messageComponents, messagePositions);
	}
	
	private static void splitComponent(BaseComponent component, int index, List<BaseComponent> left, List<BaseComponent> right)
	{
		if (index == 0) {
			right.add(component);
		}
		else if (index == component.toPlainText().length()) {
			left.add(component);
		}
		else {
			TextComponent leftComponent = (TextComponent) component;
			String componentText = leftComponent.getText();
			String leftText = componentText.substring(0, index);
			leftComponent.setText(leftText);
			left.add(leftComponent);
			
			TextComponent rightComponent = leftComponent.duplicate();
			StringBuilder colorCodes = new StringBuilder();
			for (int i = 0; i < leftText.length(); i++)
			{
				if (leftText.charAt(i) == ChatColor.COLOR_CHAR) {
					colorCodes.append(leftText.charAt(i));
					i++;
					if (i < leftText.length())
						colorCodes.append(leftText.charAt(i));
					
					continue;
				}
			}
			rightComponent.setText(colorCodes + componentText.substring(index));
			right.add(rightComponent);
		}
	}
	
	private static BaseComponent[] flattenComponents(BaseComponent[] components)
	{
		List<BaseComponent> result = Lists.newArrayList();
		for (BaseComponent component : components)
			for (BaseComponent newComponent : flattenComponent(component, new TextComponent()))
				result.add(newComponent);
		
		return result.toArray(new BaseComponent[0]);
	}
	
	private static Collection<? extends BaseComponent> flattenComponent(BaseComponent component, BaseComponent parentFormatting)
	{
		BaseComponent componentCopy = component.duplicate();
		// TODO test identity of formatting
		componentCopy.copyFormatting(parentFormatting, FormatRetention.ALL, true);
		componentCopy.copyFormatting(component, FormatRetention.ALL, false);
		resetExtra(componentCopy);

		List<BaseComponent> result = Lists.newArrayList(componentCopy);
		if (component.getExtra() == null)
			return result;
		
		for (BaseComponent extra : component.getExtra())
			result.addAll(flattenComponent(extra, componentCopy));

		return result;
	}
	
	private static void resetExtra(BaseComponent component)
	{
		// could not just
		// 1. componentCopy.setExtra(Lists.newArrayList());
		// causes Invalid chat component: Unexpected empty array of components
		// 2. componentCopy.setExtra(null);
		// throws NullPointerException
		// TODO optimize reflection
		Field extraField;
		try {
			extraField = BaseComponent.class.getDeclaredField("extra");
			extraField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return;
		}

		try {
			extraField.set(component, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private static ComponentSubstringData getSubstringData(BaseComponent[] flattenComponents, String colorlessSubstring)
	{
		// merge plain text from consecutive TextComponents
		// checking if plain text contains substring
		int startComponentIndex = -1;
		int startInnerIndex = -1;
		int endComponentIndex = -1;
		int endInnerIndex = -1;
		int substringLength = 0;
		for (int i = 0; i < flattenComponents.length; i++)
		{
			if (!(flattenComponents[i] instanceof TextComponent)) {
				continue;
			}
			
			StringBuilder text = new StringBuilder();
			while (i < flattenComponents.length && flattenComponents[i] instanceof TextComponent) {
				String componentText = flattenComponents[i].toPlainText();
				text.append(componentText);
				SubstringBounds substringBounds = indexOfColorCoded(text.toString(), colorlessSubstring);
				if (substringBounds != null) {
					endComponentIndex = i;
					endInnerIndex = componentText.length() - (text.length() - substringBounds.end);
					substringLength = substringBounds.end - substringBounds.begin;
					break;
				}
				i++;
			}
			if (endComponentIndex >= 0)
			{
				int remainingLength = substringLength - endInnerIndex;
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
	
	private static SubstringBounds indexOfColorCoded(String text, String colorlessSubstring)
	{
		String colorlessText = ChatColor.stripColor(text.toString());
		int toSkip = colorlessText.indexOf(colorlessSubstring);
		if (toSkip < 0)
			return null;
		
		int actualStart = 0;
		while (toSkip > 0)
		{
			if (text.charAt(actualStart) == ChatColor.COLOR_CHAR) {
				actualStart += 2;
				continue;
			}
			toSkip--;
			actualStart++;
		}
		
		int actualEnd = actualStart;
		toSkip = colorlessSubstring.length();
		while (toSkip > 0)
		{
			if (text.charAt(actualEnd) == ChatColor.COLOR_CHAR) {
				actualEnd += 2;
				continue;
			}
			toSkip--;
			actualEnd++;
		}
		
		return new SubstringBounds(actualStart, actualEnd);
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
	
	private static class SubstringBounds
	{
		public final int begin;
		public final int end;
		
		public SubstringBounds(int begin, int end) {
			this.begin = begin;
			this.end = end;
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

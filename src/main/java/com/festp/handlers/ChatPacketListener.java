package com.festp.handlers;

import java.lang.reflect.Field;
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
		
		boolean sent = chatter.sendFormatted(Lists.newArrayList(event.getPlayer()), Bukkit.getPlayer(senderUuid), messageContent, "<%1$s> %2$s", false);
		if (sent)
			event.setCancelled(true);
    }
    
    private void onSystemChatMessage(PacketEvent event)
    {
    	if (event.isCancelled())
    		return;
    	
    	String rawJson = event.getPacket().getStrings().read(0);
		BaseComponent[] components = ComponentSerializer.parse(rawJson);

		String legacy = BaseComponent.toLegacyText(components);
		MessageInfo messageInfo = tryGetMessageInfo(legacy);
		if (messageInfo == null || !messageInfo.recipients.contains(event.getPlayer()))
			return;
		
		boolean sent = chatter.sendIntercepted(event.getPlayer(), components);
		if (sent)
			event.setCancelled(true);
	}

	private MessageInfo tryGetMessageInfo(String message)
	{
		String colorlessMessage = ChatColor.stripColor(message);
		MessageInfo[] recentMessages = messageInfoProvider.getRecentMessages();
		System.out.println("Message count: " + recentMessages.length);
		for (MessageInfo messageInfo : recentMessages)
		{
			System.out.println("[MessageInfo] " + messageInfo.sender.getDisplayName() + ": " + messageInfo.content);
			if (!colorlessMessage.contains(ChatColor.stripColor(messageInfo.content))) continue;
			if (!colorlessMessage.contains(ChatColor.stripColor(messageInfo.sender.getDisplayName()))) continue;
			return messageInfo;
		}
		return null;
	}
}

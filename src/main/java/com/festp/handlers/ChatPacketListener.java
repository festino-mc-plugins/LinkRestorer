package com.festp.handlers;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.festp.Logger;
import com.festp.config.Config;
import com.festp.handlers.MessageInfoProvider.MessageInfo;
import com.festp.messaging.Chatter;
import com.festp.utils.BaseComponentUtils;
import com.festp.utils.BaseComponentUtils.ParseResult;
import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPacketListener extends PacketAdapter
{
	private static final PacketType CHAT_PACKET = PacketType.Play.Server.CHAT;
	private static final PacketType SYSTEM_CHAT_PACKET = PacketType.Play.Server.SYSTEM_CHAT;

	private final Chatter chatter;
	private final MessageInfoProvider messageInfoProvider;
	private final Config config;
	
	public ChatPacketListener(JavaPlugin plugin, Chatter chatter, MessageInfoProvider messageInfoProvider, Config config) {
		super(plugin, ListenerPriority.HIGHEST, CHAT_PACKET, SYSTEM_CHAT_PACKET);
		this.chatter = chatter;
		this.messageInfoProvider = messageInfoProvider;
		this.config = config;
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

		boolean isLogging = config.get(Config.Key.LOG_DEBUG, false);
    	if (isLogging) Logger.info("Processing chat message from " + Bukkit.getPlayer(senderUuid).getName() + " to " + event.getPlayer().getName());
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

		boolean isLogging = config.get(Config.Key.LOG_DEBUG, false);
    	if (isLogging) Logger.info("Processing system message from " + parseResult.messageInfo.sender.getName() + " to " + event.getPlayer().getName());
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
			ParseResult result = BaseComponentUtils.tryParse(messageInfo, components);
			if (result != null)
				return result;
		}
		return null;
	}

}

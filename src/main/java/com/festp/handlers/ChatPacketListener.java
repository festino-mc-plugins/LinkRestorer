package com.festp.handlers;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.festp.messaging.Chatter;
import com.google.common.collect.Lists;

public class ChatPacketListener extends PacketAdapter
{
	private static final PacketType CHAT_PACKET = PacketType.Play.Server.CHAT;
	private static final PacketType SYSTEM_CHAT_PACKET = PacketType.Play.Server.SYSTEM_CHAT;

	private final Chatter chatter;
	
	public ChatPacketListener(JavaPlugin plugin, Chatter chatter) {
		super(plugin, ListenerPriority.HIGHEST, CHAT_PACKET, SYSTEM_CHAT_PACKET);
		this.chatter = chatter;
	}

	public void register() {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		
		// cannot addPacketListener in Main: NoClassDefFoundError if ProtocolLib is not presented
		protocolManager.addPacketListener(this);
	}

	public void unregister() {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.removePacketListener(this);
	}
	
    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == SYSTEM_CHAT_PACKET)
        	onSystemChatMessage(event);
        
        // getChatComponents => [null] 
        else if (event.getPacketType() == CHAT_PACKET)
        	onChatMessage(event);
    }

	private void onChatMessage(PacketEvent event)
    {	
		if (event.isCancelled())
			return;
		
        UUID senderUuid = event.getPacket().getUUIDs().read(0);
		
		System.out.println("Components: " + event.getPacket().getChatComponents().getValues());
        StructureModifier<WrappedChatComponent> ccs = event.getPacket().getChatComponents();
        for (int i = 0; i < ccs.size(); ++i) {
            WrappedChatComponent msg = ccs.readSafely(i);
            if (msg != null)
            	System.out.println("\"" + msg.toString() + "\" + json: \"" + msg.getJson() + "\"");
            else
            	System.out.println("null");
        }

        /*for (int i = 0; i < event.getPacket().getModifier().size(); ++i) {
            Object field = event.getPacket().getModifier().readSafely(i);
            if (field != null)
            	System.out.println(field.toString());
            else
            	System.out.println("null");
        }*/
        
        String messageContent = "";
        WrappedChatComponent senderName;
		try {
			// according to https://www.wiki.vg/Protocol#Player_Chat_Message
			// a is String message, b is Instant timestamp, c is long salt, d is LastSeenMessages
	        Object signedMessageBody = event.getPacket().getModifier().readSafely(3);
			// a is int chat type, b is IChatBaseComponent sender name, c is IChatBaseComponent target name
	        Object chatMessageType = event.getPacket().getModifier().readSafely(6);

			printFields(signedMessageBody.getClass());
			printFields(chatMessageType.getClass());
	        
			Field contentField = signedMessageBody.getClass().getDeclaredField("a");
			contentField.setAccessible(true);
	    	System.out.println(signedMessageBody.getClass() + " " + contentField.get(signedMessageBody).getClass() + " " + contentField.get(signedMessageBody));
	    	messageContent = contentField.get(signedMessageBody).toString();
			
	        Field nameField = chatMessageType.getClass().getDeclaredField("b");
	        nameField.setAccessible(true);
	    	System.out.println(chatMessageType.getClass() + " " + nameField.get(chatMessageType).getClass() + " " + nameField.get(chatMessageType));
	    	senderName = WrappedChatComponent.fromHandle(nameField.get(chatMessageType));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean sent = chatter.sendFormatted(Lists.newArrayList(event.getPlayer()), Bukkit.getPlayer(senderUuid), messageContent, "<%1$s> %2$s", false);
		if (sent)
			event.setCancelled(true);
    }
    
    private void onSystemChatMessage(PacketEvent event) {
		System.out.println(event.getPacket().toString());
    	String rawJson = event.getPacket().getStrings().read(0);
    	// TODO check if it is a chat message (how???)
    	// TODO concat plain text, parse it, try merge events (our have less priority), send only if any event has changed
        //event.setCancelled(true);
	}
    
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
    
    private static void printFields(Class<?> clazz)
    {
		System.out.println(clazz.getName() + " fields:");
    	for (Field field : clazz.getDeclaredFields()) {
    		System.out.println("  " + field.getName() + " " + field.getType());
    	}
    }
}

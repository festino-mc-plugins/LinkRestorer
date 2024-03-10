package com.festp.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.festp.Chatter;
import com.festp.parsing.Link;
import com.festp.parsing.StyledMessage;

public class RawJsonBuilder
{
	RawJsonBuilderSettings settings;
	StringBuilder command;
	
	public RawJsonBuilder(RawJsonBuilderSettings settings)
	{
		this.settings = settings;
		command = new StringBuilder();
	}
	public RawJsonBuilder(RawJsonBuilderSettings settings, CharSequence c)
	{
		this.settings = settings;
		command = new StringBuilder(c);
	}
	
	public void tryWrap(String str, String color)
	{
		command.append(tryGetWrapped(str, color));
	}
	public void append(String str) {
		command.append(str);
	}

	public void appendSender(CommandSender sender, String color, boolean decorating)
	{
		if (sender instanceof Player)
			appendPlayer((Player)sender, color, decorating);
		else {
			String name = Chatter.getDisplayName(sender);
			wrap(name, color);
		}
	}
	public void appendPlayer(Player player, String color, boolean decorating)
	{
		String nickname;
		if (decorating)
			nickname = Chatter.getDisplayName(player);
		else
			nickname = player.getName();
		String uuid = player.getUniqueId().toString();
		command.append("{\"text\":\"" + color + nickname + "\",");
		command.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + nickname + "\\nType: Player\\n" + uuid + "\"},");
		command.append("\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell " + nickname + " \"}");
		command.append("},");
	}
	
	public void appendMessage(StyledMessage styledMessage, String color)
	{
		String message = styledMessage.plainText;
		Iterable<Link> links = styledMessage.links;
		startList();
        
		int lastIndex = 0;
		for (Link link : links)
		{
			if (lastIndex < link.beginIndex) {
				tryWrap(message.substring(lastIndex, link.beginIndex), color);
			}
			else if (lastIndex == 0 && link.beginIndex == 0) {
				// workaround for links at the beginning: they would convert all the plain text to that link
				wrap("", color);
			}
			
			appendLink(link, color);
			
			lastIndex = link.endIndex;
		}
		if (lastIndex < message.length()) {
			tryWrap(message.substring(lastIndex), color);
		}
		
		endList();
	}
	
	public void appendJoinedLinks(Iterable<Link> links, String color, String sep)
	{
		startList();
        
		boolean isFirst = true;
		String wrappedSep = tryGetWrapped(sep, color);
		for (Link link : links)
		{
			if (isFirst) {
				isFirst = false;
			}
			else {
				command.append(wrappedSep);
			}
			appendLink(link, color);
		}

		endList();
	}
	
	public void appendLink(Link link, String color)
	{
		String linkStr = link.getString();
		String linkClick = LinkUtils.applyBrowserEncoding(linkStr);
		if (!link.hasProtocol)
			linkClick = "https://" + linkClick;
		command.append("{\"text\":\"");
		command.append(color);
		if (settings.isLinkUnderlined)
			command.append(ChatColor.UNDERLINE);
		command.append(linkStr);
		command.append("\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"");
		command.append(linkClick);
		command.append("\"}},");
	}
	
	/** Check for more info: <a>https://minecraft.fandom.com/wiki/Raw_JSON_text_format#Translated_Text</a> */
	public void appendTranslated(String identifier, CharSequence[] textComponents, String color)
	{
		command.append("{");
		command.append(getColorTags(color));
		command.append("\"translate\":\"");
		command.append(identifier);
		command.append("\"");
		if (textComponents == null || textComponents.length == 0) {
			command.append("},");
			return;
		}
		command.append(",\"with\":[");
		int n = 0;
		for (CharSequence component : textComponents) {
			if (n > 0)
				command.append(',');
			command.append(component);
			n++;
		}
		command.append("]},");
	}
	
	/** @param color is ChatColor.GRAY.toString() + ChatColor.ITALIC.toString()
	 *  @return "color":"gray","italic":"true", */
	private static String getColorTags(String color)
	{
		StringBuilder res = new StringBuilder();
		for (int i = 1; i < color.length(); i += 2)
		{
			ChatColor c = ChatColor.getByChar(color.charAt(i));
			if (c == ChatColor.ITALIC)
				res.append("\"italic\":\"true\",");
			else if (c == ChatColor.UNDERLINE)
				res.append("\"underlined\":\"true\",");
			else if (c == ChatColor.BOLD)
				res.append("\"bold\":\"true\",");
			else if (c == ChatColor.STRIKETHROUGH)
				res.append("\"strikethrough\":\"true\",");
			else if (c == ChatColor.MAGIC)
				res.append("\"obfuscated\":\"true\",");
			else {
				res.append("\"color\":\"");
				res.append(c.name().toLowerCase());
				res.append("\",");
			}
		}
		return res.toString();
	}
	
	public void startList()
	{
		command.append("[");
	}
	public void endList()
	{
		tryRemoveComma();
		command.append("]");
	}
	public String build()
	{
		tryRemoveComma();
		return command.toString();
	}
	public StringBuilder releaseStringBuilder() {
		tryRemoveComma();
		StringBuilder res = command;
		command = null;
		return res;
	}
	
	private void tryRemoveComma() {
		int index = command.length() - 1;
		if (index >= 0 && command.charAt(index) == ',')
			command.deleteCharAt(index);
	}
	
	private void wrap(String str, String color)
	{
		command.append(getWrapped(str, color));
	}

	private  String tryGetWrapped(String str, String color)
	{
		return str == "" ? "" : getWrapped(str, color);
	}
	
	private String getWrapped(String str, String color)
	{
		return "{\"text\":\"" + color + str + "\"},";
	}
}

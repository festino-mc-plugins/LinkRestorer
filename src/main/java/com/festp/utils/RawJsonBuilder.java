package com.festp.utils;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.festp.Chatter;
import com.festp.parsing.Link;
import com.festp.parsing.StyledMessage;
import com.festp.parsing.StyledMessageParser;
import com.festp.parsing.TextStyle;
import com.festp.parsing.TextStyleSwitch;

public class RawJsonBuilder
{
	private final RawJsonBuilderSettings settings;
	private final TextStyle baseStyle;
	private StringBuilder command;
	
	public RawJsonBuilder(RawJsonBuilderSettings settings)
	{
		this.settings = settings;
		this.baseStyle = new TextStyle();
		command = new StringBuilder();
	}
	public RawJsonBuilder(RawJsonBuilderSettings settings, TextStyle baseStyle)
	{
		this.settings = settings;
		this.baseStyle = baseStyle;
		command = new StringBuilder();
	}
	
	public void tryWrap(String str, TextStyle style)
	{
		command.append(tryGetWrapped(str, style));
	}
	public void append(String str) {
		command.append(str);
	}

	public void appendSender(CommandSender sender, TextStyle style, boolean decorating)
	{
		if (sender instanceof Player)
			appendPlayer((Player)sender, style, decorating);
		else {
			String name = Chatter.getDisplayName(sender);
			wrap(name, style);
		}
	}
	public void appendPlayer(Player player, TextStyle style, boolean decorating)
	{
		String nickname;
		if (decorating)
			nickname = Chatter.getDisplayName(player);
		else
			nickname = player.getName();
		String plainName = ChatColor.stripColor(nickname);
		String uuid = player.getUniqueId().toString();
		String eventsJson = new StringBuilder()
				.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + plainName + "\\nType: Player\\n" + uuid + "\"},")
				.append("\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell " + plainName + " \"},")
				.toString();
		
		wrapMultiColor(nickname, style, eventsJson);
	}
	
	public TextStyle wrapMultiColor(String s, TextStyle startStyle, String extraJson)
	{
		TextStyle style = startStyle;
		StyledMessage styledText = StyledMessageParser.parse(s);
		s = styledText.plainText;
		List<TextStyleSwitch> styleSwitches = styledText.styleSwitches;
		int startIndex, endIndex = 0;
		for (int i = 0; i <= styledText.styleSwitches.size(); i++)
		{
			startIndex = endIndex;
			if (i > 0) {
				style = styleSwitches.get(i - 1).style;
			}
			endIndex = i < styleSwitches.size() ? styleSwitches.get(i).index : s.length();
			if (startIndex < endIndex)
			{
				wrap(s.substring(startIndex, endIndex), style, extraJson);
			}
		}
		return style;
	}
	
	public void appendMessage(StyledMessage styledMessage, TextStyle style)
	{
		String message = styledMessage.plainText;
		List<Link> links = styledMessage.links;
		List<TextStyleSwitch> styles = styledMessage.styleSwitches;
		startList();
		
		int index = 0;
		int linkIndex = 0, styleIndex = 0;
		while (index < message.length())
		{
			// find style, endIndex and link (may be null)
			int endIndex = message.length();
			if (styleIndex < styles.size())
			{
				if (index >= styles.get(styleIndex).index) {
					style = styles.get(styleIndex).style;
					styleIndex++;
				}
			}
			if (styleIndex < styles.size()) {
				endIndex = styles.get(styleIndex).index;
			}
			
			Link link = null;
			if (linkIndex < links.size())
			{
				if (links.get(linkIndex).endIndex <= index)
				{
					linkIndex++;
				}
			}
			if (linkIndex < links.size())
			{
				link = links.get(linkIndex);
				if (index < link.beginIndex) {
					endIndex = Math.min(link.beginIndex, endIndex);
					link = null;
				}
				else {
					endIndex = Math.min(link.endIndex, endIndex);
				}
			}

			if (index == 0 && link != null) {
				// workaround for links at the beginning: they would convert all the plain text to that link
				wrap("", new TextStyle());
			}
			
			String text = message.substring(index, endIndex);
			if (link == null)
				tryWrap(text, style);
			else
				appendLink(text, link.getUrl(), style);

			index = endIndex;
		}
		
		endList();
	}
	
	public void appendJoinedLinks(Iterable<Link> links, TextStyle style, String sep)
	{
		startList();
        
		boolean isFirst = true;
		String wrappedSep = tryGetWrapped(sep, style);
		for (Link link : links)
		{
			if (isFirst) {
				isFirst = false;
			}
			else {
				command.append(wrappedSep);
			}
			appendLink(link.getText(), link.getUrl(), style);
		}

		endList();
	}
	
	public void appendLink(String text, String url, TextStyle style)
	{
		command.append("{");
		command.append(style.getJson());
		command.append("\"text\":\"");
		command.append(style.getCodes());
		if (settings.isLinkUnderlined)
			command.append(ChatColor.UNDERLINE);
		command.append(text);
		command.append("\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"");
		command.append(url);
		command.append("\"}},");
	}
	
	/** Check for more info: <a>https://minecraft.fandom.com/wiki/Raw_JSON_text_format#Translated_Text</a> */
	public void appendTranslated(String identifier, CharSequence[] textComponents, TextStyle style)
	{
		command.append("{");
		command.append(style.getFullJson());
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
	
	public void startList()
	{
		command.append("[").append("{").append(baseStyle.getFullJson()).append("\"text\":\"\"},");
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
	
	private void wrap(String str, TextStyle style)
	{
		wrap(str, style, "");
	}
	private void wrap(String str, TextStyle style, String extraJson)
	{
		command.append(getWrapped(str, style, extraJson));
	}

	private String tryGetWrapped(String str, TextStyle style)
	{
		return str == "" ? "" : getWrapped(str, style);
	}
	
	private String getWrapped(String str, TextStyle style)
	{
		return getWrapped(str, style, "");
	}
	private String getWrapped(String str, TextStyle style, String extraJson)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append(extraJson);
		builder.append(style.getJson());
		builder.append("\"text\":\"");
		builder.append(style.getCodes());
		builder.append(str);
		builder.append("\"},");
		return builder.toString();
	}
}

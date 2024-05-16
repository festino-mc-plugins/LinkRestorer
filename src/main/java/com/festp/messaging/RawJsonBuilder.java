package com.festp.messaging;

import org.bukkit.ChatColor;
import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.components.Command;
import com.festp.styledmessage.components.CopyableText;
import com.festp.styledmessage.components.Link;
import com.festp.styledmessage.components.MentionedPlayer;
import com.festp.styledmessage.components.TextComponent;
import com.festp.styledmessage.components.TextStyle;
import com.festp.utils.LinkUtils;
import com.google.common.collect.Lists;

public class RawJsonBuilder
{
	private final DisplaySettings settings;
	private StringBuilder command;

	public RawJsonBuilder(DisplaySettings settings)
	{
		this(settings, new TextStyle());
	}
	
	public RawJsonBuilder(DisplaySettings settings, TextStyle baseTextStyle)
	{
		this.settings = settings;
		command = new StringBuilder();
		command.append("{").append(baseTextStyle.getFullJson()).append("\"text\":\"\"},");
	}
	
	@Override
	public String toString()
	{
		tryEncloseList();
		return command.toString();
	}
	
	public CharSequence toCharSequence()
	{
		tryEncloseList();
		return command;
	}
	
	public void appendRawJsonBuilder(RawJsonBuilder builder) {
		this.command.append(builder.command);
	}
	
	public void appendStyledMessage(StyledMessage styledMessage)
	{
		for (SingleStyleMessage part : styledMessage.getStyledParts())
		{
			StringBuilder text = new StringBuilder();
			StringBuilder extraJson = new StringBuilder();
			for (TextComponent component : part.getComponents())
			{
				if (component instanceof TextStyle)
				{
					appendTextStyle(text, extraJson, (TextStyle) component);
				}
				else if (component instanceof Link)
				{
					appendLink(text, extraJson, (Link) component);
				}
				else if (component instanceof Command)
				{
					appendCommand(text, extraJson, (Command) component);
				}
				else if (component instanceof CopyableText)
				{
					appendCopyableText(text, extraJson, (CopyableText) component);
				}
				else if (component instanceof MentionedPlayer)
				{
					appendPlayer(text, extraJson, (MentionedPlayer) component);
				}
			}
			text.append(part.getText());
			tryWrap(text.toString(), extraJson);
		}
	}
	
	public void appendJoinedLinks(Iterable<Link> links, TextStyle style, String sep)
	{
		boolean isFirst = true;
		StyledMessage styledMessage = new StyledMessage();
		for (Link link : links)
		{
			if (isFirst) {
				isFirst = false;
			}
			else {
				styledMessage.addAll(Lists.newArrayList(new SingleStyleMessage(sep, Lists.newArrayList())));
			}
			styledMessage.addAll(Lists.newArrayList(new SingleStyleMessage(link.getUrl(), Lists.newArrayList(link))));
		}
		appendStyledMessage(styledMessage);
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
	
	private void appendPlayer(StringBuilder text, StringBuilder json, MentionedPlayer player)
	{
		String plainName = player.getName();
		String uuid = player.getPlayer().getUniqueId().toString();
		StringBuilder eventsJson = new StringBuilder()
				.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + plainName + "\\nType: Player\\n" + uuid + "\"},")
				.append("\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell " + plainName + " \"},");
		
		json.append(eventsJson);
	}
	
	private void appendTextStyle(StringBuilder text, StringBuilder json, TextStyle style) {
		text.append(style.getCodes());
		json.append(style.getJson());
	}
	
	private void appendLink(StringBuilder text, StringBuilder json, Link link)
	{
		if (settings.underlineLinks)
			text.append(ChatColor.UNDERLINE);
		json.append(getLinkJson(link));
	}
	
	private CharSequence getLinkJson(Link link)
	{
		String tooltip = settings.tooltipLinks;
		String encodedUrl = LinkUtils.applyBrowserEncoding(link.getUrl());
		StringBuilder eventsJson = new StringBuilder();
		eventsJson.append("\"clickEvent\":{\"action\":\"open_url\",\"value\":\"")
		          .append(encodedUrl)
		          .append("\"},");
		if (!tooltip.isEmpty())
			eventsJson.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"")
			          .append(tooltip)
			          .append("\"},");
		return eventsJson;
	}
	
	private void appendCommand(StringBuilder text, StringBuilder json, Command command) {
		if (settings.underlineCommands)
			text.append(ChatColor.UNDERLINE);
		json.append(getSuggestCommandJson(command.getCommand(), settings.tooltipCommands));
	}
	
	private void appendCopyableText(StringBuilder text, StringBuilder json, CopyableText copyableText) {
		if (settings.underlineCopyableText)
			text.append(ChatColor.UNDERLINE);
		json.append(getSuggestCommandJson(copyableText.getText(), settings.tooltipCopyableText));
	}
	
	private static String getSuggestCommandJson(String command, String tooltip) {
		StringBuilder eventsJson = new StringBuilder();
		eventsJson.append("\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"")
		          .append(command)
		          .append("\"},");
		if (!tooltip.isEmpty())
			eventsJson.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"")
			          .append(tooltip)
			          .append("\"},");
		
		return eventsJson.toString();
	}

	private void tryEncloseList()
	{
		command.insert(0, "[");
		tryRemoveComma();
		command.append("]");
	}
	
	private void tryRemoveComma() {
		int index = command.length() - 1;
		if (index >= 0 && command.charAt(index) == ',')
			command.deleteCharAt(index);
	}
	
	private void tryWrap(String str, CharSequence extraJson)
	{
		if (str.length() == 0) return;
		command.append(getWrapped(str, extraJson));
	}
	
	private static String escapeQuotes(String s) {
		return s.replace("\"", "\\\"");
	}
	
	private static CharSequence getWrapped(String text, CharSequence extraJson)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append(extraJson);
		builder.append("\"text\":\"");
		builder.append(escapeQuotes(text));
		builder.append("\"},");
		return builder;
	}
}

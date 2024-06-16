package com.festp.messaging;

import java.util.List;

import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.attributes.Command;
import com.festp.styledmessage.attributes.CopyableText;
import com.festp.styledmessage.attributes.Formatting;
import com.festp.styledmessage.attributes.Link;
import com.festp.styledmessage.attributes.MentionedPlayer;
import com.festp.styledmessage.attributes.StyleAttribute;
import com.festp.utils.LinkUtils;
import com.google.common.collect.Lists;

public class RawJsonBuilder
{
	private final DisplaySettings settings;
	private StringBuilder command;

	public RawJsonBuilder(DisplaySettings settings)
	{
		this(settings, new Formatting());
	}
	
	public RawJsonBuilder(DisplaySettings settings, Formatting baseFormatting)
	{
		this.settings = settings;
		command = new StringBuilder();
		command.append("{").append(baseFormatting.getFullJson()).append("\"text\":\"\"},");
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
			List<StyleAttribute> style = part.getStyle();
			String text = part.getText();
			StringBuilder json = new StringBuilder();
			// add the last attributes first (otherwise Formatting may rewrite Link codes)
			for (int i = style.size() - 1; i >= 0; i--)
			{
				StyleAttribute attribute = style.get(i);
				if (attribute instanceof Formatting)
				{
					appendTextStyle(json, (Formatting) attribute);
					text = ((Formatting) attribute).getCodes() + text;
				}
				else if (attribute instanceof Link)
				{
					appendLink(json, (Link) attribute);
					text = String.format(settings.formatLinks, text);
				}
				else if (attribute instanceof Command)
				{
					appendCommand(json, (Command) attribute);
					text = String.format(settings.formatCommands, text);
				}
				else if (attribute instanceof CopyableText)
				{
					appendCopyableText(json, (CopyableText) attribute);
					text = String.format(settings.formatCopyableText, text);
				}
				else if (attribute instanceof MentionedPlayer)
				{
					appendPlayer(json, (MentionedPlayer) attribute);
				}
			}
			tryWrap(text, json);
		}
	}
	
	public void appendJoinedLinks(Iterable<Link> links, Formatting formatting, String sep)
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
	public void appendTranslated(String identifier, CharSequence[] components, Formatting formatting)
	{
		command.append("{");
		command.append(formatting.getFullJson());
		command.append("\"translate\":\"");
		command.append(identifier);
		command.append("\"");
		if (components == null || components.length == 0) {
			command.append("},");
			return;
		}
		command.append(",\"with\":[");
		int n = 0;
		for (CharSequence component : components) {
			if (n > 0)
				command.append(',');
			command.append(component);
			n++;
		}
		command.append("]},");
	}
	
	private void appendPlayer(StringBuilder json, MentionedPlayer player)
	{
		String plainName = player.getName();
		String uuid = player.getPlayer().getUniqueId().toString();
		String tooltip = plainName + "\nType: Player\n" + uuid;
		json.append(getSuggestCommandJson("/tell " + plainName + " "));
		json.append(getShowTextJson(tooltip));
	}
	
	private void appendTextStyle(StringBuilder json, Formatting formatting) {
		json.append(formatting.getJson());
	}
	
	private void appendLink(StringBuilder json, Link link)
	{
		json.append(getLinkJson(link));
	}
	
	private CharSequence getLinkJson(Link link)
	{
		String tooltip = settings.tooltipLinks;
		String encodedUrl = LinkUtils.applyBrowserEncoding(link.getUrl());
		StringBuilder eventsJson = new StringBuilder();
		eventsJson.append("\"clickEvent\":{\"action\":\"open_url\",\"value\":\"")
		          .append(escapeJsonString(encodedUrl))
		          .append("\"},");
		if (!tooltip.isEmpty())
			eventsJson.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"")
			          .append(escapeJsonString(tooltip))
			          .append("\"},");
		return eventsJson;
	}
	
	private void appendCommand(StringBuilder json, Command command) {
		if (settings.runCommands)
			json.append(getRunCommandJson(command.getCommand()));
		else
			json.append(getSuggestCommandJson(command.getCommand()));
		
		json.append(getShowTextJson(settings.tooltipCommands));
	}
	
	private void appendCopyableText(StringBuilder json, CopyableText copyableText) {
		json.append(getSuggestCommandJson(copyableText.getText()));
		json.append(getShowTextJson(settings.tooltipCopyableText));
	}
	
	private static String getSuggestCommandJson(String command) {
		StringBuilder eventsJson = new StringBuilder();
		eventsJson.append("\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"")
		          .append(escapeJsonString(command))
		          .append("\"},");
		
		return eventsJson.toString();
	}
	
	private static String getRunCommandJson(String command) {
		StringBuilder eventsJson = new StringBuilder();
		eventsJson.append("\"clickEvent\":{\"action\":\"run_command\",\"value\":\"")
		          .append(escapeJsonString(command))
		          .append("\"},");
		
		return eventsJson.toString();
	}
	
	private static String getShowTextJson(String tooltip) {
		StringBuilder eventsJson = new StringBuilder();
		if (!tooltip.isEmpty())
			eventsJson.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"")
			          .append(escapeJsonString(tooltip))
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
	
	private static String escapeJsonString(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}
	
	private static CharSequence getWrapped(String text, CharSequence extraJson)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append(extraJson);
		builder.append("\"text\":\"");
		builder.append(escapeJsonString(text));
		builder.append("\"},");
		return builder;
	}
}

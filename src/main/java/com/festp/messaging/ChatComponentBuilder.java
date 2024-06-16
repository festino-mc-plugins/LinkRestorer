package com.festp.messaging;

import java.util.List;

import org.bukkit.entity.Player;

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

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ChatComponentBuilder
{
	private final DisplaySettings settings;
	private final Formatting baseFormatting;
	private final List<BaseComponent> components;
	private boolean changed = false;

	public ChatComponentBuilder(DisplaySettings settings)
	{
		this(settings, new Formatting());
	}
	
	public ChatComponentBuilder(DisplaySettings settings, Formatting baseFormatting)
	{
		this.settings = settings;
		this.baseFormatting = baseFormatting;
		components = Lists.newArrayList();
	}

	public boolean isChanged() {
		return changed;
	}
	
	public BaseComponent build() {
		TextComponent component = new TextComponent(components.toArray(new BaseComponent[0]));
		applyFormatting(component, baseFormatting, true);
		return component;
	}
	
	public void appendComponentBuilder(ChatComponentBuilder builder) {
		this.components.addAll(builder.components);
	}
	
	public void appendStyledMessage(StyledMessage styledMessage)
	{
		for (SingleStyleMessage part : styledMessage.getStyledParts())
		{
			TextComponent component = new TextComponent();
			List<StyleAttribute> style = part.getStyle();
			String text = part.getText();
			// add the last attributes first (otherwise Formatting may rewrite Link codes)
			for (int i = style.size() - 1; i >= 0; i--)
			{
				StyleAttribute attribute = style.get(i);
				if (attribute instanceof Formatting)
				{
					applyFormatting(component, (Formatting) attribute, false);
					text = ((Formatting) attribute).getCodes() + text;
				}
				else if (attribute instanceof Link)
				{
					setLink(component, (Link) attribute);
					text = String.format(settings.formatLinks, text);
					changed = true;
				}
				else if (attribute instanceof Command)
				{
					setCommand(component, (Command) attribute);
					text = String.format(settings.formatCommands, text);
					changed = true;
				}
				else if (attribute instanceof CopyableText)
				{
					setCopyableText(component, (CopyableText) attribute);
					text = String.format(settings.formatCopyableText, text);
					changed = true;
				}
				else if (attribute instanceof MentionedPlayer)
				{
					setShowPlayer(component, ((MentionedPlayer) attribute).getPlayer());
				}
			}
			component.setText(text);
			components.add(component);
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
	public void appendTranslated(String identifier, List<BaseComponent> components)
	{
		TranslatableComponent component = new TranslatableComponent(identifier);
		component.setWith(components);
		this.components.add(component);
		changed = true;
	}
	
	private void applyFormatting(BaseComponent component, Formatting formatting, boolean applyCodes) {
		if (formatting.getHexColor() != null)
			component.setColor(ChatColor.of(formatting.getHexColor()));
		
		if (applyCodes)
			for (ChatColor c : formatting.getChatColors()) {
				if (c == ChatColor.BOLD)
					component.setBold(true);
				else if (c == ChatColor.STRIKETHROUGH)
					component.setStrikethrough(true);
				else if (c == ChatColor.ITALIC)
					component.setItalic(true);
				else if (c == ChatColor.MAGIC)
					component.setObfuscated(true);
				else if (c == ChatColor.UNDERLINE)
					component.setUnderlined(true);
				else
					component.setColor(c);
			}
	}
	
	private void setLink(BaseComponent component, Link link)
	{
		setOpenUrl(component, link.getUrl());
		setShowText(component, settings.tooltipLinks);
	}
	
	private void setCommand(BaseComponent component, Command command) {
		if (settings.runCommands)
			setRunCommand(component, command.getCommand());
		else
			setSuggestCommand(component, command.getCommand());
		
		setShowText(component, settings.tooltipCommands);
	}
	
	private void setCopyableText(BaseComponent component, CopyableText copyableText) {
		setSuggestCommand(component, copyableText.getText());
		setShowText(component, settings.tooltipCopyableText);
	}
	
	private static boolean setShowText(BaseComponent component, String tooltip) {
		if (tooltip.isEmpty())
			return false;
		
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(tooltip)));
		return true;
	}
	
	private static boolean setShowPlayer(BaseComponent component, Player player) {
		String plainName = player.getName();
		Entity entity = new Entity("player", player.getUniqueId().toString(), new TextComponent(plainName));
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, entity));
		return true;
	}
	
	private static boolean setOpenUrl(BaseComponent component, String url) {
		String encodedUrl = LinkUtils.applyBrowserEncoding(url);
		return setClickEvent(component, ClickEvent.Action.OPEN_URL, encodedUrl);
	}
	
	private static boolean setSuggestCommand(BaseComponent component, String command) {
		return setClickEvent(component, ClickEvent.Action.SUGGEST_COMMAND, command);
	}
	
	private static boolean setRunCommand(BaseComponent component, String command) {
		return setClickEvent(component, ClickEvent.Action.RUN_COMMAND, command);
	}
	
	private static boolean setClickEvent(BaseComponent component, ClickEvent.Action action, String value) {
		if (value.isEmpty())
			return false;
		
		component.setClickEvent(new ClickEvent(action, value));
		return true;
	}
}

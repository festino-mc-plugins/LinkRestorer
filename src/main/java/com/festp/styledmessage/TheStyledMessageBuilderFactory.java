package com.festp.styledmessage;

import java.util.List;

import org.bukkit.permissions.Permissible;

import com.festp.config.Config;
import com.festp.config.Config.Key;
import com.festp.parsing.CommandParser;
import com.festp.parsing.StyleParser;
import com.festp.parsing.CopyableTextParser;
import com.festp.parsing.LinkParser;
import com.festp.parsing.FormattingParser;
import com.festp.utils.CommandValidator;
import com.google.common.collect.Lists;

public class TheStyledMessageBuilderFactory implements StyledMessageBuilderFactory
{
	private static final String PERMISSION_LINKS = "clickablelinks.use.links";
	private static final String PERMISSION_IP_LINKS = "clickablelinks.use.iplinks";
	private static final String PERMISSION_COMMANDS = "clickablelinks.use.commands";
	private static final String PERMISSION_COPYABLE_TEXT = "clickablelinks.use.copyable";
	
	private final Config config;
	private final CommandValidator commandValidator;
	
	public TheStyledMessageBuilderFactory(Config config, CommandValidator commandValidator) {
		this.config = config;
		this.commandValidator = commandValidator;
	}

	@Override
	public StyledMessageBuilder create(Permissible user)
	{
		boolean useLinks = config.get(Key.ENABLE_LINKS, false) && user.hasPermission(PERMISSION_LINKS);
		boolean useIpLinks = config.get(Key.ENABLE_IP_LINKS, false) && user.hasPermission(PERMISSION_IP_LINKS);
		boolean useCommands = config.get(Key.ENABLE_COMMANDS, false) && user.hasPermission(PERMISSION_COMMANDS);
		boolean useCopyableText = config.get(Key.ENABLE_COPYABLE_TEXT, false) && user.hasPermission(PERMISSION_COPYABLE_TEXT);
		List<StyleParser> globalParsers = Lists.newArrayList(getFormattingParser());
		
		List<StyleParser> splittingParsers = Lists.newArrayList();
		if (useCommands || useCopyableText)
			splittingParsers.add(getCopyableTextParser(useCommands, useCopyableText));
		if (useLinks || useIpLinks)
			splittingParsers.add(getLinkParser(useLinks, useIpLinks));
		if (useCommands)
			splittingParsers.add(getCommandParser());
		
		return new TheStyledMessageBuilder(globalParsers, splittingParsers);
	}
	
	private StyleParser getFormattingParser()
	{
		return new FormattingParser();
	}
	
	private StyleParser getCopyableTextParser(boolean useCommands, boolean useCopyableText)
	{
		return new CopyableTextParser(
				config.get(Key.COPYABLE_TEXT_BEGIN_QUOTES), config.get(Key.COPYABLE_TEXT_END_QUOTES),
				useCommands, useCopyableText, commandValidator);
	}
	
	private StyleParser getLinkParser(boolean useLinks, boolean useIpLinks)
	{
		return new LinkParser(useLinks, useIpLinks);
	}
	
	private StyleParser getCommandParser()
	{
		return new CommandParser(commandValidator, config.get(Key.COMMANDS_REMOVE_STARTING_DOT, true));
	}
}

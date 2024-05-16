package com.festp.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CommandUtils
{
	/** @return Command without first '/' and namespace. */
	public static String getCommand(String fullCommand)
	{
		int index = fullCommand.indexOf(' ');
		String command = "";
		if (index < 0)
			command = fullCommand.substring(1);
		else
			command = fullCommand.substring(1, index);
		command = removeNamespace(command);
		return command;
	}
	/** @return Empty string or args if any. */
	public static String getArgs(String fullCommand)
	{
		int index = fullCommand.indexOf(' ');
		String args = "";
		if (index >= 0)
			args = fullCommand.substring(index + 1);
		return args;
	}

	/** Select first arg, for example @@a[distance = ..50]*/
	public static int[] selectRecipientsFromArg0(String command)
	{
		int indexStart = command.indexOf(" ");
		if (indexStart < 0)
			return null;
		indexStart++;
		
		int length = command.length();
		while (indexStart < length && command.charAt(indexStart) == ' ')
			indexStart++;
		if (indexStart >= length)
			return null;
		
		int indexEnd;
		if (command.charAt(indexStart) != '@' || indexStart + 2 >= length) {
			indexEnd = command.indexOf(" ", indexStart);
			indexEnd = indexEnd < 0 ? length : indexEnd;
			return new int[] { indexStart, indexEnd };
		}
		
		indexEnd = indexStart + 2;
		if (command.charAt(indexEnd) != '[') {
			if (command.charAt(indexEnd) == ' ')
				return new int[] { indexStart, indexEnd };
			return null;
		}
		
		indexEnd++;
		int openedBrackets = 1;
		while (indexEnd < length && openedBrackets > 0) {
			char c = command.charAt(indexEnd);
			if (c == '[')
				openedBrackets++;
			else if (c == ']')
				openedBrackets--;
			indexEnd++;
		}
		return new int[] { indexStart, indexEnd };
	}
	
	public static Player[] getRecipients(String selector, CommandSender sender)
	{
		if (selector.charAt(0) != '@') {
			Player recipient = tryGetPlayer(selector);
			if (recipient == null)
				return null;
			return new Player[] { recipient };
		}
		
		List<Entity> entities = Bukkit.getServer().selectEntities(sender, selector);
		List<Player> players = new ArrayList<>();
		for (Entity e : entities)
			if (e instanceof Player)
				players.add((Player)e);
		return players.toArray(new Player[0]);
	}

	private static Player tryGetPlayer(String name)
	{
		return Bukkit.getPlayerExact(name);
	}

	private static String removeNamespace(String command) {
		int index = command.indexOf(':');
		if (index < 0)
			return command;
		return command.substring(index + 1);
	}
}

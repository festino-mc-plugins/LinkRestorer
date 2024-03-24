package com.festp.handlers;

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

	private static String removeNamespace(String command) {
		int index = command.indexOf(':');
		if (index < 0)
			return command;
		return command.substring(index + 1);
	}
}

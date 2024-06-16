package com.festp.styledmessage.attributes;

public class Command implements StyleAttribute
{
	private final String command;
	
	public Command(String command)
	{
		this.command = command;
	}

	public String getCommand() {
		return command;
	}
}

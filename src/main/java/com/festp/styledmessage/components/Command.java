package com.festp.styledmessage.components;

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

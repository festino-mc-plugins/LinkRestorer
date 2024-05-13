package com.festp.styledmessage.components;

public class Command implements TextComponent
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

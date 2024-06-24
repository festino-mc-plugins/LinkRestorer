package com.festp.utils;

import java.util.Collection;

public interface CommandValidator
{
	public boolean commandExists(String command);

	public Collection<String> getCommandAliases(String command);
}

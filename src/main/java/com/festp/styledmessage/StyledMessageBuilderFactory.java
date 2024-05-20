package com.festp.styledmessage;

import org.bukkit.permissions.Permissible;

public interface StyledMessageBuilderFactory
{
	public StyledMessageBuilder create(Permissible user);
}

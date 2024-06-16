package com.festp.styledmessage.attributes;

import org.bukkit.entity.Player;

public class MentionedPlayer implements StyleAttribute {
	private final Player player;
	private final String decoratedName;
	
	public MentionedPlayer(Player player, String decoratedName)
	{
		this.player = player;
		this.decoratedName = decoratedName;
	}

	public Player getPlayer() {
		return player;
	}

	public String getName() {
		return decoratedName;
	}
}

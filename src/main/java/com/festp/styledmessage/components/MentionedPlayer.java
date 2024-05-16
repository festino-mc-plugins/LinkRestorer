package com.festp.styledmessage.components;

import org.bukkit.entity.Player;

public class MentionedPlayer implements TextComponent {
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

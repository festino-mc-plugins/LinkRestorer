package com.festp.styledmessage.components;

import org.bukkit.entity.Player;

public class MentionedPlayer implements TextComponent {
	private String plainText;
	private Player player;
	
	public MentionedPlayer(String plainText, Player player)
	{
		this.plainText = plainText;
		this.player = player;
	}

	@Override
	public String getPlainText() {
		return plainText;
	}

	public Player getPlayer() {
		return player;
	}

	// TODO remove indices from TextComponent
	
	@Override
	public int getBeginIndex() {
		return 0;
	}

	@Override
	public int getEndIndex() {
		return plainText.length();
	}
}

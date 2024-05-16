package com.festp.styledmessage.components;

public interface UpdatableTextComponent extends TextComponent
{
	public void update(UpdatableTextComponent withOther);
	
	public UpdatableTextComponent clone();
}

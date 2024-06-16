package com.festp.styledmessage.components;

public interface UpdatableStyleAttribute extends StyleAttribute
{
	public void update(UpdatableStyleAttribute withOther);
	
	public UpdatableStyleAttribute clone();
}

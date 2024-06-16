package com.festp.styledmessage.attributes;

public interface UpdatableStyleAttribute extends StyleAttribute
{
	public void update(UpdatableStyleAttribute withOther);
	
	public UpdatableStyleAttribute clone();
}

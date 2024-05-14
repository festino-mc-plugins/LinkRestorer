package com.festp.parsing;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;

import com.festp.styledmessage.components.Command;
import com.festp.styledmessage.components.Link;
import com.festp.styledmessage.components.TextStyle;

class SingleStyleSubstringHelpers
{
	
	public static void assertPlain(SingleStyleSubstring substring, int beginIndex, int endIndex)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(0, substring.components.size());
	}
	
	public static void assertLink(SingleStyleSubstring substring, int beginIndex, int endIndex, String url)
	{
		assertLink(substring, beginIndex, endIndex);
		Link link = (Link) substring.components.get(0);
		Assertions.assertEquals(url, link.getUrl());
	}
	
	public static void assertLink(SingleStyleSubstring substring, int beginIndex, int endIndex)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(1, substring.components.size());
		Assertions.assertTrue(substring.components.get(0) instanceof Link);
	}
	
	public static void assertCommand(SingleStyleSubstring substring, int beginIndex, int endIndex, String command)
	{
		assertCommand(substring, beginIndex, endIndex);
		Command commandComponent = (Command) substring.components.get(0);
		Assertions.assertEquals(command, commandComponent.getCommand());
	}
	
	public static void assertCommand(SingleStyleSubstring substring, int beginIndex, int endIndex)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(1, substring.components.size());
		Assertions.assertTrue(substring.components.get(0) instanceof Command);
	}

	public static void assertColor(SingleStyleSubstring substring, int beginIndex, int endIndex, ChatColor chatColor)
	{
		assertColor(substring, beginIndex, endIndex, chatColor.toString());
	}

	public static void assertColor(SingleStyleSubstring substring, int beginIndex, int endIndex, String color)
	{
		assertColor(substring, beginIndex, endIndex, new TextStyle().update(color));
	}
	
	public static void assertColor(SingleStyleSubstring substring, int beginIndex, int endIndex, TextStyle expectedStyle)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(1, substring.components.size());
		TextStyle style = (TextStyle) substring.components.get(0);
		Assertions.assertEquals(expectedStyle.getFullJson(), style.getFullJson());
	}
}

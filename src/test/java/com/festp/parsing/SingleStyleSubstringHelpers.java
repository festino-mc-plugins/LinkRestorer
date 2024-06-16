package com.festp.parsing;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;

import com.festp.styledmessage.attributes.Command;
import com.festp.styledmessage.attributes.CopyableText;
import com.festp.styledmessage.attributes.Formatting;
import com.festp.styledmessage.attributes.Link;

class SingleStyleSubstringHelpers
{
	
	public static void assertPlain(SingleStyleSubstring substring, int beginIndex, int endIndex)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(0, substring.style.size());
	}
	
	public static void assertLink(SingleStyleSubstring substring, int beginIndex, int endIndex, String url)
	{
		assertLink(substring, beginIndex, endIndex);
		Link link = (Link) substring.style.get(0);
		Assertions.assertEquals(url, link.getUrl());
	}
	
	public static void assertLink(SingleStyleSubstring substring, int beginIndex, int endIndex)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(1, substring.style.size());
		Assertions.assertTrue(substring.style.get(0) instanceof Link);
	}
	
	public static void assertCommand(SingleStyleSubstring substring, int beginIndex, int endIndex, String command)
	{
		assertCommand(substring, beginIndex, endIndex);
		Command commandAttribute = (Command) substring.style.get(0);
		Assertions.assertEquals(command, commandAttribute.getCommand());
	}
	
	public static void assertCommand(SingleStyleSubstring substring, int beginIndex, int endIndex)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(1, substring.style.size());
		Assertions.assertTrue(substring.style.get(0) instanceof Command);
	}
	
	public static void assertCopyable(SingleStyleSubstring substring, int beginIndex, int endIndex, String copyableText)
	{
		assertCopyable(substring, beginIndex, endIndex);
		CopyableText copyableAttribute = (CopyableText) substring.style.get(0);
		Assertions.assertEquals(copyableText, copyableAttribute.getText());
	}
	
	public static void assertCopyable(SingleStyleSubstring substring, int beginIndex, int endIndex)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(1, substring.style.size());
		Assertions.assertTrue(substring.style.get(0) instanceof CopyableText);
	}

	public static void assertFormatting(SingleStyleSubstring substring, int beginIndex, int endIndex, ChatColor chatColor)
	{
		assertFormatting(substring, beginIndex, endIndex, chatColor.toString());
	}

	public static void assertFormatting(SingleStyleSubstring substring, int beginIndex, int endIndex, String formatting)
	{
		assertFormatting(substring, beginIndex, endIndex, new Formatting().update(formatting));
	}
	
	public static void assertFormatting(SingleStyleSubstring substring, int beginIndex, int endIndex, Formatting expectedFormatting)
	{
		Assertions.assertEquals(beginIndex, substring.beginIndex);
		Assertions.assertEquals(endIndex, substring.endIndex);
		Assertions.assertEquals(1, substring.style.size());
		Formatting formatting = (Formatting) substring.style.get(0);
		Assertions.assertEquals(expectedFormatting.getFullJson(), formatting.getFullJson());
	}
}

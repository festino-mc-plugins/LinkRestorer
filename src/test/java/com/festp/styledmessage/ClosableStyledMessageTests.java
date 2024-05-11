package com.festp.styledmessage;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.festp.parsing.SingleStyleSubstring;
import com.festp.styledmessage.ClosableStyledMessage.ClosableStyledMessagePart;
import com.festp.styledmessage.SingleStyleMessage;
import com.google.common.collect.Lists;

public class ClosableStyledMessageTests {
	@Test
	public void getStyledParts_ReturnConstructorValue()
	{
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("a", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		
		List<SingleStyleMessage> newParts = message.getStyledParts();
		
		Assert.assertArrayEquals(parts.toArray(), newParts.toArray());
	}
	
	@Test
	public void getOpenParts_ReturnMessage()
	{
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("a", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		
		List<ClosableStyledMessagePart> openParts = message.getOpenParts();
		
		Assert.assertEquals("a", openParts.get(0).getPlainText());
		Assert.assertEquals(1, openParts.size());
	}
	
	@Test
	public void replace_UseMiddle()
	{
		List<SingleStyleSubstring> styledSubstrings = Lists.newArrayList(
				new SingleStyleSubstring(1, 2, Lists.newArrayList()),
				new SingleStyleSubstring(3, 4, Lists.newArrayList())
		);
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("abcde", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		List<ClosableStyledMessagePart> openParts = message.getOpenParts();
		
		openParts.get(0).replace(styledSubstrings, false);
		List<SingleStyleMessage> newParts = message.getStyledParts();

		Assert.assertEquals("b", newParts.get(0).getText());
		Assert.assertEquals("d", newParts.get(1).getText());
		Assert.assertEquals(2, newParts.size());
	}
	
	@Test
	public void replace_UseBeginning()
	{
		List<SingleStyleSubstring> styledSubstrings = Lists.newArrayList(
				new SingleStyleSubstring(0, 3, Lists.newArrayList())
		);
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("abcde", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		List<ClosableStyledMessagePart> openParts = message.getOpenParts();
		
		openParts.get(0).replace(styledSubstrings, false);
		List<SingleStyleMessage> newParts = message.getStyledParts();

		Assert.assertEquals("abc", newParts.get(0).getText());
		Assert.assertEquals(1, newParts.size());
	}
	
	@Test
	public void replace_UseEnding()
	{
		List<SingleStyleSubstring> styledSubstrings = Lists.newArrayList(
				new SingleStyleSubstring(2, 5, Lists.newArrayList())
		);
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("abcde", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		List<ClosableStyledMessagePart> openParts = message.getOpenParts();
		
		openParts.get(0).replace(styledSubstrings, false);
		List<SingleStyleMessage> newParts = message.getStyledParts();

		Assert.assertEquals("cde", newParts.get(0).getText());
		Assert.assertEquals(1, newParts.size());
	}
	
	@Test
	public void replace_UpdateBeginIndexOnContinue()
	{
		List<SingleStyleSubstring> styledSubstrings1 = Lists.newArrayList(
				new SingleStyleSubstring(0, 3, Lists.newArrayList()),
				new SingleStyleSubstring(5, 7, Lists.newArrayList()));
		List<SingleStyleSubstring> styledSubstrings2 = Lists.newArrayList(
				new SingleStyleSubstring(0, 1, Lists.newArrayList()),
                new SingleStyleSubstring(4, 5, Lists.newArrayList()));
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("abededc", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		
		message.getOpenParts().get(0).replace(styledSubstrings1, false);
		message.getOpenParts().get(0).replace(styledSubstrings2, false);
		List<SingleStyleMessage> newParts = message.getStyledParts();

		Assert.assertEquals("a", newParts.get(0).getText());
		Assert.assertEquals("c", newParts.get(1).getText());
		Assert.assertEquals(2, newParts.size());
	}
	
	// closed
	
	// component merging
}

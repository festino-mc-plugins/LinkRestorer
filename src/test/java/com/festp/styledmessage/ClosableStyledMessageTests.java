package com.festp.styledmessage;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.festp.parsing.SingleStyleSubstring;
import com.festp.styledmessage.ClosableStyledMessage.ClosableStyledMessagePart;
import com.festp.styledmessage.components.StyleAttribute;
import com.festp.styledmessage.SingleStyleMessage;
import com.google.common.collect.Lists;

public class ClosableStyledMessageTests {
	@Test
	public void getStyledParts_ReturnConstructorValue()
	{
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("a", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		
		List<SingleStyleMessage> newParts = message.getStyledParts();
		
		Assertions.assertArrayEquals(parts.toArray(), newParts.toArray());
	}
	
	@Test
	public void getOpenParts_ReturnMessage()
	{
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("a", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		
		List<ClosableStyledMessagePart> openParts = message.getOpenParts();
		
		Assertions.assertEquals("a", openParts.get(0).getPlainText());
		Assertions.assertEquals(1, openParts.size());
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

		Assertions.assertEquals("b", newParts.get(0).getText());
		Assertions.assertEquals("d", newParts.get(1).getText());
		Assertions.assertEquals(2, newParts.size());
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

		Assertions.assertEquals("abc", newParts.get(0).getText());
		Assertions.assertEquals(1, newParts.size());
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

		Assertions.assertEquals("cde", newParts.get(0).getText());
		Assertions.assertEquals(1, newParts.size());
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

		Assertions.assertEquals("a", newParts.get(0).getText());
		Assertions.assertEquals("c", newParts.get(1).getText());
		Assertions.assertEquals(2, newParts.size());
	}
	
	@Test
	public void replace_CloseChanged()
	{
		List<SingleStyleSubstring> styledSubstrings = Lists.newArrayList(
				new SingleStyleSubstring(1, 2, Lists.newArrayList()),
				new SingleStyleSubstring(3, 4, Lists.newArrayList(Mockito.mock(StyleAttribute.class))));
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("abcde", Lists.newArrayList()));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		
		message.getOpenParts().get(0).replace(styledSubstrings, true);
		List<ClosableStyledMessagePart> openParts = message.getOpenParts();
		List<SingleStyleMessage> newParts = message.getStyledParts();

		Assertions.assertEquals(1, openParts.size());
		Assertions.assertEquals(1, openParts.get(0).getStyledParts().size());
		SingleStyleMessage part = openParts.get(0).getStyledParts().get(0);
		Assertions.assertEquals("b", part.getText());
		Assertions.assertEquals(0, part.getStyle().size());

		Assertions.assertEquals("b", newParts.get(0).getText());
		Assertions.assertEquals("d", newParts.get(1).getText());
		Assertions.assertEquals(2, newParts.size());
	}

	
	@Test
	public void replace_MergeStyles()
	{
		StyleAttribute attributeMock_1 = Mockito.mock(StyleAttribute.class);
		StyleAttribute attributeMock_2 = Mockito.mock(StyleAttribute.class);
		List<SingleStyleSubstring> styledSubstrings = Lists.newArrayList(
				new SingleStyleSubstring(0, 5, Lists.newArrayList(attributeMock_2)));
		List<SingleStyleMessage> parts = Lists.newArrayList(new SingleStyleMessage("abcde", Lists.newArrayList(attributeMock_1)));
		ClosableStyledMessage message = new ClosableStyledMessage(parts);
		
		message.getOpenParts().get(0).replace(styledSubstrings, false);
		List<SingleStyleMessage> newParts = message.getStyledParts();

		List<StyleAttribute> newStyle = newParts.get(0).getStyle();
		Assertions.assertEquals(attributeMock_1, newStyle.get(0));
		Assertions.assertEquals(attributeMock_2, newStyle.get(1));
		Assertions.assertEquals(2, newStyle.size());
	}
}

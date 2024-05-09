package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.StyledMessageBuilder;
import com.festp.styledmessage.components.TextComponent;
import com.google.common.collect.Lists;

class StyledMessageBuilderTests
{
	private class DummyTextComponent implements TextComponent { }

	private class RemovingComponentParser implements ComponentParser
	{
		private final char removingChar;
		
		public RemovingComponentParser(char removingChar) {
			this.removingChar = removingChar;
		}
		
		@Override
		public Iterable<SingleStyleSubstring> getComponents(String text) {
			List<SingleStyleSubstring> result = Lists.newArrayList();
			int startIndex = 0;
			for (int i = 0; i < text.length(); i++) {
				if (text.charAt(i) != removingChar)
					continue;
				
				if (startIndex < i)
					result.add(new SingleStyleSubstring(startIndex, i, Lists.newArrayList()));
				
				startIndex = i + 1;
			}
			if (startIndex != text.length())
				result.add(new SingleStyleSubstring(startIndex, text.length(), Lists.newArrayList()));
			
			return result;
		} }
	
	@Test
	public void buildEmptyString_WhenZeroAppends() {
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(), Lists.newArrayList());
		
		StyledMessage styledMessage = parser.build();
		
		Assertions.assertTrue(styledMessage.isPlain());
		Assertions.assertEquals(1, styledMessage.getStyledParts().size());
		SingleStyleMessage styledPart = styledMessage.getStyledParts().get(0);
		Assertions.assertEquals("", styledPart.getText());
		Assertions.assertEquals(0, styledPart.getComponents().size());
	}

	@Test
	public void build_WhenZeroParsers() {
		String message = "hi";
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(), Lists.newArrayList());
		
		parser.append(message);
		StyledMessage styledMessage = parser.build();
		
		Assertions.assertTrue(styledMessage.isPlain());
		Assertions.assertEquals(1, styledMessage.getStyledParts().size());
		SingleStyleMessage styledPart = styledMessage.getStyledParts().get(0);
		Assertions.assertEquals(message, styledPart.getText());
		Assertions.assertEquals(0, styledPart.getComponents().size());
	}

	@Test
	public void build_GlobalParser_NoTextComponents() {
		String message = "abcdedcba";
		RemovingComponentParser removingParser = new RemovingComponentParser('b');
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(removingParser), Lists.newArrayList());
		
		parser.append(message);
		StyledMessage styledMessage = parser.build();
		
		Assertions.assertTrue(styledMessage.isPlain());
		Assertions.assertEquals("a", styledMessage.getStyledParts().get(0).getText());
		Assertions.assertEquals("cdedc", styledMessage.getStyledParts().get(1).getText());
		Assertions.assertEquals("a", styledMessage.getStyledParts().get(2).getText());
		Assertions.assertEquals(3, styledMessage.getStyledParts().size());
	}

	/*@Test
	void parsesColors() {
		String message = ChatColor.GRAY + "hi, " + ChatColor.WHITE + "Player";
		StyledMessage styledMessage = new RecursiveStyledMessageParser().parse(message);
		Assertions.assertFalse(styledMessage.isPlain);
		Assertions.assertEquals("hi, Player", styledMessage.plainText);
		Assertions.assertEquals(2, styledMessage.styleSwitches.size());
		Assertions.assertEquals(0, styledMessage.styleSwitches.get(0).index);
		Assertions.assertEquals(4, styledMessage.styleSwitches.get(1).index);
	}

	@Test
	void parsesLinks() {
		String message = "see example.com";
		StyledMessage styledMessage = new RecursiveStyledMessageParser().parse(message);
		Assertions.assertFalse(styledMessage.isPlain);
		Assertions.assertTrue(styledMessage.hasLinks);
		Assertions.assertEquals(message, styledMessage.plainText);
		Assertions.assertEquals(1, styledMessage.links.size());
		Assertions.assertEquals("example.com", styledMessage.links.get(0).getText());
	}*/
}

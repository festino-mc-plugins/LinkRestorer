package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runners.Parameterized.Parameters;

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
		private final String removingString;
		
		public RemovingComponentParser(String removingString) {
			this.removingString = removingString;
		}
		
		@Override
		public Iterable<SingleStyleSubstring> getComponents(String text) {
			List<SingleStyleSubstring> result = Lists.newArrayList();
			int startIndex = 0;
			int i = text.indexOf(removingString, startIndex);
			while (i >= 0) {
				if (startIndex < i)
					result.add(new SingleStyleSubstring(startIndex, i, Lists.newArrayList()));
				
				startIndex = i + removingString.length();
				i = text.indexOf(removingString, startIndex);
			}
			if (startIndex != text.length())
				result.add(new SingleStyleSubstring(startIndex, text.length(), Lists.newArrayList()));
			
			return result;
		}
	}

	private class SelectingComponentParser implements ComponentParser
	{
		private final String substring;
		private final TextComponent component;
		
		public SelectingComponentParser(String substring, TextComponent component) {
			this.substring = substring;
			this.component = component;
		}
		
		@Override
		public Iterable<SingleStyleSubstring> getComponents(String text) {
			List<SingleStyleSubstring> result = Lists.newArrayList();
			int startIndex = 0;
			int i = text.indexOf(substring, startIndex);
			while (i >= 0) {
				if (startIndex < i)
					result.add(new SingleStyleSubstring(startIndex, i, Lists.newArrayList()));

				result.add(new SingleStyleSubstring(i, i + substring.length(), Lists.newArrayList(component)));
				startIndex = i + substring.length();
				i = text.indexOf(substring, startIndex);
			}
			if (startIndex != text.length())
				result.add(new SingleStyleSubstring(startIndex, text.length(), Lists.newArrayList()));
			
			return result;
		}
	}
	
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
		RemovingComponentParser removingParser = new RemovingComponentParser("b");
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(removingParser), Lists.newArrayList());
		
		parser.append(message);
		StyledMessage styledMessage = parser.build();
		
		Assertions.assertTrue(styledMessage.isPlain());
		Assertions.assertEquals("a", styledMessage.getStyledParts().get(0).getText());
		Assertions.assertEquals("cdedc", styledMessage.getStyledParts().get(1).getText());
		Assertions.assertEquals("a", styledMessage.getStyledParts().get(2).getText());
		Assertions.assertEquals(3, styledMessage.getStyledParts().size());
	}

	@Test
	public void build_GlobalParsers_MergeComponents() {
		// test components merging and updating
		
	}

	@Test
	public void build_GlobalParsers_Order() {
		String message = "abededc";
		RemovingComponentParser removingParser_b = new RemovingComponentParser("de");
		RemovingComponentParser removingParser_bed = new RemovingComponentParser("bed");
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(removingParser_b, removingParser_bed), Lists.newArrayList());
		
		parser.append(message);
		StyledMessage styledMessage = parser.build();
		
		Assertions.assertEquals("a", styledMessage.getStyledParts().get(0).getText());
		Assertions.assertEquals("c", styledMessage.getStyledParts().get(1).getText());
		Assertions.assertEquals(2, styledMessage.getStyledParts().size());
	}
	
	@Test
	public void build_SplittingParsers_Order() {
		// test splitting parsers intersection (order)
		String message = "abededc";
		RemovingComponentParser removingParser_b = new RemovingComponentParser("de");
		RemovingComponentParser removingParser_bed = new RemovingComponentParser("bed");
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(removingParser_b, removingParser_bed), Lists.newArrayList());
		
		parser.append(message);
		StyledMessage styledMessage = parser.build();
		
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void build_MultipleAppends_StartStyle(boolean isSplitting) {
		SelectingComponentParser selectingParser = new SelectingComponentParser("cd", new DummyTextComponent());
		List<ComponentParser> globalParsers = isSplitting ? Lists.newArrayList() : Lists.newArrayList(selectingParser);
		List<ComponentParser> splittingParsers = isSplitting ? Lists.newArrayList(selectingParser) : Lists.newArrayList();
		StyledMessageBuilder parser = new StyledMessageBuilder(globalParsers, splittingParsers);
		String message1 = "abcd";
		String message2 = "abc";
		String message3 = "ab";
		int abcComponents = isSplitting ? 0 : 1;
		
		parser.append(message1);
		parser.append(message2);
		parser.append(message3);
		StyledMessage styledMessage = parser.build();
		
		List<SingleStyleMessage> parts = styledMessage.getStyledParts();
		Assertions.assertEquals("ab", parts.get(0).getText());
		Assertions.assertEquals("cd", parts.get(1).getText());
		Assertions.assertEquals("abc", parts.get(2).getText());
		Assertions.assertEquals("ab", parts.get(3).getText());
		Assertions.assertEquals(0, parts.get(0).getComponents().size());
		Assertions.assertEquals(1, parts.get(1).getComponents().size());
		Assertions.assertEquals(abcComponents, parts.get(2).getComponents().size());
		Assertions.assertEquals(abcComponents, parts.get(3).getComponents().size());
		Assertions.assertEquals(4, parts.size());
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

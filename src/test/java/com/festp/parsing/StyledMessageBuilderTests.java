package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.TheStyledMessageBuilder;
import com.festp.styledmessage.components.StyleAttribute;
import com.google.common.collect.Lists;

class StyledMessageBuilderTests
{
	@Test
	public void buildEmptyString_WhenZeroAppends() {
		TheStyledMessageBuilder parser = new TheStyledMessageBuilder(Lists.newArrayList(), Lists.newArrayList());
		
		StyledMessage styledMessage = parser.build();
		
		Assertions.assertEquals(1, styledMessage.getStyledParts().size());
		SingleStyleMessage styledPart = styledMessage.getStyledParts().get(0);
		Assertions.assertEquals("", styledPart.getText());
		Assertions.assertEquals(0, styledPart.getStyle().size());
	}

	@Test
	public void build_WhenZeroParsers() {
		String message = "hi";
		TheStyledMessageBuilder parser = new TheStyledMessageBuilder(Lists.newArrayList(), Lists.newArrayList());
		
		parser.append(message);
		StyledMessage styledMessage = parser.build();
		
		Assertions.assertEquals(1, styledMessage.getStyledParts().size());
		SingleStyleMessage styledPart = styledMessage.getStyledParts().get(0);
		Assertions.assertEquals(message, styledPart.getText());
		Assertions.assertEquals(0, styledPart.getStyle().size());
	}

	@Test
	public void build_GlobalParser_NoStyleAttributes() {
		String message = "abcdedcba";
		StyleParser parserMock = Mockito.mock(StyleParser.class);
		Mockito.when(parserMock.getStyles(message)).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 1, Lists.newArrayList()),
					new SingleStyleSubstring(2, 7, Lists.newArrayList()),
					new SingleStyleSubstring(8, 9, Lists.newArrayList())
				));
		TheStyledMessageBuilder parser = new TheStyledMessageBuilder(Lists.newArrayList(parserMock), Lists.newArrayList());
		
		parser.append(message);
		StyledMessage styledMessage = parser.build();

		Assertions.assertEquals(3, styledMessage.getStyledParts().size());
		Assertions.assertEquals("a", styledMessage.getStyledParts().get(0).getText());
		Assertions.assertEquals("cdedc", styledMessage.getStyledParts().get(1).getText());
		Assertions.assertEquals("a", styledMessage.getStyledParts().get(2).getText());
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void build_ParsersOrder(boolean isSplitting) {
		// TODO test if closeChanged is false
		String message = "abcd";
		StyleParser parserMock_1 = Mockito.mock(StyleParser.class);
		Mockito.when(parserMock_1.getStyles(message)).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 3, Lists.newArrayList())
				));
		StyleParser parserMock_2 = Mockito.mock(StyleParser.class);
		Mockito.when(parserMock_2.getStyles(Mockito.any())).thenReturn(Lists.newArrayList());
		List<StyleParser> parsers = Lists.newArrayList(parserMock_1, parserMock_2);
		List<StyleParser> globalParsers = isSplitting ? Lists.newArrayList() : parsers;
		List<StyleParser> splittingParsers =  isSplitting ? parsers : Lists.newArrayList();
		TheStyledMessageBuilder parser = new TheStyledMessageBuilder(globalParsers, splittingParsers);
		
		parser.append(message);

		Mockito.verify(parserMock_1).getStyles("abcd");
		Mockito.verify(parserMock_2).getStyles("abc");
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void build_MultipleAppends_StartStyle(boolean isSplitting) {
		StyleAttribute attributeMock = Mockito.mock(StyleAttribute.class);
		StyleParser parserMock = Mockito.mock(StyleParser.class);
		Mockito.when(parserMock.getStyles(Mockito.anyString())).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 2, Lists.newArrayList()),
					new SingleStyleSubstring(2, 4, Lists.newArrayList(attributeMock))
				)).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 3, Lists.newArrayList())
				)).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 2, Lists.newArrayList())
				));
		List<StyleParser> globalParsers = isSplitting ? Lists.newArrayList() : Lists.newArrayList(parserMock);
		List<StyleParser> splittingParsers = isSplitting ? Lists.newArrayList(parserMock) : Lists.newArrayList();
		TheStyledMessageBuilder parser = new TheStyledMessageBuilder(globalParsers, splittingParsers);
		String message1 = "abcd";
		String message2 = "abc";
		String message3 = "ab";
		int abcAttributeCount = isSplitting ? 0 : 1;
		
		parser.append(message1);
		parser.append(message2);
		parser.append(message3);
		StyledMessage styledMessage = parser.build();
		
		List<SingleStyleMessage> parts = styledMessage.getStyledParts();
		Assertions.assertEquals(4, parts.size());
		Assertions.assertEquals("ab", parts.get(0).getText());
		Assertions.assertEquals("cd", parts.get(1).getText());
		Assertions.assertEquals("abc", parts.get(2).getText());
		Assertions.assertEquals("ab", parts.get(3).getText());
		Assertions.assertEquals(0, parts.get(0).getStyle().size());
		Assertions.assertEquals(1, parts.get(1).getStyle().size());
		Assertions.assertEquals(abcAttributeCount, parts.get(2).getStyle().size());
		Assertions.assertEquals(abcAttributeCount, parts.get(3).getStyle().size());
	}
}

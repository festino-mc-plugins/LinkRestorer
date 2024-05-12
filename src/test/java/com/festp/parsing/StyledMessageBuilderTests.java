package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.StyledMessageBuilder;
import com.festp.styledmessage.components.TextComponent;
import com.google.common.collect.Lists;

class StyledMessageBuilderTests
{
	@Test
	public void buildEmptyString_WhenZeroAppends() {
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(), Lists.newArrayList());
		
		StyledMessage styledMessage = parser.build();
		
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
		
		Assertions.assertEquals(1, styledMessage.getStyledParts().size());
		SingleStyleMessage styledPart = styledMessage.getStyledParts().get(0);
		Assertions.assertEquals(message, styledPart.getText());
		Assertions.assertEquals(0, styledPart.getComponents().size());
	}

	@Test
	public void build_GlobalParser_NoTextComponents() {
		String message = "abcdedcba";
		ComponentParser parserMock = Mockito.mock(ComponentParser.class);
		Mockito.when(parserMock.getComponents(message)).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 1, Lists.newArrayList()),
					new SingleStyleSubstring(2, 7, Lists.newArrayList()),
					new SingleStyleSubstring(8, 9, Lists.newArrayList())
				));
		StyledMessageBuilder parser = new StyledMessageBuilder(Lists.newArrayList(parserMock), Lists.newArrayList());
		
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
		ComponentParser parserMock_1 = Mockito.mock(ComponentParser.class);
		Mockito.when(parserMock_1.getComponents(message)).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 3, Lists.newArrayList())
				));
		ComponentParser parserMock_2 = Mockito.mock(ComponentParser.class);
		Mockito.when(parserMock_2.getComponents(Mockito.any())).thenReturn(Lists.newArrayList());
		List<ComponentParser> parsers = Lists.newArrayList(parserMock_1, parserMock_2);
		List<ComponentParser> globalParsers = isSplitting ? Lists.newArrayList() : parsers;
		List<ComponentParser> splittingParsers =  isSplitting ? parsers : Lists.newArrayList();
		StyledMessageBuilder parser = new StyledMessageBuilder(globalParsers, splittingParsers);
		
		parser.append(message);

		Mockito.verify(parserMock_1).getComponents("abcd");
		Mockito.verify(parserMock_2).getComponents("abc");
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void build_MultipleAppends_StartStyle(boolean isSplitting) {
		TextComponent componentMock = Mockito.mock(TextComponent.class);
		ComponentParser parserMock = Mockito.mock(ComponentParser.class);
		Mockito.when(parserMock.getComponents(Mockito.anyString())).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 2, Lists.newArrayList()),
					new SingleStyleSubstring(2, 4, Lists.newArrayList(componentMock))
				)).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 3, Lists.newArrayList())
				)).thenReturn(Lists.newArrayList(
					new SingleStyleSubstring(0, 2, Lists.newArrayList())
				));
		List<ComponentParser> globalParsers = isSplitting ? Lists.newArrayList() : Lists.newArrayList(parserMock);
		List<ComponentParser> splittingParsers = isSplitting ? Lists.newArrayList(parserMock) : Lists.newArrayList();
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
		Assertions.assertEquals(4, parts.size());
		Assertions.assertEquals("ab", parts.get(0).getText());
		Assertions.assertEquals("cd", parts.get(1).getText());
		Assertions.assertEquals("abc", parts.get(2).getText());
		Assertions.assertEquals("ab", parts.get(3).getText());
		Assertions.assertEquals(0, parts.get(0).getComponents().size());
		Assertions.assertEquals(1, parts.get(1).getComponents().size());
		Assertions.assertEquals(abcComponents, parts.get(2).getComponents().size());
		Assertions.assertEquals(abcComponents, parts.get(3).getComponents().size());
	}
}

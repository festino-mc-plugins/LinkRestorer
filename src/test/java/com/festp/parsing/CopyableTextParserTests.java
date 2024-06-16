package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.festp.utils.CommandValidator;

class CopyableTextParserTests extends SingleStyleSubstringHelpers
{
	@ParameterizedTest
	@ValueSource(strings = {"test", "a, b", "a ,, b", ",,a,b,"})
	void parse_NoCopyable(String text)
	{
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		CopyableTextParser parser = new CopyableTextParser(",,", ",,", true, true, commandValidator);
		
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		Assertions.assertEquals(1, substrings.size());
		assertPlain(substrings.get(0), 0, text.length());
	}
	
	@Test
	void parse_SingleCopyable()
	{
		String copyable = "abc";
		String text = ",," + copyable + ",,";
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		CopyableTextParser parser = new CopyableTextParser(",,", ",,", true, true, commandValidator);
		
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		Assertions.assertEquals(1, substrings.size());
		assertCopyable(substrings.get(0), 2, text.length() - 2, copyable);
	}
	
	@Test
	void parse_Command()
	{
		String copyable = "abc";
		String text = ",," + copyable + ",,";
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		Mockito.when(commandValidator.commandExists(Mockito.anyString())).thenReturn(true);
		CopyableTextParser parser = new CopyableTextParser(",,", ",,", true, true, commandValidator);
		
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		Assertions.assertEquals(1, substrings.size());
		assertCommand(substrings.get(0), 2, text.length() - 2, copyable);
	}
	
	@Test
	void parse_OnlyCommands()
	{
		String copyable = "abc";
		String text = ",," + copyable + ",,";
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		Mockito.when(commandValidator.commandExists(Mockito.anyString())).thenReturn(true).thenReturn(false);
		CopyableTextParser parser = new CopyableTextParser(",,", ",,", true, false, commandValidator);
		
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		Assertions.assertEquals(1, substrings.size());
		assertCommand(substrings.get(0), 2, text.length() - 2, copyable);

		substrings = parser.getStyles(text);
		
		Assertions.assertEquals(1, substrings.size());
		assertPlain(substrings.get(0), 2, text.length() - 2);
	}
	
	@Test
	void parse_RegexEscaping()
	{
		String copyable = "abc";
		String text = ".." + copyable + "??";
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		
		CopyableTextParser parser = new CopyableTextParser("..", "??", true, true, commandValidator);
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		Assertions.assertEquals(1, substrings.size());
		assertCopyable(substrings.get(0), 2, text.length() - 2, copyable);
	}
	
	@Test
	void parse_QuotedBeginQuotes()
	{
		String text = "....??";
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		
		CopyableTextParser parser = new CopyableTextParser("..", "??", true, true, commandValidator);
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		Assertions.assertEquals(1, substrings.size());
		assertCopyable(substrings.get(0), 2, text.length() - 2, "..");
	}
	
	@Test
	void parse_QuotedEndQuotes()
	{
		String text = "..????";
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		
		CopyableTextParser parser = new CopyableTextParser("..", "??", true, true, commandValidator);
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		Assertions.assertEquals(1, substrings.size());
		assertCopyable(substrings.get(0), 2, text.length() - 2, "??");
	}

	@ParameterizedTest
	@ValueSource(strings = {",,", ",, ", " ,,"})
	void parse_QuotedQuotes(String copyable)
	{
		String text = ",," + copyable + ",,";
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		
		CopyableTextParser parser = new CopyableTextParser(",,", ",,", true, true, commandValidator);
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		Assertions.assertEquals(1, substrings.size());
		assertCopyable(substrings.get(0), 2, text.length() - 2, copyable);
	}
	
	@Test
	void parse_MultipleCopyables()
	{
		String copyable1 = "abc";
		String copyable2 = "def";
		String text = "See ,," + copyable1 + ",, or ,," + copyable2 + ",,.";
		CommandValidator commandValidator = Mockito.mock(CommandValidator.class);
		CopyableTextParser parser = new CopyableTextParser(",,", ",,", true, true, commandValidator);
		
		List<SingleStyleSubstring> substrings = parser.getStyles(text);

		int copyable1_start = text.indexOf(copyable1);
		int copyable2_start = text.indexOf(copyable2);
		Assertions.assertEquals(5, substrings.size());
		assertPlain(substrings.get(0), 0, 4);
		assertCopyable(substrings.get(1), copyable1_start, copyable1_start + copyable1.length(), copyable1);
		assertPlain(substrings.get(2), copyable2_start - 6, copyable2_start - 2);
		assertCopyable(substrings.get(3), copyable2_start, copyable2_start + copyable2.length(), copyable2);
		assertPlain(substrings.get(4), text.length() - 1, text.length());
	}
}

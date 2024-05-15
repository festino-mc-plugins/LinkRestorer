package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CopyableTextParserTests extends SingleStyleSubstringHelpers
{
	@ParameterizedTest
	@ValueSource(strings = {"test", "a, b", "a ,, b", ",,a,b,"})
	void parse_NoCopyable(String text)
	{
		List<SingleStyleSubstring> substrings = new CopyableTextParser().getComponents(text);

		Assertions.assertEquals(1, substrings.size());
		assertPlain(substrings.get(0), 0, text.length());
	}
	
	@Test
	void parse_SingleCopyable()
	{
		String copyable = "abc";
		String text = ",," + copyable + ",,";
		
		List<SingleStyleSubstring> substrings = new CopyableTextParser().getComponents(text);

		Assertions.assertEquals(1, substrings.size());
		assertCopyable(substrings.get(0), 2, text.length() - 2, copyable);
	}
	
	@Test
	void parse_MultipleCopyables()
	{
		String copyable1 = "abc";
		String copyable2 = "def";
		String text = "See ,," + copyable1 + ",, or ,," + copyable2 + ",,.";
		
		List<SingleStyleSubstring> substrings = new CopyableTextParser().getComponents(text);

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

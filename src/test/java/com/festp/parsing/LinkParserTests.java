package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LinkParserTests extends SingleStyleSubstringHelpers
{
	@ParameterizedTest
	@ValueSource(strings = {"simple text", "o.o", "T.T"})
	void parseNoLinks(String text) {
		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(text);
		
		Assertions.assertEquals(1, substrings.size());
		assertPlain(substrings.get(0), 0, text.length());
	}

	@Test
	void parse_SingleLink_OnlyLink() {
		String url = "http://www.example.com";
		String text = url;

		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(text);
		
		Assertions.assertEquals(1, substrings.size());
		assertLink(substrings.get(0), 0, url.length(), url);
	}

	@Test
	void parse_SingleLink_DotAtTheEnd() {
		String url = "http://www.example.com";
		String text = url + ".";

		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(text);

		Assertions.assertEquals(2, substrings.size());
		assertLink(substrings.get(0), 0, url.length(), url);
		assertPlain(substrings.get(1), url.length(), url.length() + 1);
	}

	@Test
	void parse_SingleLink_WordAtBeginning() {
		String url = "http://www.example.com";
		String text = "see " + url;

		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(text);

		Assertions.assertEquals(2, substrings.size());
		assertPlain(substrings.get(0), 0, 4);
		assertLink(substrings.get(1), 4, 4 + url.length(), url);
	}

	@Test
	void parse_SingleLink_WordsAtEnds() {
		String url = "http://www.example.com";
		String text = "link -> " + url + " <- !!!";

		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(text);

		Assertions.assertEquals(3, substrings.size());
		assertPlain(substrings.get(0), 0, 8);
		assertLink(substrings.get(1), 8, 8 + url.length(), url);
		assertPlain(substrings.get(2), 8 + url.length(), 8 + url.length() + 7);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"www.example.com",
			"example.com"})
	void parseProtocolless(String text) {
		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(text);

		Assertions.assertEquals(1, substrings.size());
		assertLink(substrings.get(0), 0, text.length(), "https://" + text);
	}

	@Test
	void parseMultipleLinks() {
		String url_1 = "http://www.link1.com";
		String url_2 = "http://www.link2.com";
		String message = "link1: " + url_1 + ", link2: " + url_2 + ".";
		int start_1 = message.indexOf(url_1);
		int start_2 = message.indexOf(url_2);
		
		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(message);

		Assertions.assertEquals(5, substrings.size());
		assertPlain(substrings.get(0), 0, start_1);
		assertLink(substrings.get(1), start_1, start_1 + url_1.length(), url_1);
		assertPlain(substrings.get(2), start_1 + url_1.length(), start_2);
		assertLink(substrings.get(3), start_2, start_2 + url_2.length(), url_2);
		assertPlain(substrings.get(4), start_2 + url_2.length(), start_2 + url_2.length() + 1);
	}

	@Test
	void parseComplexLink() {
		// won't support "http://username:password@example.com:8080/test?param=value&p=1#anchor";
		String url = "http://example.com:8080/test?param=value&p=1#anchor";

		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(url);

		Assertions.assertEquals(1, substrings.size());
		assertLink(substrings.get(0), 0, url.length(), url);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"https://www.лекарство.net/",
			"https://example.com/初音ミク"})
	void parseNonLatin(String url) {
		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(url);

		Assertions.assertEquals(1, substrings.size());
		assertLink(substrings.get(0), 0, url.length());
	}

	@Test
	void parseIPv4() {
		LinkParser linkParser = new LinkParser(true, true);
		String url = "127.0.0.1";
		
		List<SingleStyleSubstring> substrings = linkParser.getComponents(url);
		
		Assertions.assertEquals(1, substrings.size());
		assertLink(substrings.get(0), 0, url.length(), "https://" + url);
		
		substrings = linkParser.getComponents(url + ".");
		
		Assertions.assertEquals(2, substrings.size());
		assertLink(substrings.get(0), 0, url.length(), "https://" + url);
		assertPlain(substrings.get(1), url.length(), url.length() + 1);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"256.0.0.1",
			"127.0.0",
			"127.0.0.1.1",
			"1.0..1",
			"127.0.FF.01"})
	void parseInvalidIPv4(String url) {
		List<SingleStyleSubstring> substrings = new LinkParser(true, true).getComponents(url);
		
		Assertions.assertEquals(1, substrings.size());
		assertPlain(substrings.get(0), 0, url.length());
	}
}

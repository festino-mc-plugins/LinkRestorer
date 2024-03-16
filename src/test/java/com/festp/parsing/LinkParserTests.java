package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LinkParserTests {

	@ParameterizedTest
	@ValueSource(strings = {"simple text", "o.o"})
	void parseNoLinks(String text) {
		List<Link> links = LinkParser.getLinks(text);
		Assertions.assertEquals(0, links.size());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"http://www.example.com",
			"http://www.example.com.",
			"see http://www.example.com", 
			"link -> http://www.example.com <- !!!"})
	void parseSingleLink(String text) {
		String url = "http://www.example.com";
		List<Link> links = LinkParser.getLinks(text);
		Assertions.assertEquals(1, links.size());
		Link link = links.get(0);
		Assertions.assertEquals(url, link.getText());
		Assertions.assertEquals(url, link.getUrl());
		Assertions.assertEquals(text.indexOf(url), link.beginIndex);
		Assertions.assertEquals(text.indexOf(url) + url.length(), link.endIndex);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"www.example.com",
			"example.com"})
	void parseProtocolless(String text) {
		List<Link> links = LinkParser.getLinks(text);
		Assertions.assertEquals(1, links.size());
		Link link = links.get(0);
		Assertions.assertEquals(text, link.getText());
		Assertions.assertEquals(Link.DEFAULT_PROTOCOL + text, link.getUrl());
	}

	@Test
	void parseMultipleLinks() {
		String message = "link1: http://www.link1.com, link2: http://www.link2.com.";
		List<Link> links = LinkParser.getLinks(message);
		Assertions.assertEquals(2, links.size());
		
		Link link1 = links.get(0);
		Assertions.assertEquals(7, link1.beginIndex);
		Assertions.assertEquals(27, link1.endIndex);
		Assertions.assertEquals("http://www.link1.com", link1.getText());
		Assertions.assertEquals("http://www.link1.com", link1.getUrl());

		Link link2 = links.get(1);
		Assertions.assertEquals(36, link2.beginIndex);
		Assertions.assertEquals(56, link2.endIndex);
		Assertions.assertEquals("http://www.link2.com", link2.getText());
		Assertions.assertEquals("http://www.link2.com", link2.getUrl());
	}

	@Test
	void parseIPv4() {
		String url = "127.0.0.1";
		List<Link> links = LinkParser.getLinks(url);
		Assertions.assertEquals(1, links.size());
		Assertions.assertEquals(url, links.get(0).getText());
		Assertions.assertEquals(Link.DEFAULT_PROTOCOL + url, links.get(0).getUrl());
		
		links = LinkParser.getLinks(url + ".");
		Assertions.assertEquals(1, links.size());
		Assertions.assertEquals(url, links.get(0).getText());
		Assertions.assertEquals(Link.DEFAULT_PROTOCOL + url, links.get(0).getUrl());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"256.0.0.1",
			"127.0.0",
			"127.0.0.1.1",
			"1.0..1",
			"127.0.FF.01"})
	void parseInvalidIPv4(String url) {
		List<Link> links = LinkParser.getLinks(url);
		Assertions.assertEquals(0, links.size());
	}
}

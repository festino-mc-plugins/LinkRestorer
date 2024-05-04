package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.festp.styledmessage.components.Link;

public class LinkParserTests {

	@ParameterizedTest
	@ValueSource(strings = {"simple text", "o.o", "T.T"})
	void parseNoLinks(String text) {
		List<Link> links = new LinkParser().getComponents(text);
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
		List<Link> links = new LinkParser().getComponents(text);
		Assertions.assertEquals(1, links.size());
		Link link = links.get(0);
		Assertions.assertEquals(url, link.getPlainText());
		Assertions.assertEquals(url, link.getUrl());
		Assertions.assertEquals(text.indexOf(url), link.getBeginIndex());
		Assertions.assertEquals(text.indexOf(url) + url.length(), link.getEndIndex());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"www.example.com",
			"example.com"})
	void parseProtocolless(String text) {
		List<Link> links = new LinkParser().getComponents(text);
		Assertions.assertEquals(1, links.size());
		Link link = links.get(0);
		Assertions.assertEquals(text, link.getPlainText());
		Assertions.assertEquals(Link.DEFAULT_PROTOCOL + text, link.getUrl());
	}

	@Test
	void parseMultipleLinks() {
		String message = "link1: http://www.link1.com, link2: http://www.link2.com.";
		List<Link> links = new LinkParser().getComponents(message);
		Assertions.assertEquals(2, links.size());
		
		Link link1 = links.get(0);
		Assertions.assertEquals(7, link1.getBeginIndex());
		Assertions.assertEquals(27, link1.getEndIndex());
		Assertions.assertEquals("http://www.link1.com", link1.getPlainText());
		Assertions.assertEquals("http://www.link1.com", link1.getUrl());

		Link link2 = links.get(1);
		Assertions.assertEquals(36, link2.getBeginIndex());
		Assertions.assertEquals(56, link2.getEndIndex());
		Assertions.assertEquals("http://www.link2.com", link2.getPlainText());
		Assertions.assertEquals("http://www.link2.com", link2.getUrl());
	}

	@Test
	void parseComplexLink() {
		//String url = "a http://username:password@example.com:8080/test?param=value&p=1#anchor a";
		String url = "http://example.com:8080/test?param=value&p=1#anchor";
		List<Link> links = new LinkParser().getComponents("a " + url + " a");
		Assertions.assertEquals(1, links.size());
		
		Link link = links.get(0);
		Assertions.assertEquals(2, link.getBeginIndex());
		Assertions.assertEquals(url.length() + 2, link.getEndIndex());
		Assertions.assertEquals(url, link.getPlainText());
		Assertions.assertEquals(url, link.getUrl());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"https://www.лекарство.net/",
			"https://example.com/初音ミク"})
	void parseNonLatin(String url) {
		List<Link> links = new LinkParser().getComponents(url);
		Assertions.assertEquals(1, links.size());
		
		Link link = links.get(0);
		Assertions.assertEquals(0, link.getBeginIndex());
		Assertions.assertEquals(url.length(), link.getEndIndex());
		Assertions.assertEquals(url, link.getPlainText());
	}

	@Test
	void parseIPv4() {
		LinkParser linkParser = new LinkParser();
		String url = "127.0.0.1";
		List<Link> links = linkParser.getComponents(url);
		Assertions.assertEquals(1, links.size());
		Assertions.assertEquals(url, links.get(0).getPlainText());
		Assertions.assertEquals(Link.DEFAULT_PROTOCOL + url, links.get(0).getUrl());
		
		links = linkParser.getComponents(url + ".");
		Assertions.assertEquals(1, links.size());
		Assertions.assertEquals(url, links.get(0).getPlainText());
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
		List<Link> links = new LinkParser().getComponents(url);
		Assertions.assertEquals(0, links.size());
	}
}

package com.festp.styledmessage;

import java.util.List;

import com.festp.parsing.SingleStyleSubstring;
import com.festp.styledmessage.attributes.StyleAttribute;
import com.festp.styledmessage.attributes.UpdatableStyleAttribute;
import com.google.common.collect.Lists;

class ClosableStyledMessage
{
	private List<ClosableStyledMessagePart> styledParts;
	private List<StyleAttribute> endStyle;
	
	public ClosableStyledMessage(List<SingleStyleMessage> styledParts)
	{
		this.styledParts = Lists.newArrayList(new ClosableStyledMessagePart(styledParts, false));
		this.endStyle = styledParts.size() == 0 ? Lists.newArrayList() : styledParts.get(styledParts.size() - 1).getStyle();
	}
	
	public class ClosableStyledMessagePart
	{
		private final boolean isClosed;
		private List<SingleStyleMessage> styledParts;
		
		private ClosableStyledMessagePart(List<SingleStyleMessage> styledParts, boolean isClosed) {
			this.styledParts = styledParts;
			this.isClosed = isClosed;
		}
		
		public boolean isClosed() { return isClosed; }
		
		public List<SingleStyleMessage> getStyledParts() { return styledParts; }
		
		public String getPlainText() {
			StringBuilder builder = new StringBuilder();
			for (SingleStyleMessage part: styledParts) {
				builder.append(part.getText());
			}
			return builder.toString();
		}
		
		public void replace(Iterable<SingleStyleSubstring> styledSubstrings, boolean closeChanged)
		{
			ClosableStyledMessage.this.replace(this, styledSubstrings, closeChanged);
		}
	}
	
	public List<SingleStyleMessage> getStyledParts()
	{
		List<SingleStyleMessage> styledParts = Lists.newArrayList();
		for (ClosableStyledMessagePart styledPart : this.styledParts)
		{
			styledParts.addAll(styledPart.getStyledParts());
		}
		return styledParts;
	}

	public List<StyleAttribute> getEndStyle() {
		return endStyle;
	}
	
	public List<ClosableStyledMessagePart> getOpenParts()
	{
		List<ClosableStyledMessagePart> styledParts = Lists.newArrayList();
		for (ClosableStyledMessagePart styledPart : this.styledParts)
		{
			if (!styledPart.isClosed)
				styledParts.add(styledPart);
		}
		return styledParts;
	}
	
	private void replace(ClosableStyledMessagePart part, Iterable<SingleStyleSubstring> styledSubstrings, boolean closeChanged)
	{
		int index = styledParts.indexOf(part);
		if (index < 0) return;

		int partLength = part.getPlainText().length();
		List<ClosableStyledMessagePart> newParts = Lists.newArrayList();
		boolean inClosed = false;
		List<SingleStyleMessage> newStyledParts = Lists.newArrayList();
		List<StyleAttribute> endAttributes = null;
		for (SingleStyleSubstring styledSubstring : styledSubstrings)
		{
			if (!validateSubstring(styledSubstring, partLength))
				continue;
			
			if (closeChanged && (styledSubstring.style.size() > 0) != inClosed) {
				if (newStyledParts.size() > 0) {
					newParts.add(new ClosableStyledMessagePart(newStyledParts, inClosed));
					newStyledParts = Lists.newArrayList();
				}
				inClosed = !inClosed;
			}
			appendSubstring(newStyledParts, styledSubstring, part.styledParts);
			endAttributes = styledSubstring.style;
		}
		if (newStyledParts.size() > 0) {
			newParts.add(new ClosableStyledMessagePart(newStyledParts, closeChanged && inClosed));
		}
		
		if (endAttributes != null)
		{
			endStyle = mergeStyles(endStyle, endAttributes);
		}
		
		styledParts.remove(index);
		styledParts.addAll(index, newParts);
	}

	private boolean validateSubstring(SingleStyleSubstring styledSubstring, int length)
	{
		if (styledSubstring.beginIndex < 0)
		{
			System.err.println("Styled substring index is out of range " + styledSubstring.beginIndex + " < 0");
			return false;
		}
		if (styledSubstring.endIndex > length)
		{
			System.err.println("Styled substring index is out of range " + styledSubstring.endIndex + " > " + length);
			return false;
		}
		if (styledSubstring.beginIndex > styledSubstring.endIndex)
		{
			System.err.println("Styled substring has negative length: " + styledSubstring.beginIndex + " > " + styledSubstring.endIndex);
			return false;
		}
		
		return true;
	}
	
	/** Append <b>styledSubstring</b> to <b>styles</b> updating indices and merging styles with <b>oldStyles</b>
	 * @param styles is new styled parts
	 * @param styledSubstring is new styled substring, indices belong to old plain text
	 * @param oldStyles defines old styled parts (and therefore old plain text)
	 * */
	private void appendSubstring(List<SingleStyleMessage> parts, SingleStyleSubstring styledSubstring, List<SingleStyleMessage> oldParts)
	{
		if (styledSubstring.beginIndex == styledSubstring.endIndex) return;
		
		int partBeginIndex = 0;
		for (SingleStyleMessage oldPart : oldParts)
		{
			int partLength = oldPart.getText().length();
			int partEndIndex = partBeginIndex + partLength;
			if (partEndIndex < styledSubstring.beginIndex) {
				partBeginIndex = partEndIndex;
				continue;
			}
			
			List<StyleAttribute> style = styledSubstring.style.size() == 0 ? oldPart.getStyle() :
				oldPart.getStyle().size() == 0 ? styledSubstring.style : null;
			
			if (style == null) {
				style = mergeStyles(oldPart.getStyle(), styledSubstring.style);
			}
			
			int beginIndex = Math.max(styledSubstring.beginIndex - partBeginIndex, 0);
			int endIndex = Math.min(styledSubstring.endIndex - partBeginIndex, partLength);
			if (beginIndex < endIndex) {
				String plainText = oldPart.getText().substring(beginIndex, endIndex);
				parts.add(new SingleStyleMessage(plainText, style));
			}
			partBeginIndex = partEndIndex;
			if (partBeginIndex >= styledSubstring.endIndex) break;
		} 
	}
	
	
	private List<StyleAttribute> mergeStyles(List<StyleAttribute> oldStyle, List<StyleAttribute> newAttributes) {
		List<StyleAttribute> newStyle = Lists.newArrayList(oldStyle);
		for (StyleAttribute newAttribute : newAttributes)
		{
			if (!(newAttribute instanceof UpdatableStyleAttribute)) {
				newStyle.add(newAttribute);
				continue;
			}
			boolean updated = false;
			for (int i = 0; i < newStyle.size(); i++)
			{
				StyleAttribute oldAttribute = newStyle.get(i);
				if (newAttribute.getClass().equals(oldAttribute.getClass())) {
					UpdatableStyleAttribute updatedAttribute = ((UpdatableStyleAttribute) oldAttribute).clone();
					((UpdatableStyleAttribute) updatedAttribute).update((UpdatableStyleAttribute) newAttribute);
					newStyle.set(i, updatedAttribute);
					updated = true;
					break;
				}
			}
			if (!updated) {
				newStyle.add(newAttribute);
			}
		}
		return newStyle;
	}
}

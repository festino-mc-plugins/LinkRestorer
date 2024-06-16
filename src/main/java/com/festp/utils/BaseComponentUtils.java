package com.festp.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;

import com.festp.handlers.MessageInfoProvider.MessageInfo;
import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;

public class BaseComponentUtils
{
	public static class ParseResult
	{
		public final MessageInfo messageInfo;
		public final List<BaseComponent> formatComponents;
		public final List<BaseComponent> messageComponents;
		public final List<Integer> messagePositions;
		
		public ParseResult(
				MessageInfo messageInfo,
				List<BaseComponent> formatComponents,
				List<BaseComponent> messageComponents,
				List<Integer> messagePositions)
		{
			this.messageInfo = messageInfo;
			this.formatComponents = formatComponents;
			this.messageComponents = messageComponents;
			this.messagePositions = messagePositions;
		}
	}
	
	public static ParseResult tryParse(MessageInfo messageInfo, BaseComponent[] components)
	{
		// assumption: sender name could not be a parsable component (no need to distinguish between sender and content)
		// assumption: format has only one content specifier
		String colorlessMessage = BaseComponent.toPlainText(components);
		String colorlessSender = ChatColor.stripColor(messageInfo.sender.getDisplayName());
		if (!colorlessMessage.contains(colorlessSender))
			return null;

		String colorlessContent = ChatColor.stripColor(messageInfo.content);
		if (!colorlessMessage.contains(colorlessContent))
			return null;

		// BaseComponents have tree structure
		BaseComponent[] flatComponents = flattenComponents(components);
		ComponentSubstringData substringData = getSubstringData(flatComponents, colorlessContent);
		if (substringData == null)
			return null;

		int messageComponentLength = flatComponents[substringData.startComponentIndex].toPlainText().length();
		List<BaseComponent> formatComponents = Lists.newArrayList();
		List<BaseComponent> messageComponents = Lists.newArrayList();
		List<Integer> messagePositions = Lists.newArrayList();
		for (int i = 0; i < substringData.startComponentIndex; i++)
			formatComponents.add(flatComponents[i]);
		
		splitComponent(flatComponents[substringData.startComponentIndex], substringData.startInnerIndex, formatComponents, messageComponents);
		
		messagePositions.add(formatComponents.size());

		for (int i = substringData.startComponentIndex + 1; i < substringData.endComponentIndex; i++)
			messageComponents.add(flatComponents[i]);

		if (substringData.startComponentIndex == substringData.endComponentIndex) {
			BaseComponent componentPart = messageComponents.remove(messageComponents.size() - 1);
			// component text could be prefixed by color codes
			int index = componentPart.toPlainText().length() - (messageComponentLength - substringData.endInnerIndex);
			splitComponent(componentPart, index, messageComponents, formatComponents);
		}
		else {
			splitComponent(flatComponents[substringData.endComponentIndex], substringData.endInnerIndex, messageComponents, formatComponents);
		}

		for (int i = substringData.endComponentIndex + 1; i < flatComponents.length; i++)
			formatComponents.add(flatComponents[i]);
		
		return new ParseResult(messageInfo, formatComponents, messageComponents, messagePositions);
	}
	
	private static void splitComponent(BaseComponent component, int index, List<BaseComponent> left, List<BaseComponent> right)
	{
		if (index == 0) {
			right.add(component);
		}
		else if (index == component.toPlainText().length()) {
			left.add(component);
		}
		else {
			TextComponent leftComponent = (TextComponent) component;
			String componentText = leftComponent.getText();
			String leftText = componentText.substring(0, index);
			leftComponent.setText(leftText);
			left.add(leftComponent);
			
			TextComponent rightComponent = leftComponent.duplicate();
			StringBuilder colorCodes = new StringBuilder();
			for (int i = 0; i < leftText.length(); i++)
			{
				if (leftText.charAt(i) == ChatColor.COLOR_CHAR) {
					colorCodes.append(leftText.charAt(i));
					i++;
					if (i < leftText.length())
						colorCodes.append(leftText.charAt(i));
					
					continue;
				}
			}
			rightComponent.setText(colorCodes + componentText.substring(index));
			right.add(rightComponent);
		}
	}
	
	public static BaseComponent[] flattenComponents(BaseComponent[] components)
	{
		List<BaseComponent> result = Lists.newArrayList();
		for (BaseComponent component : components)
			for (BaseComponent newComponent : flattenComponent(component, new TextComponent()))
				result.add(newComponent);
		
		return result.toArray(new BaseComponent[0]);
	}
	
	private static Collection<? extends BaseComponent> flattenComponent(BaseComponent component, BaseComponent parentFormatting)
	{
		BaseComponent componentCopy = component.duplicate();
		// TODO test identity of formatting
		componentCopy.copyFormatting(parentFormatting, FormatRetention.ALL, true);
		componentCopy.copyFormatting(component, FormatRetention.ALL, false);
		resetExtra(componentCopy);

		List<BaseComponent> result = Lists.newArrayList(componentCopy);
		if (component.getExtra() == null)
			return result;
		
		for (BaseComponent extra : component.getExtra())
			result.addAll(flattenComponent(extra, componentCopy));

		return result;
	}
	
	private static void resetExtra(BaseComponent component)
	{
		// could not just
		// 1. componentCopy.setExtra(Lists.newArrayList());
		// causes Invalid chat component: Unexpected empty array of components
		// 2. componentCopy.setExtra(null);
		// throws NullPointerException
		// TODO optimize reflection
		Field extraField;
		try {
			extraField = BaseComponent.class.getDeclaredField("extra");
			extraField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return;
		}

		try {
			extraField.set(component, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private static ComponentSubstringData getSubstringData(BaseComponent[] flattenComponents, String colorlessSubstring)
	{
		// merge plain text from consecutive TextComponents
		// checking if plain text contains substring
		int startComponentIndex = -1;
		int startInnerIndex = -1;
		int endComponentIndex = -1;
		int endInnerIndex = -1;
		int substringLength = 0;
		for (int i = 0; i < flattenComponents.length; i++)
		{
			if (!(flattenComponents[i] instanceof TextComponent)) {
				continue;
			}
			
			StringBuilder text = new StringBuilder();
			while (i < flattenComponents.length && flattenComponents[i] instanceof TextComponent) {
				String componentText = flattenComponents[i].toPlainText();
				text.append(componentText);
				SubstringBounds substringBounds = indexOfColorCoded(text.toString(), colorlessSubstring);
				if (substringBounds != null) {
					endComponentIndex = i;
					endInnerIndex = componentText.length() - (text.length() - substringBounds.end);
					substringLength = substringBounds.end - substringBounds.begin;
					break;
				}
				i++;
			}
			if (endComponentIndex >= 0)
			{
				int remainingLength = substringLength - endInnerIndex;
				if (remainingLength <= 0) {
					startComponentIndex = i;
					startInnerIndex = -remainingLength;
				}
				else {
					while (i >= 0) {
						i--;
						int componentLength = flattenComponents[i].toPlainText().length();
						remainingLength -= componentLength;
						if (remainingLength <= 0) {
							startComponentIndex = i;
							startInnerIndex = -remainingLength;
							break;
						}
					}
				}
				break;
			}
		}
		if (startComponentIndex < 0)
			return null;
		
		return new ComponentSubstringData(startComponentIndex, startInnerIndex, endComponentIndex, endInnerIndex);
	}
	
	private static SubstringBounds indexOfColorCoded(String text, String colorlessSubstring)
	{
		String colorlessText = ChatColor.stripColor(text.toString());
		int toSkip = colorlessText.indexOf(colorlessSubstring);
		if (toSkip < 0)
			return null;
		
		int actualStart = 0;
		while (toSkip > 0)
		{
			if (text.charAt(actualStart) == ChatColor.COLOR_CHAR) {
				actualStart += 2;
				continue;
			}
			toSkip--;
			actualStart++;
		}
		
		int actualEnd = actualStart;
		toSkip = colorlessSubstring.length();
		while (toSkip > 0)
		{
			if (text.charAt(actualEnd) == ChatColor.COLOR_CHAR) {
				actualEnd += 2;
				continue;
			}
			toSkip--;
			actualEnd++;
		}
		
		return new SubstringBounds(actualStart, actualEnd);
	}
	
	private static class SubstringBounds
	{
		public final int begin;
		public final int end;
		
		public SubstringBounds(int begin, int end) {
			this.begin = begin;
			this.end = end;
		}
	}
	
	private static class ComponentSubstringData
	{
		public final int startComponentIndex;
		public final int startInnerIndex;
		public final int endComponentIndex;
		public final int endInnerIndex;
		
		public ComponentSubstringData(int startComponentIndex, int startInnerIndex,int endComponentIndex, int endInnerIndex) {
			this.startComponentIndex = startComponentIndex;
			this.startInnerIndex = startInnerIndex;
			this.endComponentIndex = endComponentIndex;
			this.endInnerIndex = endInnerIndex;
		}
	}
}

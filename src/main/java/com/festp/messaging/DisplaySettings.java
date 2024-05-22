package com.festp.messaging;

public class DisplaySettings
{
	public final String formatLinks;
	public final String formatCommands;
	public final String formatCopyableText;

	public final boolean runCommands;
	
	public final String tooltipLinks;
	public final String tooltipCommands;
	public final String tooltipCopyableText;
	
	public DisplaySettings(
			String formatLinks,
			String formatCommands,
			String formatCopyableText,
			String tooltipLinks,
			String tooltipCommands,
			String tooltipCopyableText,
			boolean runCommands)
	{
		this.formatLinks = formatLinks;
		this.formatCommands = formatCommands;
		this.formatCopyableText = formatCopyableText;
		this.runCommands = runCommands;
		this.tooltipLinks = tooltipLinks;
		this.tooltipCommands = tooltipCommands;
		this.tooltipCopyableText = tooltipCopyableText;
	}
}

package com.festp.messaging;

public class DisplaySettings
{
	public final boolean underlineLinks;
	public final boolean underlineCommands;
	public final boolean underlineCopyableText;

	public final boolean runCommands;
	
	public final String tooltipLinks;
	public final String tooltipCommands;
	public final String tooltipCopyableText;
	
	public DisplaySettings(
			boolean underlineLinks,
			boolean underlineCommands,
			boolean underlineCopyableText,
			String tooltipLinks,
			String tooltipCommands,
			String tooltipCopyableText,
			boolean runCommands)
	{
		this.underlineLinks = underlineLinks;
		this.underlineCommands = underlineCommands;
		this.underlineCopyableText = underlineCopyableText;
		this.runCommands = runCommands;
		this.tooltipLinks = tooltipLinks;
		this.tooltipCommands = tooltipCommands;
		this.tooltipCopyableText = tooltipCopyableText;
	}
}

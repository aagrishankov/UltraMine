package org.ultramine.server.bootstrap.log4j;

/**
 * Used in {@link UMStripColorsRewritePolicy} to provide custom
 * unformatted (without control sequences) log messages
 * @see org.ultramine.server.internal.ChatComponentLogMessage
 */
public interface IUnformattedMessage
{
	/**
	 * @return text for writing to log file without control sequences (colors)
	 */
	String getUnformattedMessage();
}

package org.ultramine.server.internal;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.message.Message;
import org.ultramine.server.bootstrap.log4j.IUnformattedMessage;

public class ChatComponentLogMessage implements Message, IUnformattedMessage
{
	private final IChatComponent comp;
	private String formattedMessage;
	private String unformattedMessage;

	public ChatComponentLogMessage(IChatComponent comp)
	{
		this.comp = comp;
	}

	@Override
	public String getFormattedMessage()
	{
		if(formattedMessage == null)
			formattedMessage = comp.getFormattedText();
		return formattedMessage;
	}

	@Override
	public String getUnformattedMessage()
	{
		if(unformattedMessage == null)
			unformattedMessage = comp.getUnformattedText();
		return unformattedMessage;
	}

	@Override
	public String getFormat()
	{
		return comp instanceof ChatComponentTranslation ? ((ChatComponentTranslation) comp).getKey() : getUnformattedMessage();
	}

	@Override
	public Object[] getParameters()
	{
		return comp instanceof ChatComponentTranslation ? ((ChatComponentTranslation) comp).getFormatArgs() : null;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final ChatComponentLogMessage that = (ChatComponentLogMessage) o;

		return !(comp != null ? !comp.equals(that.comp) : that.comp != null);
	}

	@Override
	public int hashCode()
	{
		return comp.hashCode();
	}

	@Override
	public String toString()
	{
		return "ChatComponentLogMessage[message=" + comp + "]";
	}

	@Override
	public Throwable getThrowable()
	{
		return null;
	}
}

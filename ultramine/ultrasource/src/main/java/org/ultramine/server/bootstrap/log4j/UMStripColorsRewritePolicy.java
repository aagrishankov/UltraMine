package org.ultramine.server.bootstrap.log4j;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFormatMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.StringFormattedMessage;

@Plugin(name = "UMStripColorsRewritePolicy", category = "Core", elementType = "rewritePolicy", printObject = false)
public class UMStripColorsRewritePolicy implements RewritePolicy
{
	@Override
	public LogEvent rewrite(LogEvent source)
	{
		Message message = source.getMessage();
		if(message instanceof IUnformattedMessage)
		{
			message = new SimpleMessage(((IUnformattedMessage) message).getUnformattedMessage());
		}
		else if(message instanceof SimpleMessage || message instanceof ParameterizedMessage || message instanceof MessageFormatMessage || message instanceof StringFormattedMessage)
		{
			String text = message.getFormattedMessage();
			StringBuilder sb = new StringBuilder(text.length());
			UMConsoleLayout.stripControlSequences(sb, text);
			message = new SimpleMessage(sb.toString());
		}
		else
		{
			return source;
		}

		return new Log4jLogEvent(source.getLoggerName(), source.getMarker(), source.getFQCN(), source.getLevel(),
				message, source.getThrown(), source.getContextMap(), source.getContextStack(), source.getThreadName(),
				source.getSource(), source.getMillis());
	}

	@PluginFactory
	public static UMStripColorsRewritePolicy createPolicy(@PluginConfiguration final Configuration config)
	{
		return new UMStripColorsRewritePolicy();
	}
}

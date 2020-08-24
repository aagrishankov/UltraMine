package org.ultramine.server.bootstrap.log4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.helpers.Charsets;
import org.apache.logging.log4j.core.helpers.Constants;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.fusesource.jansi.Ansi;
import org.ultramine.server.bootstrap.UMBootstrap;
import org.ultramine.server.internal.JLineSupport;

@Plugin(name = "UMConsoleLayout", category = "Core", elementType = "layout", printObject = false)
public class UMConsoleLayout extends AbstractStringLayout
{
	private static final boolean COLORED = UMBootstrap.isColoredTerminal();
	private static final boolean ANSI = UMBootstrap.isAnsiColors();

	private static final String[] controlMap = new String[128];
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	protected UMConsoleLayout(Charset charset)
	{
		super(charset);
	}

	@Override
	public String toSerializable(LogEvent event)
	{
		String msg = event.getMessage().getFormattedMessage();
		StringBuffer trwn = formatThrowable(event.getThrown());
		StringBuilder sb = new StringBuilder(10+1+6+4+1+2 + msg.length() + (trwn == null ? 0 : trwn.length() + 2) + (ANSI ? 100 : 0));
		
		sb.append('[');
		sb.append(dateFormat.format(event.getMillis()));
		sb.append("] ");
		
		sb.append('[');
		Level level = event.getLevel();
		if(COLORED)
		{
			if(level == Level.WARN)
				control(sb, 'e');
			else if(level == Level.ERROR)
				control(sb, 'c');
			else if(level == Level.FATAL)
				control(sb, '4');
		}
		String levelS = event.getLevel().toString();
		int llen = levelS.length();
		if(llen > 4)
			levelS = levelS.substring(0, 4);
		sb.append(levelS);
		if(llen < 4)
			sb.append(' ');
		if(COLORED && (level == Level.WARN || level == Level.ERROR || level == Level.FATAL))
			control(sb, 'r');
		sb.append("] ");

		if(COLORED)
			simplifyControlSequences(sb, msg);
		else
			stripControlSequences(sb, msg);

		if(trwn != null)
		{
			sb.append(Constants.LINE_SEP);
			sb.append(trwn);
		}
		
		sb.append(Constants.LINE_SEP);
		
		return sb.toString();
	}

	private static StringBuffer formatThrowable(Throwable t)
	{
		if(t == null)
			return null;
		StringWriter w = new StringWriter(4096);
		t.printStackTrace(new PrintWriter(w));
		return w.getBuffer();
	}
	
	private static void control(StringBuilder sb, char code)
	{
		if(!ANSI)
		{
			sb.append('\u00a7');
			sb.append(code);
		}
		else
		{
			sb.append(controlMap[code]);
		}
	}

	/** simplifies redundant sequence, for example &4&5&5_&r&6&5_ will be simplified to &5__ */
	private static void simplifyControlSequences(StringBuilder sb, String msg)
	{
		char prevCode = 'r';
		for(int i = 0, s = msg.length(); i < s; i++)
		{
			char c = msg.charAt(i);
			if(c == '\u00a7')
			{
				char code = msg.charAt(++i);
				if(!isValidCode(code) || i+2 < s && msg.charAt(i+1) == '\u00a7' && isResetCode(msg.charAt(i+2)))
					continue;
				if(code != prevCode)
					control(sb, code);
				prevCode = code;
				continue;
			}
			sb.append(c);
		}
		if(ANSI && prevCode != 'r')
			control(sb, 'r');
	}

	private static boolean isValidCode(char code)
	{
		return code == 'r' || code >= '0' && code <= '9' || code >= 'a' && code <= 'f' || code >= 'k' && code <= 'o';
	}

	private static boolean isResetCode(char code)
	{
		return !(code >= 'k' && code <= 'o');
	}

	static void stripControlSequences(StringBuilder sb, String msg)
	{
		for(int i = 0, s = msg.length(); i < s; i++)
		{
			char c = msg.charAt(i);
			if(c == '\u00a7')
			{
				i++;
				continue;
			}
			sb.append(c);
		}
	}

	@Override
	public Map<String, String> getContentFormat()
	{
		return Collections.emptyMap();
	}
	
	@PluginFactory
	public static UMConsoleLayout createLayout(@PluginAttribute("charset") final String charsetName)
	{
		String overrideCharset = UMBootstrap.getTerminalCharset();
		return new UMConsoleLayout(Charsets.getSupportedCharset(overrideCharset != null && !overrideCharset.isEmpty() ? overrideCharset : charsetName));
	}

	static
	{
		if(ANSI)
		{
			controlMap['0'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString();
			controlMap['1'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString();
			controlMap['2'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString();
			controlMap['3'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString();
			controlMap['4'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString();
			controlMap['5'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString();
			controlMap['6'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString();
			controlMap['7'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString();
			controlMap['8'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString();
			controlMap['9'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString();
			controlMap['a'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString();
			controlMap['b'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString();
			controlMap['c'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString();
			controlMap['d'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString();
			controlMap['e'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString();
			controlMap['f'] = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString();
			controlMap['k'] = Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString();
			controlMap['l'] = Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString();
			controlMap['m'] = Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString();
			controlMap['n'] = Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString();
			controlMap['o'] = Ansi.ansi().a(Ansi.Attribute.ITALIC).toString();
			controlMap['r'] = Ansi.ansi().a(Ansi.Attribute.RESET).toString();
		}
	}
}

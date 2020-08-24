package org.ultramine.server.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.bootstrap.UMBootstrap;
import org.ultramine.server.util.GlobalExecutors;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.SERVER)
public class JLineSupport
{
	private static final Logger log = LogManager.getLogger();

	public static Thread setupReadingThread(final DedicatedServer server)
	{
		UMBootstrap.getJLineReader().addCompleter(new JLineCompleter());
		return new Thread("Server console handler")
		{
			public void run()
			{
				ConsoleReader reader = UMBootstrap.getJLineReader();
				String s4;

				try
				{
					while (!server.isServerStopped() && server.isServerRunning() && (s4 = reader.readLine()) != null)
					{
						server.addPendingCommand(s4, server);
					}
				}
				catch (IOException e)
				{
					log.error("Exception handling console input", e);
				}
			}
		};
	}

	@SideOnly(Side.SERVER)
	private static class JLineCompleter implements Completer
	{
		@Override
		public int complete(String buffer, int cursor, List<CharSequence> candidates)
		{
			MinecraftServer server = MinecraftServer.getServer();
			@SuppressWarnings("unchecked")
			List<String> offers =
					GlobalExecutors.nextTick().await(() -> (server.getCommandManager().getPossibleCommands(server, buffer)));
			if(offers == null || offers.isEmpty())
				return cursor;

			candidates.addAll(offers);

			final int lastSpace = buffer.lastIndexOf(' ');
			if(lastSpace == -1)
				return cursor - buffer.length();
			else
				return cursor - (buffer.length() - lastSpace - 1);

		}
	}
}

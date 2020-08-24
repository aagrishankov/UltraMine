package net.minecraft.network.rcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.internal.RConCommandRequest;
import org.ultramine.server.util.GlobalExecutors;

@SideOnly(Side.SERVER)
public class RConThreadClient extends RConThreadBase
{
	private static final Logger field_164005_h = LogManager.getLogger();
	private boolean loggedIn;
	private Socket clientSocket;
	private byte[] buffer = new byte[4096];
	private String rconPassword;
	private static final String __OBFID = "CL_00001804";

	RConThreadClient(IServer p_i1537_1_, Socket p_i1537_2_)
	{
		super(p_i1537_1_, "RCON Client");
		this.clientSocket = p_i1537_2_;
		
		List<String> whitelist = ConfigurationHandler.getServerConfig().listen.rcon.whitelist;
		if(whitelist != null && !whitelist.isEmpty() && !whitelist.contains(p_i1537_2_.getInetAddress().toString().replace("/", "")))
		{
			this.logWarning("Rcon connection from not whitelisted address: " + p_i1537_2_.getInetAddress());
			closeSocket();
			return;
		}

		try
		{
			this.clientSocket.setSoTimeout(0);
		}
		catch (Exception exception)
		{
			closeSocket();
		}

		this.rconPassword = ConfigurationHandler.getServerConfig().listen.rcon.password;
		this.logDebug("Rcon connection from: " + p_i1537_2_.getInetAddress());
	}
	
	public synchronized void startThread()
	{
		if(clientSocket == null)
			return;
		super.startThread();
	}

	public void run()
	{
		try
		{
			DataInputStream data = new DataInputStream(new BufferedInputStream(this.clientSocket.getInputStream()));
			while (true)
			{
				if (!this.running)
				{
					break;
				}

				int size = Integer.reverseBytes(data.readInt());
				data.readFully(this.buffer, 0, size);
				int i = size;

				if (10 > i)
				{
					return;
				}

				if (true)
				{
					int i1 = 0;
					int k = RConUtils.getBytesAsLEInt(this.buffer, i1, i);
					i1 += 4;
					int l = RConUtils.getRemainingBytesAsLEInt(this.buffer, i1);
					i1 += 4;

					switch (l)
					{
						case 2:
							if (this.loggedIn)
							{
								String s1 = RConUtils.getBytesAsString(this.buffer, i1, i);

								try
								{
									this.sendMultipacketResponse(k, GlobalExecutors.nextTick().await(new RConCommandRequest(s1)));
								}
								catch (Exception exception)
								{
									this.sendMultipacketResponse(k, "Error executing: " + s1 + " (" + exception.toString() + ")");
								}

								continue;
							}

							this.sendLoginFailedResponse();
							continue;
						case 3:
							String s = RConUtils.getBytesAsString(this.buffer, i1, i);
							int j1 = i1 + s.length();

							if (0 != s.length() && s.equals(this.rconPassword))
							{
								this.loggedIn = true;
								this.sendResponse(k, 2, "");
								continue;
							}

							this.loggedIn = false;
							this.sendLoginFailedResponse();
							continue;
						default:
							this.sendMultipacketResponse(k, String.format("Unknown request %s", new Object[] {Integer.toHexString(l)}));
							continue;
					}
				}
			}
		}
		catch (SocketTimeoutException sockettimeoutexception)
		{
			return;
		}
		catch (IOException ioexception)
		{
			return;
		}
		catch (Exception exception1)
		{
			field_164005_h.error("Exception whilst parsing RCON input", exception1);
			return;
		}
		finally
		{
			this.closeSocket();
		}
	}

	private void sendResponse(int p_72654_1_, int p_72654_2_, String p_72654_3_) throws IOException
	{
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(1248);
		DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
		byte[] abyte = p_72654_3_.getBytes("UTF-8");
		dataoutputstream.writeInt(Integer.reverseBytes(abyte.length + 10));
		dataoutputstream.writeInt(Integer.reverseBytes(p_72654_1_));
		dataoutputstream.writeInt(Integer.reverseBytes(p_72654_2_));
		dataoutputstream.write(abyte);
		dataoutputstream.write(0);
		dataoutputstream.write(0);
		this.clientSocket.getOutputStream().write(bytearrayoutputstream.toByteArray());
	}

	private void sendLoginFailedResponse() throws IOException
	{
		this.sendResponse(-1, 2, "");
	}

	private void sendMultipacketResponse(int p_72655_1_, String p_72655_2_) throws IOException
	{
		int j = p_72655_2_.length();

		do
		{
			int k = 4096 <= j ? 4096 : j;
			this.sendResponse(p_72655_1_, 0, p_72655_2_.substring(0, k));
			p_72655_2_ = p_72655_2_.substring(k);
			j = p_72655_2_.length();
		}
		while (0 != j);
	}

	private void closeSocket()
	{
		this.running = false;
		if (null != this.clientSocket)
		{
			try
			{
				this.clientSocket.close();
			}
			catch (IOException ioexception)
			{
				this.logWarning("IO: " + ioexception.getMessage());
			}

			this.clientSocket = null;
		}
	}
}

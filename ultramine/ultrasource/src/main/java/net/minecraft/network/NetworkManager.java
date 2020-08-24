package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;

import javax.crypto.SecretKey;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.CryptManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MessageDeserializer;
import net.minecraft.util.MessageDeserializer2;
import net.minecraft.util.MessageSerializer;
import net.minecraft.util.MessageSerializer2;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.ultramine.server.event.WorldUpdateObjectType;

public class NetworkManager extends SimpleChannelInboundHandler
{
	private static final Logger logger = LogManager.getLogger();
	public static final Marker logMarkerNetwork = MarkerManager.getMarker("NETWORK");
	public static final Marker logMarkerPackets = MarkerManager.getMarker("NETWORK_PACKETS", logMarkerNetwork);
	public static final Marker field_152461_c = MarkerManager.getMarker("NETWORK_STAT", logMarkerNetwork);
	public static final AttributeKey attrKeyConnectionState = new AttributeKey("protocol");
	public static final AttributeKey attrKeyReceivable = new AttributeKey("receivable_packets");
	public static final AttributeKey attrKeySendable = new AttributeKey("sendable_packets");
	public static final NioEventLoopGroup eventLoops = new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
	public static final NetworkStatistics field_152462_h = new NetworkStatistics();
	private final boolean isClientSide;
	private final Queue receivedPacketsQueue = Queues.newConcurrentLinkedQueue();
	private final Queue outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
	private Channel channel;
	private SocketAddress socketAddress;
	private INetHandler netHandler;
	private EnumConnectionState connectionState;
	private IChatComponent terminationReason;
	private boolean field_152463_r;
	private static final String __OBFID = "CL_00001240";

	public NetworkManager(boolean p_i45147_1_)
	{
		this.isClientSide = p_i45147_1_;
	}

	public void channelActive(ChannelHandlerContext p_channelActive_1_) throws Exception
	{
		super.channelActive(p_channelActive_1_);
		this.channel = p_channelActive_1_.channel();
		this.socketAddress = this.channel.remoteAddress();
		this.setConnectionState(EnumConnectionState.HANDSHAKING);
	}

	public void setConnectionState(EnumConnectionState p_150723_1_)
	{
		this.connectionState = (EnumConnectionState)this.channel.attr(attrKeyConnectionState).getAndSet(p_150723_1_);
		this.channel.attr(attrKeyReceivable).set(p_150723_1_.func_150757_a(this.isClientSide));
		this.channel.attr(attrKeySendable).set(p_150723_1_.func_150754_b(this.isClientSide));
		this.channel.config().setAutoRead(true);
		logger.debug("Enabled auto read");
	}

	public void channelInactive(ChannelHandlerContext p_channelInactive_1_)
	{
		this.closeChannel(new ChatComponentTranslation("disconnect.endOfStream", new Object[0]));
	}

	public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_)
	{
		ChatComponentTranslation chatcomponenttranslation;

		if (p_exceptionCaught_2_ instanceof TimeoutException)
		{
			chatcomponenttranslation = new ChatComponentTranslation("disconnect.timeout", new Object[0]);
		}
		else
		{
			chatcomponenttranslation = new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Internal Exception: " + p_exceptionCaught_2_});
		}

		this.closeChannel(chatcomponenttranslation);
	}

	protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet p_channelRead0_2_)
	{
		if (this.channel.isOpen())
		{
			if (p_channelRead0_2_.hasPriority())
			{
				p_channelRead0_2_.processPacket(this.netHandler);
			}
			else
			{
				this.receivedPacketsQueue.add(p_channelRead0_2_);
			}
		}
	}

	public void setNetHandler(INetHandler p_150719_1_)
	{
		Validate.notNull(p_150719_1_, "packetListener", new Object[0]);
		logger.debug("Set listener of {} to {}", new Object[] {this, p_150719_1_});
		this.netHandler = p_150719_1_;
	}

	public void scheduleOutboundPacket(Packet p_150725_1_, GenericFutureListener ... p_150725_2_)
	{
		if (this.channel != null && this.channel.isOpen())
		{
			this.flushOutboundQueue();
			this.dispatchPacket(p_150725_1_, p_150725_2_);
		}
		else
		{
			this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(p_150725_1_, p_150725_2_));
		}
	}

	private void dispatchPacket(final Packet p_150732_1_, final GenericFutureListener[] p_150732_2_)
	{
		final EnumConnectionState enumconnectionstate = EnumConnectionState.func_150752_a(p_150732_1_);
		final EnumConnectionState enumconnectionstate1 = (EnumConnectionState)this.channel.attr(attrKeyConnectionState).get();

		if (enumconnectionstate1 != enumconnectionstate && !( p_150732_1_ instanceof FMLProxyPacket))
		{
			logger.debug("Disabled auto read");
			this.channel.config().setAutoRead(false);
		}

		if (this.channel.eventLoop().inEventLoop())
		{
			if (enumconnectionstate != enumconnectionstate1 && !( p_150732_1_ instanceof FMLProxyPacket))
			{
				this.setConnectionState(enumconnectionstate);
			}

			this.channel.writeAndFlush(p_150732_1_).addListeners(p_150732_2_).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		}
		else
		{
			this.channel.eventLoop().execute(new Runnable()
			{
				private static final String __OBFID = "CL_00001241";
				public void run()
				{
					if (enumconnectionstate != enumconnectionstate1  && !( p_150732_1_ instanceof FMLProxyPacket))
					{
						NetworkManager.this.setConnectionState(enumconnectionstate);
					}

					NetworkManager.this.channel.writeAndFlush(p_150732_1_).addListeners(p_150732_2_).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
				}
			});
		}
	}

	private void flushOutboundQueue()
	{
		if (this.channel != null && this.channel.isOpen())
		{
			while (!this.outboundPacketsQueue.isEmpty())
			{
				NetworkManager.InboundHandlerTuplePacketListener inboundhandlertuplepacketlistener = (NetworkManager.InboundHandlerTuplePacketListener)this.outboundPacketsQueue.poll();
				this.dispatchPacket(inboundhandlertuplepacketlistener.field_150774_a, inboundhandlertuplepacketlistener.field_150773_b);
			}
		}
	}

	public void processReceivedPackets()
	{
		this.flushOutboundQueue();
		EnumConnectionState enumconnectionstate = (EnumConnectionState)this.channel.attr(attrKeyConnectionState).get();

		if (this.connectionState != enumconnectionstate)
		{
			if (this.connectionState != null)
			{
				this.netHandler.onConnectionStateTransition(this.connectionState, enumconnectionstate);
			}

			this.connectionState = enumconnectionstate;
		}

		if (this.netHandler != null)
		{
			EntityPlayerMP player = netHandler instanceof NetHandlerPlayServer ? ((NetHandlerPlayServer)netHandler).playerEntity : null;
			net.minecraft.profiler.Profiler profiler = null;
			if(player != null)
			{
				player.worldObj.getEventProxy().pushState(WorldUpdateObjectType.PLAYER);
				player.worldObj.getEventProxy().startEntity(player);
				profiler = player.worldObj.theProfiler;
			}

			for (int i = 1000; !this.receivedPacketsQueue.isEmpty() && i >= 0; --i)
			{
				Packet packet = (Packet)this.receivedPacketsQueue.poll();
				if(profiler != null)
					profiler.startSection(packet.getClass().getSimpleName());
				long startT = System.nanoTime();
				packet.processPacket(this.netHandler);
				long elapsed = System.nanoTime() - startT;
				if(elapsed > 20000000)
				{
					logger.warn("Possible lag source on processiong packet {} from {} {}ms", packet.getClass().getSimpleName(), player, (elapsed/1000000));
					if(packet instanceof C0EPacketClickWindow && player != null)
						logger.warn("    Container: " + player.openContainer.getClass().getName());
					else if(packet instanceof FMLProxyPacket)
						logger.warn("    Channel: " + ((FMLProxyPacket)packet).channel());
				}
				if(profiler != null)
					profiler.endSection();
			}

			if(player != null)
				player.worldObj.getEventProxy().popState();

			this.netHandler.onNetworkTick();
		}

		this.channel.flush();
	}

	public SocketAddress getSocketAddress()
	{
		return this.socketAddress;
	}

	public void closeChannel(IChatComponent p_150718_1_)
	{
		if (this.channel.isOpen())
		{
			this.channel.close();
			this.terminationReason = p_150718_1_;
		}
	}

	public boolean isLocalChannel()
	{
		return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
	}

	@SideOnly(Side.CLIENT)
	public static NetworkManager provideLanClient(InetAddress p_150726_0_, int p_150726_1_)
	{
		final NetworkManager networkmanager = new NetworkManager(true);
		((Bootstrap)((Bootstrap)((Bootstrap)(new Bootstrap()).group(eventLoops)).handler(new ChannelInitializer()
		{
			private static final String __OBFID = "CL_00001242";
			protected void initChannel(Channel p_initChannel_1_)
			{
				try
				{
					p_initChannel_1_.config().setOption(ChannelOption.IP_TOS, Integer.valueOf(24));
				}
				catch (ChannelException channelexception1)
				{
					;
				}

				try
				{
					p_initChannel_1_.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(false));
				}
				catch (ChannelException channelexception)
				{
					;
				}

				p_initChannel_1_.pipeline().addLast("timeout", new ReadTimeoutHandler(20)).addLast("splitter", new MessageDeserializer2()).addLast("decoder", new MessageDeserializer(NetworkManager.field_152462_h)).addLast("prepender", new MessageSerializer2()).addLast("encoder", new MessageSerializer(NetworkManager.field_152462_h)).addLast("packet_handler", networkmanager);
			}
		})).channel(NioSocketChannel.class)).connect(p_150726_0_, p_150726_1_).syncUninterruptibly();
		return networkmanager;
	}

	@SideOnly(Side.CLIENT)
	public static NetworkManager provideLocalClient(SocketAddress p_150722_0_)
	{
		final NetworkManager networkmanager = new NetworkManager(true);
		((Bootstrap)((Bootstrap)((Bootstrap)(new Bootstrap()).group(eventLoops)).handler(new ChannelInitializer()
		{
			private static final String __OBFID = "CL_00001243";
			protected void initChannel(Channel p_initChannel_1_)
			{
				p_initChannel_1_.pipeline().addLast("packet_handler", networkmanager);
			}
		})).channel(LocalChannel.class)).connect(p_150722_0_).syncUninterruptibly();
		return networkmanager;
	}

	public void enableEncryption(SecretKey p_150727_1_)
	{
		this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.func_151229_a(2, p_150727_1_)));
		this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.func_151229_a(1, p_150727_1_)));
		this.field_152463_r = true;
	}

	public boolean isChannelOpen()
	{
		return this.channel != null && this.channel.isOpen();
	}

	public INetHandler getNetHandler()
	{
		return this.netHandler;
	}

	public IChatComponent getExitMessage()
	{
		return this.terminationReason;
	}

	public void disableAutoRead()
	{
		this.channel.config().setAutoRead(false);
	}

	protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Object p_channelRead0_2_)
	{
		this.channelRead0(p_channelRead0_1_, (Packet)p_channelRead0_2_);
	}

	public Channel channel()
	{
		return channel;
	}

	static class InboundHandlerTuplePacketListener
		{
			private final Packet field_150774_a;
			private final GenericFutureListener[] field_150773_b;
			private static final String __OBFID = "CL_00001244";

			public InboundHandlerTuplePacketListener(Packet p_i45146_1_, GenericFutureListener ... p_i45146_2_)
			{
				this.field_150774_a = p_i45146_1_;
				this.field_150773_b = p_i45146_2_;
			}
		}
}
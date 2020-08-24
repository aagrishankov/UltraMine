package org.ultramine.server.internal;

import net.minecraft.util.DamageSource;
import org.ultramine.commands.basic.GenWorldCommand;
import org.ultramine.core.service.InjectService;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.Teleporter;
import org.ultramine.server.UltramineServerConfig.ToolsConf.AutoBroacastConf;
import org.ultramine.server.UltramineServerConfig.ToolsConf.AutoDebugInfoConf;
import org.ultramine.server.chunk.ChunkProfiler;
import org.ultramine.server.data.player.PlayerCoreData;
import org.ultramine.server.event.ForgeModIdMappingEvent;
import org.ultramine.server.event.PlayerDeathEvent;
import org.ultramine.server.util.BasicTypeFormatter;
import org.ultramine.server.util.BasicTypeParser;
import org.ultramine.server.util.WarpLocation;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.functions.GenericIterableFactory;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;

import static net.minecraft.util.EnumChatFormatting.*;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.ultramine.core.permissions.Permissions;

public class UMEventHandler
{
	@InjectService private static Permissions perms;

	@SideOnly(Side.SERVER)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void checkChatPermission(ServerChatEvent e)
	{
		if(e.player.playerNetServerHandler == null || e.player.getData() == null)
			return;
		PlayerCoreData data = e.player.getData().core();
		if(data.isMuted())
		{
			e.setCanceled(true);
			if(data.getUnmuteTime() != Long.MAX_VALUE)
			{
				e.player.addChatMessage(new ChatComponentTranslation("ultramine.ability.chat.muted",
						BasicTypeFormatter.formatTime(data.getUnmuteTime() - System.currentTimeMillis(), true))
						.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			}
			else
			{
				e.player.addChatMessage(new ChatComponentTranslation("ultramine.ability.chat.muted.forever").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			}
		}
		else if(e.player.isHidden())
		{
			e.setCanceled(true);
			e.player.addChatMessage(new ChatComponentTranslation("ultramine.hidden.chat").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
	}
	
	@SubscribeEvent
	public void onServerChat(ServerChatEvent e)
	{
		if(e.player.playerNetServerHandler == null || e.player.getData() == null)
			return;
		String prefix = perms.getMeta(e.player, "prefix").replace('&', '\u00A7');
		String postfix = perms.getMeta(e.player, "postfix").replace('&', '\u00A7');
		
		ChatComponentStyle username = (ChatComponentStyle) e.player.func_145748_c_();
		IChatComponent msg = ForgeHooks.newChatWithLinks(e.message);
		
		username.getChatStyle().setColor(BasicTypeParser.parseColor(perms.getMeta(e.player, "color")));
		EnumChatFormatting color = BasicTypeParser.parseColor(perms.getMeta(e.player, "textcolor"));
		msg.getChatStyle().setColor(color != null ? color : EnumChatFormatting.WHITE);
		
		e.component = new ChatComponentTranslation("%s%s%s\u00A77: %s", prefix, username, postfix, msg);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onServerCommand(CommandEvent e)
	{
		if(e.sender instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)e.sender;
			if(player.playerNetServerHandler != null && player.getData() != null && player.getData().core().isCommandsMuted())
			{
				e.setCanceled(true);
				if(player.getData().core().getUnmuteTime() != Long.MAX_VALUE)
				{
					player.addChatMessage(new ChatComponentTranslation("ultramine.ability.command.muted",
							BasicTypeFormatter.formatTime(player.getData().core().getUnmuteTime() - System.currentTimeMillis(), true))
							.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
				}
				else
				{
					player.addChatMessage(new ChatComponentTranslation("ultramine.ability.command.muted.forever").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onServerTickCommon(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			MinecraftServer server = MinecraftServer.getServer();
			
			Teleporter.tick();
			GenWorldCommand.tick();
			ChunkProfiler.instance().tick(server.getTickCounter());
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.SERVER)
	public void onServerTickServer(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			MinecraftServer server = MinecraftServer.getServer();
			server.getBackupManager().tick();
			
			AutoDebugInfoConf cfg = ConfigurationHandler.getServerConfig().tools.autoDebugInfo;
			if(cfg.enabled && server.getTickCounter() % (cfg.intervalSeconds*20) == 0)
			{
				double tps = Math.round(server.currentTPS*10)/10d;
				double downtime = server.currentWait/1000/1000d;
				double peakdowntime = server.peakWait /1000/1000d;
				int load = (int)Math.round((50-downtime)/50*100);
				int peakload = (int)Math.round((50-peakdowntime)/50*100);
				ChatComponentText loadcomp = new ChatComponentText(Integer.toString(load).concat("%"));
				ChatComponentText peakloadcomp = new ChatComponentText(Integer.toString(peakload).concat("%"));
				ChatComponentText tpscomp = new ChatComponentText(Double.toString(tps));
				loadcomp.getChatStyle().setColor(load > 100 ? RED : DARK_GREEN);
				peakloadcomp.getChatStyle().setColor(peakload >= 200 ? RED : DARK_GREEN);
				tpscomp.getChatStyle().setColor(tps < 15 ? RED : DARK_GREEN);

				ChatComponentTranslation full = new ChatComponentTranslation("ultramine.autobroadcast.debugmsg", loadcomp, peakloadcomp, tpscomp,
						server.getConfigurationManager().playerEntityList.size());
				full.getChatStyle().setColor(YELLOW);
				
				server.addChatMessage(full);
				for(EntityPlayerMP player : GenericIterableFactory.newCastingIterable(server.getConfigurationManager().playerEntityList, EntityPlayerMP.class))
					if(perms.has(player, "show.debuginfo"))
						player.addChatMessage(full);
			}
			
			AutoBroacastConf msgcfg = ConfigurationHandler.getServerConfig().tools.autobroadcast;
			if(msgcfg.enabled && server.getTickCounter() % (msgcfg.intervalSeconds*20) == 0)
			{
				if(msgcfg.messages.length != 0)
				{
					if(msgcfg.showAllMessages)
					{
						for(String msg : msgcfg.messages)
							broadcastMessage(msg);
					}
					else
					{
						broadcastMessage(msgcfg.messages[server.getTickCounter() % (msgcfg.intervalSeconds*20*msgcfg.messages.length) / (msgcfg.intervalSeconds*20)]);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.SERVER)
	public void onPlayerTickServer(TickEvent.PlayerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END && e.side.isServer())
		{
			EntityPlayerMP player = (EntityPlayerMP)e.player;
			int x = MathHelper.floor_double(player.posX);
			int z = MathHelper.floor_double(player.posZ);
			if(!player.getServerForPlayer().getBorder().isInsideBorder(x, z))
			{
				ChunkPosition pos = player.getServerForPlayer().getBorder().correctPosition(x, z);
				player.playerNetServerHandler.setPlayerLocation(pos.chunkPosX, player.lastTickPosY, pos.chunkPosZ, player.rotationYaw, player.rotationPitch);
			}
		}
	}
	
	private static void broadcastMessage(String msg)
	{
		ChatComponentText msgcomp = new ChatComponentText(msg);
		msgcomp.getChatStyle().setColor(DARK_GREEN);
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(msgcomp);
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone e)
	{
		if(e.entityPlayer.isEntityPlayerMP())
		{
			((EntityPlayerMP)e.entityPlayer).setData(((EntityPlayerMP)e.original).getData());
			((EntityPlayerMP)e.entityPlayer).setStatisticsFile(MinecraftServer.getServer().getConfigurationManager().func_152602_a(e.entityPlayer));
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent e)
	{
		if(e.entityLiving.isEntityPlayerMP())
		{
			EntityPlayerMP player = (EntityPlayerMP)e.entityLiving;
			Teleporter tp = player.getData().core().getTeleporter();
			if(tp != null)
				tp.cancel();
			player.getData().core().setLastLocation(WarpLocation.getFromPlayer(player));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent e)
	{
		MinecraftServer.getServer().getConfigurationManager().getDataLoader().handlePlayerDimensionChange((EntityPlayerMP)e.player, e.fromDim, e.toDim);
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onPlayerLoggedIn(PlayerLoggedInEvent e)
	{
		((EntityPlayerMP)e.player).getData().core().onLogin();
	}

	@SubscribeEvent
	public void onForgeModIdMapping(ForgeModIdMappingEvent e)
	{
		UMInternalRegistry.onRemap();
	}

	@SubscribeEvent
	public void onPlayerKeepInvCheckEvent(PlayerDeathEvent e)
	{
		if(e.damageSource == DamageSource.command)
			e.setDeathMessage(null);
		if(perms.has(e.entityPlayer, "ability.admin.keepinventory"))
			e.setKeepInventory(true);
	}
}

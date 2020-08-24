package org.ultramine.server.tools;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.tools.ItemBlocker.ItemBlockerSettings.BlockingSettings;
import org.ultramine.server.tools.ItemBlocker.ItemBlockerSettings.BlockingWorldList;
import org.ultramine.server.util.BasicTypeParser;
import org.ultramine.server.util.ItemStackHashMap;
import org.ultramine.server.util.YamlConfigProvider;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.functions.GenericIterableFactory;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

@SideOnly(Side.SERVER)
public class ItemBlocker
{
	private final File storage;
	private final TIntObjectMap<PerWorldBlocker> map = new TIntObjectHashMap<PerWorldBlocker>();
	private PerWorldBlocker global;
	
	public ItemBlocker()
	{
		this.storage = new File(ConfigurationHandler.getSettingDir(), "itemblocker.yml");
	}
	
	public void load()
	{
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		reload();
	}
	
	public void reload()
	{
		global = null;
		map.clear();
		ItemBlockerSettings set = YamlConfigProvider.getOrCreateConfig(storage, ItemBlockerSettings.class);
		if(set.global != null)
			global = new PerWorldBlocker(set.global);
		for(Map.Entry<Integer, BlockingWorldList> ent : set.worlds.entrySet())
		{
			map.put(ent.getKey(), new PerWorldBlocker(ent.getValue().list));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEvent(PlayerInteractEvent e)
	{
		ItemStack is = e.entityPlayer.inventory.getCurrentItem();
		BlockingSettings set = getBlockingSettings(e.entityPlayer.dimension, is);
		if(set != null && set.useItem)
		{
			e.setCanceled(true);
			if(set.rmItem)
				e.entityPlayer.inventory.setInventorySlotContents(e.entityPlayer.inventory.currentItem, null);
			if(e.entityPlayer.isEntityPlayerMP() && ((EntityPlayerMP)e.entityPlayer).playerNetServerHandler != null)
				e.entityPlayer.addChatMessage(new ChatComponentTranslation("ultramine.tools.itemblocker.useitem").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}
		
		if(e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
			return;
		
		set = getBlockingSettings(e.entityPlayer.dimension, Block.getIdFromBlock(e.world.getBlock(e.x, e.y, e.z)), e.world.getBlockMetadata(e.x, e.y, e.z));
		if(set != null && set.useBlock)
		{
			e.setCanceled(true);
			if(set.rmBlock)
				e.world.setBlock(e.x, e.y, e.z, Blocks.air, 0, 3);
			if(e.entityPlayer.isEntityPlayerMP() && ((EntityPlayerMP)e.entityPlayer).playerNetServerHandler != null)
				e.entityPlayer.addChatMessage(new ChatComponentTranslation("ultramine.tools.itemblocker.useblock").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityItemPickup(EntityItemPickupEvent e)
	{
		BlockingSettings set = getBlockingSettings(e.entityPlayer.dimension, e.item.getEntityItem());
		if(set != null && set.rmItem)
		{
			e.setCanceled(true);
			e.item.setDead();
			if(e.entityPlayer.isEntityPlayerMP() && ((EntityPlayerMP)e.entityPlayer).playerNetServerHandler != null)
				e.entityPlayer.addChatMessage(new ChatComponentTranslation("ultramine.tools.itemblocker.rmblock").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END)
		{
			 for(EntityPlayerMP player : GenericIterableFactory.newCastingIterable(MinecraftServer.getServer().getConfigurationManager().playerEntityList, EntityPlayerMP.class))
			 {
				 if(player.openContainer != player.inventoryContainer)
				 {
					 for(Slot slot :  GenericIterableFactory.newCastingIterable(player.openContainer.inventorySlots, Slot.class))
					 {
						if(slot.inventory == null || slot.getSlotIndex() >= slot.inventory.getSizeInventory())
							continue; //Fix for some broken containers
						ItemStack is = slot.getStack();
						if(is == null)
							continue;
						if(is.getItem() == null)
						{//Fix for broken items
							slot.putStack(null);
							continue;
						}
						BlockingSettings set = getBlockingSettings(player.dimension, is);
						if(set != null && set.rmItem)
						{
							slot.putStack(null);
							player.addChatMessage(new ChatComponentTranslation("ultramine.tools.itemblocker.rmblock").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
						}
					 }
				 }
			 }
		}
	}
	
	private BlockingSettings getBlockingSettings(int dim, int id, int data)
	{
		PerWorldBlocker ws = map.get(dim);
		BlockingSettings ret = ws == null ? null : ws.getBlockingSettings(id, data);
		if(ret == null && global != null)
			ret = global.getBlockingSettings(id, data);
		return ret;
	}
	
	private BlockingSettings getBlockingSettings(int dim, ItemStack stack)
	{
		return stack == null ? null : getBlockingSettings(dim, Item.getIdFromItem(stack.getItem()), stack.getItemDamage());
	}
	
	private class PerWorldBlocker
	{
		private final ItemStackHashMap<BlockingSettings> itemMap = new ItemStackHashMap<>();
		
		public PerWorldBlocker(List<BlockingSettings> list)
		{
			for(BlockingSettings ent : list)
			{
				addBlocking(BasicTypeParser.parseStackType(ent.item), ent);
			}
		}
		
		private void addBlocking(ItemStack type, BlockingSettings set)
		{
			itemMap.put(type, set);
		}
		
		public BlockingSettings getBlockingSettings(int id, int data)
		{
			return itemMap.get(id, data);
		}
	}
	
	public static class ItemBlockerSettings
	{
		public List<BlockingSettings> global;
		public Map<Integer, BlockingWorldList> worlds = new HashMap<Integer, BlockingWorldList>();
		
		public static class BlockingSettings
		{
			public String item;
			public boolean useItem;
			public boolean rmItem;
			public boolean useBlock;
			public boolean rmBlock;
		}
		
		public static class BlockingWorldList
		{
			public List<BlockingSettings> list;
		}
	}
}

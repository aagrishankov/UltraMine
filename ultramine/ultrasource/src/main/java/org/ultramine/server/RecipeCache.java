package org.ultramine.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.ultramine.server.util.CachedEntry;
import org.ultramine.server.util.CollectionUtil;
import org.ultramine.server.util.ModificationControlList;

import javax.annotation.Nullable;

public class RecipeCache
{
	private static final int CACHE_SIZE = 12287;
	private final ModificationControlList<IRecipe> originList;
	private final Map<RecipeKey, CachedEntry<IRecipe>> cache = new HashMap<>();
	private boolean enabled;

	@SuppressWarnings("unchecked")
	public RecipeCache()
	{
		CraftingManager craftMgr = CraftingManager.getInstance();
		craftMgr.recipes = originList = new ModificationControlList<>(craftMgr.getRecipeList());
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public @Nullable IRecipe findRecipe(InventoryCrafting inv, World world)
	{
		if(!enabled)
			return originalSearch(inv, world);
		RecipeKey key = new RecipeKeyBuilder(inv).build();
		if(key.width == 0)
			return null;

		CachedEntry<IRecipe> rcp = cache.get(key);
		if (rcp != null)
		{
			IRecipe recipe = rcp.getValueAndUpdateTime();
			if(recipe == null)
				return null;
			if(recipe.matches(inv, world))
				return recipe;
		}

		IRecipe recipe = originalSearch(inv, world);
		addToCache(key, recipe);
		return recipe;
	}

	private void addToCache(RecipeKey key, @Nullable IRecipe recipe)
	{
		if(cache.size() >= CACHE_SIZE)
			CollectionUtil.retainNewestEntries(cache.values(), CACHE_SIZE / 2);
		cache.put(key, CachedEntry.of(recipe));
	}

	private @Nullable IRecipe originalSearch(InventoryCrafting inv, World world)
	{
		for(IRecipe recipe : originList)
		{
			if (recipe.matches(inv, world))
			{
				return recipe;
			}
		}

		return null;
	}

	public void clearCache()
	{
		cache.clear();
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase != TickEvent.Phase.END)
			return;
		if(originList.checkModifiedAndReset())
		{
			clearCache();
		}
	}
	
	private static class RecipeKey implements Comparable<RecipeKey>
	{
		private final int[] contents;
		private final int width;
		
		public RecipeKey(int[] contents, int width)
		{
			this.contents = contents;
			this.width = width;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o == null || o.getClass() != RecipeKey.class)
				return false;
			RecipeKey rk = (RecipeKey)o;
			return width == rk.width && Arrays.equals(contents, rk.contents);
		}

		@Override
		public int hashCode()
		{
			return Arrays.hashCode(contents) ^ width;
		}

		@Override
		public int compareTo(RecipeKey rk)
		{
			int c1 = width - rk.width;
			if(c1 != 0)
				return c1;
			int c2 = contents.length - rk.contents.length;
			if(c2 != 0)
				return c2;
			for(int i = 0; i < contents.length; i++)
			{
				int c3 = Integer.compare(contents[i], rk.contents[i]);
				if(c3 != 0)
					return c3;
			}
			
			return 0;
		}
	}
	
	private static class RecipeKeyBuilder
	{
		private static final int[] EMPTY_INT_ARRAY = new int[0];
		private int[] contents;
		private int x;
		private int y;
		private int width;
		private int height;
		private int newWidth;
		private int newHeight;
		
		public RecipeKeyBuilder(InventoryCrafting inv)
		{
			contents = new int[inv.getSizeInventory()];
			for(int i = 0; i < contents.length; i++)
			{
				ItemStack is = inv.getStackInSlot(i);
				if(is != null)
					contents[i] = (is.getItemDamage() << 16) | Item.getIdFromItem(is.getItem());
			}
			newWidth = width = inv.getWidth();
			newHeight = height = contents.length/width;
			
			while(trimHorisontal(false));
			if(y == height)
			{
				contents = EMPTY_INT_ARRAY;
				newWidth = 0;
			}
			else
			{
				while(trimHorisontal(true));
				while(trimVertical(false));
				while(trimVertical(true));
				if(width != newWidth || height != newHeight)
				{
					int[] newContents = new int[newWidth*newHeight];
					for(int i = 0; i < newWidth; i++)
						for(int j = 0; j < newHeight; j++)
							newContents[i + j*newWidth] = contents[(x+i) + (y+j)*width];
					contents = newContents;
				}
			}
		}
		
		private boolean trimHorisontal(boolean bottom)
		{
			boolean empty = true;
			for(int i = 0; i < width; i++)
			{
				if(contents[bottom ? (y+newHeight-1)*width + i : y*width + i] != 0)
				{
					empty = false;
					break;
				}
			}
			if(empty)
			{
				newHeight--;
				if(!bottom)
					y++;
				return newHeight != 0;
			}
			
			return false;
		}
		
		private boolean trimVertical(boolean right)
		{
			boolean empty = true;
			for(int i = 0; i < newHeight; i++)
			{
				if(contents[y*width + i*(width) + x + (right ? newWidth-1 : 0)] != 0)
				{
					empty = false;
					break;
				}
			}
			if(empty)
			{
				newWidth--;
				if(!right)
					x++;
				return true;
			}
			
			return false;
		}
		
		public RecipeKey build()
		{
			return new RecipeKey(contents, newWidth);
		}
	}
}

package net.minecraft.item.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ShapelessRecipes implements IRecipe
{
	private final ItemStack recipeOutput;
	public final List recipeItems;
	private static final String __OBFID = "CL_00000094";

	public ShapelessRecipes(ItemStack p_i1918_1_, List p_i1918_2_)
	{
		this.recipeOutput = p_i1918_1_;
		this.recipeItems = p_i1918_2_;
	}

	public ItemStack getRecipeOutput()
	{
		return this.recipeOutput;
	}

	public boolean matches(InventoryCrafting p_77569_1_, World p_77569_2_)
	{
		ArrayList arraylist = new ArrayList(this.recipeItems);

		for (int i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 3; ++j)
			{
				ItemStack itemstack = p_77569_1_.getStackInRowAndColumn(j, i);

				if (itemstack != null)
				{
					boolean flag = false;
					Iterator iterator = arraylist.iterator();

					while (iterator.hasNext())
					{
						ItemStack itemstack1 = (ItemStack)iterator.next();

						if (itemstack.getItem() == itemstack1.getItem() && (itemstack1.getItemDamage() == 32767 || itemstack.getItemDamage() == itemstack1.getItemDamage()))
						{
							flag = true;
							arraylist.remove(itemstack1);
							break;
						}
					}

					if (!flag)
					{
						return false;
					}
				}
			}
		}

		return arraylist.isEmpty();
	}

	public ItemStack getCraftingResult(InventoryCrafting p_77572_1_)
	{
		return this.recipeOutput.copy();
	}

	public int getRecipeSize()
	{
		return this.recipeItems.size();
	}
}
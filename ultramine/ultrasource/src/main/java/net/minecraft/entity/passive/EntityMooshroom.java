package net.minecraft.entity.passive;

import java.util.ArrayList;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public class EntityMooshroom extends EntityCow implements IShearable
{
	private static final String __OBFID = "CL_00001645";

	public EntityMooshroom(World p_i1687_1_)
	{
		super(p_i1687_1_);
		this.setSize(0.9F, 1.3F);
	}

	public boolean interact(EntityPlayer p_70085_1_)
	{
		ItemStack itemstack = p_70085_1_.inventory.getCurrentItem();

		if (itemstack != null && itemstack.getItem() == Items.bowl && this.getGrowingAge() >= 0)
		{
			if (itemstack.stackSize == 1)
			{
				p_70085_1_.inventory.setInventorySlotContents(p_70085_1_.inventory.currentItem, new ItemStack(Items.mushroom_stew));
				return true;
			}

			if (p_70085_1_.inventory.addItemStackToInventory(new ItemStack(Items.mushroom_stew)) && !p_70085_1_.capabilities.isCreativeMode)
			{
				p_70085_1_.inventory.decrStackSize(p_70085_1_.inventory.currentItem, 1);
				return true;
			}
		}

		{
			return super.interact(p_70085_1_);
		}
	}

	public EntityMooshroom createChild(EntityAgeable p_90011_1_)
	{
		return new EntityMooshroom(this.worldObj);
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z)
	{
		return getGrowingAge() >= 0;
	}

	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune)
	{
		setDead();
		EntityCow entitycow = new EntityCow(worldObj);
		entitycow.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
		entitycow.setHealth(this.getHealth());
		entitycow.renderYawOffset = renderYawOffset;
		worldObj.spawnEntityInWorld(entitycow);
		worldObj.spawnParticle("largeexplode", posX, posY + (double)(height / 2.0F), posZ, 0.0D, 0.0D, 0.0D);

		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		for (int i = 0; i < 5; i++)
		{
			ret.add(new ItemStack(Blocks.red_mushroom));
		}
		playSound("mob.sheep.shear", 1.0F, 1.0F);
		return ret;
	}
}
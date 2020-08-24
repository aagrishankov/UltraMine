package net.minecraft.entity.passive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public class EntitySheep extends EntityAnimal implements IShearable
{
	private final InventoryCrafting field_90016_e = new InventoryCrafting(new Container()
	{
		private static final String __OBFID = "CL_00001649";
		public boolean canInteractWith(EntityPlayer p_75145_1_)
		{
			return false;
		}
	}, 2, 1);
	public static final float[][] fleeceColorTable = new float[][] {{1.0F, 1.0F, 1.0F}, {0.85F, 0.5F, 0.2F}, {0.7F, 0.3F, 0.85F}, {0.4F, 0.6F, 0.85F}, {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.5F, 0.65F}, {0.3F, 0.3F, 0.3F}, {0.6F, 0.6F, 0.6F}, {0.3F, 0.5F, 0.6F}, {0.5F, 0.25F, 0.7F}, {0.2F, 0.3F, 0.7F}, {0.4F, 0.3F, 0.2F}, {0.4F, 0.5F, 0.2F}, {0.6F, 0.2F, 0.2F}, {0.1F, 0.1F, 0.1F}};
	private int sheepTimer;
	private EntityAIEatGrass field_146087_bs = new EntityAIEatGrass(this);
	private static final String __OBFID = "CL_00001648";

	public EntitySheep(World p_i1691_1_)
	{
		super(p_i1691_1_);
		this.setSize(0.9F, 1.3F);
		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIPanic(this, 1.25D));
		this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
		this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.wheat, false));
		this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
		this.tasks.addTask(5, this.field_146087_bs);
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.field_90016_e.setInventorySlotContents(0, new ItemStack(Items.dye, 1, 0));
		this.field_90016_e.setInventorySlotContents(1, new ItemStack(Items.dye, 1, 0));
	}

	protected boolean isAIEnabled()
	{
		return true;
	}

	protected void updateAITasks()
	{
		this.sheepTimer = this.field_146087_bs.func_151499_f();
		super.updateAITasks();
	}

	public void onLivingUpdate()
	{
		if (this.worldObj.isRemote)
		{
			this.sheepTimer = Math.max(0, this.sheepTimer - 1);
		}

		super.onLivingUpdate();
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
	}

	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, new Byte((byte)0));
	}

	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
	{
		if (!this.getSheared())
		{
			this.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, this.getFleeceColor()), 0.0F);
		}
	}

	protected Item getDropItem()
	{
		return Item.getItemFromBlock(Blocks.wool);
	}

	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte p_70103_1_)
	{
		if (p_70103_1_ == 10)
		{
			this.sheepTimer = 40;
		}
		else
		{
			super.handleHealthUpdate(p_70103_1_);
		}
	}

	public boolean interact(EntityPlayer p_70085_1_)
	{
		return super.interact(p_70085_1_);
	}

	@SideOnly(Side.CLIENT)
	public float func_70894_j(float p_70894_1_)
	{
		return this.sheepTimer <= 0 ? 0.0F : (this.sheepTimer >= 4 && this.sheepTimer <= 36 ? 1.0F : (this.sheepTimer < 4 ? ((float)this.sheepTimer - p_70894_1_) / 4.0F : -((float)(this.sheepTimer - 40) - p_70894_1_) / 4.0F));
	}

	@SideOnly(Side.CLIENT)
	public float func_70890_k(float p_70890_1_)
	{
		if (this.sheepTimer > 4 && this.sheepTimer <= 36)
		{
			float f1 = ((float)(this.sheepTimer - 4) - p_70890_1_) / 32.0F;
			return ((float)Math.PI / 5F) + ((float)Math.PI * 7F / 100F) * MathHelper.sin(f1 * 28.7F);
		}
		else
		{
			return this.sheepTimer > 0 ? ((float)Math.PI / 5F) : this.rotationPitch / (180F / (float)Math.PI);
		}
	}

	public void writeEntityToNBT(NBTTagCompound p_70014_1_)
	{
		super.writeEntityToNBT(p_70014_1_);
		p_70014_1_.setBoolean("Sheared", this.getSheared());
		p_70014_1_.setByte("Color", (byte)this.getFleeceColor());
	}

	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
		super.readEntityFromNBT(p_70037_1_);
		this.setSheared(p_70037_1_.getBoolean("Sheared"));
		this.setFleeceColor(p_70037_1_.getByte("Color"));
	}

	protected String getLivingSound()
	{
		return "mob.sheep.say";
	}

	protected String getHurtSound()
	{
		return "mob.sheep.say";
	}

	protected String getDeathSound()
	{
		return "mob.sheep.say";
	}

	protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
	{
		this.playSound("mob.sheep.step", 0.15F, 1.0F);
	}

	public int getFleeceColor()
	{
		return this.dataWatcher.getWatchableObjectByte(16) & 15;
	}

	public void setFleeceColor(int p_70891_1_)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte(16);
		this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & 240 | p_70891_1_ & 15)));
	}

	public boolean getSheared()
	{
		return (this.dataWatcher.getWatchableObjectByte(16) & 16) != 0;
	}

	public void setSheared(boolean p_70893_1_)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte(16);

		if (p_70893_1_)
		{
			this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 16)));
		}
		else
		{
			this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & -17)));
		}
	}

	public static int getRandomFleeceColor(Random p_70895_0_)
	{
		int i = p_70895_0_.nextInt(100);
		return i < 5 ? 15 : (i < 10 ? 7 : (i < 15 ? 8 : (i < 18 ? 12 : (p_70895_0_.nextInt(500) == 0 ? 6 : 0))));
	}

	public EntitySheep createChild(EntityAgeable p_90011_1_)
	{
		EntitySheep entitysheep = (EntitySheep)p_90011_1_;
		EntitySheep entitysheep1 = new EntitySheep(this.worldObj);
		int i = this.func_90014_a(this, entitysheep);
		entitysheep1.setFleeceColor(15 - i);
		return entitysheep1;
	}

	public void eatGrassBonus()
	{
		this.setSheared(false);

		if (this.isChild())
		{
			this.addGrowth(60);
		}
	}

	public IEntityLivingData onSpawnWithEgg(IEntityLivingData p_110161_1_)
	{
		p_110161_1_ = super.onSpawnWithEgg(p_110161_1_);
		this.setFleeceColor(getRandomFleeceColor(this.worldObj.rand));
		return p_110161_1_;
	}

	private int func_90014_a(EntityAnimal p_90014_1_, EntityAnimal p_90014_2_)
	{
		int i = this.func_90013_b(p_90014_1_);
		int j = this.func_90013_b(p_90014_2_);
		this.field_90016_e.getStackInSlot(0).setItemDamage(i);
		this.field_90016_e.getStackInSlot(1).setItemDamage(j);
		ItemStack itemstack = CraftingManager.getInstance().findMatchingRecipe(this.field_90016_e, ((EntitySheep)p_90014_1_).worldObj);
		int k;

		if (itemstack != null && itemstack.getItem() == Items.dye)
		{
			k = itemstack.getItemDamage();
		}
		else
		{
			k = this.worldObj.rand.nextBoolean() ? i : j;
		}

		return k;
	}

	private int func_90013_b(EntityAnimal p_90013_1_)
	{
		return 15 - ((EntitySheep)p_90013_1_).getFleeceColor();
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z)
	{
		return !getSheared() && !isChild();
	}

	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		setSheared(true);
		int i = 1 + rand.nextInt(3);
		for (int j = 0; j < i; j++)
		{
			ret.add(new ItemStack(Blocks.wool, 1, getFleeceColor()));
		}
		this.playSound("mob.sheep.shear", 1.0F, 1.0F);
		return ret;
	}
}
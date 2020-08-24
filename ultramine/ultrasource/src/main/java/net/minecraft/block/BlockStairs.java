package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStairs extends Block
{
	private static final int[][] field_150150_a = new int[][] {{2, 6}, {3, 7}, {2, 3}, {6, 7}, {0, 4}, {1, 5}, {0, 1}, {4, 5}};
	private final Block field_150149_b;
	private final int field_150151_M;
	private boolean field_150152_N;
	private int field_150153_O;
	private static final String __OBFID = "CL_00000314";

	protected BlockStairs(Block p_i45428_1_, int p_i45428_2_)
	{
		super(p_i45428_1_.blockMaterial);
		this.field_150149_b = p_i45428_1_;
		this.field_150151_M = p_i45428_2_;
		this.setHardness(p_i45428_1_.blockHardness);
		this.setResistance(p_i45428_1_.blockResistance / 3.0F);
		this.setStepSound(p_i45428_1_.stepSound);
		this.setLightOpacity(255);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	public void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_)
	{
		if (this.field_150152_N)
		{
			this.setBlockBounds(0.5F * (float)(this.field_150153_O % 2), 0.5F * (float)(this.field_150153_O / 2 % 2), 0.5F * (float)(this.field_150153_O / 4 % 2), 0.5F + 0.5F * (float)(this.field_150153_O % 2), 0.5F + 0.5F * (float)(this.field_150153_O / 2 % 2), 0.5F + 0.5F * (float)(this.field_150153_O / 4 % 2));
		}
		else
		{
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	public boolean isOpaqueCube()
	{
		return false;
	}

	public boolean renderAsNormalBlock()
	{
		return false;
	}

	public int getRenderType()
	{
		return 10;
	}

	public void func_150147_e(IBlockAccess p_150147_1_, int p_150147_2_, int p_150147_3_, int p_150147_4_)
	{
		int l = p_150147_1_.getBlockMetadata(p_150147_2_, p_150147_3_, p_150147_4_);

		if ((l & 4) != 0)
		{
			this.setBlockBounds(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
		else
		{
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		}
	}

	public static boolean func_150148_a(Block p_150148_0_)
	{
		return p_150148_0_ instanceof BlockStairs;
	}

	private boolean func_150146_f(IBlockAccess p_150146_1_, int p_150146_2_, int p_150146_3_, int p_150146_4_, int p_150146_5_)
	{
		Block block = p_150146_1_.getBlock(p_150146_2_, p_150146_3_, p_150146_4_);
		return func_150148_a(block) && p_150146_1_.getBlockMetadata(p_150146_2_, p_150146_3_, p_150146_4_) == p_150146_5_;
	}

	public boolean func_150145_f(IBlockAccess p_150145_1_, int p_150145_2_, int p_150145_3_, int p_150145_4_)
	{
		int l = p_150145_1_.getBlockMetadata(p_150145_2_, p_150145_3_, p_150145_4_);
		int i1 = l & 3;
		float f = 0.5F;
		float f1 = 1.0F;

		if ((l & 4) != 0)
		{
			f = 0.0F;
			f1 = 0.5F;
		}

		float f2 = 0.0F;
		float f3 = 1.0F;
		float f4 = 0.0F;
		float f5 = 0.5F;
		boolean flag = true;
		Block block;
		int j1;
		int k1;

		if (i1 == 0)
		{
			f2 = 0.5F;
			f5 = 1.0F;
			block = p_150145_1_.getBlock(p_150145_2_ + 1, p_150145_3_, p_150145_4_);
			j1 = p_150145_1_.getBlockMetadata(p_150145_2_ + 1, p_150145_3_, p_150145_4_);

			if (func_150148_a(block) && (l & 4) == (j1 & 4))
			{
				k1 = j1 & 3;

				if (k1 == 3 && !this.func_150146_f(p_150145_1_, p_150145_2_, p_150145_3_, p_150145_4_ + 1, l))
				{
					f5 = 0.5F;
					flag = false;
				}
				else if (k1 == 2 && !this.func_150146_f(p_150145_1_, p_150145_2_, p_150145_3_, p_150145_4_ - 1, l))
				{
					f4 = 0.5F;
					flag = false;
				}
			}
		}
		else if (i1 == 1)
		{
			f3 = 0.5F;
			f5 = 1.0F;
			block = p_150145_1_.getBlock(p_150145_2_ - 1, p_150145_3_, p_150145_4_);
			j1 = p_150145_1_.getBlockMetadata(p_150145_2_ - 1, p_150145_3_, p_150145_4_);

			if (func_150148_a(block) && (l & 4) == (j1 & 4))
			{
				k1 = j1 & 3;

				if (k1 == 3 && !this.func_150146_f(p_150145_1_, p_150145_2_, p_150145_3_, p_150145_4_ + 1, l))
				{
					f5 = 0.5F;
					flag = false;
				}
				else if (k1 == 2 && !this.func_150146_f(p_150145_1_, p_150145_2_, p_150145_3_, p_150145_4_ - 1, l))
				{
					f4 = 0.5F;
					flag = false;
				}
			}
		}
		else if (i1 == 2)
		{
			f4 = 0.5F;
			f5 = 1.0F;
			block = p_150145_1_.getBlock(p_150145_2_, p_150145_3_, p_150145_4_ + 1);
			j1 = p_150145_1_.getBlockMetadata(p_150145_2_, p_150145_3_, p_150145_4_ + 1);

			if (func_150148_a(block) && (l & 4) == (j1 & 4))
			{
				k1 = j1 & 3;

				if (k1 == 1 && !this.func_150146_f(p_150145_1_, p_150145_2_ + 1, p_150145_3_, p_150145_4_, l))
				{
					f3 = 0.5F;
					flag = false;
				}
				else if (k1 == 0 && !this.func_150146_f(p_150145_1_, p_150145_2_ - 1, p_150145_3_, p_150145_4_, l))
				{
					f2 = 0.5F;
					flag = false;
				}
			}
		}
		else if (i1 == 3)
		{
			block = p_150145_1_.getBlock(p_150145_2_, p_150145_3_, p_150145_4_ - 1);
			j1 = p_150145_1_.getBlockMetadata(p_150145_2_, p_150145_3_, p_150145_4_ - 1);

			if (func_150148_a(block) && (l & 4) == (j1 & 4))
			{
				k1 = j1 & 3;

				if (k1 == 1 && !this.func_150146_f(p_150145_1_, p_150145_2_ + 1, p_150145_3_, p_150145_4_, l))
				{
					f3 = 0.5F;
					flag = false;
				}
				else if (k1 == 0 && !this.func_150146_f(p_150145_1_, p_150145_2_ - 1, p_150145_3_, p_150145_4_, l))
				{
					f2 = 0.5F;
					flag = false;
				}
			}
		}

		this.setBlockBounds(f2, f, f4, f3, f1, f5);
		return flag;
	}

	public boolean func_150144_g(IBlockAccess p_150144_1_, int p_150144_2_, int p_150144_3_, int p_150144_4_)
	{
		int l = p_150144_1_.getBlockMetadata(p_150144_2_, p_150144_3_, p_150144_4_);
		int i1 = l & 3;
		float f = 0.5F;
		float f1 = 1.0F;

		if ((l & 4) != 0)
		{
			f = 0.0F;
			f1 = 0.5F;
		}

		float f2 = 0.0F;
		float f3 = 0.5F;
		float f4 = 0.5F;
		float f5 = 1.0F;
		boolean flag = false;
		Block block;
		int j1;
		int k1;

		if (i1 == 0)
		{
			block = p_150144_1_.getBlock(p_150144_2_ - 1, p_150144_3_, p_150144_4_);
			j1 = p_150144_1_.getBlockMetadata(p_150144_2_ - 1, p_150144_3_, p_150144_4_);

			if (func_150148_a(block) && (l & 4) == (j1 & 4))
			{
				k1 = j1 & 3;

				if (k1 == 3 && !this.func_150146_f(p_150144_1_, p_150144_2_, p_150144_3_, p_150144_4_ - 1, l))
				{
					f4 = 0.0F;
					f5 = 0.5F;
					flag = true;
				}
				else if (k1 == 2 && !this.func_150146_f(p_150144_1_, p_150144_2_, p_150144_3_, p_150144_4_ + 1, l))
				{
					f4 = 0.5F;
					f5 = 1.0F;
					flag = true;
				}
			}
		}
		else if (i1 == 1)
		{
			block = p_150144_1_.getBlock(p_150144_2_ + 1, p_150144_3_, p_150144_4_);
			j1 = p_150144_1_.getBlockMetadata(p_150144_2_ + 1, p_150144_3_, p_150144_4_);

			if (func_150148_a(block) && (l & 4) == (j1 & 4))
			{
				f2 = 0.5F;
				f3 = 1.0F;
				k1 = j1 & 3;

				if (k1 == 3 && !this.func_150146_f(p_150144_1_, p_150144_2_, p_150144_3_, p_150144_4_ - 1, l))
				{
					f4 = 0.0F;
					f5 = 0.5F;
					flag = true;
				}
				else if (k1 == 2 && !this.func_150146_f(p_150144_1_, p_150144_2_, p_150144_3_, p_150144_4_ + 1, l))
				{
					f4 = 0.5F;
					f5 = 1.0F;
					flag = true;
				}
			}
		}
		else if (i1 == 2)
		{
			block = p_150144_1_.getBlock(p_150144_2_, p_150144_3_, p_150144_4_ - 1);
			j1 = p_150144_1_.getBlockMetadata(p_150144_2_, p_150144_3_, p_150144_4_ - 1);

			if (func_150148_a(block) && (l & 4) == (j1 & 4))
			{
				f4 = 0.0F;
				f5 = 0.5F;
				k1 = j1 & 3;

				if (k1 == 1 && !this.func_150146_f(p_150144_1_, p_150144_2_ - 1, p_150144_3_, p_150144_4_, l))
				{
					flag = true;
				}
				else if (k1 == 0 && !this.func_150146_f(p_150144_1_, p_150144_2_ + 1, p_150144_3_, p_150144_4_, l))
				{
					f2 = 0.5F;
					f3 = 1.0F;
					flag = true;
				}
			}
		}
		else if (i1 == 3)
		{
			block = p_150144_1_.getBlock(p_150144_2_, p_150144_3_, p_150144_4_ + 1);
			j1 = p_150144_1_.getBlockMetadata(p_150144_2_, p_150144_3_, p_150144_4_ + 1);

			if (func_150148_a(block) && (l & 4) == (j1 & 4))
			{
				k1 = j1 & 3;

				if (k1 == 1 && !this.func_150146_f(p_150144_1_, p_150144_2_ - 1, p_150144_3_, p_150144_4_, l))
				{
					flag = true;
				}
				else if (k1 == 0 && !this.func_150146_f(p_150144_1_, p_150144_2_ + 1, p_150144_3_, p_150144_4_, l))
				{
					f2 = 0.5F;
					f3 = 1.0F;
					flag = true;
				}
			}
		}

		if (flag)
		{
			this.setBlockBounds(f2, f, f4, f3, f1, f5);
		}

		return flag;
	}

	public void addCollisionBoxesToList(World p_149743_1_, int p_149743_2_, int p_149743_3_, int p_149743_4_, AxisAlignedBB p_149743_5_, List p_149743_6_, Entity p_149743_7_)
	{
		this.func_150147_e(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_);
		super.addCollisionBoxesToList(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_, p_149743_5_, p_149743_6_, p_149743_7_);
		boolean flag = this.func_150145_f(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_);
		super.addCollisionBoxesToList(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_, p_149743_5_, p_149743_6_, p_149743_7_);

		if (flag && this.func_150144_g(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_))
		{
			super.addCollisionBoxesToList(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_, p_149743_5_, p_149743_6_, p_149743_7_);
		}

		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	public void onBlockClicked(World p_149699_1_, int p_149699_2_, int p_149699_3_, int p_149699_4_, EntityPlayer p_149699_5_)
	{
		this.field_150149_b.onBlockClicked(p_149699_1_, p_149699_2_, p_149699_3_, p_149699_4_, p_149699_5_);
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World p_149734_1_, int p_149734_2_, int p_149734_3_, int p_149734_4_, Random p_149734_5_)
	{
		this.field_150149_b.randomDisplayTick(p_149734_1_, p_149734_2_, p_149734_3_, p_149734_4_, p_149734_5_);
	}

	public void onBlockDestroyedByPlayer(World p_149664_1_, int p_149664_2_, int p_149664_3_, int p_149664_4_, int p_149664_5_)
	{
		this.field_150149_b.onBlockDestroyedByPlayer(p_149664_1_, p_149664_2_, p_149664_3_, p_149664_4_, p_149664_5_);
	}

	public float getExplosionResistance(Entity p_149638_1_)
	{
		return this.field_150149_b.getExplosionResistance(p_149638_1_);
	}

	public int tickRate(World p_149738_1_)
	{
		return this.field_150149_b.tickRate(p_149738_1_);
	}

	public void velocityToAddToEntity(World p_149640_1_, int p_149640_2_, int p_149640_3_, int p_149640_4_, Entity p_149640_5_, Vec3 p_149640_6_)
	{
		this.field_150149_b.velocityToAddToEntity(p_149640_1_, p_149640_2_, p_149640_3_, p_149640_4_, p_149640_5_, p_149640_6_);
	}

	@SideOnly(Side.CLIENT)
	public int getMixedBrightnessForBlock(IBlockAccess p_149677_1_, int p_149677_2_, int p_149677_3_, int p_149677_4_)
	{
		return this.field_150149_b.getMixedBrightnessForBlock(p_149677_1_, p_149677_2_, p_149677_3_, p_149677_4_);
	}

	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass()
	{
		return this.field_150149_b.getRenderBlockPass();
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int p_149691_1_, int p_149691_2_)
	{
		return this.field_150149_b.getIcon(p_149691_1_, this.field_150151_M);
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World p_149633_1_, int p_149633_2_, int p_149633_3_, int p_149633_4_)
	{
		return this.field_150149_b.getSelectedBoundingBoxFromPool(p_149633_1_, p_149633_2_, p_149633_3_, p_149633_4_);
	}

	public boolean isCollidable()
	{
		return this.field_150149_b.isCollidable();
	}

	public boolean canCollideCheck(int p_149678_1_, boolean p_149678_2_)
	{
		return this.field_150149_b.canCollideCheck(p_149678_1_, p_149678_2_);
	}

	public boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_)
	{
		return this.field_150149_b.canPlaceBlockAt(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_);
	}

	public void onBlockAdded(World p_149726_1_, int p_149726_2_, int p_149726_3_, int p_149726_4_)
	{
		this.onNeighborBlockChange(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_, Blocks.air);
		this.field_150149_b.onBlockAdded(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_);
	}

	public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_)
	{
		this.field_150149_b.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_);
	}

	public void onEntityWalking(World p_149724_1_, int p_149724_2_, int p_149724_3_, int p_149724_4_, Entity p_149724_5_)
	{
		this.field_150149_b.onEntityWalking(p_149724_1_, p_149724_2_, p_149724_3_, p_149724_4_, p_149724_5_);
	}

	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		this.field_150149_b.updateTick(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
	}

	public boolean onBlockActivated(World p_149727_1_, int p_149727_2_, int p_149727_3_, int p_149727_4_, EntityPlayer p_149727_5_, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
	{
		return this.field_150149_b.onBlockActivated(p_149727_1_, p_149727_2_, p_149727_3_, p_149727_4_, p_149727_5_, 0, 0.0F, 0.0F, 0.0F);
	}

	public void onBlockDestroyedByExplosion(World p_149723_1_, int p_149723_2_, int p_149723_3_, int p_149723_4_, Explosion p_149723_5_)
	{
		this.field_150149_b.onBlockDestroyedByExplosion(p_149723_1_, p_149723_2_, p_149723_3_, p_149723_4_, p_149723_5_);
	}

	public MapColor getMapColor(int p_149728_1_)
	{
		return this.field_150149_b.getMapColor(this.field_150151_M);
	}

	public void onBlockPlacedBy(World p_149689_1_, int p_149689_2_, int p_149689_3_, int p_149689_4_, EntityLivingBase p_149689_5_, ItemStack p_149689_6_)
	{
		int l = MathHelper.floor_double((double)(p_149689_5_.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int i1 = p_149689_1_.getBlockMetadata(p_149689_2_, p_149689_3_, p_149689_4_) & 4;

		if (l == 0)
		{
			p_149689_1_.setBlockMetadataWithNotify(p_149689_2_, p_149689_3_, p_149689_4_, 2 | i1, 2);
		}

		if (l == 1)
		{
			p_149689_1_.setBlockMetadataWithNotify(p_149689_2_, p_149689_3_, p_149689_4_, 1 | i1, 2);
		}

		if (l == 2)
		{
			p_149689_1_.setBlockMetadataWithNotify(p_149689_2_, p_149689_3_, p_149689_4_, 3 | i1, 2);
		}

		if (l == 3)
		{
			p_149689_1_.setBlockMetadataWithNotify(p_149689_2_, p_149689_3_, p_149689_4_, 0 | i1, 2);
		}
	}

	public int onBlockPlaced(World p_149660_1_, int p_149660_2_, int p_149660_3_, int p_149660_4_, int p_149660_5_, float p_149660_6_, float p_149660_7_, float p_149660_8_, int p_149660_9_)
	{
		return p_149660_5_ != 0 && (p_149660_5_ == 1 || (double)p_149660_7_ <= 0.5D) ? p_149660_9_ : p_149660_9_ | 4;
	}

	public MovingObjectPosition collisionRayTrace(World p_149731_1_, int p_149731_2_, int p_149731_3_, int p_149731_4_, Vec3 p_149731_5_, Vec3 p_149731_6_)
	{
		MovingObjectPosition[] amovingobjectposition = new MovingObjectPosition[8];
		int l = p_149731_1_.getBlockMetadata(p_149731_2_, p_149731_3_, p_149731_4_);
		int i1 = l & 3;
		boolean flag = (l & 4) == 4;
		int[] aint = field_150150_a[i1 + (flag?4:0)];
		this.field_150152_N = true;
		int k1;
		int l1;
		int i2;

		for (int j1 = 0; j1 < 8; ++j1)
		{
			this.field_150153_O = j1;
			int[] aint1 = aint;
			k1 = aint.length;

			for (l1 = 0; l1 < k1; ++l1)
			{
				i2 = aint1[l1];

				if (i2 == j1)
				{
					;
				}
			}

			amovingobjectposition[j1] = super.collisionRayTrace(p_149731_1_, p_149731_2_, p_149731_3_, p_149731_4_, p_149731_5_, p_149731_6_);
		}

		int[] aint2 = aint;
		int k2 = aint.length;

		for (k1 = 0; k1 < k2; ++k1)
		{
			l1 = aint2[k1];
			amovingobjectposition[l1] = null;
		}

		MovingObjectPosition movingobjectposition1 = null;
		double d1 = 0.0D;
		MovingObjectPosition[] amovingobjectposition1 = amovingobjectposition;
		i2 = amovingobjectposition.length;

		for (int j2 = 0; j2 < i2; ++j2)
		{
			MovingObjectPosition movingobjectposition = amovingobjectposition1[j2];

			if (movingobjectposition != null)
			{
				double d0 = movingobjectposition.hitVec.squareDistanceTo(p_149731_6_);

				if (d0 > d1)
				{
					movingobjectposition1 = movingobjectposition;
					d1 = d0;
				}
			}
		}

		return movingobjectposition1;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister p_149651_1_) {}
}
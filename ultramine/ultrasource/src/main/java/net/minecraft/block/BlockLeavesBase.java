package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockLeavesBase extends Block
{
	protected boolean field_150121_P;
	private static final String __OBFID = "CL_00000326";

	protected BlockLeavesBase(Material p_i45433_1_, boolean p_i45433_2_)
	{
		super(p_i45433_1_);
		this.field_150121_P = p_i45433_2_;
	}

	public boolean isOpaqueCube()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_)
	{
		Block block = p_149646_1_.getBlock(p_149646_2_, p_149646_3_, p_149646_4_);
		return !this.field_150121_P && block == this ? false : super.shouldSideBeRendered(p_149646_1_, p_149646_2_, p_149646_3_, p_149646_4_, p_149646_5_);
	}

	/**
	 * This method added by UltraMine and may be replaced by mods.
	 * @see org.ultramine.server.asm.transformers.BlockLeavesBaseFixer
	 */
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		// If you modifying this code, don't forget to change signature in BlockLeavesBaseFixer
		if(!world.isRemote && ((WorldServer)world).getConfig().settings.fastLeafDecay)
		{
			int meta = world.getBlockMetadata(x, y, z);

			if ((meta & 8) != 0 && (meta & 4) == 0)
				world.scheduleBlockUpdate(x, y, z, this, 4 + world.rand.nextInt(7));
		}
	}
}
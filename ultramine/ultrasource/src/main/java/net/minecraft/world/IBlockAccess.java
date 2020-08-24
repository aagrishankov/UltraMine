package net.minecraft.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public interface IBlockAccess
{
	Block getBlock(int p_147439_1_, int p_147439_2_, int p_147439_3_);

	TileEntity getTileEntity(int p_147438_1_, int p_147438_2_, int p_147438_3_);

	@SideOnly(Side.CLIENT)
	int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_);

	int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_);

	int isBlockProvidingPowerTo(int p_72879_1_, int p_72879_2_, int p_72879_3_, int p_72879_4_);

	boolean isAirBlock(int p_147437_1_, int p_147437_2_, int p_147437_3_);

	@SideOnly(Side.CLIENT)
	BiomeGenBase getBiomeGenForCoords(int p_72807_1_, int p_72807_2_);

	@SideOnly(Side.CLIENT)
	int getHeight();

	@SideOnly(Side.CLIENT)
	boolean extendedLevelsInChunkCache();

	/**
	 * FORGE: isSideSolid, pulled up from {@link World}
	 *
	 * @param x X coord
	 * @param y Y coord
	 * @param z Z coord
	 * @param side Side
	 * @param _default default return value
	 * @return if the block is solid on the side
	 */
	boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default);
}
package net.minecraft.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface ITileEntityProvider
{
	TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_);
}
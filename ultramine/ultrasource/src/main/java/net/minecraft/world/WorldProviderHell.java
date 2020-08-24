package net.minecraft.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderHell;

public class WorldProviderHell extends WorldProvider
{
	private static final String __OBFID = "CL_00000387";

	public void registerWorldChunkManager()
	{
		this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F);
		this.isHellWorld = true;
		this.hasNoSky = true;
		this.dimensionId = -1;
	}

	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float p_76562_1_, float p_76562_2_)
	{
		return Vec3.createVectorHelper(0.20000000298023224D, 0.029999999329447746D, 0.029999999329447746D);
	}

	protected void generateLightBrightnessTable()
	{
		float f = 0.1F;

		for (int i = 0; i <= 15; ++i)
		{
			float f1 = 1.0F - (float)i / 15.0F;
			this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
		}
	}

	public IChunkProvider createChunkGenerator()
	{
		return new ChunkProviderHell(this.worldObj, this.worldObj.getSeed());
	}

	public boolean isSurfaceWorld()
	{
		return false;
	}

	public boolean canCoordinateBeSpawn(int p_76566_1_, int p_76566_2_)
	{
		return false;
	}

	public float calculateCelestialAngle(long p_76563_1_, float p_76563_3_)
	{
		return 0.5F;
	}

	public boolean canRespawnHere()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public boolean doesXZShowFog(int p_76568_1_, int p_76568_2_)
	{
		return true;
	}

	public String getDimensionName()
	{
		return "Nether";
	}
}
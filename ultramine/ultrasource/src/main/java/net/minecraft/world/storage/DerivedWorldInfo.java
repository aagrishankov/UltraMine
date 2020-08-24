package net.minecraft.world.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class DerivedWorldInfo extends WorldInfo
{
	private final WorldInfo theWorldInfo;
	private static final String __OBFID = "CL_00000584";

	public DerivedWorldInfo(WorldInfo p_i2145_1_)
	{
		this.theWorldInfo = p_i2145_1_;
	}

	public NBTTagCompound getNBTTagCompound()
	{
		return this.theWorldInfo.getNBTTagCompound();
	}

	public NBTTagCompound cloneNBTCompound(NBTTagCompound p_76082_1_)
	{
		return this.theWorldInfo.cloneNBTCompound(p_76082_1_);
	}

	public long getSeed()
	{
		return this.theWorldInfo.getSeed();
	}

	public int getSpawnX()
	{
		return this.theWorldInfo.getSpawnX();
	}

	public int getSpawnY()
	{
		return this.theWorldInfo.getSpawnY();
	}

	public int getSpawnZ()
	{
		return this.theWorldInfo.getSpawnZ();
	}

	public long getWorldTotalTime()
	{
		return this.theWorldInfo.getWorldTotalTime();
	}

	public long getWorldTime()
	{
		return this.theWorldInfo.getWorldTime();
	}

	@SideOnly(Side.CLIENT)
	public long getSizeOnDisk()
	{
		return this.theWorldInfo.getSizeOnDisk();
	}

	public NBTTagCompound getPlayerNBTTagCompound()
	{
		return this.theWorldInfo.getPlayerNBTTagCompound();
	}

	public int getVanillaDimension()
	{
		return this.theWorldInfo.getVanillaDimension();
	}

	public String getWorldName()
	{
		return this.theWorldInfo.getWorldName();
	}

	public int getSaveVersion()
	{
		return this.theWorldInfo.getSaveVersion();
	}

	@SideOnly(Side.CLIENT)
	public long getLastTimePlayed()
	{
		return this.theWorldInfo.getLastTimePlayed();
	}

	public boolean isThundering()
	{
		return this.theWorldInfo.isThundering();
	}

	public int getThunderTime()
	{
		return this.theWorldInfo.getThunderTime();
	}

	public boolean isRaining()
	{
		return this.theWorldInfo.isRaining();
	}

	public int getRainTime()
	{
		return this.theWorldInfo.getRainTime();
	}

	public WorldSettings.GameType getGameType()
	{
		return this.theWorldInfo.getGameType();
	}

	@SideOnly(Side.CLIENT)
	public void setSpawnX(int p_76058_1_) {}

	@SideOnly(Side.CLIENT)
	public void setSpawnY(int p_76056_1_) {}

	public void incrementTotalWorldTime(long p_82572_1_) {}

	@SideOnly(Side.CLIENT)
	public void setSpawnZ(int p_76087_1_) {}

	public void setWorldTime(long p_76068_1_) {}

	public void setSpawnPosition(int p_76081_1_, int p_76081_2_, int p_76081_3_) {}

	public void setWorldName(String p_76062_1_) {}

	public void setSaveVersion(int p_76078_1_) {}

	public void setThundering(boolean p_76069_1_) {}

	public void setThunderTime(int p_76090_1_) {}

	public void setRaining(boolean p_76084_1_) {}

	public void setRainTime(int p_76080_1_) {}

	public boolean isMapFeaturesEnabled()
	{
		return this.theWorldInfo.isMapFeaturesEnabled();
	}

	public boolean isHardcoreModeEnabled()
	{
		return this.theWorldInfo.isHardcoreModeEnabled();
	}

	public WorldType getTerrainType()
	{
		return this.theWorldInfo.getTerrainType();
	}

	public void setTerrainType(WorldType p_76085_1_) {}

	public boolean areCommandsAllowed()
	{
		return this.theWorldInfo.areCommandsAllowed();
	}

	public boolean isInitialized()
	{
		return this.theWorldInfo.isInitialized();
	}

	public void setServerInitialized(boolean p_76091_1_) {}

	public GameRules getGameRulesInstance()
	{
		return this.theWorldInfo.getGameRulesInstance();
	}
}
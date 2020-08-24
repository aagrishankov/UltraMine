package net.minecraft.world.chunk;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import gnu.trove.iterator.TByteIterator;
import gnu.trove.set.TByteSet;
import gnu.trove.set.hash.TByteHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.openhft.koloboke.collect.map.ShortObjMap;
import net.openhft.koloboke.collect.map.hash.HashShortObjMaps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.EntityType;
import org.ultramine.server.chunk.ChunkBindState;
import org.ultramine.server.chunk.ChunkHash;
import org.ultramine.server.chunk.IChunkDependency;
import org.ultramine.server.chunk.PendingBlockUpdate;
import org.ultramine.server.internal.LambdaHolder;
import org.ultramine.server.util.WeakObjectPool;
import org.ultramine.server.event.WorldUpdateObject;
import org.ultramine.server.event.WorldUpdateObjectType;

public class Chunk implements IChunkDependency
{
	private static final Logger logger = LogManager.getLogger();
	public static boolean isLit;
	private ExtendedBlockStorage[] storageArrays;
	private byte[] blockBiomeArray;
	public int[] precipitationHeightMap;
	public boolean[] updateSkylightColumns;
	public boolean isChunkLoaded;
	public World worldObj;
	public int[] heightMap;
	public final int xPosition;
	public final int zPosition;
	private boolean isGapLightingUpdated;
	public Map chunkTileEntityMap;
	public List[] entityLists;
	public boolean isTerrainPopulated;
	public boolean isLightPopulated;
	public boolean field_150815_m;
	public boolean isModified;
	public boolean hasEntities;
	public long lastSaveTime;
	public boolean sendUpdates;
	public int heightMapMinimum;
	public long inhabitedTime;
	private int queuedLightChecks;
	private static final String __OBFID = "CL_00000373";

	public Chunk(World p_i1995_1_, int p_i1995_2_, int p_i1995_3_)
	{
		this.storageArrays = new ExtendedBlockStorage[16];
		this.blockBiomeArray = new byte[256];
		this.precipitationHeightMap = new int[256];
		this.updateSkylightColumns = new boolean[0];
		this.chunkTileEntityMap = new HashMap();
		this.queuedLightChecks = 4096;
		this.entityLists = new List[16];
		this.worldObj = p_i1995_1_;
		this.xPosition = p_i1995_2_;
		this.zPosition = p_i1995_3_;
		this.heightMap = new int[256];

		for (int k = 0; k < this.entityLists.length; ++k)
		{
			this.entityLists[k] = new ArrayList();
		}

		Arrays.fill(this.precipitationHeightMap, -999);
		Arrays.fill(this.blockBiomeArray, (byte) - 1);
	}

	public Chunk(World p_i45446_1_, Block[] p_i45446_2_, int p_i45446_3_, int p_i45446_4_)
	{
		this(p_i45446_1_, p_i45446_3_, p_i45446_4_);
		int k = p_i45446_2_.length / 256;
		boolean flag = !p_i45446_1_.provider.hasNoSky;

		for (int l = 0; l < 16; ++l)
		{
			for (int i1 = 0; i1 < 16; ++i1)
			{
				for (int j1 = 0; j1 < k; ++j1)
				{
					Block block = p_i45446_2_[l << 11 | i1 << 7 | j1];

					if (block != null && block.getMaterial() != Material.air)
					{
						int k1 = j1 >> 4;

						if (this.storageArrays[k1] == null)
						{
							this.storageArrays[k1] = new ExtendedBlockStorage(k1 << 4, flag);
						}

						this.storageArrays[k1].func_150818_a(l, j1 & 15, i1, block);
					}
				}
			}
		}
	}

	public Chunk(World p_i45447_1_, Block[] p_i45447_2_, byte[] p_i45447_3_, int p_i45447_4_, int p_i45447_5_)
	{
		this(p_i45447_1_, p_i45447_4_, p_i45447_5_);
		int k = p_i45447_2_.length / 256;
		boolean flag = !p_i45447_1_.provider.hasNoSky;

		for (int l = 0; l < 16; ++l)
		{
			for (int i1 = 0; i1 < 16; ++i1)
			{
				for (int j1 = 0; j1 < k; ++j1)
				{
					int k1 = l * k * 16 | i1 * k | j1;
					Block block = p_i45447_2_[k1];

					if (block != null && block != Blocks.air)
					{
						int l1 = j1 >> 4;

						if (this.storageArrays[l1] == null)
						{
							this.storageArrays[l1] = new ExtendedBlockStorage(l1 << 4, flag);
						}

						this.storageArrays[l1].func_150818_a(l, j1 & 15, i1, block);
						this.storageArrays[l1].setExtBlockMetadata(l, j1 & 15, i1, p_i45447_3_[k1]);
					}
				}
			}
		}
	}

	public boolean isAtLocation(int p_76600_1_, int p_76600_2_)
	{
		return p_76600_1_ == this.xPosition && p_76600_2_ == this.zPosition;
	}

	public int getHeightValue(int p_76611_1_, int p_76611_2_)
	{
		return this.heightMap[p_76611_2_ << 4 | p_76611_1_];
	}

	public int getTopFilledSegment()
	{
		for (int i = this.storageArrays.length - 1; i >= 0; --i)
		{
			if (this.storageArrays[i] != null)
			{
				return this.storageArrays[i].getYLocation();
			}
		}

		return 0;
	}

	public ExtendedBlockStorage[] getBlockStorageArray()
	{
		return this.storageArrays;
	}

	@SideOnly(Side.CLIENT)
	public void generateHeightMap()
	{
		int i = this.getTopFilledSegment();
		this.heightMapMinimum = Integer.MAX_VALUE;

		for (int j = 0; j < 16; ++j)
		{
			int k = 0;

			while (k < 16)
			{
				this.precipitationHeightMap[j + (k << 4)] = -999;
				int l = i + 16 - 1;

				while (true)
				{
					if (l > 0)
					{
						Block block = this.getBlock(j, l - 1, k);

						if (func_150808_b(j, l - 1, k) == 0)
						{
							--l;
							continue;
						}

						this.heightMap[k << 4 | j] = l;

						if (l < this.heightMapMinimum)
						{
							this.heightMapMinimum = l;
						}
					}

					++k;
					break;
				}
			}
		}

		this.isModified = true;
	}

	public void generateSkylightMap()
	{
		int i = this.getTopFilledSegment();
		this.heightMapMinimum = Integer.MAX_VALUE;

		for (int j = 0; j < 16; ++j)
		{
			int k = 0;

			while (k < 16)
			{
				this.precipitationHeightMap[j + (k << 4)] = -999;
				int l = i + 16 - 1;

				while (true)
				{
					if (l > 0)
					{
						if (this.func_150808_b(j, l - 1, k) == 0)
						{
							--l;
							continue;
						}

						this.heightMap[k << 4 | j] = l;

						if (l < this.heightMapMinimum)
						{
							this.heightMapMinimum = l;
						}
					}

					if (!this.worldObj.provider.hasNoSky)
					{
						l = 15;
						int i1 = i + 16 - 1;

						do
						{
							int j1 = this.func_150808_b(j, i1, k);

							if (j1 == 0 && l != 15)
							{
								j1 = 1;
							}

							l -= j1;

							if (l > 0)
							{
								ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];

								if (extendedblockstorage != null)
								{
									extendedblockstorage.setExtSkylightValue(j, i1 & 15, k, l);
									this.worldObj.func_147479_m((this.xPosition << 4) + j, i1, (this.zPosition << 4) + k);
								}
							}

							--i1;
						}
						while (i1 > 0 && l > 0);
					}

					++k;
					break;
				}
			}
		}

		this.isModified = true;
	}

	private void propagateSkylightOcclusion(int p_76595_1_, int p_76595_2_)
	{
		updateLightCoords.add((byte)(p_76595_1_ + p_76595_2_ * 16));
		this.isGapLightingUpdated = true;
	}

	private void recheckGaps(boolean p_150803_1_)
	{
		this.worldObj.theProfiler.startSection("recheckGaps");

		if (this.worldObj.doChunksNearChunkExist(this.xPosition * 16 + 8, 0, this.zPosition * 16 + 8, 16))
		{
//			for (int i = 0; i < 16; ++i)
			{
//				for (int j = 0; j < 16; ++j)
				for(TByteIterator it = updateLightCoords.iterator(); it.hasNext();)
				{
					int coord = it.next() & 0xFF;
					int i = coord & 0xF;
					int j = coord >> 4;
//					if (this.updateSkylightColumns[i + j * 16])
					{
//						this.updateSkylightColumns[i + j * 16] = false;
						int k = this.getHeightValue(i, j);
						int l = this.xPosition * 16 + i;
						int i1 = this.zPosition * 16 + j;
						int j1 = this.worldObj.getChunkHeightMapMinimum(l - 1, i1);
						int k1 = this.worldObj.getChunkHeightMapMinimum(l + 1, i1);
						int l1 = this.worldObj.getChunkHeightMapMinimum(l, i1 - 1);
						int i2 = this.worldObj.getChunkHeightMapMinimum(l, i1 + 1);

						if (k1 < j1)
						{
							j1 = k1;
						}

						if (l1 < j1)
						{
							j1 = l1;
						}

						if (i2 < j1)
						{
							j1 = i2;
						}

						this.checkSkylightNeighborHeight(l, i1, j1);
						this.checkSkylightNeighborHeight(l - 1, i1, k);
						this.checkSkylightNeighborHeight(l + 1, i1, k);
						this.checkSkylightNeighborHeight(l, i1 - 1, k);
						this.checkSkylightNeighborHeight(l, i1 + 1, k);

						if (p_150803_1_)
						{
							it.remove();
							this.worldObj.theProfiler.endSection();
							return;
						}
					}
				}

				updateLightCoords.clear();
			}

			this.isGapLightingUpdated = false;
		}

		this.worldObj.theProfiler.endSection();
	}

	private void checkSkylightNeighborHeight(int p_76599_1_, int p_76599_2_, int p_76599_3_)
	{
		int l = this.worldObj.getHeightValue(p_76599_1_, p_76599_2_);

		if (l > p_76599_3_)
		{
			this.updateSkylightNeighborHeight(p_76599_1_, p_76599_2_, p_76599_3_, l + 1);
		}
		else if (l < p_76599_3_)
		{
			this.updateSkylightNeighborHeight(p_76599_1_, p_76599_2_, l, p_76599_3_ + 1);
		}
	}

	private void updateSkylightNeighborHeight(int p_76609_1_, int p_76609_2_, int p_76609_3_, int p_76609_4_)
	{
		if (p_76609_4_ > p_76609_3_ && this.worldObj.doChunksNearChunkExist(p_76609_1_, 0, p_76609_2_, 16))
		{
			for (int i1 = p_76609_3_; i1 < p_76609_4_; ++i1)
			{
				this.worldObj.updateLightByType(EnumSkyBlock.Sky, p_76609_1_, i1, p_76609_2_);
			}

			this.isModified = true;
		}
	}

	private void relightBlock(int p_76615_1_, int p_76615_2_, int p_76615_3_)
	{
		int l = this.heightMap[p_76615_3_ << 4 | p_76615_1_] & 255;
		int i1 = l;

		if (p_76615_2_ > l)
		{
			i1 = p_76615_2_;
		}

		while (i1 > 0 && this.func_150808_b(p_76615_1_, i1 - 1, p_76615_3_) == 0)
		{
			--i1;
		}

		if (i1 != l)
		{
			this.worldObj.markBlocksDirtyVertical(p_76615_1_ + this.xPosition * 16, p_76615_3_ + this.zPosition * 16, i1, l);
			this.heightMap[p_76615_3_ << 4 | p_76615_1_] = i1;
			int j1 = this.xPosition * 16 + p_76615_1_;
			int k1 = this.zPosition * 16 + p_76615_3_;
			int l1;
			int i2;

			if (!this.worldObj.provider.hasNoSky)
			{
				ExtendedBlockStorage extendedblockstorage;

				if (i1 < l)
				{
					for (l1 = i1; l1 < l; ++l1)
					{
						extendedblockstorage = this.storageArrays[l1 >> 4];

						if (extendedblockstorage != null)
						{
							extendedblockstorage.setExtSkylightValue(p_76615_1_, l1 & 15, p_76615_3_, 15);
							this.worldObj.func_147479_m((this.xPosition << 4) + p_76615_1_, l1, (this.zPosition << 4) + p_76615_3_);
						}
					}
				}
				else
				{
					for (l1 = l; l1 < i1; ++l1)
					{
						extendedblockstorage = this.storageArrays[l1 >> 4];

						if (extendedblockstorage != null)
						{
							extendedblockstorage.setExtSkylightValue(p_76615_1_, l1 & 15, p_76615_3_, 0);
							this.worldObj.func_147479_m((this.xPosition << 4) + p_76615_1_, l1, (this.zPosition << 4) + p_76615_3_);
						}
					}
				}

				l1 = 15;

				while (i1 > 0 && l1 > 0)
				{
					--i1;
					i2 = this.func_150808_b(p_76615_1_, i1, p_76615_3_);

					if (i2 == 0)
					{
						i2 = 1;
					}

					l1 -= i2;

					if (l1 < 0)
					{
						l1 = 0;
					}

					ExtendedBlockStorage extendedblockstorage1 = this.storageArrays[i1 >> 4];

					if (extendedblockstorage1 != null)
					{
						extendedblockstorage1.setExtSkylightValue(p_76615_1_, i1 & 15, p_76615_3_, l1);
					}
				}
			}

			l1 = this.heightMap[p_76615_3_ << 4 | p_76615_1_];
			i2 = l;
			int j2 = l1;

			if (l1 < l)
			{
				i2 = l1;
				j2 = l;
			}

			if (l1 < this.heightMapMinimum)
			{
				this.heightMapMinimum = l1;
			}

			if (!this.worldObj.provider.hasNoSky)
			{
				this.updateSkylightNeighborHeight(j1 - 1, k1, i2, j2);
				this.updateSkylightNeighborHeight(j1 + 1, k1, i2, j2);
				this.updateSkylightNeighborHeight(j1, k1 - 1, i2, j2);
				this.updateSkylightNeighborHeight(j1, k1 + 1, i2, j2);
				this.updateSkylightNeighborHeight(j1, k1, i2, j2);
			}

			this.isModified = true;
		}
	}

	public int func_150808_b(int p_150808_1_, int p_150808_2_, int p_150808_3_)
	{
		int x = (xPosition << 4) + p_150808_1_;
		int z = (zPosition << 4) + p_150808_3_;
		return this.getBlock(p_150808_1_, p_150808_2_, p_150808_3_).getLightOpacity(worldObj, x, p_150808_2_, z);
	}

	public Block getBlock(final int p_150810_1_, final int p_150810_2_, final int p_150810_3_)
	{
		Block block = Blocks.air;

		if (p_150810_2_ >> 4 < this.storageArrays.length)
		{
			ExtendedBlockStorage extendedblockstorage = this.storageArrays[p_150810_2_ >> 4];

			if (extendedblockstorage != null)
			{
				try
				{
					block = extendedblockstorage.getBlockByExtId(p_150810_1_, p_150810_2_ & 15, p_150810_3_);
				}
				catch (Throwable throwable)
				{
					CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block");
					CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
					crashreportcategory.addCrashSectionCallable("Location", new Callable()
					{
						private static final String __OBFID = "CL_00000374";
						public String call()
						{
							return CrashReportCategory.getLocationInfo(p_150810_1_, p_150810_2_, p_150810_3_);
						}
					});
					throw new ReportedException(crashreport);
				}
			}
		}

		return block;
	}

	public int getBlockMetadata(int p_76628_1_, int p_76628_2_, int p_76628_3_)
	{
		if (p_76628_2_ >> 4 >= this.storageArrays.length)
		{
			return 0;
		}
		else
		{
			ExtendedBlockStorage extendedblockstorage = this.storageArrays[p_76628_2_ >> 4];
			return extendedblockstorage != null ? extendedblockstorage.getExtBlockMetadata(p_76628_1_, p_76628_2_ & 15, p_76628_3_) : 0;
		}
	}

	public boolean func_150807_a(int p_150807_1_, int p_150807_2_, int p_150807_3_, Block p_150807_4_, int p_150807_5_)
	{
		int i1 = p_150807_3_ << 4 | p_150807_1_;

		if (p_150807_2_ >= this.precipitationHeightMap[i1] - 1)
		{
			this.precipitationHeightMap[i1] = -999;
		}

		int j1 = this.heightMap[i1];
		Block block1 = this.getBlock(p_150807_1_, p_150807_2_, p_150807_3_);
		int k1 = this.getBlockMetadata(p_150807_1_, p_150807_2_, p_150807_3_);

		if (block1 == p_150807_4_ && k1 == p_150807_5_)
		{
			return false;
		}
		else
		{
			ExtendedBlockStorage extendedblockstorage = this.storageArrays[p_150807_2_ >> 4];
			boolean flag = false;

			if (extendedblockstorage == null)
			{
				if (p_150807_4_ == Blocks.air)
				{
					return false;
				}

				extendedblockstorage = this.storageArrays[p_150807_2_ >> 4] = new ExtendedBlockStorage(p_150807_2_ >> 4 << 4, !this.worldObj.provider.hasNoSky);
				flag = p_150807_2_ >= j1;
			}

			int l1 = this.xPosition * 16 + p_150807_1_;
			int i2 = this.zPosition * 16 + p_150807_3_;

			int k2 = block1.getLightOpacity(this.worldObj, l1, p_150807_2_, i2);

			if (!this.worldObj.isRemote)
			{
				block1.onBlockPreDestroy(this.worldObj, l1, p_150807_2_, i2, k1);
			}

			extendedblockstorage.func_150818_a(p_150807_1_, p_150807_2_ & 15, p_150807_3_, p_150807_4_);
			extendedblockstorage.setExtBlockMetadata(p_150807_1_, p_150807_2_ & 15, p_150807_3_, p_150807_5_); // This line duplicates the one below, so breakBlock fires with valid worldstate

			if (!this.worldObj.isRemote)
			{
				block1.breakBlock(this.worldObj, l1, p_150807_2_, i2, block1, k1);
				// After breakBlock a phantom TE might have been created with incorrect meta. This attempts to kill that phantom TE so the normal one can be create properly later
				TileEntity te = this.getTileEntityUnsafe(p_150807_1_ & 0x0F, p_150807_2_, p_150807_3_ & 0x0F);
				if (te != null && te.shouldRefresh(block1, getBlock(p_150807_1_ & 0x0F, p_150807_2_, p_150807_3_ & 0x0F), k1, getBlockMetadata(p_150807_1_ & 0x0F, p_150807_2_, p_150807_3_ & 0x0F), worldObj, l1, p_150807_2_, i2))
				{
					this.removeTileEntity(p_150807_1_ & 0x0F, p_150807_2_, p_150807_3_ & 0x0F);
				}
			}
			else if (block1.hasTileEntity(k1))
			{
				TileEntity te = this.getTileEntityUnsafe(p_150807_1_ & 0x0F, p_150807_2_, p_150807_3_ & 0x0F);
				if (te != null && te.shouldRefresh(block1, p_150807_4_, k1, p_150807_5_, worldObj, l1, p_150807_2_, i2))
				{
					this.worldObj.removeTileEntity(l1, p_150807_2_, i2);
				}
			}

			if (extendedblockstorage.getBlockByExtId(p_150807_1_, p_150807_2_ & 15, p_150807_3_) != p_150807_4_)
			{
				return false;
			}
			else
			{
				extendedblockstorage.setExtBlockMetadata(p_150807_1_, p_150807_2_ & 15, p_150807_3_, p_150807_5_);

				if (flag)
				{
					this.generateSkylightMap();
				}
				else
				{
					int j2 = p_150807_4_.getLightOpacity(this.worldObj, l1, p_150807_2_, i2);

					if (j2 > 0)
					{
						if (p_150807_2_ >= j1)
						{
							this.relightBlock(p_150807_1_, p_150807_2_ + 1, p_150807_3_);
						}
					}
					else if (p_150807_2_ == j1 - 1)
					{
						this.relightBlock(p_150807_1_, p_150807_2_, p_150807_3_);
					}

					if (j2 != k2 && (j2 < k2 || this.getSavedLightValue(EnumSkyBlock.Sky, p_150807_1_, p_150807_2_, p_150807_3_) > 0 || this.getSavedLightValue(EnumSkyBlock.Block, p_150807_1_, p_150807_2_, p_150807_3_) > 0))
					{
						this.propagateSkylightOcclusion(p_150807_1_, p_150807_3_);
					}
				}

				TileEntity tileentity;

				if (!this.worldObj.isRemote)
				{
					p_150807_4_.onBlockAdded(this.worldObj, l1, p_150807_2_, i2);
				}

				if (p_150807_4_.hasTileEntity(p_150807_5_))
				{
					tileentity = this.func_150806_e(p_150807_1_, p_150807_2_, p_150807_3_);

					if (tileentity != null)
					{
						tileentity.updateContainingBlockInfo();
						tileentity.blockMetadata = p_150807_5_;
					}
				}

				this.isModified = true;
				return true;
			}
		}
	}

	public boolean setBlockMetadata(int p_76589_1_, int p_76589_2_, int p_76589_3_, int p_76589_4_)
	{
		ExtendedBlockStorage extendedblockstorage = this.storageArrays[p_76589_2_ >> 4];

		if (extendedblockstorage == null)
		{
			return false;
		}
		else
		{
			int i1 = extendedblockstorage.getExtBlockMetadata(p_76589_1_, p_76589_2_ & 15, p_76589_3_);

			if (i1 == p_76589_4_)
			{
				return false;
			}
			else
			{
				this.isModified = true;
				extendedblockstorage.setExtBlockMetadata(p_76589_1_, p_76589_2_ & 15, p_76589_3_, p_76589_4_);

				if (extendedblockstorage.getBlockByExtId(p_76589_1_, p_76589_2_ & 15, p_76589_3_).hasTileEntity(p_76589_4_))
				{
					TileEntity tileentity = this.func_150806_e(p_76589_1_, p_76589_2_, p_76589_3_);

					if (tileentity != null)
					{
						tileentity.updateContainingBlockInfo();
						tileentity.blockMetadata = p_76589_4_;
					}
				}

				return true;
			}
		}
	}

	public int getSavedLightValue(EnumSkyBlock p_76614_1_, int p_76614_2_, int p_76614_3_, int p_76614_4_)
	{
		ExtendedBlockStorage extendedblockstorage = this.storageArrays[p_76614_3_ >> 4];
		return extendedblockstorage == null ? (this.canBlockSeeTheSky(p_76614_2_, p_76614_3_, p_76614_4_) ? p_76614_1_.defaultLightValue : 0) : (p_76614_1_ == EnumSkyBlock.Sky ? (this.worldObj.provider.hasNoSky ? 0 : extendedblockstorage.getExtSkylightValue(p_76614_2_, p_76614_3_ & 15, p_76614_4_)) : (p_76614_1_ == EnumSkyBlock.Block ? extendedblockstorage.getExtBlocklightValue(p_76614_2_, p_76614_3_ & 15, p_76614_4_) : p_76614_1_.defaultLightValue));
	}

	public void setLightValue(EnumSkyBlock p_76633_1_, int p_76633_2_, int p_76633_3_, int p_76633_4_, int p_76633_5_)
	{
		ExtendedBlockStorage extendedblockstorage = this.storageArrays[p_76633_3_ >> 4];

		if (extendedblockstorage == null)
		{
			extendedblockstorage = this.storageArrays[p_76633_3_ >> 4] = new ExtendedBlockStorage(p_76633_3_ >> 4 << 4, !this.worldObj.provider.hasNoSky);
			this.generateSkylightMap();
		}

		this.isModified = true;

		if (p_76633_1_ == EnumSkyBlock.Sky)
		{
			if (!this.worldObj.provider.hasNoSky)
			{
				extendedblockstorage.setExtSkylightValue(p_76633_2_, p_76633_3_ & 15, p_76633_4_, p_76633_5_);
			}
		}
		else if (p_76633_1_ == EnumSkyBlock.Block)
		{
			extendedblockstorage.setExtBlocklightValue(p_76633_2_, p_76633_3_ & 15, p_76633_4_, p_76633_5_);
		}
	}

	public int getBlockLightValue(int p_76629_1_, int p_76629_2_, int p_76629_3_, int p_76629_4_)
	{
		ExtendedBlockStorage extendedblockstorage = this.storageArrays[p_76629_2_ >> 4];

		if (extendedblockstorage == null)
		{
			return !this.worldObj.provider.hasNoSky && p_76629_4_ < EnumSkyBlock.Sky.defaultLightValue ? EnumSkyBlock.Sky.defaultLightValue - p_76629_4_ : 0;
		}
		else
		{
			int i1 = this.worldObj.provider.hasNoSky ? 0 : extendedblockstorage.getExtSkylightValue(p_76629_1_, p_76629_2_ & 15, p_76629_3_);

			if (i1 > 0)
			{
				isLit = true;
			}

			i1 -= p_76629_4_;
			int j1 = extendedblockstorage.getExtBlocklightValue(p_76629_1_, p_76629_2_ & 15, p_76629_3_);

			if (j1 > i1)
			{
				i1 = j1;
			}

			return i1;
		}
	}

	public void addEntity(Entity p_76612_1_)
	{
		this.hasEntities = true;
		int i = MathHelper.floor_double(p_76612_1_.posX / 16.0D);
		int j = MathHelper.floor_double(p_76612_1_.posZ / 16.0D);

		if (i != this.xPosition || j != this.zPosition)
		{
			logger.warn("Wrong location! " + p_76612_1_ + " (at " + i + ", " + j + " instead of " + this.xPosition + ", " + this.zPosition + ")", new Throwable());
			p_76612_1_.setDead();
			return;
		}

		int k = MathHelper.floor_double(p_76612_1_.posY / 16.0D);

		if (k < 0)
		{
			k = 0;
		}

		if (k >= this.entityLists.length)
		{
			k = this.entityLists.length - 1;
		}

		MinecraftForge.EVENT_BUS.post(new EntityEvent.EnteringChunk(p_76612_1_, this.xPosition, this.zPosition, p_76612_1_.chunkCoordX, p_76612_1_.chunkCoordZ));
		p_76612_1_.addedToChunk = true;
		p_76612_1_.chunkCoordX = this.xPosition;
		p_76612_1_.chunkCoordY = k;
		p_76612_1_.chunkCoordZ = this.zPosition;
		this.entityLists[k].add(p_76612_1_);
		onEntityAdd(p_76612_1_);
	}

	public void removeEntity(Entity p_76622_1_)
	{
		this.removeEntityAtIndex(p_76622_1_, p_76622_1_.chunkCoordY);
	}

	public void removeEntityAtIndex(Entity p_76608_1_, int p_76608_2_)
	{
		if (p_76608_2_ < 0)
		{
			p_76608_2_ = 0;
		}

		if (p_76608_2_ >= this.entityLists.length)
		{
			p_76608_2_ = this.entityLists.length - 1;
		}

		if(this.entityLists[p_76608_2_].remove(p_76608_1_))
			onEntityRemove(p_76608_1_);
	}

	public boolean canBlockSeeTheSky(int p_76619_1_, int p_76619_2_, int p_76619_3_)
	{
		return p_76619_2_ >= this.heightMap[p_76619_3_ << 4 | p_76619_1_];
	}

	public TileEntity func_150806_e(int p_150806_1_, int p_150806_2_, int p_150806_3_)
	{
		short hash = ChunkHash.chunkCoordToHash(p_150806_1_, p_150806_2_, p_150806_3_);
		TileEntity tileentity = fastTileEntityMap.get(hash);

		if (tileentity != null && tileentity.isInvalid())
		{
			chunkTileEntityMap.remove(new ChunkPosition(p_150806_1_, p_150806_2_, p_150806_3_));
			fastTileEntityMap.remove(hash);
			tileentity = null;
		}

		if (tileentity == null)
		{
			Block block = this.getBlock(p_150806_1_, p_150806_2_, p_150806_3_);
			int meta = this.getBlockMetadata(p_150806_1_, p_150806_2_, p_150806_3_);

			if (!block.hasTileEntity(meta))
			{
				return null;
			}

			// This vanilla code restores broken TileEntities. Invokes only on TileEntity GET.
			// Newly created TileEntity should not be attached to WorldUpdateObject that invokes this method.
			// So using this hack to override and then restore WorldUpdateObject
			WorldUpdateObject obj = worldObj.getEventProxy().getUpdateObject();
			WorldUpdateObjectType type = obj == null ? null : obj.getType();
			if(type != null)
				worldObj.getEventProxy().pushState(WorldUpdateObjectType.UNKNOWN);

			tileentity = block.createTileEntity(worldObj, meta);
			this.worldObj.setTileEntity(this.xPosition * 16 + p_150806_1_, p_150806_2_, this.zPosition * 16 + p_150806_3_, tileentity);

			if(type != null)
				worldObj.getEventProxy().pushState(type);
		}

		return tileentity;
	}

	public void addTileEntity(TileEntity p_150813_1_)
	{
		int i = p_150813_1_.xCoord - this.xPosition * 16;
		int j = p_150813_1_.yCoord;
		int k = p_150813_1_.zCoord - this.zPosition * 16;
		this.func_150812_a(i, j, k, p_150813_1_);

		if (this.isChunkLoaded)
		{
			this.worldObj.addTileEntity(p_150813_1_);
		}
	}

	public void func_150812_a(int p_150812_1_, int p_150812_2_, int p_150812_3_, TileEntity p_150812_4_)
	{
		ChunkPosition chunkposition = new ChunkPosition(p_150812_1_, p_150812_2_, p_150812_3_);
		short hash = ChunkHash.chunkCoordToHash(p_150812_1_, p_150812_2_, p_150812_3_);
		p_150812_4_.setWorldObj(this.worldObj);
		p_150812_4_.xCoord = this.xPosition * 16 + p_150812_1_;
		p_150812_4_.yCoord = p_150812_2_;
		p_150812_4_.zCoord = this.zPosition * 16 + p_150812_3_;

		int metadata = getBlockMetadata(p_150812_1_, p_150812_2_, p_150812_3_);
		if (this.getBlock(p_150812_1_, p_150812_2_, p_150812_3_).hasTileEntity(metadata))
		{
			TileEntity old = fastTileEntityMap.get(hash);
			if(old != null) old.invalidate();

			p_150812_4_.validate();
			this.chunkTileEntityMap.put(chunkposition, p_150812_4_);
			fastTileEntityMap.put(hash, p_150812_4_);
		}
	}

	public void removeTileEntity(int p_150805_1_, int p_150805_2_, int p_150805_3_)
	{
		ChunkPosition chunkposition = new ChunkPosition(p_150805_1_, p_150805_2_, p_150805_3_);

		if (this.isChunkLoaded)
		{
			TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.remove(chunkposition);

			if (tileentity != null)
			{
				tileentity.invalidate();
			}
			
			fastTileEntityMap.remove(ChunkHash.chunkCoordToHash(p_150805_1_, p_150805_2_, p_150805_3_));
		}
	}

	public void onChunkLoad()
	{
		this.isChunkLoaded = true;
		this.worldObj.func_147448_a(this.chunkTileEntityMap.values());

		for (int i = 0; i < this.entityLists.length; ++i)
		{
			Iterator iterator = this.entityLists[i].iterator();

			while (iterator.hasNext())
			{
				Entity entity = (Entity)iterator.next();
				entity.onChunkLoad();
			}

			this.worldObj.addLoadedEntities(this.entityLists[i]);
		}
		MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(this));

		convertTileEntityMap();
		loadTime = unbindTime = MinecraftServer.getServer().getTickCounter();
	}

	public void onChunkUnload()
	{
		this.isChunkLoaded = false;
		Iterator iterator = this.chunkTileEntityMap.values().iterator();

		while (iterator.hasNext())
		{
			TileEntity tileentity = (TileEntity)iterator.next();
			this.worldObj.func_147457_a(tileentity);
		}

		for (int i = 0; i < this.entityLists.length; ++i)
		{
			for(Iterator it = entityLists[i].iterator(); it.hasNext();)
			{
				Entity ent = (Entity)it.next();
				if(ent.isEntityPlayerMP() || ent.isDead)
					it.remove();
			}
			this.worldObj.unloadEntities(this.entityLists[i]);
		}
		MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(this));
		resetEntityCounters();
	}

	public void setChunkModified()
	{
		this.isModified = true;
	}

	public void getEntitiesWithinAABBForEntity(Entity p_76588_1_, AxisAlignedBB p_76588_2_, List p_76588_3_, IEntitySelector p_76588_4_)
	{
		int i = MathHelper.floor_double((p_76588_2_.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
		int j = MathHelper.floor_double((p_76588_2_.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
		i = MathHelper.clamp_int(i, 0, this.entityLists.length - 1);
		j = MathHelper.clamp_int(j, 0, this.entityLists.length - 1);

		for (int k = i; k <= j; ++k)
		{
			List list1 = this.entityLists[k];

			for (int l = 0; l < list1.size(); ++l)
			{
				Entity entity1 = (Entity)list1.get(l);

				if (!entity1.isDead)
				if (entity1 != p_76588_1_ && entity1.boundingBox.intersectsWith(p_76588_2_) && (p_76588_4_ == null || p_76588_4_.isEntityApplicable(entity1)))
				{
					p_76588_3_.add(entity1);
					Entity[] aentity = entity1.getParts();

					if (aentity != null)
					{
						for (int i1 = 0; i1 < aentity.length; ++i1)
						{
							entity1 = aentity[i1];

							if (entity1 != p_76588_1_ && entity1.boundingBox.intersectsWith(p_76588_2_) && (p_76588_4_ == null || p_76588_4_.isEntityApplicable(entity1)))
							{
								p_76588_3_.add(entity1);
							}
						}
					}
				}
			}
		}
	}

	public void getEntitiesOfTypeWithinAAAB(Class p_76618_1_, AxisAlignedBB p_76618_2_, List p_76618_3_, IEntitySelector p_76618_4_)
	{
		int i = MathHelper.floor_double((p_76618_2_.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
		int j = MathHelper.floor_double((p_76618_2_.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
		i = MathHelper.clamp_int(i, 0, this.entityLists.length - 1);
		j = MathHelper.clamp_int(j, 0, this.entityLists.length - 1);

		for (int k = i; k <= j; ++k)
		{
			List list1 = this.entityLists[k];

			for (int l = 0; l < list1.size(); ++l)
			{
				Entity entity = (Entity)list1.get(l);

				if (!entity.isDead)
				if (p_76618_1_.isAssignableFrom(entity.getClass()) && entity.boundingBox.intersectsWith(p_76618_2_) && (p_76618_4_ == null || p_76618_4_.isEntityApplicable(entity)))
				{
					p_76618_3_.add(entity);
				}
			}
		}
	}

	public boolean needsSaving(boolean p_76601_1_)
	{
		if (p_76601_1_)
		{
			return shouldSaveOnUnload();
		}
		else if (wasActive && this.hasEntities && this.worldObj.getTotalWorldTime() >= this.lastSaveTime + 600L)
		{
			return true;
		}

		return this.isModified;
	}

	public Random getRandomWithSeed(long p_76617_1_)
	{
		return new Random(this.worldObj.getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ p_76617_1_);
	}

	public boolean isEmpty()
	{
		return false;
	}

	public void populateChunk(IChunkProvider p_76624_1_, IChunkProvider p_76624_2_, int p_76624_3_, int p_76624_4_)
	{
		((ChunkProviderServer)p_76624_1_).populateChunk(this, p_76624_3_, p_76624_4_);
	}

	public int getPrecipitationHeight(int p_76626_1_, int p_76626_2_)
	{
		int k = p_76626_1_ | p_76626_2_ << 4;
		int l = this.precipitationHeightMap[k];

		if (l == -999)
		{
			int i1 = this.getTopFilledSegment() + 15;
			l = -1;

			while (i1 > 0 && l == -1)
			{
				Block block = this.getBlock(p_76626_1_, i1, p_76626_2_);
				Material material = block.getMaterial();

				if (!material.blocksMovement() && !material.isLiquid())
				{
					--i1;
				}
				else
				{
					l = i1 + 1;
				}
			}

			this.precipitationHeightMap[k] = l;
		}

		return l;
	}

	public void func_150804_b(boolean p_150804_1_)
	{
		if (this.isGapLightingUpdated && !this.worldObj.provider.hasNoSky && !p_150804_1_)
		{
			this.recheckGaps(true);//this.worldObj.isRemote);
		}

		this.field_150815_m = true;

		if (!this.isLightPopulated && this.isTerrainPopulated)
		{
			this.func_150809_p();
		}
	}

	public boolean func_150802_k()
	{
		return this.field_150815_m && this.isTerrainPopulated && this.isLightPopulated;
	}

	public ChunkCoordIntPair getChunkCoordIntPair()
	{
		return new ChunkCoordIntPair(this.xPosition, this.zPosition);
	}

	public boolean getAreLevelsEmpty(int p_76606_1_, int p_76606_2_)
	{
		if (p_76606_1_ < 0)
		{
			p_76606_1_ = 0;
		}

		if (p_76606_2_ >= 256)
		{
			p_76606_2_ = 255;
		}

		for (int k = p_76606_1_; k <= p_76606_2_; k += 16)
		{
			ExtendedBlockStorage extendedblockstorage = this.storageArrays[k >> 4];

			if (extendedblockstorage != null && !extendedblockstorage.isEmpty())
			{
				return false;
			}
		}

		return true;
	}

	public void setStorageArrays(ExtendedBlockStorage[] p_76602_1_)
	{
		this.storageArrays = p_76602_1_;
	}

	@SideOnly(Side.CLIENT)
	public void fillChunk(byte[] p_76607_1_, int p_76607_2_, int p_76607_3_, boolean p_76607_4_)
	{
		Iterator iterator = chunkTileEntityMap.values().iterator();
		while(iterator.hasNext())
		{
			TileEntity tileEntity = (TileEntity)iterator.next();
			tileEntity.updateContainingBlockInfo();
			tileEntity.getBlockMetadata();
			tileEntity.getBlockType();
		}

		int k = 0;
		boolean flag1 = !this.worldObj.provider.hasNoSky;
		int l;

		for (l = 0; l < this.storageArrays.length; ++l)
		{
			if ((p_76607_2_ & 1 << l) != 0)
			{
				if (this.storageArrays[l] == null)
				{
					this.storageArrays[l] = new ExtendedBlockStorage(l << 4, flag1, false);
					if(!flag1)
						this.storageArrays[l].getSlot().zerofillSkylight();
				}

				this.storageArrays[l].getSlot().setLSB(p_76607_1_, k);
				k += 4096;
			}
			else if (p_76607_4_ && this.storageArrays[l] != null)
			{
				this.storageArrays[l] = null;
				this.storageArrays[l].release();
			}
		}

		NibbleArray nibblearray;

		for (l = 0; l < this.storageArrays.length; ++l)
		{
			if ((p_76607_2_ & 1 << l) != 0 && this.storageArrays[l] != null)
			{
				this.storageArrays[l].getSlot().setBlockMetadata(p_76607_1_, k);
				k += 2048;
			}
		}

		for (l = 0; l < this.storageArrays.length; ++l)
		{
			if ((p_76607_2_ & 1 << l) != 0 && this.storageArrays[l] != null)
			{
				this.storageArrays[l].getSlot().setBlocklight(p_76607_1_, k);
				k += 2048;
			}
		}

		if (flag1)
		{
			for (l = 0; l < this.storageArrays.length; ++l)
			{
				if ((p_76607_2_ & 1 << l) != 0 && this.storageArrays[l] != null)
				{
					this.storageArrays[l].getSlot().setSkylight(p_76607_1_, k);
					k += 2048;
				}
			}
		}

		for (l = 0; l < this.storageArrays.length; ++l)
		{
			if ((p_76607_3_ & 1 << l) != 0)
			{
				if (this.storageArrays[l] == null)
				{
					k += 2048;
				}
				else
				{
					this.storageArrays[l].getSlot().setMSB(p_76607_1_, k);
					k += 2048;
				}
			}
			else if (p_76607_4_ && this.storageArrays[l] != null/* && this.storageArrays[l].getBlockMSBArray() != null*/)
			{
				this.storageArrays[l].clearMSBArray();
			}
		}

		if (p_76607_4_)
		{
			System.arraycopy(p_76607_1_, k, this.blockBiomeArray, 0, this.blockBiomeArray.length);
			int i1 = k + this.blockBiomeArray.length;
		}

		for (l = 0; l < this.storageArrays.length; ++l)
		{
			if (this.storageArrays[l] != null && (p_76607_2_ & 1 << l) != 0)
			{
				this.storageArrays[l].removeInvalidBlocks();
			}
		}

		this.isLightPopulated = true;
		this.isTerrainPopulated = true;
		this.generateHeightMap();
		List<TileEntity> invalidList = new ArrayList<TileEntity>();
		iterator = this.chunkTileEntityMap.values().iterator();

		while (iterator.hasNext())
		{
			TileEntity tileentity = (TileEntity)iterator.next();
			int x = tileentity.xCoord & 15;
			int y = tileentity.yCoord;
			int z = tileentity.zCoord & 15;
			Block block = tileentity.getBlockType();
			if ((block != getBlock(x, y, z) || tileentity.blockMetadata != this.getBlockMetadata(x, y, z)) && tileentity.shouldRefresh(block, getBlock(x, y, z), tileentity.blockMetadata, this.getBlockMetadata(x, y, z), worldObj, x, y, z))
			{
				invalidList.add(tileentity);
			}
			tileentity.updateContainingBlockInfo();
		}

		for (TileEntity te : invalidList)
		{
			te.invalidate();
		}
	}

	public BiomeGenBase getBiomeGenForWorldCoords(int p_76591_1_, int p_76591_2_, WorldChunkManager p_76591_3_)
	{
		int k = this.blockBiomeArray[p_76591_2_ << 4 | p_76591_1_] & 255;

		if (k == 255)
		{
			BiomeGenBase biomegenbase = p_76591_3_.getBiomeGenAt((this.xPosition << 4) + p_76591_1_, (this.zPosition << 4) + p_76591_2_);
			k = biomegenbase.biomeID;
			this.blockBiomeArray[p_76591_2_ << 4 | p_76591_1_] = (byte)(k & 255);
		}

		return BiomeGenBase.getBiome(k) == null ? BiomeGenBase.plains : BiomeGenBase.getBiome(k);
	}

	public byte[] getBiomeArray()
	{
		return this.blockBiomeArray;
	}

	public void setBiomeArray(byte[] p_76616_1_)
	{
		this.blockBiomeArray = p_76616_1_;
	}

	public void resetRelightChecks()
	{
		this.queuedLightChecks = 0;
	}

	public void enqueueRelightChecks()
	{
		for (int i = 0; i < 8; ++i)
		{
			if (this.queuedLightChecks >= 4096)
			{
				return;
			}

			int j = this.queuedLightChecks % 16;
			int k = this.queuedLightChecks / 16 % 16;
			int l = this.queuedLightChecks / 256;
			++this.queuedLightChecks;
			int i1 = (this.xPosition << 4) + k;
			int j1 = (this.zPosition << 4) + l;

			for (int k1 = 0; k1 < 16; ++k1)
			{
				int l1 = (j << 4) + k1;

				if (this.storageArrays[j] == null && (k1 == 0 || k1 == 15 || k == 0 || k == 15 || l == 0 || l == 15) || this.storageArrays[j] != null && this.storageArrays[j].getBlockByExtId(k, k1, l).getMaterial() == Material.air)
				{
					if (this.worldObj.getBlock(i1, l1 - 1, j1).getLightValue() > 0)
					{
						this.worldObj.func_147451_t(i1, l1 - 1, j1);
					}

					if (this.worldObj.getBlock(i1, l1 + 1, j1).getLightValue() > 0)
					{
						this.worldObj.func_147451_t(i1, l1 + 1, j1);
					}

					if (this.worldObj.getBlock(i1 - 1, l1, j1).getLightValue() > 0)
					{
						this.worldObj.func_147451_t(i1 - 1, l1, j1);
					}

					if (this.worldObj.getBlock(i1 + 1, l1, j1).getLightValue() > 0)
					{
						this.worldObj.func_147451_t(i1 + 1, l1, j1);
					}

					if (this.worldObj.getBlock(i1, l1, j1 - 1).getLightValue() > 0)
					{
						this.worldObj.func_147451_t(i1, l1, j1 - 1);
					}

					if (this.worldObj.getBlock(i1, l1, j1 + 1).getLightValue() > 0)
					{
						this.worldObj.func_147451_t(i1, l1, j1 + 1);
					}

					this.worldObj.func_147451_t(i1, l1, j1);
				}
			}
		}
	}

	public void func_150809_p()
	{
		this.isTerrainPopulated = true;
		this.isLightPopulated = true;

		if (!this.worldObj.provider.hasNoSky)
		{
			if (this.worldObj.chunkRoundExists(xPosition, zPosition, 1))
			{
				for (int i = 0; i < 16; ++i)
				{
					for (int j = 0; j < 16; ++j)
					{
						if (!this.func_150811_f(i, j))
						{
							this.isLightPopulated = false;
							break;
						}
					}
				}

				if (this.isLightPopulated)
				{
					Chunk chunk = this.worldObj.getChunkFromBlockCoords(this.xPosition * 16 - 1, this.zPosition * 16);
					chunk.func_150801_a(3);
					chunk = this.worldObj.getChunkFromBlockCoords(this.xPosition * 16 + 16, this.zPosition * 16);
					chunk.func_150801_a(1);
					chunk = this.worldObj.getChunkFromBlockCoords(this.xPosition * 16, this.zPosition * 16 - 1);
					chunk.func_150801_a(0);
					chunk = this.worldObj.getChunkFromBlockCoords(this.xPosition * 16, this.zPosition * 16 + 16);
					chunk.func_150801_a(2);
				}
			}
			else
			{
				this.isLightPopulated = false;
			}
		}
	}

	private void func_150801_a(int p_150801_1_)
	{
		if (this.isTerrainPopulated)
		{
			int j;

			if (p_150801_1_ == 3)
			{
				for (j = 0; j < 16; ++j)
				{
					this.func_150811_f(15, j);
				}
			}
			else if (p_150801_1_ == 1)
			{
				for (j = 0; j < 16; ++j)
				{
					this.func_150811_f(0, j);
				}
			}
			else if (p_150801_1_ == 0)
			{
				for (j = 0; j < 16; ++j)
				{
					this.func_150811_f(j, 15);
				}
			}
			else if (p_150801_1_ == 2)
			{
				for (j = 0; j < 16; ++j)
				{
					this.func_150811_f(j, 0);
				}
			}
		}
	}

	private boolean func_150811_f(int p_150811_1_, int p_150811_2_)
	{
		int k = this.getTopFilledSegment();
		boolean flag = false;
		boolean flag1 = false;
		int l;

		for (l = k + 16 - 1; l > 63 || l > 0 && !flag1; --l)
		{
			int i1 = this.func_150808_b(p_150811_1_, l, p_150811_2_);

			if (i1 == 255 && l < 63)
			{
				flag1 = true;
			}

			if (!flag && i1 > 0)
			{
				flag = true;
			}
			else if (flag && i1 == 0 && !this.worldObj.func_147451_t(this.xPosition * 16 + p_150811_1_, l, this.zPosition * 16 + p_150811_2_))
			{
				return false;
			}
		}

		for (; l > 0; --l)
		{
			if (this.getBlock(p_150811_1_, l, p_150811_2_).getLightValue() > 0)
			{
				this.worldObj.func_147451_t(this.xPosition * 16 + p_150811_1_, l, this.zPosition * 16 + p_150811_2_);
			}
		}

		return true;
	}

	/**
	 * Retrieves the tile entity, WITHOUT creating it.
	 * Good for checking if it exists.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @return The tile entity at the specified location, if it exists and is valid.
	 */
	public TileEntity getTileEntityUnsafe(int x, int y, int z)
	{
		short hash = ChunkHash.chunkCoordToHash(x, y, z);
		TileEntity tileentity = fastTileEntityMap.get(hash);

		if (tileentity != null && tileentity.isInvalid())
		{
			chunkTileEntityMap.remove(new ChunkPosition(x, y, z));
			fastTileEntityMap.remove(hash);
			tileentity = null;
		}

		return tileentity;
	}

	/**
	 * Removes the tile entity at the specified position, only if it's
	 * marked as invalid.
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public void removeInvalidTileEntity(int x, int y, int z)
	{
		short hash = ChunkHash.chunkCoordToHash(x, y, z);
		if (isChunkLoaded)
		{
			TileEntity entity = fastTileEntityMap.get(hash);
			if (entity != null && entity.isInvalid())
			{
				chunkTileEntityMap.remove(new ChunkPosition(x, y, z));
				fastTileEntityMap.remove(hash);
			}
		}
	}
	
	/*======================================== ULTRAMINE START =====================================*/

	private static final WeakObjectPool<ShortObjMap<PendingBlockUpdate>> shortObjMapPool = new WeakObjectPool<>(LambdaHolder.newShortObjMap());
	private static final WeakObjectPool<TreeSet<PendingBlockUpdate>> treeSetPool = new WeakObjectPool<>(LambdaHolder.newTreeSet());

	private final ShortObjMap<TileEntity> fastTileEntityMap = HashShortObjMaps.newMutableMap();
	private final TByteSet updateLightCoords = new TByteHashSet();
	
	private ShortObjMap<PendingBlockUpdate> pendingUpdatesSet;
	private TreeSet<PendingBlockUpdate> pendingUpdatesQueue;
	
	private ChunkBindState bindState = ChunkBindState.NONE;
	private List<IChunkDependency> dependencies = new ArrayList<IChunkDependency>(2);
	private int loadTime;
	private int unbindTime;
	private boolean wasActive;

	private short entityLivingCount;
	private short entityMonsterCount;
	private short entityAnimalCount;
	private short entityAmbientCount;
	private short entityWaterCount;
	private short entityItemCount;
	private short entityXPOrbCount;

	private void releasePendingUpdatesSets()
	{
		if(pendingUpdatesQueue != null)
		{
			if(!pendingUpdatesSet.isEmpty())
				pendingUpdatesSet.clear();
			if(!pendingUpdatesQueue.isEmpty())
				pendingUpdatesQueue.clear();

			shortObjMapPool.release(pendingUpdatesSet);
			treeSetPool.release(pendingUpdatesQueue);

			pendingUpdatesSet = null;
			pendingUpdatesQueue = null;
		}
	}

	private void convertTileEntityMap()
	{
		fastTileEntityMap.clear();
		@SuppressWarnings("unchecked")
		Set<Map.Entry<ChunkPosition, TileEntity>> set = (Set<Map.Entry<ChunkPosition, TileEntity>>)chunkTileEntityMap.entrySet();
		for(Map.Entry<ChunkPosition, TileEntity> ent : set)
		{
			ChunkPosition coord = ent.getKey();
			fastTileEntityMap.put(ChunkHash.chunkCoordToHash(coord.chunkPosX, coord.chunkPosY, coord.chunkPosZ), ent.getValue());
		}
	}
	
	public PendingBlockUpdate pollPending(long time)
	{
		if(pendingUpdatesQueue == null || pendingUpdatesQueue.size() == 0) return null;
		
		PendingBlockUpdate p = pendingUpdatesQueue.first();
		if(p.scheduledTime <= time)
		{
			pendingUpdatesSet.remove(p.getChunkCoordHash());
			pendingUpdatesQueue.remove(p);
			
			if(pendingUpdatesQueue.isEmpty())
				releasePendingUpdatesSets();

			isModified = true;
			return p;
		}
		
		return null;
	}
	
	public void scheduleBlockUpdate(PendingBlockUpdate p, boolean check)
	{
		if(pendingUpdatesQueue == null)
		{
			pendingUpdatesSet = shortObjMapPool.getOrCreateInstance();
			pendingUpdatesQueue = treeSetPool.getOrCreateInstance();
		}

		PendingBlockUpdate prev = null;
		if(!check || (prev = pendingUpdatesSet.get(p.getChunkCoordHash())) == null || !Block.isEqualTo(p.getBlock(), prev.getBlock()))
		{
			if(prev != null)
				pendingUpdatesQueue.remove(prev);
			pendingUpdatesSet.put(p.getChunkCoordHash(), p);
			pendingUpdatesQueue.add(p);
			isModified = true;
		}
	}
	
	public Set<PendingBlockUpdate> getPendingUpdatesForSave()
	{
		return pendingUpdatesQueue;
	}

	public int getPendingUpdatesCount()
	{
		return pendingUpdatesSet == null ? 0 : pendingUpdatesSet.size();
	}

	public long getFirstPendingUpdateTime()
	{
		return pendingUpdatesQueue == null ? 0 : pendingUpdatesQueue.first().scheduledTime;
	}

	public ChunkBindState getBindState()
	{
		return bindState;
	}
	
	public void setBindState(ChunkBindState bindState)
	{
		this.bindState = bindState;
	}
	
	public void addDependency(IChunkDependency dep)
	{
		if(!dependencies.contains(dep) && dep != this && dep != null)
			dependencies.add(dep);
	}
	
	public boolean isDependent()
	{
		boolean isdependent = false;
		
		for(int i = 0; i < dependencies.size(); i++)
		{
			IChunkDependency dep = dependencies.get(i);
			if(dep.isDependent(this))
			{
				isdependent = true; //Не возвращаем значение сразу, проходимся по остальным, удаляем лишнее
			}
			else
			{
				dependencies.remove(i--);
				updateUnbindTime();
			}
					
		}
		
		return isdependent;
	}
	
	public boolean canUnload()
	{
		return bindState.canUnload() && !isDependent();
	}
	
	@Override
	public boolean isDependent(Chunk chunk)
	{
		return !bindState.canUnload();
	}
	
	public void unbind()
	{
		if(bindState.canChangeState())
			bindState = ChunkBindState.NONE;
		updateUnbindTime();
	}
	
	public void updateUnbindTime()
	{
		unbindTime = ((WorldServer)worldObj).func_73046_m().getTickCounter();
	}
	
	public int getLoadTime()
	{
		return loadTime;
	}
	
	public int getUnbindTime()
	{
		return unbindTime;
	}
	
	public void setActive()
	{
		wasActive = true;
	}
	
	public void postSave()
	{
		wasActive = false;
	}
	
	public boolean shouldSaveOnUnload()
	{
		return isModified || wasActive && hasEntities;
	}
	
	private void onEntityAdd(Entity e)
	{
		isModified = true;
		if(e.isEntityLiving() && !e.isEntityPlayerMP())
			entityLivingCount++;
		
		switch(e.getEntityType())
		{
		case MONSTER:	++entityMonsterCount; break;
		case ANIMAL:	++entityAnimalCount; break;
		case AMBIENT:	++entityAmbientCount; break;
		case WATER:		++entityWaterCount; break;
		case ITEM:		++entityItemCount; break;
		case XP_ORB:	++entityXPOrbCount; break;
		default: break;
		}
	}
	
	private void onEntityRemove(Entity e)
	{
		isModified = true;
		if(e.isEntityLiving() && !e.isEntityPlayerMP())
			entityLivingCount--;
		
		switch(e.getEntityType())
		{
		case MONSTER:	--entityMonsterCount; break;
		case ANIMAL:	--entityAnimalCount; break;
		case AMBIENT:	--entityAmbientCount; break;
		case WATER:		--entityWaterCount; break;
		case ITEM:		--entityItemCount; break;
		case XP_ORB:	--entityXPOrbCount; break;
		default: break;
		}
	}
	
	private void resetEntityCounters()
	{
		entityLivingCount = 0;
		entityMonsterCount = 0;
		entityAnimalCount = 0;
		entityAmbientCount = 0;
		entityWaterCount = 0;
		entityItemCount = 0;
		entityXPOrbCount = 0;
	}
	
	public int getEntityCount()
	{
		return entityLivingCount;
	}
	
	public int getEntityCountByType(EnumCreatureType type)
	{
		switch(type)
		{
		case monster:
			return entityMonsterCount;
		case creature:
			return entityAnimalCount;
		case ambient:
			return entityAmbientCount;
		case waterCreature:
			return entityWaterCount;
		default:
			return 0;
		}
	}
	
	public int getEntityCountByType(EntityType type)
	{
		switch(type)
		{
		case MONSTER:	return entityMonsterCount;
		case ANIMAL:	return entityAnimalCount;
		case AMBIENT:	return entityAmbientCount;
		case WATER:		return entityWaterCount;
		case ITEM:		return entityItemCount;
		case XP_ORB:	return entityXPOrbCount;
		default: 		return 0;
		}
	}
	
	public int getEntityCountOfSameType(Entity e)
	{
		return getEntityCountByType(e.getEntityType());
	}
	
	public void release()
	{
		for(ExtendedBlockStorage exbs : storageArrays)
		{
			if(exbs != null)
				exbs.release();
		}
		releasePendingUpdatesSets();
	}
}

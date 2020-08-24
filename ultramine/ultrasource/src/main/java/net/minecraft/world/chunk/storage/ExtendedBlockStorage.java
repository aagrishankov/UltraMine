package net.minecraft.world.chunk.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.core.service.InjectService;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.NibbleArray;
import org.ultramine.server.chunk.alloc.ChunkAllocService;
import org.ultramine.server.chunk.alloc.MemSlot;

public class ExtendedBlockStorage
{
	@InjectService private static ChunkAllocService alloc;
	private int yBase;
	private int blockRefCount;
	private int tickRefCount;
	private volatile MemSlot slot; // volatile read is cheap on x86
	private static final String __OBFID = "CL_00000375";

	public ExtendedBlockStorage(int p_i1997_1_, boolean p_i1997_2_, boolean zerofill)
	{
		this.yBase = p_i1997_1_;
		this.slot = alloc.allocateSlot();
		if(zerofill)
			slot.zerofillAll();
	}
	
	public ExtendedBlockStorage(int p_i1997_1_, boolean p_i1997_2_)
	{
		this(p_i1997_1_, p_i1997_2_, true);
	}

	public ExtendedBlockStorage(MemSlot slot, int yBase, int blockRefCount, int tickRefCount)
	{
		this.slot = slot;
		this.yBase = yBase;
		this.blockRefCount = blockRefCount;
		this.tickRefCount = tickRefCount;
	}

	public Block getBlockByExtId(int p_150819_1_, int p_150819_2_, int p_150819_3_)
	{
		return Block.getBlockById(slot.getBlockId(p_150819_1_, p_150819_2_, p_150819_3_));
	}

	public void func_150818_a(int p_150818_1_, int p_150818_2_, int p_150818_3_, Block p_150818_4_)
	{
		Block block1 = Block.getBlockById(slot.getBlockId(p_150818_1_, p_150818_2_, p_150818_3_));

		if (block1 != Blocks.air)
		{
			--this.blockRefCount;

			if (block1.getTickRandomly())
			{
				--this.tickRefCount;
			}
		}

		if (p_150818_4_ != Blocks.air)
		{
			++this.blockRefCount;

			if (p_150818_4_.getTickRandomly())
			{
				++this.tickRefCount;
			}
		}

		int i1 = Block.getIdFromBlock(p_150818_4_);
		slot.setBlockId(p_150818_1_, p_150818_2_, p_150818_3_, i1);
	}

	public int getExtBlockMetadata(int p_76665_1_, int p_76665_2_, int p_76665_3_)
	{
		return slot.getMeta(p_76665_1_, p_76665_2_, p_76665_3_);
	}

	public void setExtBlockMetadata(int p_76654_1_, int p_76654_2_, int p_76654_3_, int p_76654_4_)
	{
		slot.setMeta(p_76654_1_, p_76654_2_, p_76654_3_, p_76654_4_);
	}

	public boolean isEmpty()
	{
		return this.blockRefCount == 0;
	}

	public boolean getNeedsRandomTick()
	{
		return this.tickRefCount > 0;
	}

	public int getYLocation()
	{
		return this.yBase;
	}

	public void setExtSkylightValue(int p_76657_1_, int p_76657_2_, int p_76657_3_, int p_76657_4_)
	{
		slot.setSkylight(p_76657_1_, p_76657_2_, p_76657_3_, p_76657_4_);
	}

	public int getExtSkylightValue(int p_76670_1_, int p_76670_2_, int p_76670_3_)
	{
		return slot.getSkylight(p_76670_1_, p_76670_2_, p_76670_3_);
	}

	public void setExtBlocklightValue(int p_76677_1_, int p_76677_2_, int p_76677_3_, int p_76677_4_)
	{
		slot.setBlocklight(p_76677_1_, p_76677_2_, p_76677_3_, p_76677_4_);
	}

	public int getExtBlocklightValue(int p_76674_1_, int p_76674_2_, int p_76674_3_)
	{
		return slot.getBlocklight(p_76674_1_, p_76674_2_, p_76674_3_);
	}

	public void removeInvalidBlocks()
	{
		this.blockRefCount = 0;
		this.tickRefCount = 0;

		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				for (int k = 0; k < 16; ++k)
				{
					// ultramine: replaced loop order from (x, y, z) to (y, z, x)
					Block block = this.getBlockByExtId(k, i, j);

					if (block != Blocks.air)
					{
						++this.blockRefCount;

						if (block.getTickRandomly())
						{
							++this.tickRefCount;
						}
					}
				}
			}
		}
	}

	@Deprecated
	public byte[] getBlockLSBArray()
	{
		logDeprecation();
		return slot.copyLSB();
	}

	@SideOnly(Side.CLIENT)
	public void clearMSBArray()
	{
		slot.zerofillMSB();
	}

	@Deprecated
	public NibbleArray getBlockMSBArray()
	{
		logDeprecation();
		return new NibbleArray(slot.copyMSB(), 4);
	}

	@Deprecated
	public NibbleArray getMetadataArray()
	{
		logDeprecation();
		return new NibbleArray(slot.copyBlockMetadata(), 4);
	}

	@Deprecated
	public NibbleArray getBlocklightArray()
	{
		logDeprecation();
		return new NibbleArray(slot.copyBlocklight(), 4);
	}

	@Deprecated
	public NibbleArray getSkylightArray()
	{
		logDeprecation();
		return new NibbleArray(slot.copySkylight(), 4);
	}

	@Deprecated
	public void setBlockLSBArray(byte[] p_76664_1_)
	{
		logDeprecation();
		slot.setLSB(p_76664_1_);
	}

	@Deprecated
	public void setBlockMSBArray(NibbleArray p_76673_1_)
	{
		logDeprecation();
		slot.setMSB(p_76673_1_.data);
	}

	@Deprecated
	public void setBlockMetadataArray(NibbleArray p_76668_1_)
	{
		logDeprecation();
		slot.setBlockMetadata(p_76668_1_.data);
	}

	@Deprecated
	public void setBlocklightArray(NibbleArray p_76659_1_)
	{
		logDeprecation();
		slot.setBlocklight(p_76659_1_.data);
	}

	@Deprecated
	public void setSkylightArray(NibbleArray p_76666_1_)
	{
		logDeprecation();
		slot.setSkylight(p_76666_1_.data);
	}

	@Deprecated
	@SideOnly(Side.CLIENT)
	public NibbleArray createBlockMSBArray()
	{
		logDeprecation();
		slot.zerofillMSB();
		return getBlockMSBArray();
	}

	private static final Logger log = LogManager.getLogger();
	
	private static void logDeprecation()
	{
		log.warn("Called deprecated method in ExtendedBlockStorage. It may have no effect intended by the modder or lead to performance issues", new Throwable());
	}
	
	public MemSlot getSlot()
	{
		return slot;
	}

	public ExtendedBlockStorage copy()
	{
		slot.getClass(); //NPE
		return new ExtendedBlockStorage(slot.copy(), yBase, blockRefCount, tickRefCount);
	}
	
	public void release()
	{
		MemSlot slotLocal = this.slot;
		this.slot = null;
		slotLocal.release();
	}

	public void incBlockRefCount()
	{
		blockRefCount++;
	}
}
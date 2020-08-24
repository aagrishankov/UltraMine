package net.minecraft.world.chunk.storage;

import java.io.IOException;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public interface IChunkLoader
{
	Chunk loadChunk(World p_75815_1_, int p_75815_2_, int p_75815_3_) throws IOException;

	void saveChunk(World p_75816_1_, Chunk p_75816_2_) throws MinecraftException, IOException;

	void saveExtraChunkData(World p_75819_1_, Chunk p_75819_2_);

	void chunkTick();

	void saveExtraData();
}
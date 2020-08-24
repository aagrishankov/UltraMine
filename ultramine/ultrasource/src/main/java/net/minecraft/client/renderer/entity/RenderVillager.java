package net.minecraft.client.renderer.entity;

import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderVillager extends RenderLiving
{
	private static final ResourceLocation villagerTextures = new ResourceLocation("textures/entity/villager/villager.png");
	private static final ResourceLocation farmerVillagerTextures = new ResourceLocation("textures/entity/villager/farmer.png");
	private static final ResourceLocation librarianVillagerTextures = new ResourceLocation("textures/entity/villager/librarian.png");
	private static final ResourceLocation priestVillagerTextures = new ResourceLocation("textures/entity/villager/priest.png");
	private static final ResourceLocation smithVillagerTextures = new ResourceLocation("textures/entity/villager/smith.png");
	private static final ResourceLocation butcherVillagerTextures = new ResourceLocation("textures/entity/villager/butcher.png");
	protected ModelVillager villagerModel;
	private static final String __OBFID = "CL_00001032";

	public RenderVillager()
	{
		super(new ModelVillager(0.0F), 0.5F);
		this.villagerModel = (ModelVillager)this.mainModel;
	}

	protected int shouldRenderPass(EntityVillager p_77032_1_, int p_77032_2_, float p_77032_3_)
	{
		return -1;
	}

	public void doRender(EntityVillager p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		super.doRender((EntityLiving)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}

	protected ResourceLocation getEntityTexture(EntityVillager p_110775_1_)
	{
		switch (p_110775_1_.getProfession())
		{
			case 0:
				return farmerVillagerTextures;
			case 1:
				return librarianVillagerTextures;
			case 2:
				return priestVillagerTextures;
			case 3:
				return smithVillagerTextures;
			case 4:
				return butcherVillagerTextures;
			default:
				return VillagerRegistry.getVillagerSkin(p_110775_1_.getProfession(), villagerTextures);
		}
	}

	protected void renderEquippedItems(EntityVillager p_77029_1_, float p_77029_2_)
	{
		super.renderEquippedItems(p_77029_1_, p_77029_2_);
	}

	protected void preRenderCallback(EntityVillager p_77041_1_, float p_77041_2_)
	{
		float f1 = 0.9375F;

		if (p_77041_1_.getGrowingAge() < 0)
		{
			f1 = (float)((double)f1 * 0.5D);
			this.shadowSize = 0.25F;
		}
		else
		{
			this.shadowSize = 0.5F;
		}

		GL11.glScalef(f1, f1, f1);
	}

	public void doRender(EntityLiving p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		this.doRender((EntityVillager)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}

	protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_)
	{
		this.preRenderCallback((EntityVillager)p_77041_1_, p_77041_2_);
	}

	protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_)
	{
		return this.shouldRenderPass((EntityVillager)p_77032_1_, p_77032_2_, p_77032_3_);
	}

	protected void renderEquippedItems(EntityLivingBase p_77029_1_, float p_77029_2_)
	{
		this.renderEquippedItems((EntityVillager)p_77029_1_, p_77029_2_);
	}

	public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		this.doRender((EntityVillager)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}

	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return this.getEntityTexture((EntityVillager)p_110775_1_);
	}

	public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		this.doRender((EntityVillager)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}
}
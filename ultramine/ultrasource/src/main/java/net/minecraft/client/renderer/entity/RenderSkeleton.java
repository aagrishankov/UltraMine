package net.minecraft.client.renderer.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderSkeleton extends RenderBiped
{
	private static final ResourceLocation skeletonTextures = new ResourceLocation("textures/entity/skeleton/skeleton.png");
	private static final ResourceLocation witherSkeletonTextures = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
	private static final String __OBFID = "CL_00001023";

	public RenderSkeleton()
	{
		super(new ModelSkeleton(), 0.5F);
	}

	protected void preRenderCallback(EntitySkeleton p_77041_1_, float p_77041_2_)
	{
		if (p_77041_1_.getSkeletonType() == 1)
		{
			GL11.glScalef(1.2F, 1.2F, 1.2F);
		}
	}

	protected void func_82422_c()
	{
		GL11.glTranslatef(0.09375F, 0.1875F, 0.0F);
	}

	protected ResourceLocation getEntityTexture(EntitySkeleton p_110775_1_)
	{
		return p_110775_1_.getSkeletonType() == 1 ? witherSkeletonTextures : skeletonTextures;
	}

	protected ResourceLocation getEntityTexture(EntityLiving p_110775_1_)
	{
		return this.getEntityTexture((EntitySkeleton)p_110775_1_);
	}

	protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_)
	{
		this.preRenderCallback((EntitySkeleton)p_77041_1_, p_77041_2_);
	}

	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return this.getEntityTexture((EntitySkeleton)p_110775_1_);
	}
}
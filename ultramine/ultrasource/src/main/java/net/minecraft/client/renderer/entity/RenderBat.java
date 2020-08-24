package net.minecraft.client.renderer.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBat extends RenderLiving
{
	private static final ResourceLocation batTextures = new ResourceLocation("textures/entity/bat.png");
	private int renderedBatSize;
	private static final String __OBFID = "CL_00000979";

	public RenderBat()
	{
		super(new ModelBat(), 0.25F);
		this.renderedBatSize = ((ModelBat)this.mainModel).getBatSize();
	}

	public void doRender(EntityBat p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		int i = ((ModelBat)this.mainModel).getBatSize();

		if (i != this.renderedBatSize)
		{
			this.renderedBatSize = i;
			this.mainModel = new ModelBat();
		}

		super.doRender((EntityLiving)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}

	protected ResourceLocation getEntityTexture(EntityBat p_110775_1_)
	{
		return batTextures;
	}

	protected void preRenderCallback(EntityBat p_77041_1_, float p_77041_2_)
	{
		GL11.glScalef(0.35F, 0.35F, 0.35F);
	}

	protected void renderLivingAt(EntityBat p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_)
	{
		super.renderLivingAt(p_77039_1_, p_77039_2_, p_77039_4_, p_77039_6_);
	}

	protected void rotateCorpse(EntityBat p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_)
	{
		if (!p_77043_1_.getIsBatHanging())
		{
			GL11.glTranslatef(0.0F, MathHelper.cos(p_77043_2_ * 0.3F) * 0.1F, 0.0F);
		}
		else
		{
			GL11.glTranslatef(0.0F, -0.1F, 0.0F);
		}

		super.rotateCorpse(p_77043_1_, p_77043_2_, p_77043_3_, p_77043_4_);
	}

	public void doRender(EntityLiving p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		this.doRender((EntityBat)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}

	protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_)
	{
		this.preRenderCallback((EntityBat)p_77041_1_, p_77041_2_);
	}

	protected void rotateCorpse(EntityLivingBase p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_)
	{
		this.rotateCorpse((EntityBat)p_77043_1_, p_77043_2_, p_77043_3_, p_77043_4_);
	}

	protected void renderLivingAt(EntityLivingBase p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_)
	{
		this.renderLivingAt((EntityBat)p_77039_1_, p_77039_2_, p_77039_4_, p_77039_6_);
	}

	public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		this.doRender((EntityBat)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}

	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return this.getEntityTexture((EntityBat)p_110775_1_);
	}

	public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		this.doRender((EntityBat)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}
}
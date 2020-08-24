package net.minecraft.client.renderer.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderPig extends RenderLiving
{
	private static final ResourceLocation saddledPigTextures = new ResourceLocation("textures/entity/pig/pig_saddle.png");
	private static final ResourceLocation pigTextures = new ResourceLocation("textures/entity/pig/pig.png");
	private static final String __OBFID = "CL_00001019";

	public RenderPig(ModelBase p_i1265_1_, ModelBase p_i1265_2_, float p_i1265_3_)
	{
		super(p_i1265_1_, p_i1265_3_);
		this.setRenderPassModel(p_i1265_2_);
	}

	protected int shouldRenderPass(EntityPig p_77032_1_, int p_77032_2_, float p_77032_3_)
	{
		if (p_77032_2_ == 0 && p_77032_1_.getSaddled())
		{
			this.bindTexture(saddledPigTextures);
			return 1;
		}
		else
		{
			return -1;
		}
	}

	protected ResourceLocation getEntityTexture(EntityPig p_110775_1_)
	{
		return pigTextures;
	}

	protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_)
	{
		return this.shouldRenderPass((EntityPig)p_77032_1_, p_77032_2_, p_77032_3_);
	}

	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return this.getEntityTexture((EntityPig)p_110775_1_);
	}
}
package net.minecraft.client.renderer.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderGiantZombie extends RenderLiving
{
	private static final ResourceLocation zombieTextures = new ResourceLocation("textures/entity/zombie/zombie.png");
	private float scale;
	private static final String __OBFID = "CL_00000998";

	public RenderGiantZombie(ModelBase p_i1255_1_, float p_i1255_2_, float p_i1255_3_)
	{
		super(p_i1255_1_, p_i1255_2_ * p_i1255_3_);
		this.scale = p_i1255_3_;
	}

	protected void preRenderCallback(EntityGiantZombie p_77041_1_, float p_77041_2_)
	{
		GL11.glScalef(this.scale, this.scale, this.scale);
	}

	protected ResourceLocation getEntityTexture(EntityGiantZombie p_110775_1_)
	{
		return zombieTextures;
	}

	protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_)
	{
		this.preRenderCallback((EntityGiantZombie)p_77041_1_, p_77041_2_);
	}

	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return this.getEntityTexture((EntityGiantZombie)p_110775_1_);
	}
}
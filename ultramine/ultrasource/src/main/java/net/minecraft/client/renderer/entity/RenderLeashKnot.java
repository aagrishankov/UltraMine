package net.minecraft.client.renderer.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelLeashKnot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderLeashKnot extends Render
{
	private static final ResourceLocation leashKnotTextures = new ResourceLocation("textures/entity/lead_knot.png");
	private ModelLeashKnot leashKnotModel = new ModelLeashKnot();
	private static final String __OBFID = "CL_00001010";

	public void doRender(EntityLeashKnot p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glTranslatef((float)p_76986_2_, (float)p_76986_4_, (float)p_76986_6_);
		float f2 = 0.0625F;
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glScalef(-1.0F, -1.0F, 1.0F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		this.bindEntityTexture(p_76986_1_);
		this.leashKnotModel.render(p_76986_1_, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f2);
		GL11.glPopMatrix();
	}

	protected ResourceLocation getEntityTexture(EntityLeashKnot p_110775_1_)
	{
		return leashKnotTextures;
	}

	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return this.getEntityTexture((EntityLeashKnot)p_110775_1_);
	}

	public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		this.doRender((EntityLeashKnot)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
	}
}
package net.minecraft.client.entity;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntityOtherPlayerMP extends AbstractClientPlayer
{
	private boolean isItemInUse;
	private int otherPlayerMPPosRotationIncrements;
	private double otherPlayerMPX;
	private double otherPlayerMPY;
	private double otherPlayerMPZ;
	private double otherPlayerMPYaw;
	private double otherPlayerMPPitch;
	private static final String __OBFID = "CL_00000939";

	public EntityOtherPlayerMP(World p_i45075_1_, GameProfile p_i45075_2_)
	{
		super(p_i45075_1_, p_i45075_2_);
		this.yOffset = 0.0F;
		this.stepHeight = 0.0F;
		this.noClip = true;
		this.field_71082_cx = 0.25F;
		this.renderDistanceWeight = 10.0D;
	}

	protected void resetHeight()
	{
		this.yOffset = 0.0F;
	}

	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		return true;
	}

	public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_)
	{
		this.otherPlayerMPX = p_70056_1_;
		this.otherPlayerMPY = p_70056_3_;
		this.otherPlayerMPZ = p_70056_5_;
		this.otherPlayerMPYaw = (double)p_70056_7_;
		this.otherPlayerMPPitch = (double)p_70056_8_;
		this.otherPlayerMPPosRotationIncrements = p_70056_9_;
	}

	public void onUpdate()
	{
		this.field_71082_cx = 0.0F;
		super.onUpdate();
		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d0 = this.posX - this.prevPosX;
		double d1 = this.posZ - this.prevPosZ;
		float f = MathHelper.sqrt_double(d0 * d0 + d1 * d1) * 4.0F;

		if (f > 1.0F)
		{
			f = 1.0F;
		}

		this.limbSwingAmount += (f - this.limbSwingAmount) * 0.4F;
		this.limbSwing += this.limbSwingAmount;

		if (!this.isItemInUse && this.isEating() && this.inventory.mainInventory[this.inventory.currentItem] != null)
		{
			ItemStack itemstack = this.inventory.mainInventory[this.inventory.currentItem];
			this.setItemInUse(this.inventory.mainInventory[this.inventory.currentItem], itemstack.getItem().getMaxItemUseDuration(itemstack));
			this.isItemInUse = true;
		}
		else if (this.isItemInUse && !this.isEating())
		{
			this.clearItemInUse();
			this.isItemInUse = false;
		}
	}

	public float getShadowSize()
	{
		return 0.0F;
	}

	public void onLivingUpdate()
	{
		super.updateEntityActionState();

		if (this.otherPlayerMPPosRotationIncrements > 0)
		{
			double d0 = this.posX + (this.otherPlayerMPX - this.posX) / (double)this.otherPlayerMPPosRotationIncrements;
			double d1 = this.posY + (this.otherPlayerMPY - this.posY) / (double)this.otherPlayerMPPosRotationIncrements;
			double d2 = this.posZ + (this.otherPlayerMPZ - this.posZ) / (double)this.otherPlayerMPPosRotationIncrements;
			double d3;

			for (d3 = this.otherPlayerMPYaw - (double)this.rotationYaw; d3 < -180.0D; d3 += 360.0D)
			{
				;
			}

			while (d3 >= 180.0D)
			{
				d3 -= 360.0D;
			}

			this.rotationYaw = (float)((double)this.rotationYaw + d3 / (double)this.otherPlayerMPPosRotationIncrements);
			this.rotationPitch = (float)((double)this.rotationPitch + (this.otherPlayerMPPitch - (double)this.rotationPitch) / (double)this.otherPlayerMPPosRotationIncrements);
			--this.otherPlayerMPPosRotationIncrements;
			this.setPosition(d0, d1, d2);
			this.setRotation(this.rotationYaw, this.rotationPitch);
		}

		this.prevCameraYaw = this.cameraYaw;
		float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		float f = (float)Math.atan(-this.motionY * 0.20000000298023224D) * 15.0F;

		if (f1 > 0.1F)
		{
			f1 = 0.1F;
		}

		if (!this.onGround || this.getHealth() <= 0.0F)
		{
			f1 = 0.0F;
		}

		if (this.onGround || this.getHealth() <= 0.0F)
		{
			f = 0.0F;
		}

		this.cameraYaw += (f1 - this.cameraYaw) * 0.4F;
		this.cameraPitch += (f - this.cameraPitch) * 0.8F;
	}

	public void setCurrentItemOrArmor(int p_70062_1_, ItemStack p_70062_2_)
	{
		if (p_70062_1_ == 0)
		{
			this.inventory.mainInventory[this.inventory.currentItem] = p_70062_2_;
		}
		else
		{
			this.inventory.armorInventory[p_70062_1_ - 1] = p_70062_2_;
		}
	}

	@Override
	public float getDefaultEyeHeight()
	{
		return 1.82F;
	}

	public void addChatMessage(IChatComponent p_145747_1_)
	{
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(p_145747_1_);
	}

	public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_)
	{
		return false;
	}

	public ChunkCoordinates getPlayerCoordinates()
	{
		return new ChunkCoordinates(MathHelper.floor_double(this.posX + 0.5D), MathHelper.floor_double(this.posY + 0.5D), MathHelper.floor_double(this.posZ + 0.5D));
	}
}
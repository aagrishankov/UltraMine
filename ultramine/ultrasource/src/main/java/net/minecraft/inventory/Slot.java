package net.minecraft.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class Slot
{
	private final int slotIndex;
	public final IInventory inventory;
	public int slotNumber;
	public int xDisplayPosition;
	public int yDisplayPosition;
	private static final String __OBFID = "CL_00001762";

	/** Position within background texture file, normally -1 which causes no background to be drawn. */
	protected IIcon backgroundIcon = null;

	/** Background texture file assigned to this slot, if any. Vanilla "/gui/items.png" is used if this is null. */
	@SideOnly(Side.CLIENT)
	protected ResourceLocation texture;

	public Slot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_)
	{
		this.inventory = p_i1824_1_;
		this.slotIndex = p_i1824_2_;
		this.xDisplayPosition = p_i1824_3_;
		this.yDisplayPosition = p_i1824_4_;
	}

	public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_)
	{
		if (p_75220_1_ != null && p_75220_2_ != null)
		{
			if (p_75220_1_.getItem() == p_75220_2_.getItem())
			{
				int i = p_75220_2_.stackSize - p_75220_1_.stackSize;

				if (i > 0)
				{
					this.onCrafting(p_75220_1_, i);
				}
			}
		}
	}

	protected void onCrafting(ItemStack p_75210_1_, int p_75210_2_) {}

	protected void onCrafting(ItemStack p_75208_1_) {}

	public void onPickupFromSlot(EntityPlayer p_82870_1_, ItemStack p_82870_2_)
	{
		this.onSlotChanged();
	}

	public boolean isItemValid(ItemStack p_75214_1_)
	{
		return true;
	}

	public ItemStack getStack()
	{
		return this.inventory.getStackInSlot(this.slotIndex);
	}

	public boolean getHasStack()
	{
		return this.getStack() != null;
	}

	public void putStack(ItemStack p_75215_1_)
	{
		this.inventory.setInventorySlotContents(this.slotIndex, p_75215_1_);
		this.onSlotChanged();
	}

	public void onSlotChanged()
	{
		this.inventory.markDirty();
	}

	public int getSlotStackLimit()
	{
		return this.inventory.getInventoryStackLimit();
	}

	public ItemStack decrStackSize(int p_75209_1_)
	{
		return this.inventory.decrStackSize(this.slotIndex, p_75209_1_);
	}

	public boolean isSlotInInventory(IInventory p_75217_1_, int p_75217_2_)
	{
		return p_75217_1_ == this.inventory && p_75217_2_ == this.slotIndex;
	}

	public boolean canTakeStack(EntityPlayer p_82869_1_)
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getBackgroundIconIndex()
	{
		return backgroundIcon;
	}

	@SideOnly(Side.CLIENT)
	public boolean func_111238_b()
	{
		return true;
	}

	/*========================================= FORGE START =====================================*/
	/**
	 * Gets the path of the texture file to use for the background image of this slot when drawing the GUI.
	 * @return String: The texture file that will be used in GuiContainer.drawSlotInventory for the slot background.
	 */
	@SideOnly(Side.CLIENT)
	public ResourceLocation getBackgroundIconTexture()
	{
		return (texture == null ? TextureMap.locationItemsTexture : texture);
	}

	/**
	 * Sets which icon index to use as the background image of the slot when it's empty.
	 * @param icon The icon to use, null for none
	 */
	public void setBackgroundIcon(IIcon icon)
	{
		backgroundIcon = icon;
	}

	/**
	 * Sets the texture file to use for the background image of the slot when it's empty.
	 * @param textureFilename String: Path of texture file to use, or null to use "/gui/items.png"
	 */
	@SideOnly(Side.CLIENT)
	public void setBackgroundIconTexture(ResourceLocation texture)
	{
		this.texture = texture;
	}

	/**
	 * Retrieves the index in the inventory for this slot, this value should typically not
	 * be used, but can be useful for some occasions.
	 *
	 * @return Index in associated inventory for this slot.
	 */
	public int getSlotIndex()
	{
		return slotIndex;
	}
	/*========================================= FORGE END =====================================*/
}
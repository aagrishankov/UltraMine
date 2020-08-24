package net.minecraft.entity.ai.attributes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Collection;
import java.util.UUID;

public interface IAttributeInstance
{
	IAttribute getAttribute();

	double getBaseValue();

	void setBaseValue(double p_111128_1_);

	Collection func_111122_c();

	AttributeModifier getModifier(UUID p_111127_1_);

	void applyModifier(AttributeModifier p_111121_1_);

	void removeModifier(AttributeModifier p_111124_1_);

	@SideOnly(Side.CLIENT)
	void removeAllModifiers();

	double getAttributeValue();
}
package net.minecraft.enchantment;

import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;

public abstract class Enchantment
{
	public static final Enchantment[] enchantmentsList = new Enchantment[256];
	public static final Enchantment[] enchantmentsBookList;
	public static final Enchantment protection = new EnchantmentProtection(0, 10, 0);
	public static final Enchantment fireProtection = new EnchantmentProtection(1, 5, 1);
	public static final Enchantment featherFalling = new EnchantmentProtection(2, 5, 2);
	public static final Enchantment blastProtection = new EnchantmentProtection(3, 2, 3);
	public static final Enchantment projectileProtection = new EnchantmentProtection(4, 5, 4);
	public static final Enchantment respiration = new EnchantmentOxygen(5, 2);
	public static final Enchantment aquaAffinity = new EnchantmentWaterWorker(6, 2);
	public static final Enchantment thorns = new EnchantmentThorns(7, 1);
	public static final Enchantment sharpness = new EnchantmentDamage(16, 10, 0);
	public static final Enchantment smite = new EnchantmentDamage(17, 5, 1);
	public static final Enchantment baneOfArthropods = new EnchantmentDamage(18, 5, 2);
	public static final Enchantment knockback = new EnchantmentKnockback(19, 5);
	public static final Enchantment fireAspect = new EnchantmentFireAspect(20, 2);
	public static final Enchantment looting = new EnchantmentLootBonus(21, 2, EnumEnchantmentType.weapon);
	public static final Enchantment efficiency = new EnchantmentDigging(32, 10);
	public static final Enchantment silkTouch = new EnchantmentUntouching(33, 1);
	public static final Enchantment unbreaking = new EnchantmentDurability(34, 5);
	public static final Enchantment fortune = new EnchantmentLootBonus(35, 2, EnumEnchantmentType.digger);
	public static final Enchantment power = new EnchantmentArrowDamage(48, 10);
	public static final Enchantment punch = new EnchantmentArrowKnockback(49, 2);
	public static final Enchantment flame = new EnchantmentArrowFire(50, 2);
	public static final Enchantment infinity = new EnchantmentArrowInfinite(51, 1);
	public static final Enchantment field_151370_z = new EnchantmentLootBonus(61, 2, EnumEnchantmentType.fishing_rod);
	public static final Enchantment field_151369_A = new EnchantmentFishingSpeed(62, 2, EnumEnchantmentType.fishing_rod);
	public final int effectId;
	private final int weight;
	public EnumEnchantmentType type;
	protected String name;
	private static final String __OBFID = "CL_00000105";

	protected Enchantment(int p_i1926_1_, int p_i1926_2_, EnumEnchantmentType p_i1926_3_)
	{
		this.effectId = p_i1926_1_;
		this.weight = p_i1926_2_;
		this.type = p_i1926_3_;

		if (enchantmentsList[p_i1926_1_] != null)
		{
			throw new IllegalArgumentException("Duplicate enchantment id! " + this.getClass() + " and " + enchantmentsList[p_i1926_1_].getClass() + " Enchantment ID:" + p_i1926_1_);
		}
		else
		{
			enchantmentsList[p_i1926_1_] = this;
		}
	}

	public int getWeight()
	{
		return this.weight;
	}

	public int getMinLevel()
	{
		return 1;
	}

	public int getMaxLevel()
	{
		return 1;
	}

	public int getMinEnchantability(int p_77321_1_)
	{
		return 1 + p_77321_1_ * 10;
	}

	public int getMaxEnchantability(int p_77317_1_)
	{
		return this.getMinEnchantability(p_77317_1_) + 5;
	}

	public int calcModifierDamage(int p_77318_1_, DamageSource p_77318_2_)
	{
		return 0;
	}

	public float func_152376_a(int p_152376_1_, EnumCreatureAttribute p_152376_2_)
	{
		return 0.0F;
	}

	public boolean canApplyTogether(Enchantment p_77326_1_)
	{
		return this != p_77326_1_;
	}

	public Enchantment setName(String p_77322_1_)
	{
		this.name = p_77322_1_;
		return this;
	}

	public String getName()
	{
		return "enchantment." + this.name;
	}

	public String getTranslatedName(int p_77316_1_)
	{
		String s = StatCollector.translateToLocal(this.getName());
		return s + " " + StatCollector.translateToLocal("enchantment.level." + p_77316_1_);
	}

	public boolean canApply(ItemStack p_92089_1_)
	{
		return this.type.canEnchantItem(p_92089_1_.getItem());
	}

	public void func_151368_a(EntityLivingBase p_151368_1_, Entity p_151368_2_, int p_151368_3_) {}

	public void func_151367_b(EntityLivingBase p_151367_1_, Entity p_151367_2_, int p_151367_3_) {}

	/**
	 * This applies specifically to applying at the enchanting table. The other method {@link #canApply(ItemStack)}
	 * applies for <i>all possible</i> enchantments.
	 * @param stack
	 * @return
	 */
	public boolean canApplyAtEnchantingTable(ItemStack stack)
	{
		return canApply(stack);
	}

	private static final java.lang.reflect.Field bookSetter = Enchantment.class.getDeclaredFields()[1];
	/**
	 * Add to the list of enchantments applicable by the anvil from a book
	 *
	 * @param enchantment
	 */
	public static void addToBookList(Enchantment enchantment)
	{
		try
		{
			net.minecraftforge.common.util.EnumHelper.setFailsafeFieldValue(bookSetter, null,
				com.google.common.collect.ObjectArrays.concat(enchantmentsBookList, enchantment));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e); //Rethrow see what happens
		}
	}

	/**
	 * Is this enchantment allowed to be enchanted on books via Enchantment Table
	 * @return false to disable the vanilla feature
	 */
	public boolean isAllowedOnBooks()
	{
		return true;
	}

	static
	{
		ArrayList var0 = new ArrayList();
		Enchantment[] var1 = enchantmentsList;
		int var2 = var1.length;

		for (int var3 = 0; var3 < var2; ++var3)
		{
			Enchantment var4 = var1[var3];

			if (var4 != null)
			{
				var0.add(var4);
			}
		}

		enchantmentsBookList = (Enchantment[])var0.toArray(new Enchantment[0]);
	}
}
package org.ultramine.server.event;

import java.util.List;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.EntityEvent;

@Cancelable
public class EntityPotionApplyEffectEvent extends EntityEvent
{
	public final EntityPotion entityPotion;
	public final EntityLivingBase living;
	public List<PotionEffect> effects;

	public EntityPotionApplyEffectEvent(EntityPotion entity, EntityLivingBase living, List<PotionEffect> effects)
	{
		super(entity);
		this.entityPotion = entity;
		this.living = living;
		this.effects = effects;
	}
}

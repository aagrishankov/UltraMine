package net.minecraft.entity;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import org.ultramine.server.EntityType;
import org.ultramine.server.internal.UMHooks;
import org.ultramine.server.event.EntitySetFireEvent;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fluids.IFluidBlock;

public abstract class Entity
{
	private static int nextEntityID;
	private int entityId;
	public double renderDistanceWeight;
	public boolean preventEntitySpawning;
	public Entity riddenByEntity;
	public Entity ridingEntity;
	public boolean forceSpawn;
	public World worldObj;
	public double prevPosX;
	public double prevPosY;
	public double prevPosZ;
	public double posX;
	public double posY;
	public double posZ;
	public double motionX;
	public double motionY;
	public double motionZ;
	public float rotationYaw;
	public float rotationPitch;
	public float prevRotationYaw;
	public float prevRotationPitch;
	public final AxisAlignedBB boundingBox;
	public boolean onGround;
	public boolean isCollidedHorizontally;
	public boolean isCollidedVertically;
	public boolean isCollided;
	public boolean velocityChanged;
	protected boolean isInWeb;
	public boolean field_70135_K;
	public boolean isDead;
	public float yOffset;
	public float width;
	public float height;
	public float prevDistanceWalkedModified;
	public float distanceWalkedModified;
	public float distanceWalkedOnStepModified;
	public float fallDistance;
	private int nextStepDistance;
	public double lastTickPosX;
	public double lastTickPosY;
	public double lastTickPosZ;
	public float ySize;
	public float stepHeight;
	public boolean noClip;
	public float entityCollisionReduction;
	protected Random rand;
	public int ticksExisted;
	public int fireResistance;
	private int fire;
	protected boolean inWater;
	public int hurtResistantTime;
	private boolean firstUpdate;
	protected boolean isImmuneToFire;
	protected DataWatcher dataWatcher;
	private double entityRiderPitchDelta;
	private double entityRiderYawDelta;
	public boolean addedToChunk;
	public int chunkCoordX;
	public int chunkCoordY;
	public int chunkCoordZ;
	@SideOnly(Side.CLIENT)
	public int serverPosX;
	@SideOnly(Side.CLIENT)
	public int serverPosY;
	@SideOnly(Side.CLIENT)
	public int serverPosZ;
	public boolean ignoreFrustumCheck;
	public boolean isAirBorne;
	public int timeUntilPortal;
	protected boolean inPortal;
	protected int portalCounter;
	public int dimension;
	protected int teleportDirection;
	private boolean invulnerable;
	protected UUID entityUniqueID;
	public Entity.EnumEntitySize myEntitySize;
	private static final String __OBFID = "CL_00001533";
	/** Forge: Used to store custom data for each entity. */
	private NBTTagCompound customEntityData;
	public boolean captureDrops = false;
	public ArrayList<EntityItem> capturedDrops = new ArrayList<EntityItem>();
	private UUID persistentID;

	protected HashMap<String, IExtendedEntityProperties> extendedProperties;

	public int getEntityId()
	{
		return this.entityId;
	}

	public void setEntityId(int p_145769_1_)
	{
		this.entityId = p_145769_1_;
	}

	public Entity(World p_i1582_1_)
	{
		this.entityId = nextEntityID++;
		this.renderDistanceWeight = 1.0D;
		this.boundingBox = AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
		this.field_70135_K = true;
		this.width = 0.6F;
		this.height = 1.8F;
		this.nextStepDistance = 1;
		this.rand = new Random();
		this.fireResistance = 1;
		this.firstUpdate = true;
		this.entityUniqueID = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
		this.myEntitySize = Entity.EnumEntitySize.SIZE_2;
		this.worldObj = p_i1582_1_;
		this.setPosition(0.0D, 0.0D, 0.0D);

		if (p_i1582_1_ != null)
		{
			this.dimension = p_i1582_1_.provider.dimensionId;
		}

		this.dataWatcher = new DataWatcher(this);
		this.dataWatcher.addObject(0, Byte.valueOf((byte)0));
		this.dataWatcher.addObject(1, Short.valueOf((short)300));
		this.entityInit();

		extendedProperties = new HashMap<String, IExtendedEntityProperties>();

		MinecraftForge.EVENT_BUS.post(new EntityEvent.EntityConstructing(this));

		for (IExtendedEntityProperties props : this.extendedProperties.values())
		{
			props.init(this, p_i1582_1_);
		}
	}

	protected abstract void entityInit();

	public DataWatcher getDataWatcher()
	{
		return this.dataWatcher;
	}

	public boolean equals(Object p_equals_1_)
	{
		return p_equals_1_ instanceof Entity ? ((Entity)p_equals_1_).entityId == this.entityId : false;
	}

	public int hashCode()
	{
		return this.entityId;
	}

	@SideOnly(Side.CLIENT)
	protected void preparePlayerToSpawn()
	{
		if (this.worldObj != null)
		{
			while (this.posY > 0.0D)
			{
				this.setPosition(this.posX, this.posY, this.posZ);

				if (this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty())
				{
					break;
				}

				++this.posY;
			}

			this.motionX = this.motionY = this.motionZ = 0.0D;
			this.rotationPitch = 0.0F;
		}
	}

	public void setDead()
	{
		this.isDead = true;
	}

	protected void setSize(float p_70105_1_, float p_70105_2_)
	{
		float f2;

		if (p_70105_1_ != this.width || p_70105_2_ != this.height)
		{
			f2 = this.width;
			this.width = p_70105_1_;
			this.height = p_70105_2_;
			this.boundingBox.maxX = this.boundingBox.minX + (double)this.width;
			this.boundingBox.maxZ = this.boundingBox.minZ + (double)this.width;
			this.boundingBox.maxY = this.boundingBox.minY + (double)this.height;

			if (this.width > f2 && !this.firstUpdate && !this.worldObj.isRemote)
			{
				this.moveEntity((double)(f2 - this.width), 0.0D, (double)(f2 - this.width));
			}
		}

		f2 = p_70105_1_ % 2.0F;

		if ((double)f2 < 0.375D)
		{
			this.myEntitySize = Entity.EnumEntitySize.SIZE_1;
		}
		else if ((double)f2 < 0.75D)
		{
			this.myEntitySize = Entity.EnumEntitySize.SIZE_2;
		}
		else if ((double)f2 < 1.0D)
		{
			this.myEntitySize = Entity.EnumEntitySize.SIZE_3;
		}
		else if ((double)f2 < 1.375D)
		{
			this.myEntitySize = Entity.EnumEntitySize.SIZE_4;
		}
		else if ((double)f2 < 1.75D)
		{
			this.myEntitySize = Entity.EnumEntitySize.SIZE_5;
		}
		else
		{
			this.myEntitySize = Entity.EnumEntitySize.SIZE_6;
		}
	}

	protected void setRotation(float p_70101_1_, float p_70101_2_)
	{
		this.rotationYaw = p_70101_1_ % 360.0F;
		this.rotationPitch = p_70101_2_ % 360.0F;
	}

	public void setPosition(double p_70107_1_, double p_70107_3_, double p_70107_5_)
	{
		this.posX = p_70107_1_;
		this.posY = p_70107_3_;
		this.posZ = p_70107_5_;
		float f = this.width / 2.0F;
		float f1 = this.height;
		this.boundingBox.setBounds(p_70107_1_ - (double)f, p_70107_3_ - (double)this.yOffset + (double)this.ySize, p_70107_5_ - (double)f, p_70107_1_ + (double)f, p_70107_3_ - (double)this.yOffset + (double)this.ySize + (double)f1, p_70107_5_ + (double)f);
	}

	@SideOnly(Side.CLIENT)
	public void setAngles(float p_70082_1_, float p_70082_2_)
	{
		float f2 = this.rotationPitch;
		float f3 = this.rotationYaw;
		this.rotationYaw = (float)((double)this.rotationYaw + (double)p_70082_1_ * 0.15D);
		this.rotationPitch = (float)((double)this.rotationPitch - (double)p_70082_2_ * 0.15D);

		if (this.rotationPitch < -90.0F)
		{
			this.rotationPitch = -90.0F;
		}

		if (this.rotationPitch > 90.0F)
		{
			this.rotationPitch = 90.0F;
		}

		this.prevRotationPitch += this.rotationPitch - f2;
		this.prevRotationYaw += this.rotationYaw - f3;
	}

	public void onUpdate()
	{
		this.onEntityUpdate();
	}

	public void onEntityUpdate()
	{
		this.worldObj.theProfiler.startSection("entityBaseTick");

		if (this.ridingEntity != null && this.ridingEntity.isDead)
		{
			this.ridingEntity = null;
		}

		this.prevDistanceWalkedModified = this.distanceWalkedModified;
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		int i;

		if (!this.worldObj.isRemote && this.worldObj instanceof WorldServer)
		{
			this.worldObj.theProfiler.startSection("portal");
			MinecraftServer minecraftserver = ((WorldServer)this.worldObj).func_73046_m();
			i = this.getMaxInPortalTime();

			if (this.inPortal)
			{
				if (minecraftserver.getAllowNether())
				{
					if (this.ridingEntity == null && this.portalCounter++ >= i)
					{
						this.portalCounter = i;
						this.timeUntilPortal = this.getPortalCooldown();
						int dim = ((WorldServer)worldObj).getConfig().portals.netherLink;

						if(dim != Integer.MIN_VALUE)
							this.travelToDimension(dim);
					}

					this.inPortal = false;
				}
			}
			else
			{
				if (this.portalCounter > 0)
				{
					this.portalCounter -= 4;
				}

				if (this.portalCounter < 0)
				{
					this.portalCounter = 0;
				}
			}

			if (this.timeUntilPortal > 0)
			{
				--this.timeUntilPortal;
			}

			this.worldObj.theProfiler.endSection();
		}

		if (this.isSprinting() && !this.isInWater())
		{
			int j = MathHelper.floor_double(this.posX);
			i = MathHelper.floor_double(this.posY - 0.20000000298023224D - (double)this.yOffset);
			int k = MathHelper.floor_double(this.posZ);
			Block block = this.worldObj.getBlock(j, i, k);

			if (block.getMaterial() != Material.air)
			{
				this.worldObj.spawnParticle("blockcrack_" + Block.getIdFromBlock(block) + "_" + this.worldObj.getBlockMetadata(j, i, k), this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.boundingBox.minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D);
			}
		}

		this.handleWaterMovement();

		if (this.worldObj.isRemote)
		{
			this.fire = 0;
		}
		else if (this.fire > 0)
		{
			if (this.isImmuneToFire)
			{
				this.fire -= 4;

				if (this.fire < 0)
				{
					this.fire = 0;
				}
			}
			else
			{
				if (this.fire % 20 == 0)
				{
					this.attackEntityFrom(DamageSource.onFire, 1.0F);
				}

				--this.fire;
			}
		}

		if (this.handleLavaMovement())
		{
			this.setOnFireFromLava();
			this.fallDistance *= 0.5F;
		}

		if (this.posY < -64.0D)
		{
			this.kill();
		}

		if (!this.worldObj.isRemote)
		{
			this.setFlag(0, this.fire > 0);
		}

		this.firstUpdate = false;
		this.worldObj.theProfiler.endSection();
	}

	public int getMaxInPortalTime()
	{
		return 0;
	}

	protected void setOnFireFromLava()
	{
		if (!this.isImmuneToFire)
		{
			this.attackEntityFrom(DamageSource.lava, 4.0F);
			this.setFire(15);
		}
	}

	public void setFire(int p_70015_1_)
	{
		int j = p_70015_1_ * 20;
		EntitySetFireEvent event = new EntitySetFireEvent(this, j);
		if(MinecraftForge.EVENT_BUS.post(event)) return;
		j = event.fireTicks;
		j = EnchantmentProtection.getFireTimeForEntity(this, j);

		if (this.fire < j)
		{
			this.fire = j;
		}
	}

	public void extinguish()
	{
		this.fire = 0;
	}

	protected void kill()
	{
		this.setDead();
	}

	public boolean isOffsetPositionInLiquid(double p_70038_1_, double p_70038_3_, double p_70038_5_)
	{
		AxisAlignedBB axisalignedbb = this.boundingBox.getOffsetBoundingBox(p_70038_1_, p_70038_3_, p_70038_5_);
		List list = this.worldObj.getCollidingBoundingBoxes(this, axisalignedbb);
		return !list.isEmpty() ? false : !this.worldObj.isAnyLiquid(axisalignedbb);
	}

	public void moveEntity(double p_70091_1_, double p_70091_3_, double p_70091_5_)
	{
		if (this.noClip)
		{
			this.boundingBox.offset(p_70091_1_, p_70091_3_, p_70091_5_);
			this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
			this.posY = this.boundingBox.minY + (double)this.yOffset - (double)this.ySize;
			this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
		}
		else
		{
			this.worldObj.theProfiler.startSection("move");
			this.ySize *= 0.4F;
			double d3 = this.posX;
			double d4 = this.posY;
			double d5 = this.posZ;

			if (this.isInWeb)
			{
				this.isInWeb = false;
				p_70091_1_ *= 0.25D;
				p_70091_3_ *= 0.05000000074505806D;
				p_70091_5_ *= 0.25D;
				this.motionX = 0.0D;
				this.motionY = 0.0D;
				this.motionZ = 0.0D;
			}

			double d6 = p_70091_1_;
			double d7 = p_70091_3_;
			double d8 = p_70091_5_;
			AxisAlignedBB axisalignedbb = this.boundingBox.copy();
			boolean flag = this.onGround && this.isSneaking() && this instanceof EntityPlayer;

			if (flag)
			{
				double d9;

				for (d9 = 0.05D; p_70091_1_ != 0.0D && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.getOffsetBoundingBox(p_70091_1_, -1.0D, 0.0D)).isEmpty(); d6 = p_70091_1_)
				{
					if (p_70091_1_ < d9 && p_70091_1_ >= -d9)
					{
						p_70091_1_ = 0.0D;
					}
					else if (p_70091_1_ > 0.0D)
					{
						p_70091_1_ -= d9;
					}
					else
					{
						p_70091_1_ += d9;
					}
				}

				for (; p_70091_5_ != 0.0D && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.getOffsetBoundingBox(0.0D, -1.0D, p_70091_5_)).isEmpty(); d8 = p_70091_5_)
				{
					if (p_70091_5_ < d9 && p_70091_5_ >= -d9)
					{
						p_70091_5_ = 0.0D;
					}
					else if (p_70091_5_ > 0.0D)
					{
						p_70091_5_ -= d9;
					}
					else
					{
						p_70091_5_ += d9;
					}
				}

				while (p_70091_1_ != 0.0D && p_70091_5_ != 0.0D && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.getOffsetBoundingBox(p_70091_1_, -1.0D, p_70091_5_)).isEmpty())
				{
					if (p_70091_1_ < d9 && p_70091_1_ >= -d9)
					{
						p_70091_1_ = 0.0D;
					}
					else if (p_70091_1_ > 0.0D)
					{
						p_70091_1_ -= d9;
					}
					else
					{
						p_70091_1_ += d9;
					}

					if (p_70091_5_ < d9 && p_70091_5_ >= -d9)
					{
						p_70091_5_ = 0.0D;
					}
					else if (p_70091_5_ > 0.0D)
					{
						p_70091_5_ -= d9;
					}
					else
					{
						p_70091_5_ += d9;
					}

					d6 = p_70091_1_;
					d8 = p_70091_5_;
				}
			}

			List list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(p_70091_1_, p_70091_3_, p_70091_5_));

			for (int i = 0; i < list.size(); ++i)
			{
				p_70091_3_ = ((AxisAlignedBB)list.get(i)).calculateYOffset(this.boundingBox, p_70091_3_);
			}

			this.boundingBox.offset(0.0D, p_70091_3_, 0.0D);

			if (!this.field_70135_K && d7 != p_70091_3_)
			{
				p_70091_5_ = 0.0D;
				p_70091_3_ = 0.0D;
				p_70091_1_ = 0.0D;
			}

			boolean flag1 = this.onGround || d7 != p_70091_3_ && d7 < 0.0D;
			int j;

			for (j = 0; j < list.size(); ++j)
			{
				p_70091_1_ = ((AxisAlignedBB)list.get(j)).calculateXOffset(this.boundingBox, p_70091_1_);
			}

			this.boundingBox.offset(p_70091_1_, 0.0D, 0.0D);

			if (!this.field_70135_K && d6 != p_70091_1_)
			{
				p_70091_5_ = 0.0D;
				p_70091_3_ = 0.0D;
				p_70091_1_ = 0.0D;
			}

			for (j = 0; j < list.size(); ++j)
			{
				p_70091_5_ = ((AxisAlignedBB)list.get(j)).calculateZOffset(this.boundingBox, p_70091_5_);
			}

			this.boundingBox.offset(0.0D, 0.0D, p_70091_5_);

			if (!this.field_70135_K && d8 != p_70091_5_)
			{
				p_70091_5_ = 0.0D;
				p_70091_3_ = 0.0D;
				p_70091_1_ = 0.0D;
			}

			double d10;
			double d11;
			int k;
			double d12;

			if (this.stepHeight > 0.0F && flag1 && (flag || this.ySize < 0.05F) && (d6 != p_70091_1_ || d8 != p_70091_5_))
			{
				d12 = p_70091_1_;
				d10 = p_70091_3_;
				d11 = p_70091_5_;
				p_70091_1_ = d6;
				p_70091_3_ = (double)this.stepHeight;
				p_70091_5_ = d8;
				AxisAlignedBB axisalignedbb1 = this.boundingBox.copy();
				this.boundingBox.setBB(axisalignedbb);
				list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(d6, p_70091_3_, d8));

				for (k = 0; k < list.size(); ++k)
				{
					p_70091_3_ = ((AxisAlignedBB)list.get(k)).calculateYOffset(this.boundingBox, p_70091_3_);
				}

				this.boundingBox.offset(0.0D, p_70091_3_, 0.0D);

				if (!this.field_70135_K && d7 != p_70091_3_)
				{
					p_70091_5_ = 0.0D;
					p_70091_3_ = 0.0D;
					p_70091_1_ = 0.0D;
				}

				for (k = 0; k < list.size(); ++k)
				{
					p_70091_1_ = ((AxisAlignedBB)list.get(k)).calculateXOffset(this.boundingBox, p_70091_1_);
				}

				this.boundingBox.offset(p_70091_1_, 0.0D, 0.0D);

				if (!this.field_70135_K && d6 != p_70091_1_)
				{
					p_70091_5_ = 0.0D;
					p_70091_3_ = 0.0D;
					p_70091_1_ = 0.0D;
				}

				for (k = 0; k < list.size(); ++k)
				{
					p_70091_5_ = ((AxisAlignedBB)list.get(k)).calculateZOffset(this.boundingBox, p_70091_5_);
				}

				this.boundingBox.offset(0.0D, 0.0D, p_70091_5_);

				if (!this.field_70135_K && d8 != p_70091_5_)
				{
					p_70091_5_ = 0.0D;
					p_70091_3_ = 0.0D;
					p_70091_1_ = 0.0D;
				}

				if (!this.field_70135_K && d7 != p_70091_3_)
				{
					p_70091_5_ = 0.0D;
					p_70091_3_ = 0.0D;
					p_70091_1_ = 0.0D;
				}
				else
				{
					p_70091_3_ = (double)(-this.stepHeight);

					for (k = 0; k < list.size(); ++k)
					{
						p_70091_3_ = ((AxisAlignedBB)list.get(k)).calculateYOffset(this.boundingBox, p_70091_3_);
					}

					this.boundingBox.offset(0.0D, p_70091_3_, 0.0D);
				}

				if (d12 * d12 + d11 * d11 >= p_70091_1_ * p_70091_1_ + p_70091_5_ * p_70091_5_)
				{
					p_70091_1_ = d12;
					p_70091_3_ = d10;
					p_70091_5_ = d11;
					this.boundingBox.setBB(axisalignedbb1);
				}
			}

			this.worldObj.theProfiler.endSection();
			this.worldObj.theProfiler.startSection("rest");
			this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
			this.posY = this.boundingBox.minY + (double)this.yOffset - (double)this.ySize;
			this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
			this.isCollidedHorizontally = d6 != p_70091_1_ || d8 != p_70091_5_;
			this.isCollidedVertically = d7 != p_70091_3_;
			this.onGround = d7 != p_70091_3_ && d7 < 0.0D;
			this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
			this.updateFallState(p_70091_3_, this.onGround);

			if (d6 != p_70091_1_)
			{
				this.motionX = 0.0D;
			}

			if (d7 != p_70091_3_)
			{
				this.motionY = 0.0D;
			}

			if (d8 != p_70091_5_)
			{
				this.motionZ = 0.0D;
			}

			d12 = this.posX - d3;
			d10 = this.posY - d4;
			d11 = this.posZ - d5;

			if (this.canTriggerWalking() && !flag && this.ridingEntity == null)
			{
				int j1 = MathHelper.floor_double(this.posX);
				k = MathHelper.floor_double(this.posY - 0.20000000298023224D - (double)this.yOffset);
				int l = MathHelper.floor_double(this.posZ);
				Block block = this.worldObj.getBlock(j1, k, l);
				int i1 = this.worldObj.getBlock(j1, k - 1, l).getRenderType();

				if (i1 == 11 || i1 == 32 || i1 == 21)
				{
					block = this.worldObj.getBlock(j1, k - 1, l);
				}

				if (block != Blocks.ladder)
				{
					d10 = 0.0D;
				}

				this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt_double(d12 * d12 + d11 * d11) * 0.6D);
				this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt_double(d12 * d12 + d10 * d10 + d11 * d11) * 0.6D);

				if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && block.getMaterial() != Material.air)
				{
					this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;

					if (this.isInWater())
					{
						float f = MathHelper.sqrt_double(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;

						if (f > 1.0F)
						{
							f = 1.0F;
						}

						if(!isEntityPlayerMP() || !((EntityPlayerMP)this).isHidden())
							this.playSound(this.getSwimSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
					}

					this.func_145780_a(j1, k, l, block);
					block.onEntityWalking(this.worldObj, j1, k, l, this);
				}
			}

			try
			{
				this.func_145775_I();
			}
			catch (Throwable throwable)
			{
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
				this.addEntityCrashInfo(crashreportcategory);
				throw new ReportedException(crashreport);
			}

			boolean flag2 = this.isWet();

			if (this.worldObj.func_147470_e(this.boundingBox.contract(0.001D, 0.001D, 0.001D)))
			{
				this.dealFireDamage(1);

				if (!flag2)
				{
					++this.fire;

					if (this.fire == 0)
					{
						this.setFire(8);
					}
				}
			}
			else if (this.fire <= 0)
			{
				this.fire = -this.fireResistance;
			}

			if (flag2 && this.fire > 0)
			{
				this.playSound("random.fizz", 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
				this.fire = -this.fireResistance;
			}

			this.worldObj.theProfiler.endSection();
		}
	}

	protected String getSwimSound()
	{
		return "game.neutral.swim";
	}

	protected void func_145775_I()
	{
		int i = MathHelper.floor_double(this.boundingBox.minX + 0.001D);
		int j = MathHelper.floor_double(this.boundingBox.minY + 0.001D);
		int k = MathHelper.floor_double(this.boundingBox.minZ + 0.001D);
		int l = MathHelper.floor_double(this.boundingBox.maxX - 0.001D);
		int i1 = MathHelper.floor_double(this.boundingBox.maxY - 0.001D);
		int j1 = MathHelper.floor_double(this.boundingBox.maxZ - 0.001D);

		if (this.worldObj.checkChunksExist(i, j, k, l, i1, j1))
		{
			for (int k1 = i; k1 <= l; ++k1)
			{
				for (int l1 = j; l1 <= i1; ++l1)
				{
					for (int i2 = k; i2 <= j1; ++i2)
					{
						Block block = this.worldObj.getBlock(k1, l1, i2);

						try
						{
							block.onEntityCollidedWithBlock(this.worldObj, k1, l1, i2, this);
						}
						catch (Throwable throwable)
						{
							CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Colliding entity with block");
							CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being collided with");
							CrashReportCategory.func_147153_a(crashreportcategory, k1, l1, i2, block, this.worldObj.getBlockMetadata(k1, l1, i2));
							throw new ReportedException(crashreport);
						}
					}
				}
			}
		}
	}

	protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
	{
		if(isEntityPlayerMP() && ((EntityPlayerMP)this).isHidden())
			return;
		Block.SoundType soundtype = p_145780_4_.stepSound;

		if (this.worldObj.getBlock(p_145780_1_, p_145780_2_ + 1, p_145780_3_) == Blocks.snow_layer)
		{
			soundtype = Blocks.snow_layer.stepSound;
			this.playSound(soundtype.getStepResourcePath(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
		}
		else if (!p_145780_4_.getMaterial().isLiquid())
		{
			this.playSound(soundtype.getStepResourcePath(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
		}
	}

	public void playSound(String p_85030_1_, float p_85030_2_, float p_85030_3_)
	{
		this.worldObj.playSoundAtEntity(this, p_85030_1_, p_85030_2_, p_85030_3_);
	}

	protected boolean canTriggerWalking()
	{
		return true;
	}

	protected void updateFallState(double p_70064_1_, boolean p_70064_3_)
	{
		if (p_70064_3_)
		{
			if (this.fallDistance > 0.0F)
			{
				this.fall(this.fallDistance);
				this.fallDistance = 0.0F;
			}
		}
		else if (p_70064_1_ < 0.0D)
		{
			this.fallDistance = (float)((double)this.fallDistance - p_70064_1_);
		}
	}

	public AxisAlignedBB getBoundingBox()
	{
		return null;
	}

	protected void dealFireDamage(int p_70081_1_)
	{
		if (!this.isImmuneToFire)
		{
			this.attackEntityFrom(DamageSource.inFire, (float)p_70081_1_);
		}
	}

	public final boolean isImmuneToFire()
	{
		return this.isImmuneToFire;
	}

	protected void fall(float p_70069_1_)
	{
		if (this.riddenByEntity != null)
		{
			this.riddenByEntity.fall(p_70069_1_);
		}
	}

	public boolean isWet()
	{
		return this.inWater || this.worldObj.canLightningStrikeAt(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) || this.worldObj.canLightningStrikeAt(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY + (double)this.height), MathHelper.floor_double(this.posZ));
	}

	public boolean isInWater()
	{
		return this.inWater;
	}

	public boolean handleWaterMovement()
	{
		if (this.worldObj.handleMaterialAcceleration(this.boundingBox.expand(0.0D, -0.4000000059604645D, 0.0D).contract(0.001D, 0.001D, 0.001D), Material.water, this))
		{
			if (!this.inWater && !this.firstUpdate)
			{
				float f = MathHelper.sqrt_double(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.2F;

				if (f > 1.0F)
				{
					f = 1.0F;
				}

				this.playSound(this.getSplashSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
				float f1 = (float)MathHelper.floor_double(this.boundingBox.minY);
				int i;
				float f2;
				float f3;

				for (i = 0; (float)i < 1.0F + this.width * 20.0F; ++i)
				{
					f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
					f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
					this.worldObj.spawnParticle("bubble", this.posX + (double)f2, (double)(f1 + 1.0F), this.posZ + (double)f3, this.motionX, this.motionY - (double)(this.rand.nextFloat() * 0.2F), this.motionZ);
				}

				for (i = 0; (float)i < 1.0F + this.width * 20.0F; ++i)
				{
					f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
					f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
					this.worldObj.spawnParticle("splash", this.posX + (double)f2, (double)(f1 + 1.0F), this.posZ + (double)f3, this.motionX, this.motionY, this.motionZ);
				}
			}

			this.fallDistance = 0.0F;
			this.inWater = true;
			this.fire = 0;
		}
		else
		{
			this.inWater = false;
		}

		return this.inWater;
	}

	protected String getSplashSound()
	{
		return "game.neutral.swim.splash";
	}

	public boolean isInsideOfMaterial(Material p_70055_1_)
	{
		double d0 = this.posY + (double)this.getEyeHeight();
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_float((float)MathHelper.floor_double(d0));
		int k = MathHelper.floor_double(this.posZ);
		Block block = this.worldObj.getBlock(i, j, k);

		if (block.getMaterial() == p_70055_1_)
		{
			double filled = 1.0f; //If it's not a liquid assume it's a solid block
			if (block instanceof IFluidBlock)
			{
				filled = ((IFluidBlock)block).getFilledPercentage(worldObj, i, j, k);
			}

			if (filled < 0)
			{
				filled *= -1;
				//filled -= 0.11111111F; //Why this is needed.. not sure...
				return d0 > (double)(j + (1 - filled));
			}
			else
			{
				return d0 < (double)(j + filled);
			}
		}
		else
		{
			return false;
		}
	}

	public float getEyeHeight()
	{
		return 0.0F;
	}

	public boolean handleLavaMovement()
	{
		return this.worldObj.isMaterialInBB(this.boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.lava);
	}

	public void moveFlying(float p_70060_1_, float p_70060_2_, float p_70060_3_)
	{
		float f3 = p_70060_1_ * p_70060_1_ + p_70060_2_ * p_70060_2_;

		if (f3 >= 1.0E-4F)
		{
			f3 = MathHelper.sqrt_float(f3);

			if (f3 < 1.0F)
			{
				f3 = 1.0F;
			}

			f3 = p_70060_3_ / f3;
			p_70060_1_ *= f3;
			p_70060_2_ *= f3;
			float f4 = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
			float f5 = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
			this.motionX += (double)(p_70060_1_ * f5 - p_70060_2_ * f4);
			this.motionZ += (double)(p_70060_2_ * f5 + p_70060_1_ * f4);
		}
	}

	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_)
	{
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.posZ);

		if (this.worldObj.blockExists(i, 0, j))
		{
			double d0 = (this.boundingBox.maxY - this.boundingBox.minY) * 0.66D;
			int k = MathHelper.floor_double(this.posY - (double)this.yOffset + d0);
			return this.worldObj.getLightBrightnessForSkyBlocks(i, k, j, 0);
		}
		else
		{
			return 0;
		}
	}

	public float getBrightness(float p_70013_1_)
	{
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.posZ);

		if (this.worldObj.blockExists(i, 0, j))
		{
			double d0 = (this.boundingBox.maxY - this.boundingBox.minY) * 0.66D;
			int k = MathHelper.floor_double(this.posY - (double)this.yOffset + d0);
			return this.worldObj.getLightBrightness(i, k, j);
		}
		else
		{
			return 0.0F;
		}
	}

	public void setWorld(World p_70029_1_)
	{
		this.worldObj = p_70029_1_;
	}

	public void setPositionAndRotation(double p_70080_1_, double p_70080_3_, double p_70080_5_, float p_70080_7_, float p_70080_8_)
	{
		this.prevPosX = this.posX = p_70080_1_;
		this.prevPosY = this.posY = p_70080_3_;
		this.prevPosZ = this.posZ = p_70080_5_;
		this.prevRotationYaw = this.rotationYaw = p_70080_7_;
		this.prevRotationPitch = this.rotationPitch = p_70080_8_;
		this.ySize = 0.0F;
		double d3 = (double)(this.prevRotationYaw - p_70080_7_);

		if (d3 < -180.0D)
		{
			this.prevRotationYaw += 360.0F;
		}

		if (d3 >= 180.0D)
		{
			this.prevRotationYaw -= 360.0F;
		}

		this.setPosition(this.posX, this.posY, this.posZ);
		this.setRotation(p_70080_7_, p_70080_8_);
	}

	public void setLocationAndAngles(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_, float p_70012_8_)
	{
		this.lastTickPosX = this.prevPosX = this.posX = p_70012_1_;
		this.lastTickPosY = this.prevPosY = this.posY = p_70012_3_ + (double)this.yOffset;
		this.lastTickPosZ = this.prevPosZ = this.posZ = p_70012_5_;
		this.rotationYaw = p_70012_7_;
		this.rotationPitch = p_70012_8_;
		this.setPosition(this.posX, this.posY, this.posZ);
	}

	public float getDistanceToEntity(Entity p_70032_1_)
	{
		float f = (float)(this.posX - p_70032_1_.posX);
		float f1 = (float)(this.posY - p_70032_1_.posY);
		float f2 = (float)(this.posZ - p_70032_1_.posZ);
		return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
	}

	public double getDistanceSq(double p_70092_1_, double p_70092_3_, double p_70092_5_)
	{
		double d3 = this.posX - p_70092_1_;
		double d4 = this.posY - p_70092_3_;
		double d5 = this.posZ - p_70092_5_;
		return d3 * d3 + d4 * d4 + d5 * d5;
	}

	public double getDistance(double p_70011_1_, double p_70011_3_, double p_70011_5_)
	{
		double d3 = this.posX - p_70011_1_;
		double d4 = this.posY - p_70011_3_;
		double d5 = this.posZ - p_70011_5_;
		return (double)MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
	}

	public double getDistanceSqToEntity(Entity p_70068_1_)
	{
		double d0 = this.posX - p_70068_1_.posX;
		double d1 = this.posY - p_70068_1_.posY;
		double d2 = this.posZ - p_70068_1_.posZ;
		return d0 * d0 + d1 * d1 + d2 * d2;
	}

	public void onCollideWithPlayer(EntityPlayer p_70100_1_) {}

	public void applyEntityCollision(Entity p_70108_1_)
	{
		if (p_70108_1_.riddenByEntity != this && p_70108_1_.ridingEntity != this)
		{
			double d0 = p_70108_1_.posX - this.posX;
			double d1 = p_70108_1_.posZ - this.posZ;
			double d2 = MathHelper.abs_max(d0, d1);

			if (d2 >= 0.009999999776482582D)
			{
				d2 = (double)MathHelper.sqrt_double(d2);
				d0 /= d2;
				d1 /= d2;
				double d3 = 1.0D / d2;

				if (d3 > 1.0D)
				{
					d3 = 1.0D;
				}

				d0 *= d3;
				d1 *= d3;
				d0 *= 0.05000000074505806D;
				d1 *= 0.05000000074505806D;
				d0 *= (double)(1.0F - this.entityCollisionReduction);
				d1 *= (double)(1.0F - this.entityCollisionReduction);
				this.addVelocity(-d0, 0.0D, -d1);
				p_70108_1_.addVelocity(d0, 0.0D, d1);
			}
		}
	}

	public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_)
	{
		this.motionX += p_70024_1_;
		this.motionY += p_70024_3_;
		this.motionZ += p_70024_5_;
		this.isAirBorne = true;
	}

	protected void setBeenAttacked()
	{
		this.velocityChanged = true;
	}

	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		if (this.isEntityInvulnerable())
		{
			return false;
		}
		else
		{
			this.setBeenAttacked();
			return false;
		}
	}

	public boolean canBeCollidedWith()
	{
		return false;
	}

	public boolean canBePushed()
	{
		return false;
	}

	public void addToPlayerScore(Entity p_70084_1_, int p_70084_2_) {}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_, double p_145770_5_)
	{
		double d3 = this.posX - p_145770_1_;
		double d4 = this.posY - p_145770_3_;
		double d5 = this.posZ - p_145770_5_;
		double d6 = d3 * d3 + d4 * d4 + d5 * d5;
		return this.isInRangeToRenderDist(d6);
	}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.boundingBox.getAverageEdgeLength();
		d1 *= 64.0D * this.renderDistanceWeight;
		return p_70112_1_ < d1 * d1;
	}

	public boolean writeMountToNBT(NBTTagCompound p_98035_1_)
	{
		String s = this.getEntityString();

		if (!this.isDead && s != null)
		{
			p_98035_1_.setString("id", s);
			this.writeToNBT(p_98035_1_);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean writeToNBTOptional(NBTTagCompound p_70039_1_)
	{
		String s = this.getEntityString();

		if (!this.isDead && s != null && this.riddenByEntity == null)
		{
			p_70039_1_.setString("id", s);
			this.writeToNBT(p_70039_1_);
			return true;
		}
		else
		{
			return false;
		}
	}

	public void writeToNBT(NBTTagCompound p_70109_1_)
	{
		try
		{
			if(owner != null) UMHooks.writeObjectOwner(p_70109_1_, owner);
			p_70109_1_.setTag("Pos", this.newDoubleNBTList(new double[] {this.posX, this.posY + (double)this.ySize, this.posZ}));
			p_70109_1_.setTag("Motion", this.newDoubleNBTList(new double[] {this.motionX, this.motionY, this.motionZ}));
			p_70109_1_.setTag("Rotation", this.newFloatNBTList(new float[] {this.rotationYaw, this.rotationPitch}));
			p_70109_1_.setFloat("FallDistance", this.fallDistance);
			p_70109_1_.setShort("Fire", (short)this.fire);
			p_70109_1_.setShort("Air", (short)this.getAir());
			p_70109_1_.setBoolean("OnGround", this.onGround);
			p_70109_1_.setInteger("Dimension", this.dimension);
			p_70109_1_.setBoolean("Invulnerable", this.invulnerable);
			p_70109_1_.setInteger("PortalCooldown", this.timeUntilPortal);
			p_70109_1_.setLong("UUIDMost", this.getUniqueID().getMostSignificantBits());
			p_70109_1_.setLong("UUIDLeast", this.getUniqueID().getLeastSignificantBits());
			if (customEntityData != null)
			{
				p_70109_1_.setTag("ForgeData", customEntityData);
			}

		   for (String identifier : this.extendedProperties.keySet())
		   {
				try
				{
					IExtendedEntityProperties props = this.extendedProperties.get(identifier);
					props.saveNBTData(p_70109_1_);
				}
				catch (Throwable t)
				{
					FMLLog.severe("Failed to save extended properties for %s.  This is a mod issue.", identifier);
					t.printStackTrace();
				}
			}

		   this.writeEntityToNBT(p_70109_1_);

			if (this.ridingEntity != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();

				if (this.ridingEntity.writeMountToNBT(nbttagcompound1))
				{
					p_70109_1_.setTag("Riding", nbttagcompound1);
				}
			}
		}
		catch (Throwable throwable)
		{
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Saving entity NBT");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being saved");
			this.addEntityCrashInfo(crashreportcategory);
			throw new ReportedException(crashreport);
		}
	}

	public void readFromNBT(NBTTagCompound p_70020_1_)
	{
		try
		{
			if(owner == null) owner = UMHooks.readObjectOwner(p_70020_1_);
			NBTTagList nbttaglist = p_70020_1_.getTagList("Pos", 6);
			NBTTagList nbttaglist1 = p_70020_1_.getTagList("Motion", 6);
			NBTTagList nbttaglist2 = p_70020_1_.getTagList("Rotation", 5);
			this.motionX = nbttaglist1.func_150309_d(0);
			this.motionY = nbttaglist1.func_150309_d(1);
			this.motionZ = nbttaglist1.func_150309_d(2);

			if (Math.abs(this.motionX) > 10.0D)
			{
				this.motionX = 0.0D;
			}

			if (Math.abs(this.motionY) > 10.0D)
			{
				this.motionY = 0.0D;
			}

			if (Math.abs(this.motionZ) > 10.0D)
			{
				this.motionZ = 0.0D;
			}

			this.prevPosX = this.lastTickPosX = this.posX = nbttaglist.func_150309_d(0);
			this.prevPosY = this.lastTickPosY = this.posY = nbttaglist.func_150309_d(1);
			this.prevPosZ = this.lastTickPosZ = this.posZ = nbttaglist.func_150309_d(2);
			this.prevRotationYaw = this.rotationYaw = nbttaglist2.func_150308_e(0);
			this.prevRotationPitch = this.rotationPitch = nbttaglist2.func_150308_e(1);
			this.fallDistance = p_70020_1_.getFloat("FallDistance");
			this.fire = p_70020_1_.getShort("Fire");
			this.setAir(p_70020_1_.getShort("Air"));
			this.onGround = p_70020_1_.getBoolean("OnGround");
			this.dimension = p_70020_1_.getInteger("Dimension");
			this.invulnerable = p_70020_1_.getBoolean("Invulnerable");
			this.timeUntilPortal = p_70020_1_.getInteger("PortalCooldown");

			if (p_70020_1_.hasKey("UUIDMost", 4) && p_70020_1_.hasKey("UUIDLeast", 4))
			{
				this.entityUniqueID = new UUID(p_70020_1_.getLong("UUIDMost"), p_70020_1_.getLong("UUIDLeast"));
			}

			this.setPosition(this.posX, this.posY, this.posZ);
			this.setRotation(this.rotationYaw, this.rotationPitch);
			if (p_70020_1_.hasKey("ForgeData"))
			{
				customEntityData = p_70020_1_.getCompoundTag("ForgeData");
			}

			for (String identifier : this.extendedProperties.keySet())
			{
				try
				{
					IExtendedEntityProperties props = this.extendedProperties.get(identifier);
					props.loadNBTData(p_70020_1_);
				}
				catch (Throwable t)
				{
					FMLLog.severe("Failed to load extended properties for %s.  This is a mod issue.", identifier);
					t.printStackTrace();
				}
			}

			//Rawr, legacy code, Vanilla added a UUID, keep this so older maps will convert properly
			if (p_70020_1_.hasKey("PersistentIDMSB") && p_70020_1_.hasKey("PersistentIDLSB"))
			{
				this.entityUniqueID = new UUID(p_70020_1_.getLong("PersistentIDMSB"), p_70020_1_.getLong("PersistentIDLSB"));
			}
			this.readEntityFromNBT(p_70020_1_);

			if (this.shouldSetPosAfterLoading())
			{
				this.setPosition(this.posX, this.posY, this.posZ);
			}
		}
		catch (Throwable throwable)
		{
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Loading entity NBT");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being loaded");
			this.addEntityCrashInfo(crashreportcategory);
			throw new ReportedException(crashreport);
		}
	}

	protected boolean shouldSetPosAfterLoading()
	{
		return true;
	}

	protected final String getEntityString()
	{
		return EntityList.getEntityString(this);
	}

	protected abstract void readEntityFromNBT(NBTTagCompound p_70037_1_);

	protected abstract void writeEntityToNBT(NBTTagCompound p_70014_1_);

	public void onChunkLoad() {}

	protected NBTTagList newDoubleNBTList(double ... p_70087_1_)
	{
		NBTTagList nbttaglist = new NBTTagList();
		double[] adouble = p_70087_1_;
		int i = p_70087_1_.length;

		for (int j = 0; j < i; ++j)
		{
			double d1 = adouble[j];
			nbttaglist.appendTag(new NBTTagDouble(d1));
		}

		return nbttaglist;
	}

	protected NBTTagList newFloatNBTList(float ... p_70049_1_)
	{
		NBTTagList nbttaglist = new NBTTagList();
		float[] afloat = p_70049_1_;
		int i = p_70049_1_.length;

		for (int j = 0; j < i; ++j)
		{
			float f1 = afloat[j];
			nbttaglist.appendTag(new NBTTagFloat(f1));
		}

		return nbttaglist;
	}

	public EntityItem dropItem(Item p_145779_1_, int p_145779_2_)
	{
		return this.func_145778_a(p_145779_1_, p_145779_2_, 0.0F);
	}

	public EntityItem func_145778_a(Item p_145778_1_, int p_145778_2_, float p_145778_3_)
	{
		return this.entityDropItem(new ItemStack(p_145778_1_, p_145778_2_, 0), p_145778_3_);
	}

	public EntityItem entityDropItem(ItemStack p_70099_1_, float p_70099_2_)
	{
		if (p_70099_1_.stackSize != 0 && p_70099_1_.getItem() != null)
		{
			EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY + (double)p_70099_2_, this.posZ, p_70099_1_);
			entityitem.delayBeforeCanPickup = 10;
			if (captureDrops)
			{
				capturedDrops.add(entityitem);
			}
			else
			{
				this.worldObj.spawnEntityInWorld(entityitem);
			}
			return entityitem;
		}
		else
		{
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	public float getShadowSize()
	{
		return this.height / 2.0F;
	}

	public boolean isEntityAlive()
	{
		return !this.isDead;
	}

	public boolean isEntityInsideOpaqueBlock()
	{
		for (int i = 0; i < 8; ++i)
		{
			float f = ((float)((i >> 0) % 2) - 0.5F) * this.width * 0.8F;
			float f1 = ((float)((i >> 1) % 2) - 0.5F) * 0.1F;
			float f2 = ((float)((i >> 2) % 2) - 0.5F) * this.width * 0.8F;
			int j = MathHelper.floor_double(this.posX + (double)f);
			int k = MathHelper.floor_double(this.posY + (double)this.getEyeHeight() + (double)f1);
			int l = MathHelper.floor_double(this.posZ + (double)f2);

			if (this.worldObj.getBlock(j, k, l).isNormalCube())
			{
				return true;
			}
		}

		return false;
	}

	public boolean interactFirst(EntityPlayer p_130002_1_)
	{
		return false;
	}

	public AxisAlignedBB getCollisionBox(Entity p_70114_1_)
	{
		return null;
	}

	public void updateRidden()
	{
		if (this.ridingEntity.isDead)
		{
			this.ridingEntity = null;
		}
		else
		{
			this.motionX = 0.0D;
			this.motionY = 0.0D;
			this.motionZ = 0.0D;
			this.onUpdate();

			if (this.ridingEntity != null)
			{
				this.ridingEntity.updateRiderPosition();
				this.entityRiderYawDelta += (double)(this.ridingEntity.rotationYaw - this.ridingEntity.prevRotationYaw);

				for (this.entityRiderPitchDelta += (double)(this.ridingEntity.rotationPitch - this.ridingEntity.prevRotationPitch); this.entityRiderYawDelta >= 180.0D; this.entityRiderYawDelta -= 360.0D)
				{
					;
				}

				while (this.entityRiderYawDelta < -180.0D)
				{
					this.entityRiderYawDelta += 360.0D;
				}

				while (this.entityRiderPitchDelta >= 180.0D)
				{
					this.entityRiderPitchDelta -= 360.0D;
				}

				while (this.entityRiderPitchDelta < -180.0D)
				{
					this.entityRiderPitchDelta += 360.0D;
				}

				double d0 = this.entityRiderYawDelta * 0.5D;
				double d1 = this.entityRiderPitchDelta * 0.5D;
				float f = 10.0F;

				if (d0 > (double)f)
				{
					d0 = (double)f;
				}

				if (d0 < (double)(-f))
				{
					d0 = (double)(-f);
				}

				if (d1 > (double)f)
				{
					d1 = (double)f;
				}

				if (d1 < (double)(-f))
				{
					d1 = (double)(-f);
				}

				this.entityRiderYawDelta -= d0;
				this.entityRiderPitchDelta -= d1;
			}
		}
	}

	public void updateRiderPosition()
	{
		if (this.riddenByEntity != null)
		{
			this.riddenByEntity.setPosition(this.posX, this.posY + this.getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ);
		}
	}

	public double getYOffset()
	{
		return (double)this.yOffset;
	}

	public double getMountedYOffset()
	{
		return (double)this.height * 0.75D;
	}

	public void mountEntity(Entity p_70078_1_)
	{
		this.entityRiderPitchDelta = 0.0D;
		this.entityRiderYawDelta = 0.0D;

		if (p_70078_1_ == null)
		{
			if (this.ridingEntity != null)
			{
				this.setLocationAndAngles(this.ridingEntity.posX, this.ridingEntity.boundingBox.minY + (double)this.ridingEntity.height, this.ridingEntity.posZ, this.rotationYaw, this.rotationPitch);
				this.ridingEntity.riddenByEntity = null;
			}

			this.ridingEntity = null;
		}
		else
		{
			if (this.ridingEntity != null)
			{
				this.ridingEntity.riddenByEntity = null;
			}

			if (p_70078_1_ != null)
			{
				for (Entity entity1 = p_70078_1_.ridingEntity; entity1 != null; entity1 = entity1.ridingEntity)
				{
					if (entity1 == this)
					{
						return;
					}
				}
			}

			this.ridingEntity = p_70078_1_;
			p_70078_1_.riddenByEntity = this;
		}
	}

	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_)
	{
		this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
		this.setRotation(p_70056_7_, p_70056_8_);
		List list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.contract(0.03125D, 0.0D, 0.03125D));

		if (!list.isEmpty())
		{
			double d3 = 0.0D;

			for (int j = 0; j < list.size(); ++j)
			{
				AxisAlignedBB axisalignedbb = (AxisAlignedBB)list.get(j);

				if (axisalignedbb.maxY > d3)
				{
					d3 = axisalignedbb.maxY;
				}
			}

			p_70056_3_ += d3 - this.boundingBox.minY;
			this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
		}
	}

	public float getCollisionBorderSize()
	{
		return 0.1F;
	}

	public Vec3 getLookVec()
	{
		return null;
	}

	public void setInPortal()
	{
		if (this.timeUntilPortal > 0)
		{
			this.timeUntilPortal = this.getPortalCooldown();
		}
		else
		{
			double d0 = this.prevPosX - this.posX;
			double d1 = this.prevPosZ - this.posZ;

			if (!this.worldObj.isRemote && !this.inPortal)
			{
				this.teleportDirection = Direction.getMovementDirection(d0, d1);
			}

			this.inPortal = true;
		}
	}

	public int getPortalCooldown()
	{
		return 300;
	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_)
	{
		this.motionX = p_70016_1_;
		this.motionY = p_70016_3_;
		this.motionZ = p_70016_5_;
	}

	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte p_70103_1_) {}

	@SideOnly(Side.CLIENT)
	public void performHurtAnimation() {}

	public ItemStack[] getLastActiveItems()
	{
		return null;
	}

	public void setCurrentItemOrArmor(int p_70062_1_, ItemStack p_70062_2_) {}

	public boolean isBurning()
	{
		boolean flag = this.worldObj != null && this.worldObj.isRemote;
		return !this.isImmuneToFire && (this.fire > 0 || flag && this.getFlag(0));
	}

	public boolean isRiding()
	{
		return this.ridingEntity != null && ridingEntity.shouldRiderSit();
	}

	public boolean isSneaking()
	{
		return this.getFlag(1);
	}

	public void setSneaking(boolean p_70095_1_)
	{
		this.setFlag(1, p_70095_1_);
	}

	public boolean isSprinting()
	{
		return this.getFlag(3);
	}

	public void setSprinting(boolean p_70031_1_)
	{
		this.setFlag(3, p_70031_1_);
	}

	public boolean isInvisible()
	{
		return this.getFlag(5);
	}

	@SideOnly(Side.CLIENT)
	public boolean isInvisibleToPlayer(EntityPlayer p_98034_1_)
	{
		return this.isInvisible();
	}

	public void setInvisible(boolean p_82142_1_)
	{
		this.setFlag(5, p_82142_1_);
	}

	@SideOnly(Side.CLIENT)
	public boolean isEating()
	{
		return this.getFlag(4);
	}

	public void setEating(boolean p_70019_1_)
	{
		this.setFlag(4, p_70019_1_);
	}

	protected boolean getFlag(int p_70083_1_)
	{
		return (this.dataWatcher.getWatchableObjectByte(0) & 1 << p_70083_1_) != 0;
	}

	protected void setFlag(int p_70052_1_, boolean p_70052_2_)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte(0);

		if (p_70052_2_)
		{
			this.dataWatcher.updateObject(0, Byte.valueOf((byte)(b0 | 1 << p_70052_1_)));
		}
		else
		{
			this.dataWatcher.updateObject(0, Byte.valueOf((byte)(b0 & ~(1 << p_70052_1_))));
		}
	}

	public int getAir()
	{
		return this.dataWatcher.getWatchableObjectShort(1);
	}

	public void setAir(int p_70050_1_)
	{
		this.dataWatcher.updateObject(1, Short.valueOf((short)p_70050_1_));
	}

	public void onStruckByLightning(EntityLightningBolt p_70077_1_)
	{
		this.dealFireDamage(5);
		++this.fire;

		if (this.fire == 0)
		{
			this.setFire(8);
		}
	}

	public void onKillEntity(EntityLivingBase p_70074_1_) {}

	protected boolean func_145771_j(double p_145771_1_, double p_145771_3_, double p_145771_5_)
	{
		int i = MathHelper.floor_double(p_145771_1_);
		int j = MathHelper.floor_double(p_145771_3_);
		int k = MathHelper.floor_double(p_145771_5_);
		double d3 = p_145771_1_ - (double)i;
		double d4 = p_145771_3_ - (double)j;
		double d5 = p_145771_5_ - (double)k;
		List list = this.worldObj.func_147461_a(this.boundingBox);

		if (list.isEmpty() && !this.worldObj.func_147469_q(i, j, k))
		{
			return false;
		}
		else
		{
			boolean flag = !this.worldObj.func_147469_q(i - 1, j, k);
			boolean flag1 = !this.worldObj.func_147469_q(i + 1, j, k);
			boolean flag2 = !this.worldObj.func_147469_q(i, j - 1, k);
			boolean flag3 = !this.worldObj.func_147469_q(i, j + 1, k);
			boolean flag4 = !this.worldObj.func_147469_q(i, j, k - 1);
			boolean flag5 = !this.worldObj.func_147469_q(i, j, k + 1);
			byte b0 = 3;
			double d6 = 9999.0D;

			if (flag && d3 < d6)
			{
				d6 = d3;
				b0 = 0;
			}

			if (flag1 && 1.0D - d3 < d6)
			{
				d6 = 1.0D - d3;
				b0 = 1;
			}

			if (flag3 && 1.0D - d4 < d6)
			{
				d6 = 1.0D - d4;
				b0 = 3;
			}

			if (flag4 && d5 < d6)
			{
				d6 = d5;
				b0 = 4;
			}

			if (flag5 && 1.0D - d5 < d6)
			{
				d6 = 1.0D - d5;
				b0 = 5;
			}

			float f = this.rand.nextFloat() * 0.2F + 0.1F;

			if (b0 == 0)
			{
				this.motionX = (double)(-f);
			}

			if (b0 == 1)
			{
				this.motionX = (double)f;
			}

			if (b0 == 2)
			{
				this.motionY = (double)(-f);
			}

			if (b0 == 3)
			{
				this.motionY = (double)f;
			}

			if (b0 == 4)
			{
				this.motionZ = (double)(-f);
			}

			if (b0 == 5)
			{
				this.motionZ = (double)f;
			}

			return true;
		}
	}

	public void setInWeb()
	{
		this.isInWeb = true;
		this.fallDistance = 0.0F;
	}

	public String getCommandSenderName()
	{
		String s = EntityList.getEntityString(this);

		if (s == null)
		{
			s = "generic";
		}

		return StatCollector.translateToLocal("entity." + s + ".name");
	}

	public Entity[] getParts()
	{
		return null;
	}

	public boolean isEntityEqual(Entity p_70028_1_)
	{
		return this == p_70028_1_;
	}

	public float getRotationYawHead()
	{
		return 0.0F;
	}

	@SideOnly(Side.CLIENT)
	public void setRotationYawHead(float p_70034_1_) {}

	public boolean canAttackWithItem()
	{
		return true;
	}

	public boolean hitByEntity(Entity p_85031_1_)
	{
		return false;
	}

	public String toString()
	{
		return String.format("%s[\'%s\'/%d, l=\'%s\', x=%.2f, y=%.2f, z=%.2f]", new Object[] {this.getClass().getSimpleName(), this.getCommandSenderName(), Integer.valueOf(this.entityId), this.worldObj == null ? "~NULL~" : this.worldObj.getWorldInfo().getWorldName(), Double.valueOf(this.posX), Double.valueOf(this.posY), Double.valueOf(this.posZ)});
	}

	public boolean isEntityInvulnerable()
	{
		return this.invulnerable;
	}

	public void copyLocationAndAnglesFrom(Entity p_82149_1_)
	{
		this.setLocationAndAngles(p_82149_1_.posX, p_82149_1_.posY, p_82149_1_.posZ, p_82149_1_.rotationYaw, p_82149_1_.rotationPitch);
	}

	public void copyDataFrom(Entity p_82141_1_, boolean p_82141_2_)
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		p_82141_1_.writeToNBT(nbttagcompound);
		this.readFromNBT(nbttagcompound);
		this.timeUntilPortal = p_82141_1_.timeUntilPortal;
		this.teleportDirection = p_82141_1_.teleportDirection;
	}

	public void travelToDimension(int p_71027_1_)
	{
		if (!this.worldObj.isRemote && !this.isDead)
		{
			this.worldObj.theProfiler.startSection("changeDimension");
			int enderLink = ((WorldServer)worldObj).getConfig().portals.enderLink;
			MinecraftServer minecraftserver = MinecraftServer.getServer();
			int j = this.dimension;
			WorldServer worldserver = minecraftserver.worldServerForDimension(j);
			WorldServer worldserver1 = minecraftserver.worldServerForDimension(p_71027_1_);
			this.dimension = p_71027_1_;

			if (j == enderLink && p_71027_1_ == enderLink)
			{
//				worldserver1 = minecraftserver.worldServerForDimension(0);
//				this.dimension = 0;
				setDead();
				return;
			}

			this.worldObj.removeEntity(this);
			this.isDead = false;
			this.worldObj.theProfiler.startSection("reposition");
			minecraftserver.getConfigurationManager().transferEntityToWorld(this, j, worldserver, worldserver1);
			this.worldObj.theProfiler.endStartSection("reloading");
			Entity entity = EntityList.createEntityByName(EntityList.getEntityString(this), worldserver1);

			if (entity != null)
			{
				entity.copyDataFrom(this, true);

				if (j == enderLink && p_71027_1_ == enderLink)
				{
					ChunkCoordinates chunkcoordinates = worldserver1.getSpawnPoint();
					chunkcoordinates.posY = this.worldObj.getTopSolidOrLiquidBlock(chunkcoordinates.posX, chunkcoordinates.posZ);
					entity.setLocationAndAngles((double)chunkcoordinates.posX, (double)chunkcoordinates.posY, (double)chunkcoordinates.posZ, entity.rotationYaw, entity.rotationPitch);
				}

				worldserver1.spawnEntityInWorld(entity);
			}

			this.isDead = true;
			this.worldObj.theProfiler.endSection();
			worldserver.resetUpdateEntityTick();
			worldserver1.resetUpdateEntityTick();
			this.worldObj.theProfiler.endSection();
		}
	}

	public float func_145772_a(Explosion p_145772_1_, World p_145772_2_, int p_145772_3_, int p_145772_4_, int p_145772_5_, Block p_145772_6_)
	{
		return p_145772_6_.getExplosionResistance(this, p_145772_2_, p_145772_3_, p_145772_4_, p_145772_5_, posX, posY + getEyeHeight(), posZ);
	}

	public boolean func_145774_a(Explosion p_145774_1_, World p_145774_2_, int p_145774_3_, int p_145774_4_, int p_145774_5_, Block p_145774_6_, float p_145774_7_)
	{
		return true;
	}

	public int getMaxSafePointTries()
	{
		return 3;
	}

	public int getTeleportDirection()
	{
		return this.teleportDirection;
	}

	public boolean doesEntityNotTriggerPressurePlate()
	{
		return false;
	}

	public void addEntityCrashInfo(CrashReportCategory p_85029_1_)
	{
		p_85029_1_.addCrashSectionCallable("Entity Type", new Callable()
		{
			private static final String __OBFID = "CL_00001534";
			public String call()
			{
				return EntityList.getEntityString(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")";
			}
		});
		p_85029_1_.addCrashSection("Entity ID", Integer.valueOf(this.entityId));
		p_85029_1_.addCrashSectionCallable("Entity Name", new Callable()
		{
			private static final String __OBFID = "CL_00001535";
			public String call()
			{
				return Entity.this.getCommandSenderName();
			}
		});
		p_85029_1_.addCrashSection("Entity\'s Exact location", String.format("%.2f, %.2f, %.2f", new Object[] {Double.valueOf(this.posX), Double.valueOf(this.posY), Double.valueOf(this.posZ)}));
		p_85029_1_.addCrashSection("Entity\'s Block location", CrashReportCategory.getLocationInfo(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)));
		p_85029_1_.addCrashSection("Entity\'s Momentum", String.format("%.2f, %.2f, %.2f", new Object[] {Double.valueOf(this.motionX), Double.valueOf(this.motionY), Double.valueOf(this.motionZ)}));
	}

	@SideOnly(Side.CLIENT)
	public boolean canRenderOnFire()
	{
		return this.isBurning();
	}

	public UUID getUniqueID()
	{
		return this.entityUniqueID;
	}

	public boolean isPushedByWater()
	{
		return true;
	}

	public IChatComponent func_145748_c_()
	{
		return new ChatComponentText(this.getCommandSenderName());
	}

	public void func_145781_i(int p_145781_1_) {}

	/* ================================== Forge Start =====================================*/
	/**
	 * Returns a NBTTagCompound that can be used to store custom data for this entity.
	 * It will be written, and read from disc, so it persists over world saves.
	 * @return A NBTTagCompound
	 */
	public NBTTagCompound getEntityData()
	{
		if (customEntityData == null)
		{
			customEntityData = new NBTTagCompound();
		}
		return customEntityData;
	}

	/**
	 * Used in model rendering to determine if the entity riding this entity should be in the 'sitting' position.
	 * @return false to prevent an entity that is mounted to this entity from displaying the 'sitting' animation.
	 */
	public boolean shouldRiderSit()
	{
		return true;
	}

	/**
	 * Called when a user uses the creative pick block button on this entity.
	 *
	 * @param target The full target the player is looking at
	 * @return A ItemStack to add to the player's inventory, Null if nothing should be added.
	 */
	public ItemStack getPickedResult(MovingObjectPosition target)
	{
		if (this instanceof EntityPainting)
		{
			return new ItemStack(Items.painting);
		}
		else if (this instanceof EntityLeashKnot)
		{
			return new ItemStack(Items.lead);
		}
		else if (this instanceof EntityItemFrame)
		{
			ItemStack held = ((EntityItemFrame)this).getDisplayedItem();
			if (held == null)
			{
				return new ItemStack(Items.item_frame);
			}
			else
			{
				return held.copy();
			}
		}
		else if (this instanceof EntityMinecart)
		{
			return ((EntityMinecart)this).getCartItem();
		}
		else if (this instanceof EntityBoat)
		{
			return new ItemStack(Items.boat);
		}
		else
		{
			int id = EntityList.getEntityID(this);
			if (id > 0 && EntityList.entityEggs.containsKey(id))
			{
				return new ItemStack(Items.spawn_egg, 1, id);
			}
		}
		return null;
	}

	public UUID getPersistentID()
	{
		return entityUniqueID;
	}

	/**
	 * Reset the entity ID to a new value. Not to be used from Mod code
	 */
	public final void resetEntityId()
	{
		this.entityId = nextEntityID++;
	}

	public boolean shouldRenderInPass(int pass)
	{
		return pass == 0;
	}

	/**
	 * Returns true if the entity is of the @link{EnumCreatureType} provided
	 * @param type The EnumCreatureType type this entity is evaluating
	 * @param forSpawnCount If this is being invoked to check spawn count caps.
	 * @return If the creature is of the type provided
	 */
	public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount)
	{
		return type.getCreatureClass().isAssignableFrom(this.getClass());
	}

	/**
	 * Register the instance of IExtendedProperties into the entity's collection.
	 * @param identifier The identifier which you can use to retrieve these properties for the entity.
	 * @param properties The instanceof IExtendedProperties to register
	 * @return The identifier that was used to register the extended properties.  Empty String indicates an error.  If your requested key already existed, this will return a modified one that is unique.
	 */
	public String registerExtendedProperties(String identifier, IExtendedEntityProperties properties)
	{
		if (identifier == null)
		{
			FMLLog.warning("Someone is attempting to register extended properties using a null identifier.  This is not allowed.  Aborting.  This may have caused instability.");
			return "";
		}
		if (properties == null)
		{
			FMLLog.warning("Someone is attempting to register null extended properties.  This is not allowed.  Aborting.  This may have caused instability.");
			return "";
		}

		String baseIdentifier = identifier;
		int identifierModCount = 1;
		while (this.extendedProperties.containsKey(identifier))
		{
			identifier = String.format("%s%d", baseIdentifier, identifierModCount++);
		}

		if (baseIdentifier != identifier)
		{
			FMLLog.info("An attempt was made to register exended properties using an existing key.  The duplicate identifier (%s) has been remapped to %s.", baseIdentifier, identifier);
		}

		this.extendedProperties.put(identifier, properties);
		return identifier;
	}

	/**
	 * Gets the extended properties identified by the passed in key
	 * @param identifier The key that identifies the extended properties.
	 * @return The instance of IExtendedProperties that was found, or null.
	 */
	public IExtendedEntityProperties getExtendedProperties(String identifier)
	{
		return this.extendedProperties.get(identifier);
	}

	/**
	 * If a rider of this entity can interact with this entity. Should return true on the
	 * ridden entity if so.
	 *
	 * @return if the entity can be interacted with from a rider
	 */
	public boolean canRiderInteract()
	{
		return false;
	}

	/**
	 * If the rider should be dismounted from the entity when the entity goes under water
	 *
	 * @param rider The entity that is riding
	 * @return if the entity should be dismounted when under water
	 */
	public boolean shouldDismountInWater(Entity rider)
	{
		return this instanceof EntityLivingBase;
	}
	/* ================================== Forge End =====================================*/

	public static enum EnumEntitySize
	{
		SIZE_1,
		SIZE_2,
		SIZE_3,
		SIZE_4,
		SIZE_5,
		SIZE_6;

		private static final String __OBFID = "CL_00001537";

		public int multiplyBy32AndRound(double p_75630_1_)
		{
			double d1 = p_75630_1_ - ((double)MathHelper.floor_double(p_75630_1_) + 0.5D);

			switch (Entity.SwitchEnumEntitySize.field_96565_a[this.ordinal()])
			{
				case 1:
					if (d1 < 0.0D)
					{
						if (d1 < -0.3125D)
						{
							return MathHelper.ceiling_double_int(p_75630_1_ * 32.0D);
						}
					}
					else if (d1 < 0.3125D)
					{
						return MathHelper.ceiling_double_int(p_75630_1_ * 32.0D);
					}

					return MathHelper.floor_double(p_75630_1_ * 32.0D);
				case 2:
					if (d1 < 0.0D)
					{
						if (d1 < -0.3125D)
						{
							return MathHelper.floor_double(p_75630_1_ * 32.0D);
						}
					}
					else if (d1 < 0.3125D)
					{
						return MathHelper.floor_double(p_75630_1_ * 32.0D);
					}

					return MathHelper.ceiling_double_int(p_75630_1_ * 32.0D);
				case 3:
					if (d1 > 0.0D)
					{
						return MathHelper.floor_double(p_75630_1_ * 32.0D);
					}

					return MathHelper.ceiling_double_int(p_75630_1_ * 32.0D);
				case 4:
					if (d1 < 0.0D)
					{
						if (d1 < -0.1875D)
						{
							return MathHelper.ceiling_double_int(p_75630_1_ * 32.0D);
						}
					}
					else if (d1 < 0.1875D)
					{
						return MathHelper.ceiling_double_int(p_75630_1_ * 32.0D);
					}

					return MathHelper.floor_double(p_75630_1_ * 32.0D);
				case 5:
					if (d1 < 0.0D)
					{
						if (d1 < -0.1875D)
						{
							return MathHelper.floor_double(p_75630_1_ * 32.0D);
						}
					}
					else if (d1 < 0.1875D)
					{
						return MathHelper.floor_double(p_75630_1_ * 32.0D);
					}

					return MathHelper.ceiling_double_int(p_75630_1_ * 32.0D);
				case 6:
				default:
					if (d1 > 0.0D)
					{
						return MathHelper.ceiling_double_int(p_75630_1_ * 32.0D);
					}
					else
					{
						return MathHelper.floor_double(p_75630_1_ * 32.0D);
					}
			}
		}
	}

	static final class SwitchEnumEntitySize
		{
			static final int[] field_96565_a = new int[Entity.EnumEntitySize.values().length];
			private static final String __OBFID = "CL_00001536";

			static
			{
				try
				{
					field_96565_a[Entity.EnumEntitySize.SIZE_1.ordinal()] = 1;
				}
				catch (NoSuchFieldError var6)
				{
					;
				}

				try
				{
					field_96565_a[Entity.EnumEntitySize.SIZE_2.ordinal()] = 2;
				}
				catch (NoSuchFieldError var5)
				{
					;
				}

				try
				{
					field_96565_a[Entity.EnumEntitySize.SIZE_3.ordinal()] = 3;
				}
				catch (NoSuchFieldError var4)
				{
					;
				}

				try
				{
					field_96565_a[Entity.EnumEntitySize.SIZE_4.ordinal()] = 4;
				}
				catch (NoSuchFieldError var3)
				{
					;
				}

				try
				{
					field_96565_a[Entity.EnumEntitySize.SIZE_5.ordinal()] = 5;
				}
				catch (NoSuchFieldError var2)
				{
					;
				}

				try
				{
					field_96565_a[Entity.EnumEntitySize.SIZE_6.ordinal()] = 6;
				}
				catch (NoSuchFieldError var1)
				{
					;
				}
			}
		}
	
	/* ===================================== ULTRAMINE START =====================================*/
	
	private EntityType cachedEntityType;
	private GameProfile owner;
	public boolean removeThisTick;
	
	public final void setObjectOwner(GameProfile owner)
	{
		if(this.owner == null)
			this.owner = owner;
	}
	
	public final GameProfile getObjectOwner()
	{
		return this.owner;
	}
	
	public boolean isEntityLiving()
	{
		return false;
	}
	
	public boolean isEntityPlayer()
	{
		return false;
	}
	
	public boolean isEntityPlayerMP()
	{
		return false;
	}
	
	protected EntityType computeEntityType()
	{
		return
			isCreatureType(EnumCreatureType.monster,		false) ? EntityType.MONSTER : 
			isCreatureType(EnumCreatureType.creature,		false) ? EntityType.ANIMAL : 
			isCreatureType(EnumCreatureType.ambient,		false) ? EntityType.AMBIENT : 
			isCreatureType(EnumCreatureType.waterCreature,	false) ? EntityType.WATER :
			isCreatureType(EnumCreatureType.monster,		true) ? EntityType.MONSTER :
			isCreatureType(EnumCreatureType.creature,		true) ? EntityType.ANIMAL :
			isCreatureType(EnumCreatureType.ambient,		true) ? EntityType.AMBIENT :
			isCreatureType(EnumCreatureType.waterCreature,	true) ? EntityType.WATER :
			EntityType.OTHER;
	}
	
	public final EntityType getEntityType()
	{
		if(cachedEntityType == null)
			return cachedEntityType = computeEntityType();
		return cachedEntityType;
	}
	
	public final boolean isEntityMonster()
	{
		return getEntityType() == EntityType.MONSTER;
	}
	
	public final boolean isEntityAnimal()
	{
		return getEntityType() == EntityType.ANIMAL;
	}
	
	public final boolean isEntityAmbient()
	{
		return getEntityType() == EntityType.AMBIENT;
	}
	
	public final boolean isEntityWater()
	{
		return getEntityType() == EntityType.WATER;
	}
	
	public final boolean isEntityItem()
	{
		return getEntityType() == EntityType.ITEM;
	}
	
	public final boolean isEntityXPOrb()
	{
		return getEntityType() == EntityType.XP_ORB;
	}
	
	public double getEntityDespawnDistance()
	{
		return 9216d;//16384.0d;
	}
	
	public void updateInactive()
	{
		
	}
}

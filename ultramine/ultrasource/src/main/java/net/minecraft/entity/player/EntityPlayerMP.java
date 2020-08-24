package net.minecraft.entity.player;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.registry.LanguageRegistry;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.network.play.server.S31PacketWindowProperty;
import net.minecraft.network.play.server.S36PacketSignEditorOpen;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.JsonSerializableSet;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;

import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.service.Economy;
import org.ultramine.core.service.InjectService;
import org.ultramine.server.WorldConstants;
import org.ultramine.server.event.PlayerDeathEvent;
import org.ultramine.server.internal.UMHooks;
import org.ultramine.server.chunk.ChunkSendManager;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.internal.UMEventFactory;
import org.ultramine.server.util.BasicTypeParser;

import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import org.ultramine.core.permissions.Permissions;

public class EntityPlayerMP extends EntityPlayer implements ICrafting
{
	private static final Logger logger = LogManager.getLogger();
	private String translator = "en_US";
	public NetHandlerPlayServer playerNetServerHandler;
	public final MinecraftServer mcServer;
	public final ItemInWorldManager theItemInWorldManager;
	public double managedPosX;
	public double managedPosZ;
	public final List loadedChunks = new LinkedList();
	private final List destroyedItemsNetCache = new LinkedList();
	private StatisticsFile field_147103_bO;
	private float field_130068_bO = Float.MIN_VALUE;
	private float lastHealth = -1.0E8F;
	private int lastFoodLevel = -99999999;
	private boolean wasHungry = true;
	private int lastExperience = -99999999;
	private int field_147101_bU = 60;
	private EntityPlayer.EnumChatVisibility chatVisibility;
	private boolean chatColours = true;
	private long field_143005_bX = System.currentTimeMillis();
	public int currentWindowId;
	public boolean isChangingQuantityOnly;
	public int ping;
	public boolean playerConqueredTheEnd;
	private static final String __OBFID = "CL_00001440";

	public EntityPlayerMP(MinecraftServer p_i45285_1_, WorldServer p_i45285_2_, GameProfile p_i45285_3_, ItemInWorldManager p_i45285_4_)
	{
		super(p_i45285_2_, p_i45285_3_);
		p_i45285_4_.thisPlayerMP = this;
		this.theItemInWorldManager = p_i45285_4_;

		this.mcServer = p_i45285_1_;
		this.stepHeight = 0.0F;
		this.yOffset = 0.0F;
		
		renderDistance = p_i45285_1_.getConfigurationManager().getViewDistance();
	}

	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
		super.readEntityFromNBT(p_70037_1_);

		if (p_70037_1_.hasKey("playerGameType", 99))
		{
			if (MinecraftServer.getServer().getForceGamemode())
			{
				this.theItemInWorldManager.setGameType(MinecraftServer.getServer().getGameType());
			}
			else
			{
				this.theItemInWorldManager.setGameType(WorldSettings.GameType.getByID(p_70037_1_.getInteger("playerGameType")));
			}
		}
	}

	public void writeEntityToNBT(NBTTagCompound p_70014_1_)
	{
		super.writeEntityToNBT(p_70014_1_);
		p_70014_1_.setInteger("playerGameType", this.theItemInWorldManager.getGameType().getID());
	}

	public void addExperienceLevel(int p_82242_1_)
	{
		super.addExperienceLevel(p_82242_1_);
		this.lastExperience = -1;
	}

	public void addSelfToInternalCraftingInventory()
	{
		this.openContainer.addCraftingToCrafters(this);
	}

	protected void resetHeight()
	{
		this.yOffset = 0.0F;
	}

	public float getEyeHeight()
	{
		return super.getEyeHeight();
	}

	public void onUpdate()
	{
		this.theItemInWorldManager.updateBlockRemoving();
		--this.field_147101_bU;

		if (this.hurtResistantTime > 0)
		{
			--this.hurtResistantTime;
		}

		this.openContainer.detectAndSendChanges();

		if (!this.worldObj.isRemote && !ForgeHooks.canInteractWith(this, this.openContainer))
		{
			this.closeScreen();
			this.openContainer = this.inventoryContainer;
		}

		while (!this.destroyedItemsNetCache.isEmpty())
		{
			int i = Math.min(this.destroyedItemsNetCache.size(), 127);
			int[] aint = new int[i];
			Iterator iterator = this.destroyedItemsNetCache.iterator();
			int j = 0;

			while (iterator.hasNext() && j < i)
			{
				aint[j++] = ((Integer)iterator.next()).intValue();
				iterator.remove();
			}

			this.playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(aint));
		}
		
		getChunkMgr().update();

		if (this.field_143005_bX > 0L && this.mcServer.func_143007_ar() > 0 && MinecraftServer.getSystemTimeMillis() - this.field_143005_bX > (long)(this.mcServer.func_143007_ar() * 1000 * 60))
		{
			this.playerNetServerHandler.kickPlayerFromServer("You have been idle for too long!");
		}
	}

	public void onUpdateEntity()
	{
		try
		{
			super.onUpdate();

			for (int i = 0; i < this.inventory.getSizeInventory(); ++i)
			{
				ItemStack itemstack = this.inventory.getStackInSlot(i);

				if (itemstack != null && itemstack.getItem().isMap())
				{
					Packet packet = ((ItemMapBase)itemstack.getItem()).func_150911_c(itemstack, this.worldObj, this);

					if (packet != null)
					{
						this.playerNetServerHandler.sendPacket(packet);
					}
				}
			}

			if (this.getHealth() != this.lastHealth || this.lastFoodLevel != this.foodStats.getFoodLevel() || this.foodStats.getSaturationLevel() == 0.0F != this.wasHungry)
			{
				this.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(this.getHealth(), this.foodStats.getFoodLevel(), this.foodStats.getSaturationLevel()));
				this.lastHealth = this.getHealth();
				this.lastFoodLevel = this.foodStats.getFoodLevel();
				this.wasHungry = this.foodStats.getSaturationLevel() == 0.0F;
			}

			if (this.getHealth() + this.getAbsorptionAmount() != this.field_130068_bO)
			{
				this.field_130068_bO = this.getHealth() + this.getAbsorptionAmount();
				Collection collection = this.getWorldScoreboard().func_96520_a(IScoreObjectiveCriteria.health);
				Iterator iterator = collection.iterator();

				while (iterator.hasNext())
				{
					ScoreObjective scoreobjective = (ScoreObjective)iterator.next();
					this.getWorldScoreboard().func_96529_a(this.getCommandSenderName(), scoreobjective).func_96651_a(Arrays.asList(new EntityPlayer[] {this}));
				}
			}

			if (this.experienceTotal != this.lastExperience)
			{
				this.lastExperience = this.experienceTotal;
				this.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(this.experience, this.experienceTotal, this.experienceLevel));
			}

			if (this.ticksExisted % 20 * 5 == 0 && !this.func_147099_x().hasAchievementUnlocked(AchievementList.field_150961_L))
			{
				this.func_147098_j();
			}
		}
		catch (Throwable throwable)
		{
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking player");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
			this.addEntityCrashInfo(crashreportcategory);
			throw new ReportedException(crashreport);
		}
	}

	protected void func_147098_j()
	{
		BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ));

		if (biomegenbase != null)
		{
			String s = biomegenbase.biomeName;
			JsonSerializableSet jsonserializableset = (JsonSerializableSet)this.func_147099_x().func_150870_b(AchievementList.field_150961_L);

			if (jsonserializableset == null)
			{
				jsonserializableset = (JsonSerializableSet)this.func_147099_x().func_150872_a(AchievementList.field_150961_L, new JsonSerializableSet());
			}

			jsonserializableset.add(s);

			if (this.func_147099_x().canUnlockAchievement(AchievementList.field_150961_L) && jsonserializableset.size() == BiomeGenBase.explorationBiomesList.size())
			{
				HashSet hashset = Sets.newHashSet(BiomeGenBase.explorationBiomesList);
				Iterator iterator = jsonserializableset.iterator();

				while (iterator.hasNext())
				{
					String s1 = (String)iterator.next();
					Iterator iterator1 = hashset.iterator();

					while (iterator1.hasNext())
					{
						BiomeGenBase biomegenbase1 = (BiomeGenBase)iterator1.next();

						if (biomegenbase1.biomeName.equals(s1))
						{
							iterator1.remove();
						}
					}

					if (hashset.isEmpty())
					{
						break;
					}
				}

				if (hashset.isEmpty())
				{
					this.triggerAchievement(AchievementList.field_150961_L);
				}
			}
		}
	}

	public void onDeath(DamageSource p_70645_1_)
	{
		if (ForgeHooks.onLivingDeath(this, p_70645_1_)) return;
		PlayerDeathEvent umEvent = UMEventFactory.firePlayerDeath(this, p_70645_1_, this.func_110142_aN().func_151521_b(),
				this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory"));
		if(umEvent.getDeathMessage() != null)
			this.mcServer.getConfigurationManager().sendChatMsg(umEvent.getDeathMessage());

		if (!umEvent.isKeepInventory() && umEvent.isProcessDrops())
		{
			captureDrops = true;
			capturedDrops.clear();

			this.inventory.dropAllItems();

			captureDrops = false;
			PlayerDropsEvent event = new PlayerDropsEvent(this, p_70645_1_, capturedDrops, recentlyHit > 0);
			if (!MinecraftForge.EVENT_BUS.post(event))
			{
				for (EntityItem item : capturedDrops)
				{
					joinEntityItemWithWorld(item);
				}
			}
		}
		else
		{
			if(umEvent.isKeepInventory())
				keepInventoryOnClone = true;
		}

		Collection collection = this.worldObj.getScoreboard().func_96520_a(IScoreObjectiveCriteria.deathCount);
		Iterator iterator = collection.iterator();

		while (iterator.hasNext())
		{
			ScoreObjective scoreobjective = (ScoreObjective)iterator.next();
			Score score = this.getWorldScoreboard().func_96529_a(this.getCommandSenderName(), scoreobjective);
			score.func_96648_a();
		}

		EntityLivingBase entitylivingbase = this.func_94060_bK();

		if (entitylivingbase != null)
		{
			int i = EntityList.getEntityID(entitylivingbase);
			EntityList.EntityEggInfo entityegginfo = (EntityList.EntityEggInfo)EntityList.entityEggs.get(Integer.valueOf(i));

			if (entityegginfo != null)
			{
				this.addStat(entityegginfo.field_151513_e, 1);
			}

			entitylivingbase.addToPlayerScore(this, this.scoreValue);
		}

		this.addStat(StatList.deathsStat, 1);
		this.func_110142_aN().func_94549_h();
	}

	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		if (this.isEntityInvulnerable())
		{
			return false;
		}
		else
		{
			boolean flag = this.mcServer.isDedicatedServer() && getServerForPlayer().getConfig().settings.pvp && "fall".equals(p_70097_1_.damageType);

			if (!flag && this.field_147101_bU > 0 && p_70097_1_ != DamageSource.outOfWorld)
			{
				return false;
			}
			else
			{
				if (p_70097_1_ instanceof EntityDamageSource)
				{
					Entity entity = p_70097_1_.getEntity();

					if (entity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)entity))
					{
						return false;
					}

					if (entity instanceof EntityArrow)
					{
						EntityArrow entityarrow = (EntityArrow)entity;

						if (entityarrow.shootingEntity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)entityarrow.shootingEntity))
						{
							return false;
						}
					}
				}

				return super.attackEntityFrom(p_70097_1_, p_70097_2_);
			}
		}
	}

	public boolean canAttackPlayer(EntityPlayer p_96122_1_)
	{
		return !getServerForPlayer().getConfig().settings.pvp ? false : super.canAttackPlayer(p_96122_1_);
	}

	public void travelToDimension(int p_71027_1_)
	{
		if(mcServer.worldServerForDimension(p_71027_1_) == null)
			return;
		int enderLink = ((WorldServer)worldObj).getConfig().portals.enderLink;
		if (this.dimension == enderLink && p_71027_1_ == enderLink)
		{
			this.triggerAchievement(AchievementList.theEnd2);
			this.worldObj.removeEntity(this);
			this.playerConqueredTheEnd = true;
			this.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(4, 0.0F));
		}
		else
		{
			if (p_71027_1_ == enderLink)
			{
				this.triggerAchievement(AchievementList.theEnd);
				ChunkCoordinates chunkcoordinates = this.mcServer.worldServerForDimension(p_71027_1_).getEntrancePortalLocation();

				if (chunkcoordinates != null)
				{
					this.playerNetServerHandler.setPlayerLocation((double)chunkcoordinates.posX, (double)chunkcoordinates.posY, (double)chunkcoordinates.posZ, 0.0F, 0.0F);
				}
			}
			else
			{
				this.triggerAchievement(AchievementList.portal);
			}

			this.mcServer.getConfigurationManager().transferPlayerToDimension(this, p_71027_1_);
			this.lastExperience = -1;
			this.lastHealth = -1.0F;
			this.lastFoodLevel = -1;
		}
	}

	private void func_147097_b(TileEntity p_147097_1_)
	{
		if (p_147097_1_ != null)
		{
			Packet packet = p_147097_1_.getDescriptionPacket();

			if (packet != null)
			{
				this.playerNetServerHandler.sendPacket(packet);
			}
		}
	}

	public void onItemPickup(Entity p_71001_1_, int p_71001_2_)
	{
		super.onItemPickup(p_71001_1_, p_71001_2_);
//		this.openContainer.detectAndSendChanges();
	}

	public EntityPlayer.EnumStatus sleepInBedAt(int p_71018_1_, int p_71018_2_, int p_71018_3_)
	{
		EntityPlayer.EnumStatus enumstatus = super.sleepInBedAt(p_71018_1_, p_71018_2_, p_71018_3_);

		if (enumstatus == EntityPlayer.EnumStatus.OK)
		{
			S0APacketUseBed s0apacketusebed = new S0APacketUseBed(this, p_71018_1_, p_71018_2_, p_71018_3_);
			this.getServerForPlayer().getEntityTracker().func_151247_a(this, s0apacketusebed);
			this.playerNetServerHandler.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.playerNetServerHandler.sendPacket(s0apacketusebed);
		}

		return enumstatus;
	}

	public void wakeUpPlayer(boolean p_70999_1_, boolean p_70999_2_, boolean p_70999_3_)
	{
		if (this.isPlayerSleeping())
		{
			this.getServerForPlayer().getEntityTracker().func_151248_b(this, new S0BPacketAnimation(this, 2));
		}

		super.wakeUpPlayer(p_70999_1_, p_70999_2_, p_70999_3_);

		if (this.playerNetServerHandler != null)
		{
			this.playerNetServerHandler.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
		}
	}

	public void mountEntity(Entity p_70078_1_)
	{
		super.mountEntity(p_70078_1_);
		this.playerNetServerHandler.sendPacket(new S1BPacketEntityAttach(0, this, this.ridingEntity));
		this.playerNetServerHandler.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
	}

	protected void updateFallState(double p_70064_1_, boolean p_70064_3_) {}

	public void handleFalling(double p_71122_1_, boolean p_71122_3_)
	{
		super.updateFallState(p_71122_1_, p_71122_3_);
	}

	public void func_146100_a(TileEntity p_146100_1_)
	{
		if (p_146100_1_ instanceof TileEntitySign)
		{
			((TileEntitySign)p_146100_1_).func_145912_a(this);
			this.playerNetServerHandler.sendPacket(new S36PacketSignEditorOpen(p_146100_1_.xCoord, p_146100_1_.yCoord, p_146100_1_.zCoord));
		}
	}

	public void getNextWindowId()
	{
		this.currentWindowId = this.currentWindowId % 100 + 1;
	}

	public void displayGUIWorkbench(int p_71058_1_, int p_71058_2_, int p_71058_3_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 1, "Crafting", 9, true));
		this.openContainer = new ContainerWorkbench(this.inventory, this.worldObj, p_71058_1_, p_71058_2_, p_71058_3_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void displayGUIEnchantment(int p_71002_1_, int p_71002_2_, int p_71002_3_, String p_71002_4_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 4, p_71002_4_ == null ? "" : p_71002_4_, 9, p_71002_4_ != null));
		this.openContainer = new ContainerEnchantment(this.inventory, this.worldObj, p_71002_1_, p_71002_2_, p_71002_3_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void displayGUIAnvil(int p_82244_1_, int p_82244_2_, int p_82244_3_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 8, "Repairing", 9, true));
		this.openContainer = new ContainerRepair(this.inventory, this.worldObj, p_82244_1_, p_82244_2_, p_82244_3_, this);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void displayGUIChest(IInventory p_71007_1_)
	{
		if (this.openContainer != this.inventoryContainer)
		{
			this.closeScreen();
		}

		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 0, p_71007_1_.getInventoryName(), p_71007_1_.getSizeInventory(), p_71007_1_.hasCustomInventoryName()));
		this.openContainer = new ContainerChest(this.inventory, p_71007_1_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void func_146093_a(TileEntityHopper p_146093_1_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 9, p_146093_1_.getInventoryName(), p_146093_1_.getSizeInventory(), p_146093_1_.hasCustomInventoryName()));
		this.openContainer = new ContainerHopper(this.inventory, p_146093_1_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void displayGUIHopperMinecart(EntityMinecartHopper p_96125_1_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 9, p_96125_1_.getInventoryName(), p_96125_1_.getSizeInventory(), p_96125_1_.hasCustomInventoryName()));
		this.openContainer = new ContainerHopper(this.inventory, p_96125_1_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void func_146101_a(TileEntityFurnace p_146101_1_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 2, p_146101_1_.getInventoryName(), p_146101_1_.getSizeInventory(), p_146101_1_.hasCustomInventoryName()));
		this.openContainer = new ContainerFurnace(this.inventory, p_146101_1_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void func_146102_a(TileEntityDispenser p_146102_1_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, p_146102_1_ instanceof TileEntityDropper ? 10 : 3, p_146102_1_.getInventoryName(), p_146102_1_.getSizeInventory(), p_146102_1_.hasCustomInventoryName()));
		this.openContainer = new ContainerDispenser(this.inventory, p_146102_1_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void func_146098_a(TileEntityBrewingStand p_146098_1_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 5, p_146098_1_.getInventoryName(), p_146098_1_.getSizeInventory(), p_146098_1_.hasCustomInventoryName()));
		this.openContainer = new ContainerBrewingStand(this.inventory, p_146098_1_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void func_146104_a(TileEntityBeacon p_146104_1_)
	{
		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 7, p_146104_1_.getInventoryName(), p_146104_1_.getSizeInventory(), p_146104_1_.hasCustomInventoryName()));
		this.openContainer = new ContainerBeacon(this.inventory, p_146104_1_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void displayGUIMerchant(IMerchant p_71030_1_, String p_71030_2_)
	{
		this.getNextWindowId();
		this.openContainer = new ContainerMerchant(this.inventory, p_71030_1_, this.worldObj);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
		InventoryMerchant inventorymerchant = ((ContainerMerchant)this.openContainer).getMerchantInventory();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 6, p_71030_2_ == null ? "" : p_71030_2_, inventorymerchant.getSizeInventory(), p_71030_2_ != null));
		MerchantRecipeList merchantrecipelist = p_71030_1_.getRecipes(this);

		if (merchantrecipelist != null)
		{
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());

			try
			{
				packetbuffer.writeInt(this.currentWindowId);
				merchantrecipelist.func_151391_a(packetbuffer);
				this.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|TrList", packetbuffer));
			}
			catch (IOException ioexception)
			{
				logger.error("Couldn\'t send trade list", ioexception);
			}
			finally
			{
				packetbuffer.release();
			}
		}
	}

	public void displayGUIHorse(EntityHorse p_110298_1_, IInventory p_110298_2_)
	{
		if (this.openContainer != this.inventoryContainer)
		{
			this.closeScreen();
		}

		this.getNextWindowId();
		this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 11, p_110298_2_.getInventoryName(), p_110298_2_.getSizeInventory(), p_110298_2_.hasCustomInventoryName(), p_110298_1_.getEntityId()));
		this.openContainer = new ContainerHorseInventory(this.inventory, p_110298_2_, p_110298_1_);
		this.openContainer.windowId = this.currentWindowId;
		this.openContainer.addCraftingToCrafters(this);
	}

	public void sendSlotContents(Container p_71111_1_, int p_71111_2_, ItemStack p_71111_3_)
	{
		if (!(p_71111_1_.getSlot(p_71111_2_) instanceof SlotCrafting))
		{
			if (!this.isChangingQuantityOnly)
			{
				this.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(p_71111_1_.windowId, p_71111_2_, p_71111_3_));
			}
		}
	}

	public void sendContainerToPlayer(Container p_71120_1_)
	{
		this.sendContainerAndContentsToPlayer(p_71120_1_, p_71120_1_.getInventory());
	}

	public void sendContainerAndContentsToPlayer(Container p_71110_1_, List p_71110_2_)
	{
		this.playerNetServerHandler.sendPacket(new S30PacketWindowItems(p_71110_1_.windowId, p_71110_2_));
		this.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, this.inventory.getItemStack()));
	}

	public void sendProgressBarUpdate(Container p_71112_1_, int p_71112_2_, int p_71112_3_)
	{
		this.playerNetServerHandler.sendPacket(new S31PacketWindowProperty(p_71112_1_.windowId, p_71112_2_, p_71112_3_));
	}

	public void closeScreen()
	{
		this.playerNetServerHandler.sendPacket(new S2EPacketCloseWindow(this.openContainer.windowId));
		this.closeContainer();
	}

	public void updateHeldItem()
	{
		if (!this.isChangingQuantityOnly)
		{
			this.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, this.inventory.getItemStack()));
		}
	}

	public void closeContainer()
	{
		UMEventFactory.fireInventoryClose(this);
		this.openContainer.onContainerClosed(this);
		this.openContainer = this.inventoryContainer;
	}

	public void setEntityActionState(float p_110430_1_, float p_110430_2_, boolean p_110430_3_, boolean p_110430_4_)
	{
		if (this.ridingEntity != null)
		{
			if (p_110430_1_ >= -1.0F && p_110430_1_ <= 1.0F)
			{
				this.moveStrafing = p_110430_1_;
			}

			if (p_110430_2_ >= -1.0F && p_110430_2_ <= 1.0F)
			{
				this.moveForward = p_110430_2_;
			}

			this.isJumping = p_110430_3_;
			this.setSneaking(p_110430_4_);
		}
	}

	public void addStat(StatBase p_71064_1_, int p_71064_2_)
	{
		if (p_71064_1_ != null && field_147103_bO != null)
		{
			if (p_71064_1_.isAchievement() && MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.AchievementEvent(this, (net.minecraft.stats.Achievement) p_71064_1_))) return;
			this.field_147103_bO.func_150871_b(this, p_71064_1_, p_71064_2_);
			Iterator iterator = this.getWorldScoreboard().func_96520_a(p_71064_1_.func_150952_k()).iterator();

			while (iterator.hasNext())
			{
				ScoreObjective scoreobjective = (ScoreObjective)iterator.next();
				this.getWorldScoreboard().func_96529_a(this.getCommandSenderName(), scoreobjective).func_96648_a();
			}

			if (this.field_147103_bO.func_150879_e())
			{
				this.field_147103_bO.func_150876_a(this);
			}
		}
	}

	public void mountEntityAndWakeUp()
	{
		if (this.riddenByEntity != null)
		{
			this.riddenByEntity.mountEntity(this);
		}

		if (this.sleeping)
		{
			this.wakeUpPlayer(true, false, false);
		}
	}

	public void setPlayerHealthUpdated()
	{
		this.lastHealth = -1.0E8F;
	}

	public void addChatComponentMessage(IChatComponent p_146105_1_)
	{
		this.playerNetServerHandler.sendPacket(new S02PacketChat(UMHooks.onChatSend(this, p_146105_1_)));
	}

	protected void onItemUseFinish()
	{
		this.playerNetServerHandler.sendPacket(new S19PacketEntityStatus(this, (byte)9));
		super.onItemUseFinish();
	}

	public void setItemInUse(ItemStack p_71008_1_, int p_71008_2_)
	{
		super.setItemInUse(p_71008_1_, p_71008_2_);

		if (p_71008_1_ != null && p_71008_1_.getItem() != null && p_71008_1_.getItem().getItemUseAction(p_71008_1_) == EnumAction.eat)
		{
			this.getServerForPlayer().getEntityTracker().func_151248_b(this, new S0BPacketAnimation(this, 3));
		}
	}

	public void clonePlayer(EntityPlayer p_71049_1_, boolean p_71049_2_)
	{
		super.clonePlayer(p_71049_1_, p_71049_2_);
		this.lastExperience = -1;
		this.lastHealth = -1.0F;
		this.lastFoodLevel = -1;
		this.destroyedItemsNetCache.addAll(((EntityPlayerMP)p_71049_1_).destroyedItemsNetCache);
		this.translator = ((EntityPlayerMP)p_71049_1_).translator;
		this.renderDistance = ((EntityPlayerMP)p_71049_1_).renderDistance;
		this.chatVisibility = ((EntityPlayerMP)p_71049_1_).chatVisibility;
		this.chatColours = ((EntityPlayerMP)p_71049_1_).chatColours;
	}

	protected void onNewPotionEffect(PotionEffect p_70670_1_)
	{
		super.onNewPotionEffect(p_70670_1_);
		this.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(this.getEntityId(), p_70670_1_));
	}

	protected void onChangedPotionEffect(PotionEffect p_70695_1_, boolean p_70695_2_)
	{
		super.onChangedPotionEffect(p_70695_1_, p_70695_2_);
		this.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(this.getEntityId(), p_70695_1_));
	}

	protected void onFinishedPotionEffect(PotionEffect p_70688_1_)
	{
		super.onFinishedPotionEffect(p_70688_1_);
		this.playerNetServerHandler.sendPacket(new S1EPacketRemoveEntityEffect(this.getEntityId(), p_70688_1_));
	}

	public void setPositionAndUpdate(double p_70634_1_, double p_70634_3_, double p_70634_5_)
	{
		this.playerNetServerHandler.setPlayerLocation(p_70634_1_, p_70634_3_, p_70634_5_, this.rotationYaw, this.rotationPitch);
	}

	public void onCriticalHit(Entity p_71009_1_)
	{
		this.getServerForPlayer().getEntityTracker().func_151248_b(this, new S0BPacketAnimation(p_71009_1_, 4));
	}

	public void onEnchantmentCritical(Entity p_71047_1_)
	{
		this.getServerForPlayer().getEntityTracker().func_151248_b(this, new S0BPacketAnimation(p_71047_1_, 5));
	}

	public void sendPlayerAbilities()
	{
		if (this.playerNetServerHandler != null)
		{
			this.playerNetServerHandler.sendPacket(new S39PacketPlayerAbilities(this.capabilities));
		}
	}

	public WorldServer getServerForPlayer()
	{
		return (WorldServer)this.worldObj;
	}

	public void setGameType(WorldSettings.GameType p_71033_1_)
	{
		this.theItemInWorldManager.setGameType(p_71033_1_);
		this.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(3, (float)p_71033_1_.getID()));
	}

	public void addChatMessage(IChatComponent p_145747_1_)
	{
		this.playerNetServerHandler.sendPacket(new S02PacketChat(UMHooks.onChatSend(this, p_145747_1_)));
	}

	public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_)
	{
		return true;
	}

	public String getPlayerIP()
	{
		String s = this.playerNetServerHandler.netManager.getSocketAddress().toString();
		s = s.substring(s.indexOf("/") + 1);
		s = s.substring(0, s.indexOf(":"));
		return s;
	}

	public void func_147100_a(C15PacketClientSettings p_147100_1_)
	{
		this.translator = p_147100_1_.func_149524_c();
		int i = /*256 >>*/ p_147100_1_.func_149521_d();

		this.renderDistance = MathHelper.clamp_int(i, 3, WorldConstants.MAX_VIEW_DISTANCE);

		this.chatVisibility = p_147100_1_.func_149523_e();
		this.chatColours = p_147100_1_.func_149520_f();

		if (this.mcServer.isSinglePlayer() && this.mcServer.getServerOwner().equals(this.getCommandSenderName()))
		{
			this.mcServer.func_147139_a(p_147100_1_.func_149518_g());
		}

		this.setHideCape(1, !p_147100_1_.func_149519_h());
	}

	public EntityPlayer.EnumChatVisibility func_147096_v()
	{
		return this.chatVisibility;
	}

	public void requestTexturePackLoad(String p_147095_1_)
	{
		this.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|RPack", p_147095_1_.getBytes(Charsets.UTF_8)));
	}

	public ChunkCoordinates getPlayerCoordinates()
	{
		return new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY + 0.5D), MathHelper.floor_double(this.posZ));
	}

	public void func_143004_u()
	{
		this.field_143005_bX = MinecraftServer.getSystemTimeMillis();
	}

	public StatisticsFile func_147099_x()
	{
		return this.field_147103_bO;
	}

	public void func_152339_d(Entity p_152339_1_)
	{
		if (p_152339_1_ instanceof EntityPlayer)
		{
			this.playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(new int[] {p_152339_1_.getEntityId()}));
		}
		else
		{
			this.destroyedItemsNetCache.add(Integer.valueOf(p_152339_1_.getEntityId()));
		}
	}

	public long func_154331_x()
	{
		return this.field_143005_bX;
	}

	/* ===================================== FORGE START =====================================*/
	/**
	 * Returns the default eye height of the player
	 * @return player default eye height
	 */
	@Override
	public float getDefaultEyeHeight()
	{
		return 1.62F;
	}
	
	/* ===================================== ULTRAMINE START =====================================*/

	private int renderDistance;
	private final ChunkSendManager chunkMgr = new ChunkSendManager(this);
	private PlayerData playerData;
	@InjectService private static Permissions perms;
	@InjectService private static Economy economy;

	public boolean hasPermission(String permission)
	{
		return perms.has(this, permission);
	}

	public String getMeta(String key)
	{
		return perms.getMeta(this, key);
	}

	public Account getAccount()
	{
		return economy.getPlayerAccount(this);
	}
	
	@Override
	public boolean isEntityPlayerMP()
    {
    	return true;
    }
	
	public ChunkSendManager getChunkMgr()
	{
		return chunkMgr;
	}
	
	public int getRenderDistance()
	{
		return renderDistance;
	}

	public String getTranslator()
	{
		return translator;
	}
	
	public PlayerData getData()
	{
		return playerData;
	}
	
	public void setData(PlayerData playerData)
	{
		playerData.setProfile(getGameProfile());
		this.playerData = playerData;
	}
	
	public String getTabListName()
	{
		String meta = getMeta("tablistcolor");
		EnumChatFormatting color = meta.isEmpty() ? null : BasicTypeParser.parseColor(meta);
		String name = color == null ? getCommandSenderName() : color.toString() + getCommandSenderName();
		return name.length() > 16 ? name.substring(0, 16) : name;
	}

	public String translate(String key)
	{
		String translated = LanguageRegistry.instance().getStringLocalization(key, getTranslator());
		if(translated.isEmpty())
			translated = LanguageRegistry.instance().getStringLocalization(key, "en_US");
		return translated.isEmpty() ? key : translated;
	}
	
	/**
	 * Переносит игрока в другой мир без использования порталов. Обратите
	 * внимение: сначала нужно установить координаты назначения
	 * <code>setPosition()</code>, а потом уже переносить в другой мир.
	 */
	public void transferToDimension(int dim)
	{
		this.mcServer.getConfigurationManager().transferPlayerToDimension(this, dim, null);
		this.lastExperience = -1;
		this.lastHealth = -1.0F;
		this.lastFoodLevel = -1;
	}
	
	/** Safe transferToDimension and setPosition */
	public boolean setWorldPosition(int dim, double x, double y, double z)
	{
		if(dim == this.dimension)
		{
			playerNetServerHandler.setPlayerLocation(x, y, z, this.rotationYaw, this.rotationPitch);
			return true;
		}
		if(mcServer.worldServerForDimension(dim) == null)
			return false;
		int lastDim = this.dimension;
		double lastX = this.posX;
		double lastY = this.posY;
		double lastZ = this.posZ;
		setPosition(x, y, z);
		try {
			transferToDimension(dim);
		} catch (RuntimeException e) {
			setPosition(lastX, lastY, lastZ);
			this.dimension = lastDim;
			throw e;
		}
		return true;
	}
	
	/** Safe transferToDimension and setPositionAndRotation */
	public boolean setWorldPositionAndRotation(int dim, double x, double y, double z, float yaw, float pitch)
	{
		if(dim == this.dimension)
		{
			playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch);
			return true;
		}
		if(mcServer.worldServerForDimension(dim) == null)
			return false;
		int lastDim = this.dimension;
		double lastX = this.posX;
		double lastY = this.posY;
		double lastZ = this.posZ;
		float lastYaw = this.rotationYaw;
		float lastPitch = this.rotationPitch;
		setPositionAndRotation(x, y, z, yaw, pitch);
		try {
			transferToDimension(dim);
		} catch (RuntimeException e) {
			setPositionAndRotation(lastX, lastY, lastZ, lastYaw, lastPitch);
			this.dimension = lastDim;
			throw e;
		}
		return true;
	}
	
	public void setStatisticsFile(StatisticsFile stats)
	{
		this.field_147103_bO = stats;
	}
	
	public void hide()
	{
		if(!isHidden())
		{
			getData().core().setHidden(true);
			((WorldServer)worldObj).getEntityTracker().hidePlayer(this);
			mcServer.getConfigurationManager().sendPacketToAllPlayers(new S38PacketPlayerListItem(getTabListName(), false, 9999));
		}
	}
	
	public void show()
	{
		if(isHidden())
		{
			getData().core().setHidden(false);
			((WorldServer)worldObj).getEntityTracker().showPlayer(this);
			mcServer.getConfigurationManager().sendPacketToAllPlayers(new S38PacketPlayerListItem(getTabListName(), true, ping));
		}
	}
	
	public boolean isHidden()
	{
		return getData() != null && getData().core().isHidden();
	}
}

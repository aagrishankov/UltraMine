package net.minecraft.server.dedicated;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.IOException;

import net.minecraft.server.management.ServerConfigurationManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.ConfigurationHandler;

@SideOnly(Side.SERVER)
public class DedicatedPlayerList extends ServerConfigurationManager
{
	private static final Logger field_164439_d = LogManager.getLogger();
	private static final String __OBFID = "CL_00001783";

	public DedicatedPlayerList(DedicatedServer p_i1503_1_)
	{
		super(p_i1503_1_);
		this.func_152611_a(ConfigurationHandler.getWorldsConfig().global.chunkLoading.viewDistance);
		this.maxPlayers = ConfigurationHandler.getServerConfig().settings.player.maxPlayers;
		this.setWhiteListEnabled(ConfigurationHandler.getServerConfig().settings.player.whiteList);

		if (!p_i1503_1_.isSinglePlayer())
		{
			this.func_152608_h().func_152686_a(true);
			this.getBannedIPs().func_152686_a(true);
		}

		this.func_152620_y();
		this.func_152617_w();
		this.func_152619_x();
		this.func_152618_v();
		this.readWhiteList();
		this.loadOpsList();

		if (!this.func_152599_k().func_152691_c().exists())
		{
			this.saveWhiteList();
		}
	}

	public void setWhiteListEnabled(boolean p_72371_1_)
	{
		super.setWhiteListEnabled(p_72371_1_);
		ConfigurationHandler.getServerConfig().settings.player.whiteList = p_72371_1_;
		this.getServerInstance().saveProperties();
	}

	public void func_152605_a(GameProfile p_152605_1_)
	{
		super.func_152605_a(p_152605_1_);
	}

	public void func_152610_b(GameProfile p_152610_1_)
	{
		super.func_152610_b(p_152610_1_);
	}

	public void func_152597_c(GameProfile p_152597_1_)
	{
		super.func_152597_c(p_152597_1_);
		this.saveWhiteList();
	}

	public void func_152601_d(GameProfile p_152601_1_)
	{
		super.func_152601_d(p_152601_1_);
		this.saveWhiteList();
	}

	public void loadWhiteList()
	{
		this.readWhiteList();
	}

	private void func_152618_v()
	{
		try
		{
			this.getBannedIPs().func_152678_f();
		}
		catch (IOException ioexception)
		{
			field_164439_d.warn("Failed to save ip banlist: ", ioexception);
		}
	}

	private void func_152617_w()
	{
		try
		{
			this.func_152608_h().func_152678_f();
		}
		catch (IOException ioexception)
		{
			field_164439_d.warn("Failed to save user banlist: ", ioexception);
		}
	}

	private void func_152619_x()
	{
		try
		{
			this.getBannedIPs().func_152679_g();
		}
		catch (IOException ioexception)
		{
			field_164439_d.warn("Failed to load ip banlist: ", ioexception);
		}
	}

	private void func_152620_y()
	{
		try
		{
			this.func_152608_h().func_152679_g();
		}
		catch (IOException ioexception)
		{
			field_164439_d.warn("Failed to load user banlist: ", ioexception);
		}
	}

	private void loadOpsList()
	{
		try
		{
			this.func_152603_m().func_152679_g();
		}
		catch (Exception exception)
		{
			field_164439_d.warn("Failed to load operators list: ", exception);
		}
	}

	private void saveOpsList()
	{
		try
		{
			this.func_152603_m().func_152678_f();
		}
		catch (Exception exception)
		{
			field_164439_d.warn("Failed to save operators list: ", exception);
		}
	}

	private void readWhiteList()
	{
		try
		{
			this.func_152599_k().func_152679_g();
		}
		catch (Exception exception)
		{
			field_164439_d.warn("Failed to load white-list: ", exception);
		}
	}

	private void saveWhiteList()
	{
		try
		{
			this.func_152599_k().func_152678_f();
		}
		catch (Exception exception)
		{
			field_164439_d.warn("Failed to save white-list: ", exception);
		}
	}

	public boolean func_152607_e(GameProfile p_152607_1_)
	{
		/*
		par1Str = par1Str.trim().toLowerCase();
		return !this.isWhiteListEnabled()
				|| this.getWhiteListedPlayers().contains(par1Str)
				|| PermissionHandler.getInstance().hasGlobally(par1Str, MinecraftPermissions.IGNORE_WHITE_LIST);
		*/
		return !this.isWhiteListEnabled() || this.func_152596_g(p_152607_1_) || this.func_152599_k().func_152705_a(p_152607_1_);
	}

	public DedicatedServer getServerInstance()
	{
		return (DedicatedServer)super.getServerInstance();
	}
}
package org.ultramine.server.data;

import gnu.trove.TCollections;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.ServerConfigurationManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.data.player.PlayerDataExtension;
import org.ultramine.server.data.player.PlayerDataExtensionInfo;
import org.ultramine.server.util.GlobalExecutors;
import org.ultramine.server.util.WarpLocation;

import com.mojang.authlib.GameProfile;

public class JDBCDataProvider implements IDataProvider
{
	private static final Logger log = LogManager.getLogger();

	private final ServerConfigurationManager mgr;

	private final DataSource ds;

	private final String tab_player_ids;
	private final String tab_player_gamedata;
	private final String tab_player_data;
	private final String tab_warps;
	private final String tab_fastwarps;

	private final TObjectIntMap<UUID> playerIDs = TCollections.synchronizedMap(new TObjectIntHashMap<UUID>(128, 0.75F, -1));

	public JDBCDataProvider(ServerConfigurationManager mgr)
	{
		this.mgr = mgr;
		
		String tablePrefix = ConfigurationHandler.getServerConfig().settings.inSQLServerStorage.tablePrefix;
		tab_player_ids = tablePrefix + "player_ids";
		tab_player_gamedata = tablePrefix + "player_gamedata";
		tab_player_data = tablePrefix + "player_data";
		tab_warps = tablePrefix + "warps";
		tab_fastwarps = tablePrefix + "warps_fast";
		
		ds = Databases.getDataSource(ConfigurationHandler.getServerConfig().settings.inSQLServerStorage.database);
	}

	@Override
	public void init()
	{
		Connection conn = null;
		Statement s = null;
		try
		{
			conn = ds.getConnection();
			s = conn.createStatement();
			
			s.execute("CREATE TABLE IF NOT EXISTS `"+tab_player_ids+"` ("
					+ "`pid` int(11) unsigned NOT NULL AUTO_INCREMENT,"
					+ "`uuid` binary(16) NOT NULL,"
					+ "PRIMARY KEY (`pid`),"
					+ "UNIQUE KEY `uuid` (`uuid`)"
					+ ") ENGINE=MyISAM ROW_FORMAT=FIXED");
			
			s.execute("CREATE TABLE IF NOT EXISTS `"+tab_player_gamedata+"` ("
					+ "`pid` int(11) unsigned NOT NULL AUTO_INCREMENT,"
					+ "`forDim` int(11) NOT NULL,"
					+ "`curDim` int(11) NOT NULL,"
					+ "`data` blob NOT NULL,"
					+ "PRIMARY KEY (`pid`, `forDim`)"
					+ ") ENGINE=InnoDB");
			
			s.execute("CREATE TABLE IF NOT EXISTS `"+tab_player_data+"` ("
					+ "`pid` int(11) unsigned NOT NULL AUTO_INCREMENT,"
					+ "`data` blob NOT NULL,"
					+ "PRIMARY KEY (`pid`)"
					+ ") ENGINE=InnoDB");
			
			s.execute("CREATE TABLE IF NOT EXISTS `"+tab_warps+"` ("
					+ "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
					+ "`name` varchar(32) NOT NULL,"
					+ "`dimension` int(11) NOT NULL,"
					+ "`x` double NOT NULL,"
					+ "`y` double NOT NULL,"
					+ "`z` double NOT NULL,"
					+ "`yaw` float NOT NULL,"
					+ "`pitch` float NOT NULL,"
					+ "`random` double NOT NULL,"
					+ "PRIMARY KEY (`id`),"
					+ "UNIQUE KEY `name` (`name`)"
					+ ") ENGINE=InnoDB");
			
			s.execute("CREATE TABLE IF NOT EXISTS `"+tab_fastwarps+"` ("
					+ "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
					+ "`name` varchar(32) NOT NULL,"
					+ "PRIMARY KEY (`id`),"
					+ "UNIQUE KEY `name` (`name`)"
					+ ") ENGINE=InnoDB");
		}
		catch(Exception e)
		{
			throw new RuntimeException("Failed to create SQL tables", e);
		}
		finally
		{
			close(null, s, null);
		}
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			ps = conn.prepareStatement("SELECT * FROM `"+tab_player_ids+"`");
			rs = ps.executeQuery();
			while(rs.next())
				playerIDs.put(toUUID(rs.getBytes(2)), rs.getInt(1));
		}
		catch(Exception e)
		{
			throw new RuntimeException("Failed to load initial player database IDs", e);
		}
		finally
		{
			close(conn, ps, rs);
		}
	}

	@Override
	public boolean isUsingWorldPlayerDir()
	{
		return false;
	}

	@Override
	public NBTTagCompound loadPlayer(GameProfile player)
	{
		return loadPlayer(0, player);
	}

	@Override
	public NBTTagCompound loadPlayer(int dim, GameProfile player)
	{
		int id = playerIDs.get(player.getId());
		if(id == -1)
			return null;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = ds.getConnection();
			ps = conn.prepareStatement("SELECT `data`, `curDim` FROM `"+tab_player_gamedata+"` WHERE `pid`=? AND `forDim`=?");
			ps.setInt(1, id);
			ps.setInt(2, dim);
			rs = ps.executeQuery();
			if(!rs.next())
				return null;
			NBTTagCompound nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(rs.getBytes("data")));
			nbt.setInteger("Dimension", rs.getInt("curDim"));
			return nbt;
		}
		catch(Exception e)
		{
			log.warn("Failed to load player gamedata for " + player.getName(), e);
		}
		finally
		{
			close(conn, ps, rs);
		}

		return null;
	}

	@Override
	public void savePlayer(final GameProfile player, final NBTTagCompound nbt)
	{
		savePlayer(0, player, nbt);
	}

	@Override
	public void savePlayer(final int dim, final GameProfile player, final NBTTagCompound nbt)
	{
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				int id = playerIDs.get(player.getId());

				Connection conn = null;
				PreparedStatement ps = null;
				try
				{
					conn = ds.getConnection();
					if(id == -1)
						id = createPlayerID(conn, player);
					ps = conn.prepareStatement("INSERT INTO `"+tab_player_gamedata+"` (`pid`, `data`, `forDim`, `curDim`) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
							+ "`data`=values(data), `forDim`=values(forDim), `curDim`=values(curDim)");
					ps.setInt(1, id);
					ps.setBytes(2, CompressedStreamTools.compress(nbt));
					ps.setInt(3, dim);
					ps.setInt(4, nbt.getInteger("Dimension"));
					ps.executeUpdate();
				}
				catch(Exception e)
				{
					log.warn("Failed to save player gamedata " + player.getName(), e);
				}
				finally
				{
					close(conn, ps, null);
				}
			}
		});
	}

	@Override
	public PlayerData loadPlayerData(GameProfile player)
	{
		int id = playerIDs.get(player.getId());
		if(id == -1)
			return readPlayerData(null);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = ds.getConnection();
			ps = conn.prepareStatement("SELECT `data` FROM `"+tab_player_data+"` WHERE `pid`=?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(!rs.next())
				return readPlayerData(null);
			return readPlayerData(CompressedStreamTools.readCompressed(new ByteArrayInputStream(rs.getBytes("data"))));
		}
		catch(Exception e)
		{
			log.warn("Failed to load player data for " + player.getName(), e);
		}
		finally
		{
			close(conn, ps, rs);
		}
		
		return null;
	}

	@Override
	public List<PlayerData> loadAllPlayerData()
	{
		List<PlayerData> list = new LinkedList<PlayerData>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = ds.getConnection();
			ps = conn.prepareStatement("SELECT `data` FROM `"+tab_player_data+"`");
			rs = ps.executeQuery();
			while(rs.next())
				list.add(readPlayerData(CompressedStreamTools.readCompressed(new ByteArrayInputStream(rs.getBytes("data")))));
		}
		catch(Exception e)
		{
			log.warn("Failed to load all player data", e);
		}
		finally
		{
			close(conn, ps, rs);
		}
		
		return list;
	}

	@Override
	public void savePlayerData(PlayerData data)
	{
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("id", data.getProfile().getId().toString());
		nbt.setString("name", data.getProfile().getName());
		for(PlayerDataExtensionInfo info : mgr.getDataLoader().getDataExtProviders())
		{
			NBTTagCompound extnbt = new NBTTagCompound();
			data.get(info.getExtClass()).writeToNBT(extnbt);
			nbt.setTag(info.getTagName(), extnbt);
		}
		
		final GameProfile player = data.getProfile();
		
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				int id = playerIDs.get(player.getId());
				
				Connection conn = null;
				PreparedStatement ps = null;
				try
				{
					conn = ds.getConnection();
					if(id == -1)
						id = createPlayerID(conn, player);
					ps = conn.prepareStatement("INSERT INTO `"+tab_player_data+"` (`pid`, `data`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `data`=values(data)");
					ps.setInt(1, id);
					ps.setBytes(2, CompressedStreamTools.compress(nbt));
					ps.executeUpdate();
				}
				catch(Exception e)
				{
					log.warn("Failed to save player data " + player.getName(), e);
				}
				finally
				{
					close(conn, ps, null);
				}
			}
		});
	}

	@Override
	public Map<String, WarpLocation> loadWarps()
	{
		Map<String, WarpLocation> map = new HashMap<String, WarpLocation>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = ds.getConnection();
			ps = conn.prepareStatement("SELECT * FROM `"+tab_warps+"`");
			rs = ps.executeQuery();
			while(rs.next())
			{
				String name = rs.getString(2);
				int dim = rs.getInt(3);
				double x = rs.getDouble(4);
				double y = rs.getDouble(5);
				double z = rs.getDouble(6);
				float yaw = rs.getFloat(7);
				float pitch = rs.getFloat(8);
				double random = rs.getDouble(9);
				
				map.put(name, new WarpLocation(dim, x, y, z, yaw, pitch, random));
			}
		}
		catch(Exception e)
		{
			log.warn("Failed to load warps", e);
		}
		finally
		{
			close(conn, ps, rs);
		}
		
		return map;
	}

	@Override
	public void saveWarp(final String name, final WarpLocation warp)
	{
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				Connection conn = null;
				PreparedStatement ps = null;
				try
				{
					conn = ds.getConnection();
					ps = conn.prepareStatement("INSERT INTO `"+tab_warps+"` (`name`, `dimension`, `x`, `y`, `z`, `yaw`, `pitch`, `random`) VALUES "
							+ "(?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
							+ "`dimension`=values(dimension), `x`=values(x), `y`=values(y), `z`=values(z), `yaw`=values(yaw), `pitch`=values(pitch), `random`=values(random)");
					
					ps.setString(1, name);
					
					ps.setInt(2, warp.dimension);
					ps.setDouble(3, warp.x);
					ps.setDouble(4, warp.y);
					ps.setDouble(5, warp.z);
					ps.setFloat(6, warp.yaw);
					ps.setFloat(7, warp.pitch);
					ps.setDouble(8, warp.randomRadius);
					
					ps.executeUpdate();
				}
				catch(Exception e)
				{
					log.warn("Failed to save warp: " + name, e);
				}
				finally
				{
					close(conn, ps, null);
				}
			}
		});
	}

	@Override
	public void removeWarp(final String name)
	{
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				Connection conn = null;
				PreparedStatement ps = null;
				try
				{
					conn = ds.getConnection();
					ps = conn.prepareStatement("DELETE FROM `"+tab_warps+"` WHERE `name`=?");
					ps.setString(1, name);
					ps.executeUpdate();
				}
				catch(Exception e)
				{
					log.warn("Failed to remove warp: " + name, e);
				}
				finally
				{
					close(conn, ps, null);
				}
			}
		});
	}

	@Override
	public List<String> loadFastWarps()
	{
		List<String> list = new LinkedList<String>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = ds.getConnection();
			ps = conn.prepareStatement("SELECT * FROM `"+tab_fastwarps+"`");
			rs = ps.executeQuery();
			while(rs.next())
				list.add(rs.getString(2));
		}
		catch(Exception e)
		{
			log.warn("Failed to load fastwarps", e);
		}
		finally
		{
			close(conn, ps, rs);
		}
		
		return list;
	}

	@Override
	public void saveFastWarp(final String name)
	{
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				Connection conn = null;
				PreparedStatement ps = null;
				try
				{
					conn = ds.getConnection();
					ps = conn.prepareStatement("INSERT INTO `"+tab_fastwarps+"` (`name`) VALUES (?)");
					ps.setString(1, name);
					ps.executeUpdate();
				}
				catch(Exception e)
				{
					log.warn("Failed to save fastwarp: " + name, e);
				}
				finally
				{
					close(conn, ps, null);
				}
			}
		});
	}

	@Override
	public void removeFastWarp(final String name)
	{
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				Connection conn = null;
				PreparedStatement ps = null;
				try
				{
					conn = ds.getConnection();
					ps = conn.prepareStatement("DELETE FROM `"+tab_fastwarps+"` WHERE `name`=?");
					ps.setString(1, name);
					ps.executeUpdate();
				}
				catch(Exception e)
				{
					log.warn("Failed to remove fastwarp: " + name, e);
				}
				finally
				{
					close(conn, ps, null);
				}
			}
		});
	}

	private int createPlayerID(Connection conn, GameProfile player) throws SQLException
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = ds.getConnection();
			ps = conn.prepareStatement("INSERT INTO `"+tab_player_ids+"` (`uuid`) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			ps.setBytes(1, toBytes(player.getId()));
			ps.executeUpdate();

			rs = ps.getGeneratedKeys();  
			if(!rs.next())
				throw new RuntimeException("!keys.next()"); //impossible??
			int id = rs.getInt(1);
			playerIDs.put(player.getId(), id);
			return id;
		}
		catch(SQLException e)
		{
			log.warn("Failed to create player data id " + player.getName());
			throw e;
		}
		finally
		{
			close(null, ps, rs);
		}
	}

	private void close(Connection conn, Statement ps, ResultSet rs)
	{
		if(rs != null) try{rs.close();} catch(SQLException e){}
		if(ps != null) try{ps.close();} catch(SQLException e){}
		if(conn != null) try{conn.close();} catch(SQLException e){}
	}

	private static UUID toUUID(byte[] data)
	{
		long msb = 0;
		long lsb = 0;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (data[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (data[i] & 0xff);
		return new UUID(msb, lsb);
	}

	private static byte[] toBytes(UUID uuid)
	{
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] data = new byte[16];
		for (int i = 0; i < 8; i++)
			data[7-i] = (byte)((msb >> 8*i) & 0xff);
		for (int i = 0; i < 8; i++)
			data[15-i] = (byte)((lsb >> 8*i) & 0xff);
		return data;
	}

	private PlayerData readPlayerData(NBTTagCompound nbt)
	{
		PlayerData pdata = new PlayerData(mgr.getDataLoader());
		List<PlayerDataExtensionInfo> infos = mgr.getDataLoader().getDataExtProviders();
		List<PlayerDataExtension> data = new ArrayList<PlayerDataExtension>(infos.size());

		for(PlayerDataExtensionInfo info : infos)
		{
			data.add(info.createFromNBT(pdata, nbt));
		}
		
		pdata.loadExtensions(data);
		if(nbt != null && nbt.hasKey("id") && nbt.hasKey("name"))
			pdata.setProfile(new GameProfile(UUID.fromString(nbt.getString("id")), nbt.getString("name")));
		return pdata;
	}
}

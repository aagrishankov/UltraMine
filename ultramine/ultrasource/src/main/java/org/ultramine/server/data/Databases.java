package org.ultramine.server.data;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.UltramineServerConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class Databases
{
	private static Map<String, DataSource> databases = new HashMap<String, DataSource>();
	
	public static void init()
	{
		for(Map.Entry<String, UltramineServerConfig.DatabaseConf> ent : ConfigurationHandler.getServerConfig().databases.entrySet())
		{
			UltramineServerConfig.DatabaseConf info = ent.getValue();
			
			BasicDataSource ds = new BasicDataSource();
			if(info.url.startsWith("jdbc:mysql:"))
				ds.setDriverClassName("com.mysql.jdbc.Driver");
			ds.setUrl(info.url);
			ds.setUsername(info.username);
			ds.setPassword(info.password);
			if(info.maxConnections > 0)
				ds.setMaxIdle(info.maxConnections);
			ds.setTestWhileIdle(true);
			ds.setTestOnBorrow(false);
			ds.setValidationQueryTimeout(1);
			
			databases.put(ent.getKey(), ds);
		}
	}
	
	public static DataSource getDataSource(String name)
	{
		DataSource ds = databases.get(name);
		if(ds == null)
			throw new RuntimeException("DataSource for name: " + name + " not found! Check your server.yml");
		return ds;
	}
}

package org.ultramine.server.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class Resources
{
	public static InputStream getAsStream(String path)
	{
		return Resources.class.getResourceAsStream(path);
	}
	
	public static String getAsString(String path)
	{
		InputStream is = getAsStream(path);
		if(is == null)
			throw new RuntimeException("Requested resource not found: " + path);
		try
		{
			return IOUtils.toString(is, Charsets.UTF_8);
		}
		catch(IOException e)
		{
			throw new RuntimeException("Failed to load resource: " + path, e);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}
}

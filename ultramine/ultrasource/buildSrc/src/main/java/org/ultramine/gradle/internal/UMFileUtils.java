package org.ultramine.gradle.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class UMFileUtils
{
	public static String getRelativePath(File base, File child)
	{
		return base.toURI().relativize(child.toURI()).getPath();
	}

	public static boolean isDirEmpty(final Path directory) throws IOException
	{
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory))
		{
			return !dirStream.iterator().hasNext();
		}
	}

	public static boolean isDirEmptyRecursive(final Path directory) throws IOException
	{
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory))
		{
			for(Iterator<Path> it = dirStream.iterator(); it.hasNext();)
			{
				Path path = it.next();
				if(!Files.isDirectory(path) || !isDirEmptyRecursive(path))
					return false;
			}
		}

		return true;
	}
}

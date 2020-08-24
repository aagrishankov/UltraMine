package org.ultramine.gradle.task;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.JointProvider;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;
import org.ultramine.gradle.internal.DirectoryClassRepo;
import org.ultramine.gradle.internal.RepoInheritanceProvider;
import org.ultramine.gradle.internal.UMFileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ReobfTask extends DefaultTask
{
	@InputDirectory
	private File inputDir;
	@InputDirectory
	private File overrideInputDir;
	@InputFile
	private File srg;
	private FileCollection classpath;
	@OutputDirectory
	private File outputDir = new File(getProject().getBuildDir(), getName());
	private DirectoryClassRepo classRepo;
	private JarRemapper remapper;

	public File getSrg()
	{
		return srg;
	}

	public void setSrg(File srg)
	{
		this.srg = srg;
	}

	public void setSrg(String srg)
	{
		this.srg = getProject().file(srg);
	}

	public File getInputDir()
	{
		return inputDir;
	}

	public void setInputDir(File inputDir)
	{
		this.inputDir = inputDir;
	}

	public File getOverrideInputDir()
	{
		return overrideInputDir;
	}

	public void setOverrideInputDir(File overrideInputDir)
	{
		this.overrideInputDir = overrideInputDir;
	}

	public FileCollection getClasspath()
	{
		return classpath;
	}

	public void setClasspath(FileCollection classpath)
	{
		this.classpath = classpath;
	}

	public File getOutputDir()
	{
		return outputDir;
	}

	public void setOutputDir(File outputDir)
	{
		this.outputDir = outputDir;
	}

	@TaskAction
	void doAction(IncrementalTaskInputs inputs) throws IOException
	{
		initRemapper();
		if(!inputs.isIncremental())
		{
			FileUtils.cleanDirectory(outputDir);
			getProject().fileTree(inputDir).visit(new FileVisitor(){
				@Override
				public void visitDir(FileVisitDetails dirDetails)
				{

				}

				@Override
				public void visitFile(FileVisitDetails fileDetails)
				{
					processClass(fileDetails.getPath());
				}
			});
		}
		else
		{
			inputs.outOfDate((InputFileDetails detals) -> processClass(detals.getFile()));

			Set<File> dirsToCheck = new HashSet<File>();
			inputs.removed((InputFileDetails detals) -> {
				File file = new File(outputDir, getRelPath(detals.getFile()));
				file.delete();
				dirsToCheck.add(file.getParentFile());
			});

			for(File file : dirsToCheck)
				if(file.exists() && UMFileUtils.isDirEmptyRecursive(file.toPath()))
				{
					FileUtils.deleteDirectory(file);
					File parent = file.getParentFile();
					if(UMFileUtils.isDirEmpty(parent.toPath()))
						FileUtils.deleteDirectory(parent);
				}
		}
	}

	private String getRelPath(File file)
	{
		return UMFileUtils.getRelativePath(inputDir, file);
	}

	private void processClass(File file)
	{
		if(file.isDirectory())
			return;
		String pth1 = getRelPath(file);
		String pth2 = UMFileUtils.getRelativePath(overrideInputDir, file);
		processClass(pth2.length() < pth1.length() ? pth2 : pth1);
	}

	private File resolveFile(String path)
	{
		if(overrideInputDir != null)
		{
			File file = new File(overrideInputDir, path);
			if(file.exists())
				return file;
		}

		return new File(inputDir, path);
	}

	private void processClass(String path)
	{
		try
		{
			byte[] bytes = remapper.remapClassFile(FileUtils.readFileToByteArray(resolveFile(path)), classRepo);
			FileUtils.writeByteArrayToFile(new File(outputDir, remapper.map(path.substring(0, path.length() - 6))+".class"), bytes);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void initRemapper() throws IOException
	{
		JarMapping mapping = new JarMapping();
		mapping.loadMappings(srg);
		classRepo = new DirectoryClassRepo(inputDir);
		JointProvider inheritanceProviders = new JointProvider();
		inheritanceProviders.add(new RepoInheritanceProvider(classRepo));
		if(this.classpath != null) {
			inheritanceProviders.add(new ClassLoaderProvider(new URLClassLoader(toUrls(this.classpath))));
		}
		mapping.setFallbackInheritanceProvider(inheritanceProviders);

		this.remapper = new JarRemapper(null, mapping);
	}

	private static URL[] toUrls(FileCollection collection) throws MalformedURLException
	{
		ArrayList<URL> urls = new ArrayList<URL>();

		for(File file : collection.getFiles())
			urls.add(file.toURI().toURL());

		return urls.toArray(new URL[urls.size()]);
	}
}

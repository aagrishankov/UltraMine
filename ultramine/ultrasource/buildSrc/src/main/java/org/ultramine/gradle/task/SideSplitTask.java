package org.ultramine.gradle.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.ultramine.gradle.internal.UMFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SideSplitTask extends DefaultTask
{
	private static final String SIDEONLY_DESK = "Lcpw/mods/fml/relauncher/SideOnly;";
	@InputDirectory
	private File inputDir;
	@Input
	private boolean outputServerSide = true;
	@Input
	private boolean outputClientSide = true;
	private File taskDir = new File(getProject().getBuildDir(), getName());
	private File classesServer = new File(taskDir, "classes_server");
	private File classesClient = new File(taskDir, "classes_client");

	public SideSplitTask() throws IOException
	{
		FileUtils.forceMkdir(classesServer);
		FileUtils.forceMkdir(classesClient);
	}

	public File getInputDir()
	{
		return inputDir;
	}

	public void setInputDir(File inputDir)
	{
		this.inputDir = inputDir;
	}

	public boolean isOutputServerSide()
	{
		return outputServerSide;
	}

	public void setOutputServerSide(boolean outputServerSide)
	{
		this.outputServerSide = outputServerSide;
	}

	public boolean isOutputClientSide()
	{
		return outputClientSide;
	}

	public void setOutputClientSide(boolean outputClientSide)
	{
		this.outputClientSide = outputClientSide;
	}

	@OutputDirectory
	public File getServerClasses()
	{
		return classesServer;
	}

	@OutputDirectory
	public File getClientClasses()
	{
		return classesClient;
	}

	@TaskAction
	void doAction(IncrementalTaskInputs inputs) throws IOException
	{
		if(!inputs.isIncremental())
		{
			FileUtils.cleanDirectory(classesServer);
			FileUtils.cleanDirectory(classesClient);
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
				File file = new File(classesServer, getRelPath(detals.getFile()));
				file.delete();
				dirsToCheck.add(file.getParentFile());
				file = new File(classesClient, getRelPath(detals.getFile()));
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
		processClass(getRelPath(file));
	}

	private void processClass(String path)
	{
		try
		{
			byte[] cls = FileUtils.readFileToByteArray(new File(inputDir, path));
			if(outputServerSide)
			{
				byte[] serverCls = processClass(cls, "SERVER");
				if(serverCls != null)
					writeClass(classesServer, path, serverCls);
			}
			if(outputClientSide)
			{
				byte[] clientCls = processClass(cls, "CLIENT");
				if(clientCls != null)
					writeClass(classesClient, path, clientCls);
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void writeClass(File dir, String path, byte[] cls) throws IOException
	{
		FileUtils.writeByteArrayToFile(new File(dir, path), cls);
	}

	private byte[] processClass(byte[] input, String side)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(input);
		classReader.accept(classNode, 0);

		if(remove(classNode.visibleAnnotations, side))
			return null;

		Iterator<FieldNode> fields = classNode.fields.iterator();
		while(fields.hasNext())
		{
			FieldNode field = fields.next();
			if(remove(field.visibleAnnotations, side))
				fields.remove();
		}
		Iterator<MethodNode> methods = classNode.methods.iterator();
		while(methods.hasNext())
		{
			MethodNode method = methods.next();
			if(remove(method.visibleAnnotations, side))
				methods.remove();
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private boolean remove(List<AnnotationNode> anns, String side)
	{
		if(anns == null)
			return false;
		for(AnnotationNode ann : anns)
		{
			if(ann.desc.equals(SIDEONLY_DESK) && ann.values != null)
			{
				for(int x = 0; x < ann.values.size() - 1; x += 2)
				{
					Object key = ann.values.get(x);
					Object value = ann.values.get(x+1);
					if(key.equals("value") && value instanceof String[] && !((String[])value)[1].equals(side))
						return true;
				}
			}
		}
		return false;
	}
}

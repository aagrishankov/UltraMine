package net.minecraft.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.PriorityQueue;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.client.util.QuadComparator;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class Tessellator
{
	private static int nativeBufferSize = 0x200000;
	private static int trivertsInBuffer = (nativeBufferSize / 48) * 6;
	public static boolean renderingWorldRenderer = false;
	public boolean defaultTexture = false;
	private int rawBufferSize = 0;
	public int textureID = 0;

	private static ByteBuffer byteBuffer = GLAllocation.createDirectByteBuffer(nativeBufferSize * 4);
	private static IntBuffer intBuffer = byteBuffer.asIntBuffer();
	private static FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
	private static ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
	private int[] rawBuffer;
	private int vertexCount;
	private double textureU;
	private double textureV;
	private int brightness;
	private int color;
	private boolean hasColor;
	private boolean hasTexture;
	private boolean hasBrightness;
	private boolean hasNormals;
	private int rawBufferIndex;
	private int addedVertices;
	private boolean isColorDisabled;
	private int drawMode;
	private double xOffset;
	private double yOffset;
	private double zOffset;
	private int normal;
	public static final Tessellator instance = new Tessellator(2097152);
	private boolean isDrawing;
	private int bufferSize;
	private static final String __OBFID = "CL_00000960";

	private Tessellator(int p_i1250_1_)
	{
	}

	public Tessellator()
	{
	}

	static
	{
		instance.defaultTexture = true;
	}

	public int draw()
	{
		if (!this.isDrawing)
		{
			throw new IllegalStateException("Not tesselating!");
		}
		else
		{
			this.isDrawing = false;

			int offs = 0;
			while (offs < vertexCount)
			{
				int vtc = Math.min(vertexCount - offs, nativeBufferSize >> 5);
				this.intBuffer.clear();
				this.intBuffer.put(this.rawBuffer, offs * 8, vtc * 8);
				this.byteBuffer.position(0);
				this.byteBuffer.limit(vtc * 32);
				offs += vtc;

				if (this.hasTexture)
				{
					this.floatBuffer.position(3);
					GL11.glTexCoordPointer(2, 32, this.floatBuffer);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if (this.hasBrightness)
				{
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
					this.shortBuffer.position(14);
					GL11.glTexCoordPointer(2, 32, this.shortBuffer);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
				}

				if (this.hasColor)
				{
					this.byteBuffer.position(20);
					GL11.glColorPointer(4, true, 32, this.byteBuffer);
					GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
				}

				if (this.hasNormals)
				{
					this.byteBuffer.position(24);
					GL11.glNormalPointer(32, this.byteBuffer);
					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				}

				this.floatBuffer.position(0);
				GL11.glVertexPointer(3, 32, this.floatBuffer);
				GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				GL11.glDrawArrays(this.drawMode, 0, vtc);
				GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

				if (this.hasTexture)
				{
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if (this.hasBrightness)
				{
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
				}

				if (this.hasColor)
				{
					GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
				}

				if (this.hasNormals)
				{
					GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
				}
			}

			if (rawBufferSize > 0x20000 && rawBufferIndex < (rawBufferSize << 3))
			{
				rawBufferSize = 0x10000;
				rawBuffer = new int[rawBufferSize];
			}

			int i = this.rawBufferIndex * 4;
			this.reset();
			return i;
		}
	}

	public TesselatorVertexState getVertexState(float p_147564_1_, float p_147564_2_, float p_147564_3_)
	{
		int[] aint = new int[this.rawBufferIndex];
		PriorityQueue priorityqueue = new PriorityQueue(this.rawBufferIndex, new QuadComparator(this.rawBuffer, p_147564_1_ + (float)this.xOffset, p_147564_2_ + (float)this.yOffset, p_147564_3_ + (float)this.zOffset));
		byte b0 = 32;
		int i;

		for (i = 0; i < this.rawBufferIndex; i += b0)
		{
			priorityqueue.add(Integer.valueOf(i));
		}

		for (i = 0; !priorityqueue.isEmpty(); i += b0)
		{
			int j = ((Integer)priorityqueue.remove()).intValue();

			for (int k = 0; k < b0; ++k)
			{
				aint[i + k] = this.rawBuffer[j + k];
			}
		}

		System.arraycopy(aint, 0, this.rawBuffer, 0, aint.length);
		return new TesselatorVertexState(aint, this.rawBufferIndex, this.vertexCount, this.hasTexture, this.hasBrightness, this.hasNormals, this.hasColor);
	}

	public void setVertexState(TesselatorVertexState p_147565_1_)
	{
		while (p_147565_1_.getRawBuffer().length > rawBufferSize && rawBufferSize > 0)
		{
			rawBufferSize <<= 1;
		}
		if (rawBufferSize > rawBuffer.length)
		{
			rawBuffer = new int[rawBufferSize];
		}
		System.arraycopy(p_147565_1_.getRawBuffer(), 0, this.rawBuffer, 0, p_147565_1_.getRawBuffer().length);
		this.rawBufferIndex = p_147565_1_.getRawBufferIndex();
		this.vertexCount = p_147565_1_.getVertexCount();
		this.hasTexture = p_147565_1_.getHasTexture();
		this.hasBrightness = p_147565_1_.getHasBrightness();
		this.hasColor = p_147565_1_.getHasColor();
		this.hasNormals = p_147565_1_.getHasNormals();
	}

	private void reset()
	{
		this.vertexCount = 0;
		this.byteBuffer.clear();
		this.rawBufferIndex = 0;
		this.addedVertices = 0;
	}

	public void startDrawingQuads()
	{
		this.startDrawing(7);
	}

	public void startDrawing(int p_78371_1_)
	{
		if (this.isDrawing)
		{
			throw new IllegalStateException("Already tesselating!");
		}
		else
		{
			this.isDrawing = true;
			this.reset();
			this.drawMode = p_78371_1_;
			this.hasNormals = false;
			this.hasColor = false;
			this.hasTexture = false;
			this.hasBrightness = false;
			this.isColorDisabled = false;
		}
	}

	public void setTextureUV(double p_78385_1_, double p_78385_3_)
	{
		this.hasTexture = true;
		this.textureU = p_78385_1_;
		this.textureV = p_78385_3_;
	}

	public void setBrightness(int p_78380_1_)
	{
		this.hasBrightness = true;
		this.brightness = p_78380_1_;
	}

	public void setColorOpaque_F(float p_78386_1_, float p_78386_2_, float p_78386_3_)
	{
		this.setColorOpaque((int)(p_78386_1_ * 255.0F), (int)(p_78386_2_ * 255.0F), (int)(p_78386_3_ * 255.0F));
	}

	public void setColorRGBA_F(float p_78369_1_, float p_78369_2_, float p_78369_3_, float p_78369_4_)
	{
		this.setColorRGBA((int)(p_78369_1_ * 255.0F), (int)(p_78369_2_ * 255.0F), (int)(p_78369_3_ * 255.0F), (int)(p_78369_4_ * 255.0F));
	}

	public void setColorOpaque(int p_78376_1_, int p_78376_2_, int p_78376_3_)
	{
		this.setColorRGBA(p_78376_1_, p_78376_2_, p_78376_3_, 255);
	}

	public void setColorRGBA(int p_78370_1_, int p_78370_2_, int p_78370_3_, int p_78370_4_)
	{
		if (!this.isColorDisabled)
		{
			if (p_78370_1_ > 255)
			{
				p_78370_1_ = 255;
			}

			if (p_78370_2_ > 255)
			{
				p_78370_2_ = 255;
			}

			if (p_78370_3_ > 255)
			{
				p_78370_3_ = 255;
			}

			if (p_78370_4_ > 255)
			{
				p_78370_4_ = 255;
			}

			if (p_78370_1_ < 0)
			{
				p_78370_1_ = 0;
			}

			if (p_78370_2_ < 0)
			{
				p_78370_2_ = 0;
			}

			if (p_78370_3_ < 0)
			{
				p_78370_3_ = 0;
			}

			if (p_78370_4_ < 0)
			{
				p_78370_4_ = 0;
			}

			this.hasColor = true;

			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
			{
				this.color = p_78370_4_ << 24 | p_78370_3_ << 16 | p_78370_2_ << 8 | p_78370_1_;
			}
			else
			{
				this.color = p_78370_1_ << 24 | p_78370_2_ << 16 | p_78370_3_ << 8 | p_78370_4_;
			}
		}
	}

	public void func_154352_a(byte p_154352_1_, byte p_154352_2_, byte p_154352_3_)
	{
		this.setColorOpaque(p_154352_1_ & 255, p_154352_2_ & 255, p_154352_3_ & 255);
	}

	public void addVertexWithUV(double p_78374_1_, double p_78374_3_, double p_78374_5_, double p_78374_7_, double p_78374_9_)
	{
		this.setTextureUV(p_78374_7_, p_78374_9_);
		this.addVertex(p_78374_1_, p_78374_3_, p_78374_5_);
	}

	public void addVertex(double p_78377_1_, double p_78377_3_, double p_78377_5_)
	{
		if (rawBufferIndex >= rawBufferSize - 32) 
		{
			if (rawBufferSize == 0)
			{
				rawBufferSize = 0x10000;
				rawBuffer = new int[rawBufferSize];
			}
			else
			{
				rawBufferSize *= 2;
				rawBuffer = Arrays.copyOf(rawBuffer, rawBufferSize);
			}
		}
		++this.addedVertices;

		if (this.hasTexture)
		{
			this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits((float)this.textureU);
			this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits((float)this.textureV);
		}

		if (this.hasBrightness)
		{
			this.rawBuffer[this.rawBufferIndex + 7] = this.brightness;
		}

		if (this.hasColor)
		{
			this.rawBuffer[this.rawBufferIndex + 5] = this.color;
		}

		if (this.hasNormals)
		{
			this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
		}

		this.rawBuffer[this.rawBufferIndex + 0] = Float.floatToRawIntBits((float)(p_78377_1_ + this.xOffset));
		this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float)(p_78377_3_ + this.yOffset));
		this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float)(p_78377_5_ + this.zOffset));
		this.rawBufferIndex += 8;
		++this.vertexCount;
	}

	public void setColorOpaque_I(int p_78378_1_)
	{
		int j = p_78378_1_ >> 16 & 255;
		int k = p_78378_1_ >> 8 & 255;
		int l = p_78378_1_ & 255;
		this.setColorOpaque(j, k, l);
	}

	public void setColorRGBA_I(int p_78384_1_, int p_78384_2_)
	{
		int k = p_78384_1_ >> 16 & 255;
		int l = p_78384_1_ >> 8 & 255;
		int i1 = p_78384_1_ & 255;
		this.setColorRGBA(k, l, i1, p_78384_2_);
	}

	public void disableColor()
	{
		this.isColorDisabled = true;
	}

	public void setNormal(float p_78375_1_, float p_78375_2_, float p_78375_3_)
	{
		this.hasNormals = true;
		byte b0 = (byte)((int)(p_78375_1_ * 127.0F));
		byte b1 = (byte)((int)(p_78375_2_ * 127.0F));
		byte b2 = (byte)((int)(p_78375_3_ * 127.0F));
		this.normal = b0 & 255 | (b1 & 255) << 8 | (b2 & 255) << 16;
	}

	public void setTranslation(double p_78373_1_, double p_78373_3_, double p_78373_5_)
	{
		this.xOffset = p_78373_1_;
		this.yOffset = p_78373_3_;
		this.zOffset = p_78373_5_;
	}

	public void addTranslation(float p_78372_1_, float p_78372_2_, float p_78372_3_)
	{
		this.xOffset += (double)p_78372_1_;
		this.yOffset += (double)p_78372_2_;
		this.zOffset += (double)p_78372_3_;
	}
}
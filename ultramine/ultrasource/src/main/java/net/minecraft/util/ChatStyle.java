package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Type;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;

public class ChatStyle
{
	private ChatStyle parentStyle;
	private EnumChatFormatting color;
	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;
	private ClickEvent chatClickEvent;
	private HoverEvent chatHoverEvent;
	private static final ChatStyle rootStyle = new ChatStyle()
	{
		private static final String __OBFID = "CL_00001267";
		public EnumChatFormatting getColor()
		{
			return null;
		}
		public boolean getBold()
		{
			return false;
		}
		public boolean getItalic()
		{
			return false;
		}
		public boolean getStrikethrough()
		{
			return false;
		}
		public boolean getUnderlined()
		{
			return false;
		}
		public boolean getObfuscated()
		{
			return false;
		}
		public ClickEvent getChatClickEvent()
		{
			return null;
		}
		public HoverEvent getChatHoverEvent()
		{
			return null;
		}
		public ChatStyle setColor(EnumChatFormatting p_150238_1_)
		{
			throw new UnsupportedOperationException();
		}
		public ChatStyle setBold(Boolean p_150227_1_)
		{
			throw new UnsupportedOperationException();
		}
		public ChatStyle setItalic(Boolean p_150217_1_)
		{
			throw new UnsupportedOperationException();
		}
		public ChatStyle setStrikethrough(Boolean p_150225_1_)
		{
			throw new UnsupportedOperationException();
		}
		public ChatStyle setUnderlined(Boolean p_150228_1_)
		{
			throw new UnsupportedOperationException();
		}
		public ChatStyle setObfuscated(Boolean p_150237_1_)
		{
			throw new UnsupportedOperationException();
		}
		public ChatStyle setChatClickEvent(ClickEvent p_150241_1_)
		{
			throw new UnsupportedOperationException();
		}
		public ChatStyle setChatHoverEvent(HoverEvent p_150209_1_)
		{
			throw new UnsupportedOperationException();
		}
		public ChatStyle setParentStyle(ChatStyle p_150221_1_)
		{
			throw new UnsupportedOperationException();
		}
		public String toString()
		{
			return "Style.ROOT";
		}
		public ChatStyle createShallowCopy()
		{
			return this;
		}
		public ChatStyle createDeepCopy()
		{
			return this;
		}
//		@SideOnly(Side.CLIENT)
		public String getFormattingCode()
		{
			return "";
		}
	};
	private static final String __OBFID = "CL_00001266";

	public EnumChatFormatting getColor()
	{
		return this.color == null ? this.getParent().getColor() : this.color;
	}

	public boolean getBold()
	{
		return this.bold == null ? this.getParent().getBold() : this.bold.booleanValue();
	}

	public boolean getItalic()
	{
		return this.italic == null ? this.getParent().getItalic() : this.italic.booleanValue();
	}

	public boolean getStrikethrough()
	{
		return this.strikethrough == null ? this.getParent().getStrikethrough() : this.strikethrough.booleanValue();
	}

	public boolean getUnderlined()
	{
		return this.underlined == null ? this.getParent().getUnderlined() : this.underlined.booleanValue();
	}

	public boolean getObfuscated()
	{
		return this.obfuscated == null ? this.getParent().getObfuscated() : this.obfuscated.booleanValue();
	}

	public boolean isEmpty()
	{
		return this.bold == null && this.italic == null && this.strikethrough == null && this.underlined == null && this.obfuscated == null && this.color == null && this.chatClickEvent == null && this.chatHoverEvent == null;
	}

	public ClickEvent getChatClickEvent()
	{
		return this.chatClickEvent == null ? this.getParent().getChatClickEvent() : this.chatClickEvent;
	}

	public HoverEvent getChatHoverEvent()
	{
		return this.chatHoverEvent == null ? this.getParent().getChatHoverEvent() : this.chatHoverEvent;
	}

	public ChatStyle setColor(EnumChatFormatting p_150238_1_)
	{
		this.color = p_150238_1_;
		return this;
	}

	public ChatStyle setBold(Boolean p_150227_1_)
	{
		this.bold = p_150227_1_;
		return this;
	}

	public ChatStyle setItalic(Boolean p_150217_1_)
	{
		this.italic = p_150217_1_;
		return this;
	}

	public ChatStyle setStrikethrough(Boolean p_150225_1_)
	{
		this.strikethrough = p_150225_1_;
		return this;
	}

	public ChatStyle setUnderlined(Boolean p_150228_1_)
	{
		this.underlined = p_150228_1_;
		return this;
	}

	public ChatStyle setObfuscated(Boolean p_150237_1_)
	{
		this.obfuscated = p_150237_1_;
		return this;
	}

	public ChatStyle setChatClickEvent(ClickEvent p_150241_1_)
	{
		this.chatClickEvent = p_150241_1_;
		return this;
	}

	public ChatStyle setChatHoverEvent(HoverEvent p_150209_1_)
	{
		this.chatHoverEvent = p_150209_1_;
		return this;
	}

	public ChatStyle setParentStyle(ChatStyle p_150221_1_)
	{
		this.parentStyle = p_150221_1_;
		return this;
	}

//	@SideOnly(Side.CLIENT)
	public String getFormattingCode()
	{
		if (this.isEmpty())
		{
			return this.parentStyle != null ? this.parentStyle.getFormattingCode() : "";
		}
		else
		{
			StringBuilder stringbuilder = new StringBuilder();

			if (this.getColor() != null)
			{
				stringbuilder.append(this.getColor());
			}

			if (this.getBold())
			{
				stringbuilder.append(EnumChatFormatting.BOLD);
			}

			if (this.getItalic())
			{
				stringbuilder.append(EnumChatFormatting.ITALIC);
			}

			if (this.getUnderlined())
			{
				stringbuilder.append(EnumChatFormatting.UNDERLINE);
			}

			if (this.getObfuscated())
			{
				stringbuilder.append(EnumChatFormatting.OBFUSCATED);
			}

			if (this.getStrikethrough())
			{
				stringbuilder.append(EnumChatFormatting.STRIKETHROUGH);
			}

			return stringbuilder.toString();
		}
	}

	private ChatStyle getParent()
	{
		return this.parentStyle == null ? rootStyle : this.parentStyle;
	}

	public String toString()
	{
		return "Style{hasParent=" + (this.parentStyle != null) + ", color=" + this.color + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", obfuscated=" + this.obfuscated + ", clickEvent=" + this.getChatClickEvent() + ", hoverEvent=" + this.getChatHoverEvent() + '}';
	}

	public boolean equals(Object p_equals_1_)
	{
		if (this == p_equals_1_)
		{
			return true;
		}
		else if (!(p_equals_1_ instanceof ChatStyle))
		{
			return false;
		}
		else
		{
			ChatStyle chatstyle = (ChatStyle)p_equals_1_;
			boolean flag;

			if (this.getBold() == chatstyle.getBold() && this.getColor() == chatstyle.getColor() && this.getItalic() == chatstyle.getItalic() && this.getObfuscated() == chatstyle.getObfuscated() && this.getStrikethrough() == chatstyle.getStrikethrough() && this.getUnderlined() == chatstyle.getUnderlined())
			{
				label56:
				{
					if (this.getChatClickEvent() != null)
					{
						if (!this.getChatClickEvent().equals(chatstyle.getChatClickEvent()))
						{
							break label56;
						}
					}
					else if (chatstyle.getChatClickEvent() != null)
					{
						break label56;
					}

					if (this.getChatHoverEvent() != null)
					{
						if (!this.getChatHoverEvent().equals(chatstyle.getChatHoverEvent()))
						{
							break label56;
						}
					}
					else if (chatstyle.getChatHoverEvent() != null)
					{
						break label56;
					}

					flag = true;
					return flag;
				}
			}

			flag = false;
			return flag;
		}
	}

	public int hashCode()
	{
		int i = this.color.hashCode();
		i = 31 * i + this.bold.hashCode();
		i = 31 * i + this.italic.hashCode();
		i = 31 * i + this.underlined.hashCode();
		i = 31 * i + this.strikethrough.hashCode();
		i = 31 * i + this.obfuscated.hashCode();
		i = 31 * i + this.chatClickEvent.hashCode();
		i = 31 * i + this.chatHoverEvent.hashCode();
		return i;
	}

	public ChatStyle createShallowCopy()
	{
		ChatStyle chatstyle = new ChatStyle();
		chatstyle.bold = this.bold;
		chatstyle.italic = this.italic;
		chatstyle.strikethrough = this.strikethrough;
		chatstyle.underlined = this.underlined;
		chatstyle.obfuscated = this.obfuscated;
		chatstyle.color = this.color;
		chatstyle.chatClickEvent = this.chatClickEvent;
		chatstyle.chatHoverEvent = this.chatHoverEvent;
		chatstyle.parentStyle = this.parentStyle;
		return chatstyle;
	}

	public ChatStyle createDeepCopy()
	{
		ChatStyle chatstyle = new ChatStyle();
		chatstyle.setBold(Boolean.valueOf(this.getBold()));
		chatstyle.setItalic(Boolean.valueOf(this.getItalic()));
		chatstyle.setStrikethrough(Boolean.valueOf(this.getStrikethrough()));
		chatstyle.setUnderlined(Boolean.valueOf(this.getUnderlined()));
		chatstyle.setObfuscated(Boolean.valueOf(this.getObfuscated()));
		chatstyle.setColor(this.getColor());
		chatstyle.setChatClickEvent(this.getChatClickEvent());
		chatstyle.setChatHoverEvent(this.getChatHoverEvent());
		return chatstyle;
	}

	public static class Serializer implements JsonDeserializer, JsonSerializer
		{
			private static final String __OBFID = "CL_00001268";

			public ChatStyle deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_)
			{
				if (p_deserialize_1_.isJsonObject())
				{
					ChatStyle chatstyle = new ChatStyle();
					JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();

					if (jsonobject == null)
					{
						return null;
					}
					else
					{
						if (jsonobject.has("bold"))
						{
							chatstyle.bold = Boolean.valueOf(jsonobject.get("bold").getAsBoolean());
						}

						if (jsonobject.has("italic"))
						{
							chatstyle.italic = Boolean.valueOf(jsonobject.get("italic").getAsBoolean());
						}

						if (jsonobject.has("underlined"))
						{
							chatstyle.underlined = Boolean.valueOf(jsonobject.get("underlined").getAsBoolean());
						}

						if (jsonobject.has("strikethrough"))
						{
							chatstyle.strikethrough = Boolean.valueOf(jsonobject.get("strikethrough").getAsBoolean());
						}

						if (jsonobject.has("obfuscated"))
						{
							chatstyle.obfuscated = Boolean.valueOf(jsonobject.get("obfuscated").getAsBoolean());
						}

						if (jsonobject.has("color"))
						{
							chatstyle.color = (EnumChatFormatting)p_deserialize_3_.deserialize(jsonobject.get("color"), EnumChatFormatting.class);
						}

						JsonObject jsonobject1;
						JsonPrimitive jsonprimitive;

						if (jsonobject.has("clickEvent"))
						{
							jsonobject1 = jsonobject.getAsJsonObject("clickEvent");

							if (jsonobject1 != null)
							{
								jsonprimitive = jsonobject1.getAsJsonPrimitive("action");
								ClickEvent.Action action = jsonprimitive == null ? null : ClickEvent.Action.getValueByCanonicalName(jsonprimitive.getAsString());
								JsonPrimitive jsonprimitive1 = jsonobject1.getAsJsonPrimitive("value");
								String s = jsonprimitive1 == null ? null : jsonprimitive1.getAsString();

								if (action != null && s != null && action.shouldAllowInChat())
								{
									chatstyle.chatClickEvent = new ClickEvent(action, s);
								}
							}
						}

						if (jsonobject.has("hoverEvent"))
						{
							jsonobject1 = jsonobject.getAsJsonObject("hoverEvent");

							if (jsonobject1 != null)
							{
								jsonprimitive = jsonobject1.getAsJsonPrimitive("action");
								HoverEvent.Action action1 = jsonprimitive == null ? null : HoverEvent.Action.getValueByCanonicalName(jsonprimitive.getAsString());
								IChatComponent ichatcomponent = (IChatComponent)p_deserialize_3_.deserialize(jsonobject1.get("value"), IChatComponent.class);

								if (action1 != null && ichatcomponent != null && action1.shouldAllowInChat())
								{
									chatstyle.chatHoverEvent = new HoverEvent(action1, ichatcomponent);
								}
							}
						}

						return chatstyle;
					}
				}
				else
				{
					return null;
				}
			}

			public JsonElement serialize(ChatStyle p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_)
			{
				if (p_serialize_1_.isEmpty())
				{
					return null;
				}
				else
				{
					JsonObject jsonobject = new JsonObject();

					if (p_serialize_1_.bold != null)
					{
						jsonobject.addProperty("bold", p_serialize_1_.bold);
					}

					if (p_serialize_1_.italic != null)
					{
						jsonobject.addProperty("italic", p_serialize_1_.italic);
					}

					if (p_serialize_1_.underlined != null)
					{
						jsonobject.addProperty("underlined", p_serialize_1_.underlined);
					}

					if (p_serialize_1_.strikethrough != null)
					{
						jsonobject.addProperty("strikethrough", p_serialize_1_.strikethrough);
					}

					if (p_serialize_1_.obfuscated != null)
					{
						jsonobject.addProperty("obfuscated", p_serialize_1_.obfuscated);
					}

					if (p_serialize_1_.color != null)
					{
						jsonobject.add("color", p_serialize_3_.serialize(p_serialize_1_.color));
					}

					JsonObject jsonobject1;

					if (p_serialize_1_.chatClickEvent != null)
					{
						jsonobject1 = new JsonObject();
						jsonobject1.addProperty("action", p_serialize_1_.chatClickEvent.getAction().getCanonicalName());
						jsonobject1.addProperty("value", p_serialize_1_.chatClickEvent.getValue());
						jsonobject.add("clickEvent", jsonobject1);
					}

					if (p_serialize_1_.chatHoverEvent != null)
					{
						jsonobject1 = new JsonObject();
						jsonobject1.addProperty("action", p_serialize_1_.chatHoverEvent.getAction().getCanonicalName());
						jsonobject1.add("value", p_serialize_3_.serialize(p_serialize_1_.chatHoverEvent.getValue()));
						jsonobject.add("hoverEvent", jsonobject1);
					}

					return jsonobject;
				}
			}

			public JsonElement serialize(Object p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_)
			{
				return this.serialize((ChatStyle)p_serialize_1_, p_serialize_2_, p_serialize_3_);
			}
		}
}

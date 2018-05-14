package am2.client.gui;

import am2.client.gui.controls.GuiButtonVariableDims;
import am2.client.gui.widgets.*;
import am2.client.gui.widgets.events.WidgetEvent;
import am2.client.gui.widgets.events.WidgetEventListener;
import am2.common.defs.ItemDefs;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static javax.swing.text.html.HTML.getTag;

public class GuiSpellRecipe extends GuiScreenWidget implements WidgetEventListener{
	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/SpellRecipe.png");
	
	private final ItemStack recipeStack;
	private String spellName;
	private int spellNameWidth;
	
	private WidgetList<WidgetIngridient> ingridients;
	
	public GuiSpellRecipe(ItemStack recipeStack){
		super(256, 158);
		this.recipeStack = recipeStack;
		this.spellName = recipeStack.getDisplayName();
		
		ingridients = new WidgetList<>(0, 135, 8, 100, 135, false);
		
		this.widgets.add(ingridients);
		
		if (recipeStack.hasTagCompound()){
			NBTTagCompound compound = recipeStack.getTagCompound();
			NBTTagList materials = (NBTTagList)compound.getTag("materials");
			for (int i = 0; i < materials.tagCount(); i++){
				NBTTagCompound material = materials.getCompoundTagAt(i);
				String itemId = material.getString("id");
				int count = material.getInteger("count");
				if (itemId == null || itemId.length() == 0)
					continue;
				if (itemId.charAt(0) == 'I'){
					ingridients.add(new WidgetIngridient(
							10,
							new ItemStack(
									Item.REGISTRY.getObject(new ResourceLocation(itemId.substring(1))), count
							)
					));
				}else if (itemId.charAt(0) == 'B'){
					ingridients.add(new WidgetIngridient(
							10,
							new ItemStack(
									Item.REGISTRY.getObject(new ResourceLocation(itemId.substring(1))), count
							)
					));
				}else if (itemId.charAt(0) == 'E'){
					ingridients.add(new WidgetIngridient(
							10,
							itemId.substring(1),
							count
					));
				}
			}
		}
	}
	
	@Override
	public void initGui(){
		super.initGui();
		
		spellNameWidth = fontRendererObj.getStringWidth(spellName);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		int xMin = (width - xSize) / 2;
		int yMin = (height - ySize) / 2;
		
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		mc.renderEngine.bindTexture(background);
		this.drawTexturedModalRect(xMin, yMin, 0, 0, xSize, ySize);
		
		fontRendererObj.drawString(spellName, xMin + 64 - spellNameWidth / 2, yMin + 8, 0);
		
		super.draw(xMin, yMin, mouseX, mouseY);
	}
	
	@Override
	public void event(WidgetEvent type, int id, Object data){
	
	}
	
}

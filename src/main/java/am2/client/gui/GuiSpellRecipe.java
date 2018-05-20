package am2.client.gui;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.skill.Skill;
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
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static javax.swing.text.html.HTML.getTag;

public class GuiSpellRecipe extends GuiScreenWidget implements WidgetEventListener{
	private static final int LABEL_HEIGHT = 12;
	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/SpellRecipe.png");
	
	private final ItemStack recipeStack;
	private String spellName;
	private int spellNameWidth;
	
	private WidgetList<WidgetIngridient> ingridients;
	private WidgetList<Widget> spellInfo;
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException{
		super.keyTyped(typedChar, keyCode);
		
		if (keyCode == Keyboard.KEY_E)
			this.mc.thePlayer.closeScreen();
	}
	
	public GuiSpellRecipe(ItemStack recipeStack){
		super(256, 158);
		this.recipeStack = recipeStack;
		this.spellName = recipeStack.getDisplayName();
		
		ingridients = new WidgetList<>(0, 135, 8, 100, 135, false);
		this.widgets.add(ingridients);
		
		spellInfo = new WidgetList<>(0, 20, 18, 87, 125, false);
		this.widgets.add(spellInfo);
		
		if (recipeStack.hasTagCompound()){
			NBTTagCompound compound = recipeStack.getTagCompound();
			NBTTagList materials = (NBTTagList)compound.getTag("materials");
			for (int i = 0; i < materials.tagCount(); i++){
				NBTTagCompound material = materials.getCompoundTagAt(i);
				String itemId = material.getString("id");
				int count = material.getInteger("count");
				ItemStack itemStack = ItemStack.loadItemStackFromNBT(material.getCompoundTag("item"));
				
				if (itemStack.getItem() == ItemDefs.etherium){
					ingridients.add(new WidgetIngridient(
							10,
							itemId.substring(1),
							count
					));
				}else{
					ingridients.add(new WidgetIngridient(
							10,
							itemStack,
							count
					));
				}
			}
			
			int groupsCount = compound.getInteger("numShapeGroups");
			for (int i = 1; i <= groupsCount; i++){
				int[] group = compound.getIntArray("shapeGroupCombo_" + (i - 1));
				if (group.length == 0)
					continue;
				
				spellInfo.add(new WidgetLabel(
						10,
						I18n.format("am2.tooltip.spell_group", i),
						0, 0,
						100, LABEL_HEIGHT
				));
				for (int skillId : group){
					Skill skill = ArsMagicaAPI.getSkillRegistry().getObjectById(skillId);
					spellInfo.add(new WidgetSkill(10, skill));
				}
			}
			int[] output = compound.getIntArray("output_combo");
			spellInfo.add(new WidgetLabel(
					10,
					I18n.format("am2.tooltip.output_combo"),
					0, 0,
					100, LABEL_HEIGHT
			));
			for (int skillId : output){
				Skill skill = ArsMagicaAPI.getSkillRegistry().getObjectById(skillId);
				spellInfo.add(new WidgetSkill(10, skill));
			}
			
			spellInfo.add(new WidgetLabel(
					10,
					I18n.format("am2.gui.affinity"),
					0, 0,
					100, LABEL_HEIGHT
			));
			NBTTagList affinityList = (NBTTagList)compound.getTag("affinity");
			for (int i = 0; i < affinityList.tagCount(); i++){
				NBTTagCompound affinityTag = affinityList.getCompoundTagAt(i);
				Affinity affinity = ArsMagicaAPI.getAffinityRegistry().getObject(new ResourceLocation(affinityTag.getString("id")));
				float value = affinityTag.getFloat("value");
				spellInfo.add(new WidgetAffinity(10, affinity, value));
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
	
	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}
}

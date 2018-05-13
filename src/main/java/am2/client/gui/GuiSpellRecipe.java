package am2.client.gui;

import am2.client.gui.controls.GuiButtonVariableDims;
import am2.client.gui.widgets.GuiScreenWidget;
import am2.common.defs.ItemDefs;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiSpellRecipe extends GuiScreenWidget{
	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/SpellRecipe.png");

	private final ItemStack recipeStack;
	private final int xSize, ySize;
	private String spellName;
	private int spellNameWidth;

	private GuiButtonVariableDims btnUp;
	private GuiButtonVariableDims btnDown;
	private static final int MAX_INGRIDIENTS_VISIBLE = 6;
	private int ingridientsOffset;
	private List<SpellRecipeIngridient> ingridients;

	public GuiSpellRecipe(EntityPlayer player, ItemStack recipeStack){
		this.recipeStack = recipeStack;
		this.xSize = 256;
		this.ySize = 158;
		this.spellName = recipeStack.getDisplayName();

		this.ingridientsOffset = 0;
		ingridients = new ArrayList<>();
		for (int i = 0; i < 5; i++){
			ingridients.add(new IngridientItemStack(new ItemStack(ItemDefs.rune), "Пустая руна" + i));
			ingridients.add(new IngridientItemStack(new ItemStack(ItemDefs.spellParchment), "Пергамент заклинания" + i));
		}

		/*if (recipeStack.hasTagCompound()){
			NBTTagCompound compound = recipeStack.getTagCompound();

		}*/
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException{
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void initGui(){
		super.initGui();

		int xMin = (width - xSize) / 2;
		int yMin = (height - ySize) / 2;

		spellNameWidth = fontRendererObj.getStringWidth(spellName);

		btnUp = new GuiButtonVariableDims(0, xMin + 177, yMin + 5, "/\\").setDimensions(20, 10);
		btnDown = new GuiButtonVariableDims(1, xMin + 177, yMin + 140, "\\/").setDimensions(20, 10);
		this.buttonList.add(btnUp);
		this.buttonList.add(btnDown);
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton) throws IOException{
		super.mouseClicked(x, y, mouseButton);
//		spellName.mouseClicked(x, y, mouseButton);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException{
		super.actionPerformed(button);

		if (button.id == btnUp.id){
			ingridientsOffset--;
			if (ingridientsOffset < 0){
				ingridientsOffset = 0;
			}
		} else if (button.id == btnDown.id) {
			ingridientsOffset++;
			if (ingridientsOffset >= ingridients.size()) {
				ingridientsOffset = ingridients.size() - 1;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		int xMin = (width - xSize) / 2;
		int yMin = (height - ySize) / 2;

		GlStateManager.color(1.0f, 1.0f, 1.0f);
		mc.renderEngine.bindTexture(background);
		this.drawTexturedModalRect(xMin, yMin, 0, 0, xSize, ySize);

		fontRendererObj.drawString(spellName, xMin + 64 - spellNameWidth / 2, yMin + 8, 0);

		int max = Math.min(ingridients.size(), ingridientsOffset + MAX_INGRIDIENTS_VISIBLE);
		for (int i = ingridientsOffset; i < max; i++) {
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			this.zLevel = 200.0F;
			this.itemRender.zLevel = 200.0F;
			this.ingridients.get(i).draw(itemRender, fontRendererObj, xMin + 135, yMin + 18 + (i - ingridientsOffset) * 20);
			this.zLevel = 0.0F;
			this.itemRender.zLevel = 0.0F;
		}

		super.drawScreen(mouseX, mouseY, partialTicks);

//		spellName.drawTextBox();
	}

	/**
	 * Ингридиент крафта заклинания
	 * Может быть как эссенцией, так и предметом
	 */
	interface SpellRecipeIngridient{
		void draw(RenderItem renderItem, FontRenderer fontRenderer, int x, int y);
	}

	/**
	 * Ингридиент заклинания, представленный предметом или блоком
	 */
	class IngridientItemStack implements SpellRecipeIngridient{
		private final ItemStack itemStack;
		private final String name, size;

		public IngridientItemStack(ItemStack itemStack, String name){
			this.itemStack = itemStack;
			this.name = name;
			this.size = "x" + itemStack.stackSize;
		}

		@Override
		public void draw(RenderItem itemRenderer, FontRenderer fontRenderer, int x, int y){
			GlStateManager.translate(0.0F, 0.0F, 32.0F);
			itemRenderer.renderItemIntoGUI(this.itemStack, x, y);
			fontRenderer.drawString(this.name, x + 18, y, 0);
			fontRenderer.drawString(this.size, x + 18, y + 8, 0);
		}
	}

}

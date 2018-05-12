package am2.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiSpellRecipe extends GuiScreen{
	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/SpellRecipe.png");

	private final ItemStack recipeStack;
	private final int xSize, ySize;
	private String spellName;
	private int spellNameWidth;

//	private int page = 0;
//	private int numPages = 0;
//	private int curIndex = 0;
//	private String curName = "";
//
//	private GuiButtonVariableDims btnNext;
//	private GuiButtonVariableDims btnPrev;
//	private GuiButtonVariableDims btnRandomName;
//	private GuiTextField spellName;

	public GuiSpellRecipe(EntityPlayer player, ItemStack recipeStack){
		this.recipeStack = recipeStack;
		this.xSize = 256;
		this.ySize = 158;
		this.spellName = recipeStack.getDisplayName();

		if (recipeStack.hasTagCompound()){
			NBTTagCompound compound = recipeStack.getTagCompound();

		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException{
		/*if (spellName.isFocused()){
			if (spellName.textboxKeyTyped(typedChar, keyCode)){
				this.curName = spellName.getText();
				((ContainerSpellCustomization)this.inventorySlots).setNameAndIndex(curName, curIndex);
			}
		}else{
			super.keyTyped(typedChar, keyCode);
		}*/
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) throws IOException{
		super.actionPerformed(par1GuiButton);

		/*if (par1GuiButton.id == btnPrev.id){
			if (page > 0){
				page--;
				for (Object btn : this.buttonList){
					if (btn instanceof GuiSpellImageButton){
						if (((GuiSpellImageButton)btn).getPage() == page) ((GuiSpellImageButton)btn).visible = true;
						else ((GuiSpellImageButton)btn).visible = false;
					}
				}
			}
		}else if (par1GuiButton.id == btnNext.id){
			if (page < numPages){
				page++;
				for (Object btn : this.buttonList){
					if (btn instanceof GuiSpellImageButton){
						if (((GuiSpellImageButton)btn).getPage() == page) ((GuiSpellImageButton)btn).visible = true;
						else ((GuiSpellImageButton)btn).visible = false;
					}
				}
			}
		}else if (par1GuiButton.id == btnRandomName.id){
			spellName.setText(SeventhSanctum.instance.getNextSuggestion());
			curName = spellName.getText();
			((ContainerSpellCustomization)this.inventorySlots).setNameAndIndex(curName, curIndex);
		}

		if (par1GuiButton instanceof GuiSpellImageButton){
			this.curIndex = ((GuiSpellImageButton)par1GuiButton).getIndex();
			((ContainerSpellCustomization)this.inventorySlots).setNameAndIndex(curName, curIndex);
			for (Object btn : this.buttonList){
				if (btn instanceof GuiSpellImageButton){
					((GuiSpellImageButton)btn).setSelected(false);
				}
			}
			((GuiSpellImageButton)par1GuiButton).setSelected(true);
		}*/
	}

	@Override
	public void initGui(){
		super.initGui();

		int xMin = (width - xSize) / 2;
		int yMin = (height - ySize) / 2;

		spellNameWidth = fontRendererObj.getStringWidth(spellName);

//		spellName = new GuiTextField(0, fontRendererObj, xMin + 8, yMin + 8, xSize - 16, 16);
//		spellName.setText("test");

		/*if (ArsMagica2.config.suggestSpellNames())
			spellName = new GuiTextField(0, fontRendererObj, xMin + 8, yMin + 8, xSize - 36, 16);
		else
			spellName = new GuiTextField(0, fontRendererObj, xMin + 8, yMin + 8, xSize - 16, 16);

		String suggestion = ((ContainerSpellCustomization)this.inventorySlots).getInitialSuggestedName();
		spellName.setText(suggestion);
		if (!suggestion.equals("")){
			curName = suggestion;
			((ContainerSpellCustomization)this.inventorySlots).setNameAndIndex(curName, curIndex);
		}


		btnPrev = new GuiButtonVariableDims(0, xMin + 8, yMin + 26, I18n.format("am2.gui.prev")).setDimensions(48, 20);
		btnNext = new GuiButtonVariableDims(1, xMin + xSize - 56, yMin + 26, I18n.format("am2.gui.next")).setDimensions(48, 20);

		btnRandomName = new GuiButtonVariableDims(2, xMin + xSize - 24, yMin + 5, "???");
		btnRandomName.setDimensions(20, 20);

		this.buttonList.add(btnPrev);
		this.buttonList.add(btnNext);

		if (ArsMagica2.config.suggestSpellNames())
			this.buttonList.add(btnRandomName);

		int IIcon_start_x = xMin + 12;
		int IIcon_start_y = yMin + 50;

		int btnX = IIcon_start_x;
		int btnY = IIcon_start_y;
		int id = 3;
		int IIconCount = 0;
		int curPage = 0;

		for (ResourceLocation location : ArsMagicaModelLoader.spellIcons){
			TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
			GuiSpellImageButton spellButton = new GuiSpellImageButton(id++, btnX, btnY, icon, IIconCount++, curPage);
			if (curPage != 0){
				spellButton.visible = false;
			}
			this.buttonList.add(spellButton);
			btnX += 14;
			if (btnX > (xMin + xSize) - 15){
				btnX = IIcon_start_x;
				btnY += 14;
				if (btnY > (yMin + ySize - 10)){
					btnY = IIcon_start_y;
					curPage++;
					}
			}
		}

		this.numPages = curPage;*/
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton) throws IOException{
		super.mouseClicked(x, y, mouseButton);
//		spellName.mouseClicked(x, y, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		int xMin = (width - xSize) / 2;
		int yMin = (height - ySize) / 2;


		GlStateManager.color(1.0f, 1.0f, 1.0f);
		mc.renderEngine.bindTexture(background);
		this.drawTexturedModalRect(xMin, yMin, 0, 0, xSize, ySize);

		fontRendererObj.drawString(spellName, xMin + 64 - spellNameWidth / 2, yMin + 8, 0);

//		spellName.drawTextBox();
	}

}

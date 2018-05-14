package am2.client.gui.widgets;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Описание ингридиента заклинания
 */
public class WidgetIngridient extends Widget{
	private static final ResourceLocation energy = new ResourceLocation("arsmagica2", "textures/gui/SpellRecipe.png");
	private final String energyType;
	private final ItemStack item;
	private final String title, size;
	
	/**
	 * Ингридиент-эфир
	 */
	public WidgetIngridient(int id, String energyType, int count){
		super(id);
		this.energyType = energyType;
		this.item = null;
		this.title = parseTitleFromEnergy(energyType);
		this.size = "x" + count;
		this.width = 100;
		this.height = 20;
	}
	
	private static String parseTitleFromEnergy(String energyType){
		String[] energyTypes = energyType.split("/");
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(" (");
		for (int i = 0; i < energyTypes.length; i++){
			stringBuilder.append(I18n.format("etherium." + energyTypes[i] + ".name"));
			if (i < energyTypes.length - 1)
				stringBuilder.append('/');
		}
		stringBuilder.append(")");
		return I18n.format("item.arsmagica2:etherium.name") + stringBuilder.toString();
	}
	
	/**
	 * Ингридиент-предмет/блок
	 */
	public WidgetIngridient(int id, ItemStack item){
		super(id);
		this.energyType = null;
		this.item = item;
		this.title = item.getDisplayName();
		this.size = "x" + item.stackSize;
		this.width = 100;
		this.height = 20;
	}
	
	@Override
	public void draw(GuiScreenWidget graphics, int xMin, int yMin, int mouseX, int mouseY){
		boolean hover = intersects(mouseX - xMin, mouseY - yMin);
		
		int ix = xMin + x;
		int iy = yMin + y;
		if (hover)
			rect(1, ix - 2, ix + width, iy - 2, iy + height - 2, 0);
		
		if (item != null){
			graphics.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			graphics.drawItem(item, ix, iy);
		}else{
			GlStateManager.color(1.0f, 1.0f, 1.0f);
			graphics.mc.renderEngine.bindTexture(energy);
			graphics.drawTexturedModalRect(ix, iy, 0, 240, 16, 16);
		}
		graphics.drawString(title, ix + 18, iy, hover ? 0x858585 : 0);
		graphics.drawString(size, ix + 18, iy + 8, hover ? 0x858585 : 0);
	}
}

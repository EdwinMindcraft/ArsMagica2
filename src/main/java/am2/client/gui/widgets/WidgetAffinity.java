package am2.client.gui.widgets;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.common.defs.ItemDefs;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/**
 * Описание родства со стихией
 */
public class WidgetAffinity extends Widget{
	private String title;
	private String percent;
	private ItemStack essence;
	
	public WidgetAffinity(int id, Affinity affinity, float value){
		super(id);
		this.width = 100;
		this.height = 20;
		this.title = affinity.getLocalizedName();
		this.percent = String.format("%.2f%%", value);
		this.essence = affinity == Affinity.NONE ? null : new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(affinity));
	}
	
	@Override
	public void draw(GuiScreenWidget graphics, int xMin, int yMin, int mouseX, int mouseY){
		boolean hover = intersects(mouseX - xMin, mouseY - yMin);
		
		int ix = xMin + x;
		int iy = yMin + y;
		if (hover)
			rect(1, ix - 2, ix + width, iy - 2, iy + height - 2, 0);
		
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		if (essence != null)
			graphics.drawItemWithEffect(essence, ix, iy);
		
		graphics.drawString(title, ix + 18, iy, hover ? 0x858585 : 0);
		graphics.drawString(percent, ix + 18, iy + 8, hover ? 0x858585 : 0);
	}
}

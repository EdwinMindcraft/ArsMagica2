package am2.client.gui.widgets;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Оболочка для взаимодействия виджетов с интерфейсом
 */
public abstract class GuiScreenWidget extends GuiScreen{
	protected List<Widget> widgets;

	public GuiScreenWidget(){
		this.widgets = new ArrayList<>();
	}

	@Override
	public void handleMouseInput() throws IOException{
		super.handleMouseInput();
		int dWheel = Mouse.getEventDWheel();
		if (dWheel != 0){
			int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
			for (Widget widget : widgets){
				if (widget.intersects(x, y)
						&& widget.wheel(x - widget.x, y - widget.y, dWheel))
					break;
			}
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
		//Не будем поддерживать стандартные кнопки
		//super.mouseClicked(mouseX, mouseY, mouseButton);
		
		for (Widget widget : widgets) {
			if (widget.intersects(mouseX, mouseY)
					&& widget.click(mouseX - widget.x, mouseY - widget.y, mouseButton))
				break;
		}
	}
	
	//region Font functions
	public int getStringWidth(String text){
		return fontRendererObj.getStringWidth(text);
	}

	public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor){
		this.fontRendererObj.drawSplitString(str, x, y, wrapWidth, textColor);
	}

	public int drawStringWithShadow(String text, float x, float y, int color){
		return this.drawString(text, x, y, color, true);
	}

	/**
	 * Draws the specified string.
	 */
	public int drawString(String text, int x, int y, int color){
		return this.drawString(text, (float)x, (float)y, color, false);
	}

	/**
	 * Draws the specified string.
	 */
	public int drawString(String text, float x, float y, int color, boolean dropShadow){
		return fontRendererObj.drawString(text, x, y, color, dropShadow);
	}
	//endregion

	//region Item drawing

	/**
	 * Нарисовать предмет на выбранной позиции
	 */
	public void drawItem(ItemStack item, int x, int y){
		this.zLevel = 200.0F;
		this.itemRender.zLevel = 200.0F;
		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		this.itemRender.renderItemIntoGUI(item, x, y);
		this.zLevel = 0.0F;
		this.itemRender.zLevel = 0.0F;
	}

	/**
	 * Нарисовать предмет с эффектами на выбранной позиции
	 */
	public void drawItemWithEffect(ItemStack item, int x, int y){
		this.zLevel = 200.0F;
		this.itemRender.zLevel = 200.0F;
		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		this.itemRender.renderItemAndEffectIntoGUI(item, x, y);
		this.zLevel = 0.0F;
		this.itemRender.zLevel = 0.0F;
	}
	//endregion

}

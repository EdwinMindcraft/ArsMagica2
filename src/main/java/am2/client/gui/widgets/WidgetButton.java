package am2.client.gui.widgets;

import am2.client.gui.controls.GuiButtonVariableDims;
import am2.client.gui.widgets.events.WidgetEvent;
import am2.client.gui.widgets.events.WidgetEventListener;

/**
 * Самая обычная кнопка (но с однотонным фоном)
 */
public class WidgetButton extends Widget{
	public GuiButtonVariableDims button;
	private WidgetEventListener listener;
	
	public WidgetButton(int id, String title, int x, int y, int width, int height){
		super(id);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		button = new GuiButtonVariableDims(id, x, y, title).setDimensions(width, height);
	}
	
	/**
	 * Установить слушателя событий виджета
	 */
	public WidgetButton setEventListener(WidgetEventListener listener){
		this.listener = listener;
		return this;
	}
	
	/**
	 * Установить состояние включенности кнопки
	 */
	public WidgetButton setEnabled(boolean enabled){
		this.button.enabled = enabled;
		return this;
	}
	
	@Override
	public void draw(GuiScreenWidget graphics, int xMin, int yMin, int mouseX, int mouseY){
		this.button.xPosition = xMin + x;
		this.button.yPosition = yMin + y;
		this.button.drawButton(graphics.mc, mouseX, mouseY);
	}
	
	@Override
	public boolean click(int localX, int localY, int button){
		if (listener != null){
			listener.event(WidgetEvent.ACTION, id, null);
		}
		return true;
	}
}

package am2.client.gui.widgets;

/**
 * Виджет текстовой метки
 */
public class WidgetLabel extends Widget{
	public int color;
	public String text;
	
	public WidgetLabel(int id, String text, int x, int y, int width, int height){
		super(id);
		this.text = text;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = 0;
	}
	
	@Override
	public void draw(GuiScreenWidget graphics, int xMin, int yMin, int mouseX, int mouseY){
		graphics.drawString(text, this.x, this.y, this.color, false);
	}
	
}

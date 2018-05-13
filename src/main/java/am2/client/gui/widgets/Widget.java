package am2.client.gui.widgets;

/**
 * Представление одного обособленного элемента в интерфейсе
 */
public abstract class Widget{
	public int width, height;
	public int x, y;

	/**
	 * Проверка на нахождение заданных координат внутри виджета
	 */
	public boolean intersects(int x, int y){
		return x >= this.x
				&& x < this.x + this.width
				&& y >= this.y
				&& y < this.y + this.height;
	}

	/**
	 * Отрисовка виджета
	 */
	public void draw(GuiScreenWidget graphics, int xMin, int yMin){}

	/**
	 * Клик мышью
	 */
	public boolean click(int localX, int localY, int button){return false;}

	/**
	 * Прокрутка колёсиком
	 */
	public boolean wheel(int localX, int localY, int delta){return false;}

//	public void mouseIn(int localX, int localY){}
//	public void mouseOut(int localX, int localY){}
}

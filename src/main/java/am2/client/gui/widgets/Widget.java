package am2.client.gui.widgets;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Представление одного обособленного элемента в интерфейсе
 */
@SideOnly(Side.CLIENT)
public abstract class Widget{
	protected static final ResourceLocation texture = new ResourceLocation("arsmagica2", "textures/gui/widgets.png");
	public int width, height;
	public int x, y;
	public int id;
	
	public Widget(int id) {
		this.id = id;
	}
	
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
	public void draw(GuiScreenWidget graphics, int xMin, int yMin, int mouseX, int mouseY){}

	/**
	 * Клик мышью
	 * @param button кнопка мыши. 0 - ЛКМ, 1 - ПКМ, 2 - Колёсико, 3 - доп., 4 - доп.
	 */
	public boolean click(int localX, int localY, int button){return false;}

	/**
	 * Прокрутка колёсиком
	 * @param delta дельта прокрутки. в среднем равна по модулю 120. прокрутка вверх положительна
	 */
	public boolean wheel(int localX, int localY, int delta){return false;}
	
	protected void line(float thickness, int startX, int startY, int endX, int endY, int color){
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(thickness);
		GL11.glColor3f((color & 0xFF0000) >> 16, (color & 0x00FF00) >> 8, color & 0x0000FF);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(startX, startY, 0);
		GL11.glVertex3f(endX, endY, 0);
		GL11.glEnd();
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	protected void rect(float thickness, int xMin, int xMax, int yMin, int yMax, int color){
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(thickness);
		GL11.glColor3f((color & 0xFF0000) >> 16, (color & 0x00FF00) >> 8, color & 0x0000FF);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(xMin, yMin, 0);
		GL11.glVertex3f(xMax, yMin, 0);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(xMax, yMin, 0);
		GL11.glVertex3f(xMax, yMax, 0);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(xMax, yMax, 0);
		GL11.glVertex3f(xMin, yMax, 0);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(xMin, yMax, 0);
		GL11.glVertex3f(xMin, yMin, 0);
		GL11.glEnd();
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

//	public void mouseIn(int localX, int localY){}
//	public void mouseOut(int localX, int localY){}
}

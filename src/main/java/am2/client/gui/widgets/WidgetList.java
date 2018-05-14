package am2.client.gui.widgets;

import java.util.ArrayList;
import java.util.List;

/**
 * Простой список из виджетов одного типа
 * <b>Не имеет скроллбара, но скроллится колёсиком</b>
 *
 * @param <T> тип виджетов внутри
 */
public class WidgetList<T extends Widget> extends Widget{
	private static final int THRESHOLD = 2;
	private final boolean drawRect;
	private List<T> list;
	private int maxHeight;
	private int currentOffset;
	
	public WidgetList(int id, int x, int y, int width, int height, boolean drawRect){
		super(id);
		this.drawRect = drawRect;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		currentOffset = 0;
		
		this.list = new ArrayList<>();
		recalc();
	}
	
	private void recalc(){
		maxHeight = 0;
		for (T item : list){
			maxHeight += item.height;
		}
	}
	
	public void remove(int index){
		if (list.size() > index){
			list.remove(index);
			recalc();
		}
	}
	
	public void add(T item){
		this.list.add(item);
		recalc();
	}
	
	@Override
	public void draw(GuiScreenWidget graphics, int xMin, int yMin, int mouseX, int mouseY){
		int listXMin = xMin + x;
		int listYMin = yMin + y;
		int listXMax = listXMin + width;
		int listYMax = listYMin + height;
		int currentY = listYMin + currentOffset;
		for (T item : list) {
			//Выводим только если полностью входит в границы
			if (currentY >= listYMin && currentY + item.height < listYMax){
				item.x = listXMin;
				item.y = currentY;
				item.draw(graphics, 0, 0, mouseX, mouseY);
			}
			currentY += item.height;
		}
		
		if (drawRect)
			rect(1, listXMin, listXMax, listXMin, listYMax, 0);
	}
	
	@Override
	public boolean wheel(int localX, int localY, int delta){
		currentOffset += delta * 0.03;
		if (currentOffset + maxHeight + THRESHOLD < height)
			currentOffset = height - maxHeight - THRESHOLD;
		if (currentOffset > THRESHOLD)
			currentOffset = THRESHOLD;
		return true;
	}
}

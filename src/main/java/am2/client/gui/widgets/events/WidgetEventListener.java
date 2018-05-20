package am2.client.gui.widgets.events;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Слушатель события виджета
 */
@SideOnly(Side.CLIENT)
public interface WidgetEventListener{
	void event(WidgetEvent type, int id, Object data);
}

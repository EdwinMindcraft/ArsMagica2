package am2.client.gui.widgets;

import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistry;
import am2.api.skill.Skill;
import am2.client.gui.AMGuiHelper;
import am2.client.texture.SpellIconManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Описание компонента заклинания (скилла)
 */
public class WidgetSkill extends Widget{
	private final String title;
	private TextureAtlasSprite skillAtlas;
	
	public WidgetSkill(int id, Skill skill){
		super(id);
		this.skillAtlas = SpellIconManager.INSTANCE.getSprite(skill.getID());
		this.title = skill.getName();
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
		
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		AMGuiHelper.DrawIconAtXY(skillAtlas, ix, iy, 0, 16, 16, false);
		
		graphics.drawString(title, ix + 18, iy + 3, hover ? 0x858585 : 0);
	}
}

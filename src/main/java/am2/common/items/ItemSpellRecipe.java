package am2.common.items;

import am2.ArsMagica2;
import am2.common.defs.IDDefs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;


public class ItemSpellRecipe extends ItemArsMagica{

	public ItemSpellRecipe(){
		super();
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack par1ItemStack){
		return "Рецепт заклинания";
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (worldIn.isRemote){
			FMLNetworkHandler.openGui(playerIn, ArsMagica2.instance, IDDefs.GUI_SPELL_RECIPE, worldIn, (int)playerIn.posX, (int)playerIn.posY, (int)playerIn.posZ);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
		}
		return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
	}

	@Override
	public boolean getShareTag(){
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> info, boolean advanced){
		info.add("Какое-то описание");
	}

	@Override
	public boolean isBookEnchantable(ItemStack bookStack, ItemStack enchantBook){
		return false;
	}

}










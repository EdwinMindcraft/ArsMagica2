package am2.common.container.slot;

import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import am2.common.defs.ItemDefs;
import am2.common.items.ItemSpellBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotInscriptionTable extends Slot{

	public SlotInscriptionTable(TileEntityInscriptionTable par1iInventory, int par2, int par3, int par4){
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack stack){
		if (stack == null || stack.getItem() == null){
			return false;
		}
		if (stack.getItem() == ItemDefs.spellRecipe && (stack.getTagCompound() == null || !stack.getTagCompound().getBoolean("spellFinalized")))
			return true;
		else if (stack.getItem() == Items.WRITABLE_BOOK)
			return true;
		else if (stack.getItem() == ItemDefs.spell)
			return true;
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack){
		if (par2ItemStack.getItem() == ItemDefs.spellRecipe)
			par2ItemStack = ((TileEntityInscriptionTable)this.inventory).writeRecipeAndDataToBook(par2ItemStack, par1EntityPlayer, "Spell Recipe");
		else
			((TileEntityInscriptionTable)this.inventory).clearCurrentRecipe();
		super.onPickupFromSlot(par1EntityPlayer, par2ItemStack);
	}

	@Override
	public void onSlotChanged(){
		if (this.getStack() != null){
			Class<? extends Item> clazz = this.getStack().getItem().getClass();
			if (ItemSpellBase.class.isAssignableFrom(clazz)){
				((TileEntityInscriptionTable)this.inventory).reverseEngineerSpell(this.getStack());
			}
		}
		super.onSlotChanged();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void putStack(ItemStack stack){
		if (stack != null && stack.getItem() == Items.WRITABLE_BOOK){
			stack.setItem(ItemDefs.spellRecipe);
		}
		super.putStack(stack);
	}
}

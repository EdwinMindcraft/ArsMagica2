package am2.common.container.slot;

import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import am2.common.defs.ItemDefs;
import am2.common.items.ItemSpellBase;
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
	public boolean isItemValid(ItemStack par1ItemStack){
		if (par1ItemStack == null || par1ItemStack.getItem() == null){
			return false;
		}
		if (par1ItemStack.getItem() == Items.WRITTEN_BOOK && (par1ItemStack.getTagCompound() == null || !par1ItemStack.getTagCompound().getBoolean("spellFinalized")))
			return true;
		else if (par1ItemStack.getItem() == Items.WRITABLE_BOOK)
			return true;
		else if (par1ItemStack.getItem() == ItemDefs.spell)
			return true;
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack){
		if (par2ItemStack.getItem() == Items.WRITTEN_BOOK)
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
			stack.setItem(Items.WRITTEN_BOOK);
			// TODO: localize (am2.tooltip.unfinishedSpellRecipe)
			stack.setStackDisplayName("Unfinished Spell Recipe");
		}
		super.putStack(stack);
	}
}

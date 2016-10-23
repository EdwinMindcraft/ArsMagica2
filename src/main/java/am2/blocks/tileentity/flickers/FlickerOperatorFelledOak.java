package am2.blocks.tileentity.flickers;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.flickers.IFlickerController;
import am2.api.math.AMVector3;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.items.ItemBindingCatalyst;
import am2.packet.AMDataReader;
import am2.packet.AMDataWriter;
import am2.utils.DummyEntityPlayer;
import am2.utils.InventoryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlickerOperatorFelledOak extends AbstractFlickerFunctionality{

	public final static FlickerOperatorFelledOak instance = new FlickerOperatorFelledOak();
	
	private DummyEntityPlayer dummyPlayer;

	private static final int radius_horiz = 6;
	private static final int radius_vert = 1;

	public FlickerOperatorFelledOak(){

	}

	void destroyTree(World world, BlockPos pos, IBlockState state){
		for (int xPos = pos.getX() - 1; xPos <= pos.getX() + 1; xPos++){
			for (int yPos = pos.getY(); yPos <= pos.getY() + 1; yPos++){
				for (int zPos = pos.getZ() - 1; zPos <= pos.getZ() + 1; zPos++){
					BlockPos newPos = new BlockPos(xPos, yPos, zPos);
					IBlockState localblock = world.getBlockState(newPos);
					if (state.getBlock() == localblock.getBlock()){
						if (state.equals(localblock)){
							state.getBlock().harvestBlock(world, dummyPlayer, newPos, state, null, null);
							state.getBlock().onBlockHarvested(world, newPos, state, dummyPlayer);
							world.destroyBlock(newPos, false);
							destroyTree(world, newPos, state);
						}
					}
				}
			}
		}
	}

	void beginTreeFelling(World world, BlockPos pos){
		IBlockState wood = world.getBlockState(pos);
		while (wood.getBlock().isWood(world, pos)){
			pos = pos.down();
			wood = world.getBlockState(pos);
		}

		pos = pos.up();

		wood = world.getBlockState(pos);

		if (wood.getBlock().isWood(world, pos)){
			int height = pos.getY();
			boolean foundTop = false;
			do{
				height++;
				IBlockState block = world.getBlockState(new BlockPos(pos.getX(), height, pos.getZ()));
				if (block.getBlock() != wood.getBlock()){
					height--;
					foundTop = true;
				}
			}while (!foundTop);

			int numLeaves = 0;
			if (height - pos.getY() < 50){
				//System.out.println(pos.up(height - pos.getY()));
				for (int xPos = pos.getX() - 1; xPos <= pos.getX() + 1; xPos++){
					for (int yPos = height - 1; yPos <= height + 1; yPos++){
						for (int zPos = pos.getZ() - 1; zPos <= pos.getZ() + 1; zPos++){
							BlockPos newPos = new BlockPos(xPos, yPos, zPos);
							IBlockState leaves = world.getBlockState(newPos);
							if (leaves != null && leaves.getBlock().isLeaves(leaves, world, newPos))
								numLeaves++;
						}
					}
				}
			}
			
			if (numLeaves > 3)
				destroyTree(world, pos, world.getBlockState(pos));


//			if (!world.isRemote)
//				world.playAuxSFX(2001, pos, Block.getIdFromBlock(wood.getBlock()) + (WorldUtils.getBlockMeta(wood) << 12));
		}
	}

	@SuppressWarnings("deprecation")
	private void plantTree(World worldObj, IFlickerController<?> habitat, boolean powered){
		if (!powered || worldObj.isRemote)
			return;

		ItemStack sapling = getSaplingFromNearbyChest(worldObj, habitat);
		if (sapling == null)
			return;

		AMVector3 plantLoc = getPlantLocation(worldObj, habitat, sapling);

		if (plantLoc == null)
			return;

		deductSaplingFromNearbyChest(worldObj, habitat);
		ItemBlock block = (ItemBlock)sapling.getItem();

		worldObj.setBlockState(plantLoc.toBlockPos(), block.getBlock().getStateFromMeta(sapling.getItemDamage()), 3);
	}

	private AMVector3 getPlantLocation(World worldObj, IFlickerController<?> habitat, ItemStack sapling){
		if (sapling.getItem() instanceof ItemBlock == false)
			return null;
		TileEntity te = (TileEntity)habitat;
		byte[] data = habitat.getMetadata(this);
		AMVector3 offset = null;
		if (data == null || data.length == 0){
			offset = new AMVector3(te.getPos().getX() - radius_horiz, te.getPos().getY() - radius_vert, te.getPos().getZ() - radius_horiz);
		}else{
			AMDataReader reader = new AMDataReader(data, false);
			offset = new AMVector3(reader.getInt(), te.getPos().getY() - radius_vert, reader.getInt());
		}

		Block treeBlock = ((ItemBlock)sapling.getItem()).block;

		for (int i = (int)offset.x; i <= te.getPos().getX() + radius_horiz; i += 2){
			for (int k = (int)offset.z; k <= te.getPos().getZ() + radius_horiz; k += 2){
				for (int j = (int)offset.y; j <= te.getPos().getY() + radius_vert; ++j){
					BlockPos newPos = new BlockPos(i, j, k);
					IBlockState block = worldObj.getBlockState(newPos);
					if (block.getBlock().isReplaceable(worldObj, newPos) && treeBlock.canPlaceBlockAt(worldObj, newPos)){
						AMDataWriter writer = new AMDataWriter();
						writer.add(i).add(k);
						habitat.setMetadata(this, writer.generate());
						return new AMVector3(i, j, k);
					}
				}
			}
		}

		AMDataWriter writer = new AMDataWriter();
		writer.add(te.getPos().getX() - radius_horiz).add(te.getPos().getX() - radius_horiz);
		habitat.setMetadata(this, writer.generate());

		return null;
	}

	/**
	 * Gets a single sapling from an adjacent chest
	 *
	 * @return
	 */
	private ItemStack getSaplingFromNearbyChest(World worldObj, IFlickerController<?> habitat){
		for (EnumFacing dir : EnumFacing.values()){
			IInventory inv = getOffsetInventory(worldObj, habitat, dir);
			if (inv == null)
				continue;
			int index = InventoryUtilities.getInventorySlotIndexFor(inv, new ItemStack(Blocks.SAPLING, 1, Short.MAX_VALUE));
			if (index > -1){
				ItemStack stack = inv.getStackInSlot(index).copy();
				stack.stackSize = 1;
				return stack;
			}
		}
		return null;
	}

	private void deductSaplingFromNearbyChest(World worldObj, IFlickerController<?> habitat){
		for (EnumFacing dir : EnumFacing.values()){
			IInventory inv = getOffsetInventory(worldObj, habitat, dir);
			if (inv == null)
				continue;
			int index = InventoryUtilities.getInventorySlotIndexFor(inv, new ItemStack(Blocks.SAPLING, 1, Short.MAX_VALUE));
			if (index > -1){
				InventoryUtilities.decrementStackQuantity(inv, index, 1);
				return;
			}
		}
	}

	/**
	 * Gets an instance of the adjacent IInventory at direction offset.  Returns null if not found or invalid type adjacent.
	 */
	private IInventory getOffsetInventory(World worldObj, IFlickerController<?> habitat, EnumFacing direction){
		TileEntity te = (TileEntity)habitat;
		TileEntity adjacent = worldObj.getTileEntity(te.getPos().offset(direction));
		if (adjacent != null && adjacent instanceof IInventory)
			return (IInventory)adjacent;
		return null;
	}

	@Override
	public boolean RequiresPower(){
		return false;
	}

	@Override
	public int PowerPerOperation(){
		return 100;
	}

	@Override
	public boolean DoOperation(World worldObj, IFlickerController<?> habitat, boolean powered){
		int radius = 6;
		
		dummyPlayer = new DummyEntityPlayer(worldObj);

		for (int i = -radius; i <= radius; ++i){
			for (int j = -radius; j <= radius; ++j){
				BlockPos newPos = ((TileEntity)habitat).getPos().add(i, 0, j);
				Block block = worldObj.getBlockState(newPos).getBlock();
				if (block == Blocks.AIR) continue;
				if (block.isWood(worldObj, newPos)){
					if (!worldObj.isRemote)
						beginTreeFelling(worldObj, newPos);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean DoOperation(World worldObj, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){

		boolean hasNatureAugment = false;
		for (Affinity aff : flickers){
			if (aff == Affinity.NATURE){
				hasNatureAugment = true;
				break;
			}
		}

		if (hasNatureAugment){
			plantTree(worldObj, habitat, powered);
		}

		return DoOperation(worldObj, habitat, powered);
	}

	@Override
	public void RemoveOperator(World worldObj, IFlickerController<?> habitat, boolean powered){
	}

	@Override
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers){
		int base = powered ? 300 : 3600;
		float augments = 1.0f;
		for (Affinity aff : flickers){
			if (aff == Affinity.LIGHTNING)
				augments *= 0.5f;
		}
		return (int)Math.ceil(base * augments);
	}

	@Override
	public void RemoveOperator(World worldObj, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				"WG ",
				"NCL",
				" OW",
				Character.valueOf('W'), BlockDefs.witchwoodLog,
				Character.valueOf('G'), new ItemStack(ItemDefs.rune, 1, EnumDyeColor.GREEN.getDyeDamage()),
				Character.valueOf('N'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.NATURE)),
				Character.valueOf('L'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIGHTNING)),
				Character.valueOf('G'), new ItemStack(ItemDefs.rune, 1, EnumDyeColor.ORANGE.getDyeDamage()),
				Character.valueOf('G'), new ItemStack(ItemDefs.bindingCatalyst, 1, ItemBindingCatalyst.META_AXE)
		};
	}
	
	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "FlickerOperatorFelledOak");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[]{Affinity.NATURE, Affinity.LIGHTNING};
	}

}

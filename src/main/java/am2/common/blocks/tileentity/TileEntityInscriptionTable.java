package am2.common.blocks.tileentity;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistry;
import am2.api.affinity.Affinity;
import am2.api.event.SpellRecipeItemsEvent;
import am2.api.extensions.ISpellCaster;
import am2.api.skill.Skill;
import am2.api.spell.*;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.client.particles.ParticleHoldPosition;
import am2.common.LogHelper;
import am2.common.blocks.BlockInscriptionTable;
import am2.common.container.ContainerInscriptionTable;
import am2.common.defs.BlockDefs;
import am2.common.defs.ItemDefs;
import am2.common.lore.Story;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.packet.AMNetHandler;
import am2.common.packet.AMPacketIDs;
import am2.common.power.PowerTypes;
import am2.common.spell.SpellCaster;
import am2.common.spell.SpellValidator;
import am2.common.utils.KeyValuePair;
import am2.common.utils.NBTUtils;
import am2.common.utils.RecipeUtils;
import am2.common.utils.SpellUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

import com.google.common.collect.Lists;

public class TileEntityInscriptionTable extends TileEntity implements IInventory, ITickable, ITileEntityAMBase {

	private ItemStack inscriptionTableItemStacks[];
	private final ArrayList<AbstractSpellPart> currentRecipe;
	private final ArrayList<ArrayList<AbstractSpellPart>> shapeGroups;
	private int numStageGroups = 2;
	private boolean dirty = false;
	public static final int MAX_STAGE_GROUPS = 5;
	public static int bookIndex = 0;
	public static int paperIndex = 1;
	public static int featherIndex = 2;
	public static int inkIndex = 3;
	private EntityPlayer currentPlayerUsing;
	private int ticksToNextParticle = 20;
	private final HashMap<SpellModifiers, Integer> modifierCount;
	private String currentSpellName;
	private boolean currentSpellIsReadOnly;

	private static final byte FULL_UPDATE = 0x1;
	private static final byte MAKE_SPELL = 0x2;
	private static final byte RESET_NAME = 0x4;

	public TileEntityInscriptionTable(){
		this.inscriptionTableItemStacks = new ItemStack[this.getSizeInventory()];
		this.currentPlayerUsing = null;
		this.currentSpellName = "";
		this.currentRecipe = new ArrayList<>();
		this.shapeGroups = new ArrayList<>();

		for (int i = 0; i < MAX_STAGE_GROUPS; ++i){
			this.shapeGroups.add(new ArrayList<>());
		}

		this.modifierCount = new HashMap<>();
		this.resetModifierCount();
	}

	public ArrayList<AbstractSpellPart> getCurrentRecipe(){
		return this.currentRecipe;
	}

	@Override
	public int getSizeInventory(){
		return 4;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		return this.inscriptionTableItemStacks[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j){
		if (this.inscriptionTableItemStacks[i] != null){
			if (this.inscriptionTableItemStacks[i].stackSize <= j){
				ItemStack itemstack = this.inscriptionTableItemStacks[i];
				this.inscriptionTableItemStacks[i] = null;
				return itemstack;
			}
			ItemStack itemstack1 = this.inscriptionTableItemStacks[i].splitStack(j);
			if (this.inscriptionTableItemStacks[i].stackSize == 0){
				this.inscriptionTableItemStacks[i] = null;
			}
			return itemstack1;
		}else{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		this.inscriptionTableItemStacks[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()){
			itemstack.stackSize = this.getInventoryStackLimit();
		}
	}

	@Override
	public String getName(){
		return "Inscription Table";
	}

	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer){
		if (this.worldObj.getTileEntity(this.pos) != this){
			return false;
		}
		return entityplayer.getDistanceSqToCenter(this.pos) <= 64D;
	}

	public boolean isInUse(EntityPlayer player){
		return this.currentPlayerUsing != null && this.currentPlayerUsing.getEntityId() != player.getEntityId();
	}

	public void setInUse(EntityPlayer player){
		this.currentPlayerUsing = player;
		if (!this.worldObj.isRemote){
			this.markDirty();
			//worldObj.markAndNotifyBlock(pos, worldObj.getChunkFromBlockCoords(pos), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
		}
	}

	public EntityPlayer getCurrentPlayerUsing(){
		return this.currentPlayerUsing;
	}

	private boolean isRenderingLeft(){
		return this.worldObj.getBlockState(this.pos).getValue(BlockInscriptionTable.LEFT);
	}

	@Override
	public void update(){
		if (this.worldObj.getBlockState(this.pos).getBlock() != BlockDefs.inscriptionTable){
			this.invalidate();
			return;
		}
		if (this.numStageGroups > MAX_STAGE_GROUPS)
			this.numStageGroups = MAX_STAGE_GROUPS;
		//if (!this.worldObj.isRemote) {
			boolean shouldSet = false;
			IBlockState state = this.worldObj.getBlockState(this.pos);
			if (this.getUpgradeState() >= 1 && !state.getValue(BlockInscriptionTable.TIER_1)) {
				shouldSet = true;
			}
			else if (this.getUpgradeState() >= 2 && !state.getValue(BlockInscriptionTable.TIER_2)) {
				shouldSet = true;
			}
			else if (this.getUpgradeState() >= 3 && !state.getValue(BlockInscriptionTable.TIER_3)) {
				shouldSet = true;
			}
			if (shouldSet)
				this.worldObj.setBlockState(this.pos, this.worldObj.getBlockState(this.pos).withProperty(BlockInscriptionTable.TIER_1, this.getUpgradeState() >= 1).withProperty(BlockInscriptionTable.TIER_2, this.getUpgradeState() >= 2).withProperty(BlockInscriptionTable.TIER_3, this.getUpgradeState() >= 3), 2);
		//}
		if (this.worldObj.isRemote && this.getUpgradeState() >= 3)
			this.candleUpdate();
		this.markDirty();
		//worldObj.markAndNotifyBlock(pos, worldObj.getChunkFromBlockCoords(pos), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
	}

	public int getUpgradeState(){
		return this.numStageGroups - 2;
	}

	@SuppressWarnings("incomplete-switch")
	private void candleUpdate(){
		if (--this.ticksToNextParticle < 0)
			this.ticksToNextParticle = 0;

		if (this.isRenderingLeft()){
			if (this.ticksToNextParticle == 0 || this.ticksToNextParticle == 15) {

				double particleX = 0;
				double particleZ = 0;

				switch (this.worldObj.getBlockState(this.pos).getValue(BlockInscriptionTable.FACING)){
				case SOUTH:
					particleX = this.getPos().getX() + 0.85;
					particleZ = this.getPos().getZ() + 0.22;
					break;
				case NORTH:
					particleX = this.getPos().getX() + 0.15;
					particleZ = this.getPos().getZ() + 0.78;
					break;
				case WEST:
					particleX = this.getPos().getX() + 0.78;
					particleZ = this.getPos().getZ() + 0.85;
					break;
				case EAST:
					particleX = this.getPos().getX() + 0.22;
					particleZ = this.getPos().getZ() + 0.15;
					break;
				}

				this.ticksToNextParticle = 30;
				AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(this.worldObj, "fire_hand", particleX, this.getPos().getY() + 1.32, particleZ);
				if (effect != null){
					effect.setParticleScale(0.025f, 0.1f, 0.025f);
					effect.AddParticleController(new ParticleHoldPosition(effect, 29, 1, false));
					effect.setIgnoreMaxAge(false);
					effect.setMaxAge(400);
				}

				if (this.worldObj.rand.nextInt(100) > 80){
					AMParticle smoke = (AMParticle)ArsMagica2.proxy.particleManager.spawn(this.worldObj, "smoke", particleX, this.getPos().getY() + 1.4, particleZ);
					if (smoke != null){
						smoke.setParticleScale(0.025f);
						smoke.AddParticleController(new ParticleFloatUpward(smoke, 0.01f, 0.01f, 1, false));
						smoke.setIgnoreMaxAge(false);
						smoke.setMaxAge(20 + this.worldObj.rand.nextInt(10));
					}
				}
			}
			if (this.ticksToNextParticle == 10 || this.ticksToNextParticle == 25){
				double particleX = 0;
				double particleZ = 0;

				switch (this.worldObj.getBlockState(this.pos).getValue(BlockInscriptionTable.FACING)){
				case SOUTH:
					particleX = this.getPos().getX() + 0.41;
					particleZ = this.getPos().getZ() - 0.72;
					break;
				case NORTH:
					particleX = this.getPos().getX() + 0.59;
					particleZ = this.getPos().getZ() + 1.72;
					break;
				case EAST:
					particleX = this.getPos().getX() - 0.72;
					particleZ = this.getPos().getZ() + 0.59;
					break;
				case WEST:
					particleX = this.getPos().getX() + 1.72;
					particleZ = this.getPos().getZ() + 0.41;
					break;
				}

				AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(this.worldObj, "fire_hand", particleX, this.getPos().getY() + 1.26, particleZ);
				if (effect != null){
					effect.setParticleScale(0.025f, 0.1f, 0.025f);
					effect.AddParticleController(new ParticleHoldPosition(effect, 29, 1, false));
					effect.setIgnoreMaxAge(false);
					effect.setMaxAge(400);
				}

				if (this.worldObj.rand.nextInt(100) > 80){
					AMParticle smoke = (AMParticle)ArsMagica2.proxy.particleManager.spawn(this.worldObj, "smoke", particleX, this.getPos().getY() + 1.4, particleZ);
					if (smoke != null){
						smoke.setParticleScale(0.025f);
						smoke.AddParticleController(new ParticleFloatUpward(smoke, 0.01f, 0.01f, 1, false));
						smoke.setIgnoreMaxAge(false);
						smoke.setMaxAge(20 + this.worldObj.rand.nextInt(10));
					}
				}
			}

		}
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (this.inscriptionTableItemStacks[i] != null){
			ItemStack itemstack = this.inscriptionTableItemStacks[i];
			this.inscriptionTableItemStacks[i] = null;
			return itemstack;
		}else{
			return null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound){
		super.readFromNBT(par1NBTTagCompound);
		this.clearCurrentRecipe();
		this.parseTagCompound(par1NBTTagCompound);
	}

	private void parseTagCompound(NBTTagCompound par1NBTTagCompound){
		NBTTagList nbttaglist = par1NBTTagCompound.getTagList("InscriptionTableInventory", Constants.NBT.TAG_COMPOUND);
		this.inscriptionTableItemStacks = new ItemStack[this.getSizeInventory()];
		for (int i = 0; i < nbttaglist.tagCount(); i++){
			String tag = String.format("ArrayIndex", i);
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < this.inscriptionTableItemStacks.length){
				this.inscriptionTableItemStacks[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
		this.shapeGroups.clear();
		NBTTagList shapeGroups = par1NBTTagCompound.getTagList("ShapeGroups", Constants.NBT.TAG_LIST);
		for (int i = 0; i < shapeGroups.tagCount(); i++){
			NBTTagList tmplist = (NBTTagList) shapeGroups.get(i);
			ArrayList<AbstractSpellPart> parts = new ArrayList<>();
			for (int j = 0; j < tmplist.tagCount(); j++) {
				NBTTagCompound tmp = tmplist.getCompoundTagAt(j);
				parts.add(tmp.getInteger("Slot"), ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation(tmp.getString("ID"))));
			}
			this.shapeGroups.add(parts);
		}
		this.currentRecipe.clear();
		NBTTagList recipe = par1NBTTagCompound.getTagList("CurrentRecipe", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < recipe.tagCount(); i++){
			NBTTagCompound tmp = recipe.getCompoundTagAt(i);
			this.currentRecipe.add(tmp.getInteger("Slot"), ArsMagicaAPI.getSpellRegistry().getObject(new ResourceLocation(tmp.getString("ID"))));
		}
		this.numStageGroups = Math.max(par1NBTTagCompound.getInteger("numShapeGroupSlots"), 2);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound){
		super.writeToNBT(par1NBTTagCompound);
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < this.inscriptionTableItemStacks.length; i++){
			if (this.inscriptionTableItemStacks[i] != null){
				String tag = String.format("ArrayIndex", i);
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte(tag, (byte)i);
				this.inscriptionTableItemStacks[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		NBTTagList recipe = new NBTTagList();
		for (int i = 0; i < this.currentRecipe.size(); i++) {
			NBTTagCompound tmp = new NBTTagCompound();
			tmp.setInteger("Slot", i);
			tmp.setString("ID", SpellRegistry.getSkillFromPart(this.currentRecipe.get(i)).getID());
			recipe.appendTag(tmp);
		}
		NBTTagList shapeGroups = new NBTTagList();
		for (int j = 0; j < this.shapeGroups.size(); j++) {
			ArrayList<AbstractSpellPart> parts = this.shapeGroups.get(j);
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < parts.size(); i++) {
				NBTTagCompound tmp = new NBTTagCompound();
				tmp.setInteger("Slot", i);
				tmp.setString("ID", SpellRegistry.getSkillFromPart(parts.get(i)).getID());
				list.appendTag(tmp);
			}
			shapeGroups.appendTag(list);
		}
		par1NBTTagCompound.setTag("ShapeGroups", shapeGroups);
		par1NBTTagCompound.setTag("CurrentRecipe", recipe);
		par1NBTTagCompound.setTag("InscriptionTableInventory", nbttaglist);
		par1NBTTagCompound.setInteger("numShapeGroupSlots", this.numStageGroups);
		return par1NBTTagCompound;
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return false;
	}

	public void HandleUpdatePacket(byte[] data){
		if (this.worldObj == null)
			return;
		AMDataReader rdr = new AMDataReader(data);
		switch (rdr.ID){
		case FULL_UPDATE:
			if (!rdr.getBoolean()){
				Entity e = this.worldObj.getEntityByID(rdr.getInt());
				if (e instanceof EntityPlayer){
					EntityPlayer player = (EntityPlayer)e;
					this.setInUse(player);
				}else{
					this.setInUse(null);
				}
			}else{
				this.setInUse(null);
			}

			this.currentRecipe.clear();
			int partLength = rdr.getInt();
			for (int i = 0; i < partLength; ++i){
				Skill part = ArsMagicaAPI.getSkillRegistry().getObjectById(rdr.getInt());
				AbstractSpellPart spellPart = ArsMagicaAPI.getSpellRegistry().getObject(part.getRegistryName());
				if (spellPart != null)
					this.currentRecipe.add(spellPart);
			}

			this.shapeGroups.clear();
			int numGroups = rdr.getInt();
			for (int i = 0; i < numGroups; ++i){
				ArrayList<AbstractSpellPart> group = new ArrayList<>();
				int[] partData = rdr.getIntArray();
				for (int n : partData){
					Skill part = ArsMagicaAPI.getSkillRegistry().getObjectById(n);
					AbstractSpellPart spellPart = ArsMagicaAPI.getSpellRegistry().getObject(part.getRegistryName());
					if (spellPart != null)
						group.add(spellPart);
				}
				this.shapeGroups.add(group);
			}

			this.countModifiers();
			this.currentSpellName = rdr.getString();
			this.currentSpellIsReadOnly = rdr.getBoolean();
			this.numStageGroups = rdr.getInt();
			break;
		case MAKE_SPELL:
			int entityID = rdr.getInt();
			EntityPlayer player = (EntityPlayer) this.worldObj.getEntityByID(entityID);
			if (player != null){
				this.createSpellForPlayer(player);
			}
			break;
		case RESET_NAME:
			entityID = rdr.getInt();
			player = (EntityPlayer) this.worldObj.getEntityByID(entityID);
			if (player != null){
				((ContainerInscriptionTable)player.openContainer).resetSpellNameAndIcon();
			}
			break;
		}
	}

	private byte[] GetUpdatePacketForServer(){
		AMDataWriter writer = new AMDataWriter();
		writer.add(FULL_UPDATE);
		writer.add(this.currentPlayerUsing == null);
		if (this.currentPlayerUsing != null) writer.add(this.currentPlayerUsing.getEntityId());

		writer.add(this.currentRecipe.size());
		for (int i = 0; i < this.currentRecipe.size(); ++i){
			writer.add(ArsMagicaAPI.getSkillRegistry().getId(this.currentRecipe.get(i).getRegistryName()));
		}


		writer.add(this.shapeGroups.size());
		for (ArrayList<AbstractSpellPart> shapeGroup : this.shapeGroups){
			int[] groupData = new int[shapeGroup.size()];
			for (int i = 0; i < shapeGroup.size(); ++i){
				groupData[i] = ArsMagicaAPI.getSkillRegistry().getId(shapeGroup.get(i).getRegistryName());
			}
			writer.add(groupData);
		}

		writer.add(this.currentSpellName);
		writer.add(this.currentSpellIsReadOnly);
		writer.add(this.numStageGroups);

		return writer.generate();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		return new SPacketUpdateTileEntity(this.getPos(), this.getBlockMetadata(), compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.parseTagCompound(pkt.getNbtCompound());
	}

	private void sendDataToServer(){
		AMDataWriter writer = new AMDataWriter();
		writer.add(this.getPos().getX());
		writer.add(this.getPos().getY());
		writer.add(this.getPos().getZ());
		writer.add(this.GetUpdatePacketForServer());

		AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.INSCRIPTION_TABLE_UPDATE, writer.generate());
	}

	public void addSpellPartToStageGroup(int groupIndex, AbstractSpellPart part){
		ArrayList<AbstractSpellPart> group = this.shapeGroups.get(groupIndex);
		if (!this.currentSpellIsReadOnly && group.size() < 4 && !(part instanceof SpellComponent)){
			group.add(part);
			if (this.worldObj.isRemote)
				this.sendDataToServer();
			this.countModifiers();
		}
	}

	public void removeSpellPartFromStageGroup(int index, int groupIndex){
		ArrayList<AbstractSpellPart> group = this.shapeGroups.get(groupIndex);
		if (!this.currentSpellIsReadOnly){
			group.remove(index);
			if (this.worldObj.isRemote)
				this.sendDataToServer();
			this.countModifiers();
		}
	}

	public void removeMultipleSpellPartsFromStageGroup(int startIndex, int length, int groupIndex){
		ArrayList<AbstractSpellPart> group = this.shapeGroups.get(groupIndex);
		if (!this.currentSpellIsReadOnly){
			for (int i = 0; i <= length; ++i)
				group.remove(startIndex);
			this.countModifiers();
			if (this.worldObj.isRemote)
				this.sendDataToServer();
		}
	}

	public void addSpellPart(AbstractSpellPart part){
		if (!this.currentSpellIsReadOnly && this.currentRecipe.size() < 16){
			this.currentRecipe.add(part);
			if (this.worldObj.isRemote)
				this.sendDataToServer();
			this.countModifiers();
		}
	}

	public void removeSpellPart(int index){
		if (!this.currentSpellIsReadOnly){
			this.currentRecipe.remove(index);
			if (this.worldObj.isRemote)
				this.sendDataToServer();
			this.countModifiers();
		}
	}

	public void removeMultipleSpellParts(int startIndex, int length){
		if (!this.currentSpellIsReadOnly){
			for (int i = 0; i <= length; ++i)
				this.getCurrentRecipe().remove(startIndex);
			this.countModifiers();
			if (this.worldObj.isRemote)
				this.sendDataToServer();
		}
	}

	public int getNumStageGroups(){
		return this.numStageGroups;
	}

	private void countModifiers(){

		this.resetModifierCount();

		for (ArrayList<AbstractSpellPart> shapeGroup : this.shapeGroups){
			this.countModifiersInList(shapeGroup);
		}

		ArrayList<ArrayList<AbstractSpellPart>> stages = SpellValidator.splitToStages(this.currentRecipe);
		if (stages.size() == 0) return;

		for (ArrayList<AbstractSpellPart> currentStage : stages){
			this.countModifiersInList(currentStage);
		}
		//ArrayList<AbstractSpellPart> currentStage = stages.get(stages.size() - 1);
		//countModifiersInList(currentStage);
	}

	private void countModifiersInList(ArrayList<AbstractSpellPart> currentStage){
		for (AbstractSpellPart part : currentStage){
			if (part instanceof SpellModifier){
				EnumSet<SpellModifiers> modifiers = ((SpellModifier)part).getAspectsModified();
				for (SpellModifiers modifier : modifiers){
					int count = this.modifierCount.get(modifier) + 1;
					this.modifierCount.put(modifier, count);
				}
			}
		}
	}

	private void resetModifierCount(){
		this.modifierCount.clear();
		for (SpellModifiers modifier : SpellModifiers.values()){
			this.modifierCount.put(modifier, 0);
		}
	}

	public int getModifierCount(SpellModifiers modifier){
		return this.modifierCount.get(modifier);
	}

	public void createSpellForPlayer(EntityPlayer player){
		if (this.worldObj.isRemote){
			AMDataWriter writer = new AMDataWriter();
			writer.add(this.getPos().getX());
			writer.add(this.getPos().getY());
			writer.add(this.getPos().getZ());
			writer.add(MAKE_SPELL);
			writer.add(player.getEntityId());
			AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.INSCRIPTION_TABLE_UPDATE, writer.generate());
		}else{

			ArrayList<KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound>> shapeGroupSetup = new ArrayList<>();
			KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> curRecipeSetup = new KeyValuePair<>(this.currentRecipe, new NBTTagCompound());

			for (ArrayList<AbstractSpellPart> arr : this.shapeGroups){
				shapeGroupSetup.add(new KeyValuePair<>(arr, new NBTTagCompound()));
			}
			ItemStack stack = new ItemStack(ItemDefs.spell);
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			ISpellCaster caster = stack.getCapability(SpellCaster.INSTANCE, null);
			if (caster != null) {
				caster.setSpellCommon(SpellUtils.transformParts(curRecipeSetup.key));
				caster.setCommonStoredData(curRecipeSetup.value);
				List<List<List<AbstractSpellPart>>> shapeGroups = Lists.newArrayList();
				for (int i = 0; i < shapeGroupSetup.size(); i++) {
					KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> entry = shapeGroupSetup.get(i);
					shapeGroups.add(SpellUtils.transformParts(entry.key));
					caster.setStoredData(i, entry.value);
				}
				caster.setShapeGroups(shapeGroups);
			}

			stack.getTagCompound().setString("suggestedName", this.currentSpellName);
			player.inventory.addItemStackToInventory(stack);
		}
	}
	
	private String getFullItemName(Item item) {
		return "I" + Item.REGISTRY.getNameForObject(item).toString();
	}
	private String getFullBlockName(Block block) {
		return "B" + Block.REGISTRY.getNameForObject(block).toString();
	}
	
	public ItemStack writeRecipeAndDataToBook(ItemStack bookstack, EntityPlayer player, String title){
		if (bookstack.getItem() == ItemDefs.spellRecipe && this.currentRecipe != null){
			if (!this.currentRecipeIsValid().valid)
				return bookstack;

			if (!bookstack.hasTagCompound())
				bookstack.setTagCompound(new NBTTagCompound());
			else if (bookstack.getTagCompound().getBoolean("spellFinalized")) //don't overwrite a completed spell
				return bookstack;

			LinkedHashMap<String, Integer> materialsList = new LinkedHashMap<String, Integer>();
			
			materialsList.put(getFullItemName(ItemDefs.blankRune), 1);

			ArrayList<ItemStack> componentRecipeList = new ArrayList<ItemStack>();
			ArrayList<AbstractSpellPart> allRecipeItems = new ArrayList<AbstractSpellPart>();

			for (ArrayList<AbstractSpellPart> shapeGroup : this.shapeGroups){
				if (shapeGroup == null || shapeGroup.size() == 0)
					continue;
				allRecipeItems.addAll(shapeGroup);
			}

			allRecipeItems.addAll(this.currentRecipe);
			for (AbstractSpellPart part : allRecipeItems){
				
				if (part == null){
					LogHelper.error("Unable to write recipe to book.  Recipe part is null!");
					return bookstack;
				}
				
				Object[] recipeItems = part.getRecipe();
				SpellRecipeItemsEvent event = new SpellRecipeItemsEvent(SpellRegistry.getSkillFromPart(part).getID(), recipeItems);
				MinecraftForge.EVENT_BUS.post(event);
				recipeItems = event.recipeItems;

				if (recipeItems == null){
					LogHelper.error("Unable to write recipe to book.  Recipe items are null for part %s!", SpellRegistry.getSkillFromPart(part).getName());
					return bookstack;
				}
				for (int i = 0; i < recipeItems.length; ++i){
					Object o = recipeItems[i];
					String materialkey = "";
					int qty = 1;
					ItemStack recipeStack = null;
					if (o instanceof ItemStack){
						materialkey = getFullItemName(((ItemStack) o).getItem());
						recipeStack = (ItemStack)o;
					}else if (o instanceof Item){
						recipeStack = new ItemStack((Item)o);
						materialkey = getFullItemName((Item) o);
					}else if (o instanceof Block){
						recipeStack = new ItemStack((Block)o);
						materialkey = getFullBlockName((Block) o);
					}else if (o instanceof String){
						if (((String)o).startsWith("E")){
							int[] ids = RecipeUtils.ParseEssenceIDs((String)o);
							materialkey = "E";
							for (int powerID : ids){
								PowerTypes type = PowerTypes.getByID(powerID);
								materialkey += type.name() + "/";
							}

							if (materialkey.equals("E")){
								++i;
								continue;
							}

							o = recipeItems[++i];
							if (materialkey.startsWith("E")){
								materialkey = materialkey.substring(0, materialkey.lastIndexOf("/"));
								qty = (Integer)o;
								int flag = 0;
								for (int f : ids){
									flag |= f;
								}

								recipeStack = new ItemStack(ItemDefs.etherium, qty, flag);
							}

						}else{
							List<ItemStack> ores = OreDictionary.getOres((String)o);
							recipeStack = ores.size() > 0 ? ores.get(1) : null;
							materialkey = (String)o;
						}
					}

					if (materialsList.containsKey(materialkey)){
						int old = materialsList.get(materialkey);
						old += qty;
						materialsList.put(materialkey, old);
					}else{
						materialsList.put(materialkey, qty);
					}

					if (recipeStack != null)
						componentRecipeList.add(recipeStack);
				}
			}

			materialsList.put(getFullItemName(ItemDefs.spellParchment), 1);
			
			NBTTagList materialsNBTList = new NBTTagList();
			for (String key : materialsList.keySet()) {
				NBTTagCompound material = new NBTTagCompound();
				material.setString("id", key);
				material.setInteger("count", materialsList.get(key));
				materialsNBTList.appendTag(material);
			}
			bookstack.getTagCompound().setTag("materials", materialsNBTList);
			
			StringBuilder sb = new StringBuilder();
			int sgCount = 0;
			int[][] shapeGroupCombos = new int[this.shapeGroups.size()][];
			for (ArrayList<AbstractSpellPart> shapeGroup : this.shapeGroups){
				sb.append("Shape Group " + ++sgCount + "\n\n");
				Iterator<AbstractSpellPart> it = shapeGroup.iterator();
				shapeGroupCombos[sgCount - 1] = this.SpellPartListToStringBuilder(it, sb, " -");
				sb.append("\n");
			}

			sb.append("Combination:\n\n");
			Iterator<AbstractSpellPart> it = this.currentRecipe.iterator();
			int[] outputData = this.SpellPartListToStringBuilder(it, sb, null);
			LogHelper.info(sb.toString());

			ArrayList<NBTTagString> pages = Story.splitStoryPartIntoPages(sb.toString());

			sb = new StringBuilder();
			sb.append("\n\nMaterials List:\n\n");
			for (String s : materialsList.keySet()){
				sb.append(materialsList.get(s) + " x " + s + "\n");
			}

			pages.addAll(Story.splitStoryPartIntoPages(sb.toString()));

			sb = new StringBuilder();
			sb.append("Affinity Breakdown:\n\n");
			it = this.currentRecipe.iterator();
			HashMap<Affinity, Integer> affinityData = new HashMap<Affinity, Integer>();
			int cpCount = 0;
			while (it.hasNext()){
				AbstractSpellPart part = it.next();
				if (part instanceof SpellComponent){
					Set<Affinity> aff = ((SpellComponent)part).getAffinity();
					for (Affinity affinity : aff){
						int qty = 1;
						if (affinityData.containsKey(affinity)){
							qty = 1 + affinityData.get(affinity);
						}
						affinityData.put(affinity, qty);
					}
					cpCount++;
				}
			}
			ValueComparator vc = new ValueComparator(affinityData);
			TreeMap<Affinity, Integer> sorted = new TreeMap<Affinity, Integer>(vc);
			sorted.putAll(affinityData);
			for (Affinity aff : sorted.keySet()){
				float pct = (float)sorted.get(aff) / (float)cpCount * 100f;
				sb.append(String.format("%s: %.2f%%", aff.getUnlocalisedName(), pct));
				sb.append("\n");
			}
			pages.addAll(Story.splitStoryPartIntoPages(sb.toString()));
			Story.WritePartToNBT(bookstack.getTagCompound(), pages);
	
			bookstack = Story.finalizeStory(bookstack, title, player.getName());

			ItemStack[] recipeData = new ItemStack[componentRecipeList.size()];
			int idx = 0;
			for (ItemStack stack : componentRecipeList){
				recipeData[idx++] = stack;
			}

			NBTUtils.setItemStackArray(bookstack.getTagCompound(), "spell_combo", recipeData);
			bookstack.getTagCompound().setIntArray("output_combo", outputData);
			bookstack.getTagCompound().setInteger("numShapeGroups", shapeGroupCombos.length);
			int index = 0;
			for (int[] sgArray : shapeGroupCombos){
				bookstack.getTagCompound().setIntArray("shapeGroupCombo_" + index++, sgArray);
			}
			bookstack.getTagCompound().setString("spell_mod_version", ArsMagica2.instance.getVersion());

			if (this.currentSpellName.equals(""))
				this.currentSpellName = "Spell Recipe";
			bookstack.setStackDisplayName(this.currentSpellName);

			this.currentRecipe.clear();
			for (ArrayList<AbstractSpellPart> list : this.shapeGroups)
				list.clear();
			this.currentSpellName = "";

			bookstack.getTagCompound().setBoolean("spellFinalized", true);

			//worldObj.playSound(getPos().getX(), getPos().getY(), getPos().getZ(), "arsmagica2:misc.inscriptiontable.takebook", 1.0f, 1.0f, true);
			this.markDirty();
			//worldObj.markAndNotifyBlock(pos, worldObj.getChunkFromBlockCoords(pos), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 2);
		}
		return bookstack;
	}

	private int[] SpellPartListToStringBuilder(Iterator<AbstractSpellPart> it, StringBuilder sb, String prefix){
		ArrayList<Integer> outputCombo = new ArrayList<Integer>();
		while (it.hasNext()){
			AbstractSpellPart part = it.next();
			String displayName = SpellRegistry.getSkillFromPart(part).getName();

			if (prefix != null){
				sb.append(prefix + displayName + "\n");
			}else{
				if (part instanceof SpellShape){
					sb.append(displayName + "\n");
				}else{
					sb.append("-" + displayName + "\n");
				}
			}

			outputCombo.add(ArsMagicaAPI.getSkillRegistry().getId(part.getRegistryName()));
		}

		int[] outputData = new int[outputCombo.size()];
		int idx = 0;
		for (Integer I : outputCombo){
			outputData[idx++] = I;
		}

		return outputData;
	}

	public void clearCurrentRecipe(){
		this.currentRecipe.clear();
		for (ArrayList<AbstractSpellPart> group : this.shapeGroups)
			group.clear();
		this.currentSpellName = "";
		this.currentSpellIsReadOnly = false;
	}

	public SpellValidator.ValidationResult currentRecipeIsValid(){
		ArrayList<ArrayList<AbstractSpellPart>> segmented = SpellValidator.splitToStages(this.currentRecipe);
		return SpellValidator.instance.spellDefIsValid(this.shapeGroups, segmented);
	}

	public boolean modifierCanBeAdded(SpellModifier modifier){
		return false;
	}


	@Override
	public void markDirty() {
		this.markForUpdate();
		super.markDirty();
	}

	@Override
	public void markForUpdate() {
		this.dirty = true;
	}

	@Override
	public boolean needsUpdate() {
		return this.dirty;
	}

	@Override
	public void clean() {
		this.dirty = false;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	static class ValueComparator implements Comparator<Affinity>{

		Map<Affinity, Integer> base;

		ValueComparator(Map<Affinity, Integer> base){
			this.base = base;
		}

		@Override
		public int compare(Affinity a, Affinity b){
			Integer x = this.base.get(a);
			Integer y = this.base.get(b);
			if (x.equals(y)){
				return a.compareTo(b);
			}
			return x.compareTo(y);
		}
	}

	public void setSpellName(String name){
		this.currentSpellName = name;
		this.sendDataToServer();
	}

	public String getSpellName(){
		return this.currentSpellName != null ? this.currentSpellName : "";
	}

	public void reverseEngineerSpell(ItemStack stack){
		this.currentRecipe.clear();
		for (ArrayList<AbstractSpellPart> group : this.shapeGroups){
			group.clear();
		}
		this.currentSpellName = "";
		ISpellCaster caster = stack.getCapability(SpellCaster.INSTANCE, null);
		if (caster != null) {
			this.currentSpellName = stack.getDisplayName();
			for (int i = 0; i < caster.getShapeGroupCount(); i++) {
				List<List<AbstractSpellPart>> shapeGroup = caster.getShapeGroups().get(i);
				for (List<AbstractSpellPart> stage : shapeGroup) {
					for (AbstractSpellPart part : stage) {
						this.shapeGroups.get(i).add(part);
					}
				}
			}
			for (List<AbstractSpellPart> stage : caster.getSpellCommon()) {
				for (AbstractSpellPart part : stage) {
					this.currentRecipe.add(part);
				}
			}
			this.currentSpellIsReadOnly = true;
		}
	}

	public boolean currentSpellDefIsReadOnly(){
		return this.currentSpellIsReadOnly;
	}

	public void resetSpellNameAndIcon(ItemStack stack, EntityPlayer player){
		if (this.worldObj.isRemote){
			AMDataWriter writer = new AMDataWriter();
			writer.add(this.getPos().getX());
			writer.add(this.getPos().getY());
			writer.add(this.getPos().getZ());
			writer.add(RESET_NAME);
			writer.add(player.getEntityId());
			AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.INSCRIPTION_TABLE_UPDATE, writer.generate());
		}
		stack.setItemDamage(0);
		stack.clearCustomName();
	}

	public int getShapeGroupSize(int groupIndex){
		if(groupIndex > this.shapeGroups.size() || groupIndex < 0)
			return 0;
		return this.shapeGroups.get(groupIndex).size();
	}

	public AbstractSpellPart getShapeGroupPartAt(int groupIndex, int index){

		return this.shapeGroups.get(groupIndex).get(index);
	}


	public void incrementUpgradeState(){
		this.numStageGroups++;
		if (!this.worldObj.isRemote){
			List<EntityPlayerMP> players = this.worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(this.pos).expand(256, 256, 256));
			for (EntityPlayerMP player : players){
				player.connection.sendPacket(this.getUpdatePacket());
			}
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(this.getName());
	}

	@Override
	public int getField(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}

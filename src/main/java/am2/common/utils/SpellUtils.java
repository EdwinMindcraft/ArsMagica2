package am2.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import am2.ArsMagica2;
import am2.api.SpellRegistry;
import am2.api.spell.AbstractSpellPart;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.armor.ArmorHelper;
import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.defs.ItemDefs;
import am2.common.defs.PotionEffectsDefs;
import am2.common.entity.EntityDarkMage;
import am2.common.entity.EntityLightMage;
import am2.common.extensions.EntityExtension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SpellUtils {
	
	public static final String TYPE_SHAPE = "Shape";
	public static final String TYPE_COMPONENT = "Component";
	public static final String TYPE_MODIFIER = "Modifier";
	public static final String TYPE = "Type";
	public static final String ID = "ID";
	public static final String SHAPE_GROUP = "ShapeGroup";
	public static final String STAGE = "Stage_";
	public static final String SPELL_DATA = "SpellData";
	
	public static float modifyDamage(EntityLivingBase caster, float damage){
		float factor = (float)(EntityExtension.For(caster).getCurrentLevel() < 20 ?
				0.5 + (0.5 * (EntityExtension.For(caster).getCurrentLevel() / 19)) :
				1.0 + (1.0 * (EntityExtension.For(caster).getCurrentLevel() - 20) / 79));
		return damage * factor;
	}
	
	public static boolean attackTargetSpecial(SpellData spellStack, Entity target, DamageSource damagesource, float magnitude){

		if (target.worldObj.isRemote)
			return true;

		EntityPlayer dmgSrcPlayer = null;

		if (damagesource.getEntity() != null){
			if (damagesource.getEntity() instanceof EntityLivingBase){
				EntityLivingBase source = (EntityLivingBase)damagesource.getEntity();
				if ((source instanceof EntityLightMage || source instanceof EntityDarkMage) && target.getClass() == EntityCreeper.class){
					return false;
				}else if (source instanceof EntityLightMage && target instanceof EntityLightMage){
					return false;
				}else if (source instanceof EntityDarkMage && target instanceof EntityDarkMage){
					return false;
				}else  if (source instanceof EntityPlayer && target instanceof EntityPlayer && !target.worldObj.isRemote && (!FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled() || ((EntityPlayer)target).capabilities.isCreativeMode)){
					return false;
				}

				if (source.isPotionActive(PotionEffectsDefs.FURY))
					magnitude += 4;
			}

			if (damagesource.getEntity() instanceof EntityPlayer){
				dmgSrcPlayer = (EntityPlayer)damagesource.getEntity();
				int armorSet = ArmorHelper.getFullArsMagicaArmorSet(dmgSrcPlayer);
				if (armorSet == ArsMagicaArmorMaterial.MAGE.getMaterialID()){
					magnitude *= 1.05f;
				}else if (armorSet == ArsMagicaArmorMaterial.BATTLEMAGE.getMaterialID()){
					magnitude *= 1.025f;
				}else if (armorSet == ArsMagicaArmorMaterial.ARCHMAGE.getMaterialID()){
					magnitude *= 1.1f;
				}

				ItemStack equipped = dmgSrcPlayer.getActiveItemStack();
				if (equipped != null && equipped.getItem() == ItemDefs.arcaneSpellbook){
					magnitude *= 1.1f;
				}
			}
		}

		if (target instanceof EntityLivingBase){
			if (EntityUtils.isSummon((EntityLivingBase)target) && damagesource.damageType.equals("magic")){
				magnitude *= 3.0f;
			}
		}

		magnitude *= ArsMagica2.config.getDamageMultiplier();

//		ItemStack oldItemStack = null;

		boolean success = false;
		if (target instanceof EntityDragon){
			success = ((EntityDragon)target).attackEntityFromPart(((EntityDragon)target).dragonPartBody, damagesource, magnitude);
		}else{
			success = target.attackEntityFrom(damagesource, magnitude);
		}

		if (dmgSrcPlayer != null){
			if (spellStack != null && target instanceof EntityLivingBase){
				if (!target.worldObj.isRemote &&
						((EntityLivingBase)target).getHealth() <= 0 &&
						spellStack.isModifierPresent(SpellModifiers.DISMEMBERING_LEVEL)){
					double chance = spellStack.getModifiedValue(0, SpellModifiers.DISMEMBERING_LEVEL, Operation.ADD, dmgSrcPlayer.worldObj, dmgSrcPlayer, target);
					if (dmgSrcPlayer.worldObj.rand.nextDouble() <= chance){
						dropHead(target, dmgSrcPlayer.worldObj);
					}
				}
			}
		}

		return success;
	}
	
	private static void dropHead(Entity target, World world){
		if (target.getClass() == EntitySkeleton.class){
			if (((EntitySkeleton)target).getSkeletonType() == SkeletonType.WITHER){
				dropHead_do(world, target.posX, target.posY, target.posZ, 1);
			}else{
				dropHead_do(world, target.posX, target.posY, target.posZ, 0);
			}
		}else if (target.getClass() == EntityZombie.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 2);
		}else if (target.getClass() == EntityCreeper.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 4);
		}else if (target instanceof EntityPlayer){
			dropHead_do(world, target.posX, target.posY, target.posZ, 3);
		}
	}
	
	private static void dropHead_do(World world, double x, double y, double z, int type){
		EntityItem item = new EntityItem(world);
		ItemStack stack = new ItemStack(Items.SKULL, 1, type);
		item.setEntityItemStack(stack);
		item.setPosition(x, y, z);
		world.spawnEntityInWorld(item);
	}
	
	public static NBTTagCompound encode(KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> toEncode) {
		NBTTagCompound group = new NBTTagCompound();
		group.setTag(SPELL_DATA, toEncode.value);
		int stage = 0;
		for (AbstractSpellPart part : toEncode.key) {
			NBTTagList stageTag = NBTUtils.addCompoundList(group, STAGE + stage);
			NBTTagCompound tmp = new NBTTagCompound();
			String id = SpellRegistry.getSkillFromPart(part).getID();
			tmp.setString(ID, id);
			String type = "";
			if (part instanceof SpellShape) type = TYPE_SHAPE;
			if (part instanceof SpellModifier) type = TYPE_MODIFIER;
			if (part instanceof SpellComponent) type = TYPE_COMPONENT;
			tmp.setString(TYPE, type);
			if (part instanceof SpellShape) {
				stage++;
			} else {
			}
			stageTag.appendTag(tmp);
		}
		group.setInteger("StageNum", stage);
		return group;
	}
	
	public static KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> decode(NBTTagCompound toDecode) {
		if (toDecode == null)
			return null;
		try {
			ArrayList<AbstractSpellPart> parts = new ArrayList<>();
			for (int j = 0; j < NBTUtils.getAM2Tag(toDecode).getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(toDecode), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tmp = stageTag.getCompoundTagAt(i);
					String type = tmp.getString(TYPE);
					if (type.equalsIgnoreCase(TYPE_COMPONENT)) {
						parts.add(SpellRegistry.getComponentFromName(tmp.getString(ID)));
					}
					if (type.equalsIgnoreCase(TYPE_MODIFIER)) {
						parts.add(SpellRegistry.getModifierFromName(tmp.getString(ID)));
					}
					if (type.equalsIgnoreCase(TYPE_SHAPE)) {
						parts.add(SpellRegistry.getShapeFromName(tmp.getString(ID)));
					}
				}
			}
			return new KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound>(parts, toDecode.getCompoundTag(SPELL_DATA));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static List<List<AbstractSpellPart>> transformParts(List<AbstractSpellPart> parts) {
		List<List<AbstractSpellPart>> stages = Lists.newArrayList();
		List<AbstractSpellPart> stage = null;
		for (AbstractSpellPart part : parts) {
			if (part instanceof SpellShape) {
				if (stage != null && !stage.isEmpty()) {
					stages.add(stage);
				}
				stage = new ArrayList<>();
			}
			stage.add(part);
		}
		stages.add(stage);
		return stages;
	}

//	public static boolean casterHasAllReagents(EntityLivingBase caster, ItemStack spellStack){
//		if (caster instanceof EntityPlayer) {
//			EntityPlayer player = (EntityPlayer) caster;
//			if (player.capabilities.isCreativeMode) return true;
//			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
//				if (part.reagents(caster) == null) continue;
//				for (ItemStack stack : part.reagents(caster)) {
//					if (stack != null) {
//						boolean foundMatch = false;
//						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
//							ItemStack is = player.inventory.getStackInSlot(i);
//							if (is == null) continue;
//							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
//								if (is.stackSize >= stack.stackSize) {
//									foundMatch = true;
//									break;
//								}
//							}
//						}
//						if (!foundMatch) return false;
//					}
//				}
//			}
//		}
//		return true;
//	}
//	
//	public static String getMissingReagents(EntityLivingBase caster, ItemStack spellStack) {
//		if (caster instanceof EntityPlayer) {
//			EntityPlayer player = (EntityPlayer) caster;
//			if (player.capabilities.isCreativeMode) return "";
//			StringBuilder string = new StringBuilder(I18n.format("am2.tooltip.missingReagents"));
//			boolean first = true;
//			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
//				if (part.reagents(caster) == null) continue;
//				for (ItemStack stack : part.reagents(caster)) {
//					if (stack != null) {
//						boolean foundMatch = false;
//						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
//							ItemStack is = player.inventory.getStackInSlot(i);
//							if (is == null) continue;
//							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
//								if (is.stackSize >= stack.stackSize) {
//									foundMatch = true;
//									break;
//								}
//							}
//						}
//						if (!foundMatch)  {
//							if (!first) string.append(", ");
//							string.append(stack.stackSize).append("x ").append(stack.getDisplayName());
//							first = false;
//						}
//					}
//				}
//			}
//			return string.toString();
//		}
//		return "";		
//	}
//	
//	public static void consumeReagents(EntityLivingBase caster, ItemStack spellStack) {
//		if (caster instanceof EntityPlayer) {
//			EntityPlayer player = (EntityPlayer) caster;
//			if (player.capabilities.isCreativeMode) return;
//			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
//				if (part.reagents(caster) == null) continue;
//				for (ItemStack stack : part.reagents(caster)) {
//					if (stack != null) {
//						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
//							ItemStack is = player.inventory.getStackInSlot(i);
//							if (is == null) continue;
//							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
//								if (is.stackSize >= stack.stackSize) {
//									is.stackSize -= stack.stackSize;
//									if (is.stackSize <= 0) {
//										player.inventory.setInventorySlotContents(i, null);
//									} else {
//										player.inventory.setInventorySlotContents(i, is);
//									}
//									break;
//								}
//							}
//						}
//					}
//				}
//			}
//		}	
//	}
}
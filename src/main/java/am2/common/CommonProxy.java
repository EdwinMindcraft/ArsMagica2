package am2.common;

import static am2.common.defs.IDDefs.GUI_ARCANE_DECONSTRUCTOR;
import static am2.common.defs.IDDefs.GUI_ARCANE_RECONSTRUCTOR;
import static am2.common.defs.IDDefs.GUI_ARMOR_INFUSION;
import static am2.common.defs.IDDefs.GUI_ASTRAL_BARRIER;
import static am2.common.defs.IDDefs.GUI_CALEFACTOR;
import static am2.common.defs.IDDefs.GUI_CRYSTAL_MARKER;
import static am2.common.defs.IDDefs.GUI_ESSENCE_BAG;
import static am2.common.defs.IDDefs.GUI_ESSENCE_REFINER;
import static am2.common.defs.IDDefs.GUI_FLICKER_HABITAT;
import static am2.common.defs.IDDefs.GUI_INERT_SPAWNER;
import static am2.common.defs.IDDefs.GUI_INSCRIPTION_TABLE;
import static am2.common.defs.IDDefs.GUI_KEYSTONE;
import static am2.common.defs.IDDefs.GUI_KEYSTONE_CHEST;
import static am2.common.defs.IDDefs.GUI_KEYSTONE_LOCKABLE;
import static am2.common.defs.IDDefs.GUI_MAGICIANS_WORKBENCH;
import static am2.common.defs.IDDefs.GUI_OBELISK;
import static am2.common.defs.IDDefs.GUI_OCCULUS;
import static am2.common.defs.IDDefs.GUI_RIFT;
import static am2.common.defs.IDDefs.GUI_RUNE_BAG;
import static am2.common.defs.IDDefs.GUI_SEER_STONE;
import static am2.common.defs.IDDefs.GUI_SPELL_BOOK;
import static am2.common.defs.IDDefs.GUI_SPELL_CUSTOMIZATION;
import static am2.common.defs.IDDefs.GUI_SPELL_SEALED_DOOR;
import static am2.common.defs.IDDefs.GUI_SUMMONER;
import static am2.common.defs.IDDefs.GUI_SPELL_RECIPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import am2.common.defs.*;
import com.google.common.collect.ImmutableMap;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.blocks.IKeystoneLockable;
import am2.api.extensions.IAffinityData;
import am2.api.extensions.IArcaneCompendium;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.IRiftStorage;
import am2.api.extensions.ISkillData;
import am2.api.extensions.ISpellCaster;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.spell.AbstractSpellPart;
import am2.client.particles.ParticleManagerServer;
import am2.common.affinity.AffinityAbilityHelper;
import am2.common.armor.ArmorEventHandler;
import am2.common.armor.infusions.DamageReductionImbuement;
import am2.common.armor.infusions.Dispelling;
import am2.common.armor.infusions.FallProtection;
import am2.common.armor.infusions.FireProtection;
import am2.common.armor.infusions.Freedom;
import am2.common.armor.infusions.GenericImbuement;
import am2.common.armor.infusions.Healing;
import am2.common.armor.infusions.HungerBoost;
import am2.common.armor.infusions.ImbuementRegistry;
import am2.common.armor.infusions.JumpBoost;
import am2.common.armor.infusions.LifeSaving;
import am2.common.armor.infusions.Lightstep;
import am2.common.armor.infusions.MiningSpeed;
import am2.common.armor.infusions.Recoil;
import am2.common.armor.infusions.SwimSpeed;
import am2.common.armor.infusions.WaterBreathing;
import am2.common.armor.infusions.WaterWalking;
import am2.common.blocks.BlockArsMagicaBlock.EnumBlockType;
import am2.common.blocks.BlockArsMagicaOre.EnumOreType;
import am2.common.blocks.tileentity.TileEntityArcaneDeconstructor;
import am2.common.blocks.tileentity.TileEntityArcaneReconstructor;
import am2.common.blocks.tileentity.TileEntityArmorImbuer;
import am2.common.blocks.tileentity.TileEntityAstralBarrier;
import am2.common.blocks.tileentity.TileEntityBlackAurem;
import am2.common.blocks.tileentity.TileEntityBrokenPowerLink;
import am2.common.blocks.tileentity.TileEntityCalefactor;
import am2.common.blocks.tileentity.TileEntityCelestialPrism;
import am2.common.blocks.tileentity.TileEntityCraftingAltar;
import am2.common.blocks.tileentity.TileEntityCrystalMarker;
import am2.common.blocks.tileentity.TileEntityCrystalMarkerSpellExport;
import am2.common.blocks.tileentity.TileEntityEssenceConduit;
import am2.common.blocks.tileentity.TileEntityEssenceRefiner;
import am2.common.blocks.tileentity.TileEntityEverstone;
import am2.common.blocks.tileentity.TileEntityFlickerHabitat;
import am2.common.blocks.tileentity.TileEntityFlickerLure;
import am2.common.blocks.tileentity.TileEntityGroundRuneSpell;
import am2.common.blocks.tileentity.TileEntityIllusionBlock;
import am2.common.blocks.tileentity.TileEntityInertSpawner;
import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import am2.common.blocks.tileentity.TileEntityKeystoneChest;
import am2.common.blocks.tileentity.TileEntityKeystoneDoor;
import am2.common.blocks.tileentity.TileEntityKeystoneRecepticle;
import am2.common.blocks.tileentity.TileEntityLectern;
import am2.common.blocks.tileentity.TileEntityMagiciansWorkbench;
import am2.common.blocks.tileentity.TileEntityManaBattery;
import am2.common.blocks.tileentity.TileEntityManaDrain;
import am2.common.blocks.tileentity.TileEntityObelisk;
import am2.common.blocks.tileentity.TileEntityOcculus;
import am2.common.blocks.tileentity.TileEntityOtherworldAura;
import am2.common.blocks.tileentity.TileEntityParticleEmitter;
import am2.common.blocks.tileentity.TileEntitySeerStone;
import am2.common.blocks.tileentity.TileEntitySlipstreamGenerator;
import am2.common.blocks.tileentity.TileEntitySpellSealedDoor;
import am2.common.blocks.tileentity.TileEntitySummoner;
import am2.common.blocks.tileentity.flickers.FlickerOperatorButchery;
import am2.common.blocks.tileentity.flickers.FlickerOperatorContainment;
import am2.common.blocks.tileentity.flickers.FlickerOperatorFelledOak;
import am2.common.blocks.tileentity.flickers.FlickerOperatorFishing;
import am2.common.blocks.tileentity.flickers.FlickerOperatorFlatLands;
import am2.common.blocks.tileentity.flickers.FlickerOperatorGentleRains;
import am2.common.blocks.tileentity.flickers.FlickerOperatorInterdiction;
import am2.common.blocks.tileentity.flickers.FlickerOperatorItemTransport;
import am2.common.blocks.tileentity.flickers.FlickerOperatorLight;
import am2.common.blocks.tileentity.flickers.FlickerOperatorMoonstoneAttractor;
import am2.common.blocks.tileentity.flickers.FlickerOperatorNaturesBounty;
import am2.common.blocks.tileentity.flickers.FlickerOperatorPackedEarth;
import am2.common.blocks.tileentity.flickers.FlickerOperatorProgeny;
import am2.common.container.ContainerArcaneDeconstructor;
import am2.common.container.ContainerArcaneReconstructor;
import am2.common.container.ContainerArmorInfuser;
import am2.common.container.ContainerAstralBarrier;
import am2.common.container.ContainerCalefactor;
import am2.common.container.ContainerCrystalMarker;
import am2.common.container.ContainerEssenceBag;
import am2.common.container.ContainerEssenceRefiner;
import am2.common.container.ContainerFlickerHabitat;
import am2.common.container.ContainerInertSpawner;
import am2.common.container.ContainerInscriptionTable;
import am2.common.container.ContainerKeystone;
import am2.common.container.ContainerKeystoneLockable;
import am2.common.container.ContainerMagiciansWorkbench;
import am2.common.container.ContainerObelisk;
import am2.common.container.ContainerRiftStorage;
import am2.common.container.ContainerRuneBag;
import am2.common.container.ContainerSeerStone;
import am2.common.container.ContainerSpellBook;
import am2.common.container.ContainerSpellCustomization;
import am2.common.container.ContainerSpellSealedDoor;
import am2.common.container.ContainerSummoner;
import am2.common.enchantments.AMEnchantments;
import am2.common.extensions.RiftStorage;
import am2.common.handler.EntityHandler;
import am2.common.handler.EventManager;
import am2.common.handler.FlickerEvents;
import am2.common.handler.FuelHandler;
import am2.common.handler.PotionEffectHandler;
import am2.common.handler.ServerTickHandler;
import am2.common.handler.ShrinkHandler;
import am2.common.items.ItemEssenceBag;
import am2.common.items.ItemKeystone;
import am2.common.items.ItemOre;
import am2.common.items.ItemRuneBag;
import am2.common.items.ItemSpellBook;
import am2.common.lore.CompendiumUnlockHandler;
import am2.common.network.PacketHandler;
import am2.common.network.SeventhSanctum;
import am2.common.packet.AMNetHandler;
import am2.common.packet.AMPacketProcessorServer;
import am2.common.power.PowerNodeCache;
import am2.common.power.PowerNodeEntry;
import am2.common.power.PowerTypes;
import am2.common.spell.SpellUnlockManager;
import am2.common.trackers.ItemFrameWatcher;
import am2.common.trackers.PlayerTracker;
import am2.common.utils.InventoryUtilities;
import am2.common.utils.NPCSpells;
import am2.common.world.AM2WorldDecorator;
import am2.common.world.BiomeWitchwoodForest;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;


public class CommonProxy implements IGuiHandler{

	public ParticleManagerServer particleManager;
	protected ServerTickHandler serverTickHandler;
	protected AMPacketProcessorServer packetProcessor;
	public PacketHandler packetHandler;
	private HashMap<EntityLivingBase, ArrayList<PotionEffect>> deferredPotionEffects = new HashMap<>();
	private HashMap<EntityLivingBase, Integer> deferredDimensionTransfers = new HashMap<>();
	public ItemDefs items;
	public BlockDefs blocks;
	public AMEnchantments enchantments;
	private int totalFlickerCount = 0;
	
	public PlayerTracker playerTracker;
	public ItemFrameWatcher itemFrameWatcher;
	private AM2WorldDecorator worldGen;
	public NBTTagCompound cwCopyLoc = null;
	
	public static HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> powerPathVisuals;
	
	public CommonProxy() {
		playerTracker = new PlayerTracker();
		itemFrameWatcher = new ItemFrameWatcher();
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		switch (ID) {
		case GUI_OCCULUS: return null;
		case GUI_SPELL_CUSTOMIZATION: return new ContainerSpellCustomization(player);
		case GUI_RIFT: return new ContainerRiftStorage(player, RiftStorage.For(player));
		case GUI_SPELL_BOOK: 
			ItemStack bookStack = player.getHeldItemMainhand();
			if (bookStack.getItem() == null || !(bookStack.getItem() instanceof ItemSpellBook)){
				return null;
			}
			ItemSpellBook item = (ItemSpellBook)bookStack.getItem();
			return new ContainerSpellBook(player.inventory, bookStack, item.ConvertToInventory(bookStack));
		case GUI_OBELISK: return new ContainerObelisk((TileEntityObelisk)world.getTileEntity(new BlockPos(x, y, z)), player);
		case GUI_CRYSTAL_MARKER: return new ContainerCrystalMarker(player, (TileEntityCrystalMarker)te);
		case GUI_INSCRIPTION_TABLE: return new ContainerInscriptionTable((TileEntityInscriptionTable)world.getTileEntity(new BlockPos(x, y, z)), player.inventory);
		case GUI_ARMOR_INFUSION: return new ContainerArmorInfuser(player, (TileEntityArmorImbuer) world.getTileEntity(new BlockPos(x, y, z)));
		case GUI_KEYSTONE:
			ItemStack keystoneStack = player.getHeldItemMainhand();
			if (keystoneStack.getItem() == null || !(keystoneStack.getItem() instanceof ItemKeystone)){
				return null;
			}
			ItemKeystone keystone = (ItemKeystone)keystoneStack.getItem();

			int runeBagSlot = InventoryUtilities.getInventorySlotIndexFor(player.inventory, ItemDefs.runeBag);
			ItemStack runeBag = null;
			if (runeBagSlot > -1)
				runeBag = player.inventory.getStackInSlot(runeBagSlot);

			return new ContainerKeystone(player.inventory, player.getHeldItemMainhand(), runeBag, keystone.ConvertToInventory(keystoneStack), runeBag == null ? null : ItemDefs.runeBag.ConvertToInventory(runeBag), runeBagSlot);
		case GUI_KEYSTONE_LOCKABLE: return new ContainerKeystoneLockable(player.inventory, (IKeystoneLockable<?>)te);		
		case GUI_SPELL_SEALED_DOOR: return new ContainerSpellSealedDoor(player.inventory, (TileEntitySpellSealedDoor)te);
		case GUI_KEYSTONE_CHEST: return ((TileEntityKeystoneChest)te).createContainer(player.inventory, player);
		case GUI_RUNE_BAG: 
			ItemStack bagStack = player.getHeldItemMainhand();
			if (bagStack.getItem() == null || !(bagStack.getItem() instanceof ItemRuneBag)){
				return null;
			}
			ItemRuneBag runebag = (ItemRuneBag)bagStack.getItem();
			return new ContainerRuneBag(player.inventory, player.getHeldItemMainhand(), runebag.ConvertToInventory(bagStack));
		case GUI_FLICKER_HABITAT: return new ContainerFlickerHabitat(player, (TileEntityFlickerHabitat) te);
		case GUI_ARCANE_DECONSTRUCTOR: return new ContainerArcaneDeconstructor(player.inventory, (TileEntityArcaneDeconstructor) te);
		case GUI_ARCANE_RECONSTRUCTOR: return new ContainerArcaneReconstructor(player.inventory, (TileEntityArcaneReconstructor) te);
		case GUI_ASTRAL_BARRIER: return new ContainerAstralBarrier(player.inventory, (TileEntityAstralBarrier) te);
		case GUI_ESSENCE_REFINER: return new ContainerEssenceRefiner(player.inventory, (TileEntityEssenceRefiner) te);
		case GUI_MAGICIANS_WORKBENCH: return new ContainerMagiciansWorkbench(player.inventory, (TileEntityMagiciansWorkbench) te);
		case GUI_CALEFACTOR: return new ContainerCalefactor(player, (TileEntityCalefactor) te);
		case GUI_SEER_STONE: return new ContainerSeerStone(player.inventory, (TileEntitySeerStone) te);
		case GUI_INERT_SPAWNER: return new ContainerInertSpawner(player, (TileEntityInertSpawner) te);
		case GUI_SUMMONER: return new ContainerSummoner(player.inventory, (TileEntitySummoner) te);
		case GUI_ESSENCE_BAG: 
			bagStack = player.getHeldItemMainhand();
			if (bagStack.getItem() == null || !(bagStack.getItem() instanceof ItemEssenceBag)){
				return null;
			}
			ItemEssenceBag essenceBag = (ItemEssenceBag)bagStack.getItem();
			return new ContainerEssenceBag(player.inventory, player.getHeldItemMainhand(), essenceBag.ConvertToInventory(bagStack));
		case GUI_SPELL_RECIPE: return null;
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	public void preInit() {
		
		ForgeChunkManager.setForcedChunkLoadingCallback(ArsMagica2.instance, AMChunkLoader.INSTANCE);
		NetworkRegistry.INSTANCE.registerGuiHandler(ArsMagica2.instance, this);
		SeventhSanctum.instance.init();
		
		initHandlers();
		ArsMagica2.config.init();
		serverTickHandler = new ServerTickHandler();
		packetHandler = new PacketHandler();
		PacketHandler.registerMessages();
		enchantments = new AMEnchantments();
		AMNetHandler.INSTANCE.init();
		AMNetHandler.INSTANCE.registerChannels(packetProcessor);
		
		MinecraftForge.EVENT_BUS.register(serverTickHandler);
		MinecraftForge.EVENT_BUS.register(new CompendiumUnlockHandler());
		MinecraftForge.EVENT_BUS.register(new EntityHandler());
		MinecraftForge.EVENT_BUS.register(new PotionEffectHandler());
		MinecraftForge.EVENT_BUS.register(new AffinityAbilityHelper());
		MinecraftForge.EVENT_BUS.register(packetProcessor);
		MinecraftForge.EVENT_BUS.register(PowerNodeCache.instance);
		MinecraftForge.EVENT_BUS.register(new ArmorEventHandler());
		MinecraftForge.EVENT_BUS.register(playerTracker);
		MinecraftForge.EVENT_BUS.register(new FlickerEvents());
		MinecraftForge.EVENT_BUS.register(new ShrinkHandler());
		MinecraftForge.EVENT_BUS.register(new EventManager());
		
		registerInfusions();
		registerFlickerOperators();
				
		GameRegistry.registerTileEntity(TileEntityOcculus.class, "TileEntityOcculus");
		GameRegistry.registerTileEntity(TileEntityCraftingAltar.class, "TileEntityCraftingAltar");
		GameRegistry.registerTileEntity(TileEntityLectern.class, "TileEntityLectern");
		GameRegistry.registerTileEntity(TileEntityObelisk.class, "TileEntityObelisk");
		GameRegistry.registerTileEntity(TileEntityCelestialPrism.class, "TileEntityCelestialPrism");
		GameRegistry.registerTileEntity(TileEntityBlackAurem.class, "TileEntityBlackAurem");
		GameRegistry.registerTileEntity(TileEntityCrystalMarker.class, "TileEntityCrystalMarker");
		GameRegistry.registerTileEntity(TileEntityCrystalMarkerSpellExport.class, "TileEntityCrystalMarkerSpellExport");
		GameRegistry.registerTileEntity(TileEntityFlickerHabitat.class, "TileEntityFlickerHabitat");
		GameRegistry.registerTileEntity(TileEntityInscriptionTable.class, "TileEntityInscriptionTable");
		GameRegistry.registerTileEntity(TileEntityManaBattery.class, "TileEntityManaBattery");
		GameRegistry.registerTileEntity(TileEntityArmorImbuer.class, "TileEntityArmorImbuer");
		GameRegistry.registerTileEntity(TileEntitySlipstreamGenerator.class, "TileEntitySlipstramGenerator");
		GameRegistry.registerTileEntity(TileEntityKeystoneRecepticle.class, "TileEntityKeystoneRecepticle");
		GameRegistry.registerTileEntity(TileEntityKeystoneDoor.class, "TileEntityKeystoneDoor");
		GameRegistry.registerTileEntity(TileEntityKeystoneChest.class, "TileEntityKeystoneChest");
		GameRegistry.registerTileEntity(TileEntitySpellSealedDoor.class, "TileEntitySpellSealedDoor");
		GameRegistry.registerTileEntity(TileEntityFlickerLure.class, "TileEntityFlickerLure");
		GameRegistry.registerTileEntity(TileEntityArcaneDeconstructor.class, "TileEntityArcaneDeconstructor");
		GameRegistry.registerTileEntity(TileEntityArcaneReconstructor.class, "TileEntityArcaneReconstructor");
		GameRegistry.registerTileEntity(TileEntityAstralBarrier.class, "TileEntityAstralBarrier");
		GameRegistry.registerTileEntity(TileEntityEssenceRefiner.class, "TileEntityEssenceRefiner");
		GameRegistry.registerTileEntity(TileEntityEverstone.class, "TileEntityEverstone");
		GameRegistry.registerTileEntity(TileEntityGroundRuneSpell.class, "TileEntityGroundRuneSpell");
		GameRegistry.registerTileEntity(TileEntityEssenceConduit.class, "TileEntityEssenceConduit");
		GameRegistry.registerTileEntity(TileEntityIllusionBlock.class, "TileEntityIllusionBlock");
		GameRegistry.registerTileEntity(TileEntityCalefactor.class, "TileEntityCalefactor");
		GameRegistry.registerTileEntity(TileEntityBrokenPowerLink.class, "TileEntityBrokenPowerLink");
		GameRegistry.registerTileEntity(TileEntityInertSpawner.class, "TileEntityInertSpawner");
		GameRegistry.registerTileEntity(TileEntityMagiciansWorkbench.class, "TileEntityMagiciansWorkbench");
		GameRegistry.registerTileEntity(TileEntityOtherworldAura.class, "TileEntityOtherworldAura");
		GameRegistry.registerTileEntity(TileEntityParticleEmitter.class, "TileEntityParticleEmitter");
		GameRegistry.registerTileEntity(TileEntitySeerStone.class, "TileEntitySeerStone");
		GameRegistry.registerTileEntity(TileEntitySummoner.class, "TileEntitySummoner");
		GameRegistry.registerTileEntity(TileEntityManaDrain.class, "TileEntityManaDrain");

		worldGen = new AM2WorldDecorator();
		SoundDefs.registerSounds();
		GameRegistry.registerWorldGenerator(worldGen, 0);
		GameRegistry.registerFuelHandler(new FuelHandler());
		EntityManager.instance.registerEntities();
		EntityManager.instance.initializeSpawns();
		AMEnchantments.Init();
		SkillDefs.init();
		SpellDefs.init();
		PotionEffectsDefs.init();
		NPCSpells.instance.toString();
		items = new ItemDefs();
		ItemDefs.initEnchantedItems();
		blocks = new BlockDefs();
		blocks.preInit();
		new CreativeTabsDefs();
		initOreDict();
		new LootTablesArsMagica();

		CapabilityManager.INSTANCE.register(IEntityExtension.class, new IEntityExtension.Storage(), new IEntityExtension.Factory());
		CapabilityManager.INSTANCE.register(IAffinityData.class, new IAffinityData.Storage(), new IAffinityData.Factory());
		CapabilityManager.INSTANCE.register(ISkillData.class, new ISkillData.Storage(), new ISkillData.Factory());
		CapabilityManager.INSTANCE.register(IRiftStorage.class, new IRiftStorage.Storage(), new IRiftStorage.Factory());
		CapabilityManager.INSTANCE.register(IArcaneCompendium.class, new IArcaneCompendium.Storage(), new IArcaneCompendium.Factory());
		CapabilityManager.INSTANCE.register(ISpellCaster.class, new ISpellCaster.Storage(), () -> null);
	}
	
	public void init() {
		if (ArsMagica2.config.getEnableWitchwoodForest()){
			BiomeDictionary.registerBiomeType(BiomeWitchwoodForest.instance, Type.FOREST, Type.MAGICAL);
			BiomeManager.addBiome(BiomeType.COOL, new BiomeEntry(BiomeWitchwoodForest.instance, 6));
			int id = (BiomeWitchwoodForest.getBiomeId() != -1) ? BiomeWitchwoodForest.getBiomeId() : BiomeWitchwoodForest.getNextFreeBiomeId();
			Biome.registerBiome(id, BiomeWitchwoodForest.instance.getBiomeName(), BiomeWitchwoodForest.instance);
		}
	}
	
	public void postInit() {
		playerTracker.postInit();
		MinecraftForge.EVENT_BUS.register(playerTracker);
		MinecraftForge.EVENT_BUS.register(new SpellUnlockManager());
		SoundDefs.createSoundMaps();
		LoreDefs.postInit();	
		AMRecipes.addRecipes();
		for (AbstractSpellPart part : ArsMagicaAPI.getSpellRegistry().getValues()) {
			if (ArsMagicaAPI.getSkillRegistry().getValue(part.getRegistryName()) == null)
				throw new IllegalStateException("Spell Part " + part.getRegistryName() + " is missing a skill, this would cause severe problems");
		}
		ArsMagica2.disabledSkills.getDisabledSkills(true);
	}
	
	public void initHandlers() {
		particleManager = new ParticleManagerServer();
		packetProcessor = new AMPacketProcessorServer();
	}
	
	public void addDeferredTargetSet(EntityLiving ent, EntityLivingBase target){
		serverTickHandler.addDeferredTarget(ent, target);
	}
	
	public ImmutableMap<EntityLivingBase, ArrayList<PotionEffect>> getDeferredPotionEffects(){
		return ImmutableMap.copyOf(deferredPotionEffects);
	}
	
	public void clearDeferredPotionEffects(){
		deferredPotionEffects.clear();
	}
	
	public void clearDeferredDimensionTransfers(){
		deferredDimensionTransfers.clear();
	}

	public ImmutableMap<EntityLivingBase, Integer> getDeferredDimensionTransfers(){
		return ImmutableMap.copyOf(deferredDimensionTransfers);
	}

	public void renderGameOverlay() {}
	
	public void addDeferredDimensionTransfer(EntityLivingBase ent, int dimension){
		deferredDimensionTransfers.put(ent, dimension);
	}

	public boolean setMouseDWheel(int dwheel) {
		return false;
	}
	
	public void setTrackedPowerCompound(NBTTagCompound compound){
	}

	public void setTrackedLocation(AMVector3 location){
	}

	public boolean hasTrackedLocationSynced(){
		return false;
	}

	public PowerNodeEntry getTrackedData(){
		return null;
	}

	public void drawPowerOnBlockHighlight(EntityPlayer player, RayTraceResult target, float partialTicks) {}

	public void receivePowerPathVisuals(HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> nodePaths) {}

	public void requestPowerPathVisuals(IPowerNode<?> node, EntityPlayerMP player) {}

	public HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> getPowerPathVisuals() {
		return null;
	}

	public void blackoutArmorPiece(EntityPlayerMP player, EntityEquipmentSlot slot, int cooldown){
		serverTickHandler.blackoutArmorPiece(player, slot, cooldown);
	}
	
	public void registerInfusions(){
		DamageReductionImbuement.registerAll();
		GenericImbuement.registerAll();
		ImbuementRegistry.instance.registerImbuement(new Dispelling());
		ImbuementRegistry.instance.registerImbuement(new FallProtection());
		ImbuementRegistry.instance.registerImbuement(new FireProtection());
		ImbuementRegistry.instance.registerImbuement(new Freedom());
		ImbuementRegistry.instance.registerImbuement(new Healing());
		ImbuementRegistry.instance.registerImbuement(new HungerBoost());
		ImbuementRegistry.instance.registerImbuement(new JumpBoost());
		ImbuementRegistry.instance.registerImbuement(new LifeSaving());
		ImbuementRegistry.instance.registerImbuement(new Lightstep());
		ImbuementRegistry.instance.registerImbuement(new MiningSpeed());
		ImbuementRegistry.instance.registerImbuement(new Recoil());
		ImbuementRegistry.instance.registerImbuement(new SwimSpeed());
		ImbuementRegistry.instance.registerImbuement(new WaterBreathing());
		ImbuementRegistry.instance.registerImbuement(new WaterWalking());
	}

	public void flashManaBar() {}
	
	public void incrementFlickerCount(){
		this.totalFlickerCount++;
	}

	public void decrementFlickerCount(){
		this.totalFlickerCount--;
		if (this.totalFlickerCount < 0)
			this.totalFlickerCount = 0;
	}

	public int getTotalFlickerCount(){
		return this.totalFlickerCount;
	}
	
	private void registerFlickerOperators(){
		GameRegistry.register(FlickerOperatorItemTransport.instance, new ResourceLocation("arsmagica2", "item_transport"));
		GameRegistry.register(FlickerOperatorButchery.instance, new ResourceLocation("arsmagica2", "butchery"));
		GameRegistry.register(FlickerOperatorContainment.instance, new ResourceLocation("arsmagica2", "containment"));
		GameRegistry.register(FlickerOperatorFelledOak.instance, new ResourceLocation("arsmagica2", "felled_oak"));
		GameRegistry.register(FlickerOperatorFlatLands.instance, new ResourceLocation("arsmagica2", "flat_lands"));
		GameRegistry.register(FlickerOperatorGentleRains.instance, new ResourceLocation("arsmagica2", "gentle_rains"));
		GameRegistry.register(FlickerOperatorInterdiction.instance, new ResourceLocation("arsmagica2", "interdiction"));
		GameRegistry.register(FlickerOperatorLight.instance, new ResourceLocation("arsmagica2", "light"));
		GameRegistry.register(FlickerOperatorMoonstoneAttractor.instance, new ResourceLocation("arsmagica2", "moonstone_attractor"));
		GameRegistry.register(FlickerOperatorNaturesBounty.instance, new ResourceLocation("arsmagica2", "natures_bounty"));
		GameRegistry.register(FlickerOperatorPackedEarth.instance, new ResourceLocation("arsmagica2", "packed_earth"));
		GameRegistry.register(FlickerOperatorProgeny.instance, new ResourceLocation("arsmagica2", "progeny"));
		GameRegistry.register(FlickerOperatorFishing.instance, new ResourceLocation("arsmagica2", "fishing"));
	}
	
	public AM2WorldDecorator getWorldGenerator() {
		return worldGen;
	}

	public EntityPlayer getLocalPlayer() {
		return null;
	}

	public void openParticleBlockGUI(World worldIn, EntityPlayer playerIn, TileEntityParticleEmitter te) {
	}
	
	private void initOreDict() {
		OreDictionary.registerOre("fenceWood", Blocks.ACACIA_FENCE);
		OreDictionary.registerOre("fenceWood", Blocks.OAK_FENCE);
		OreDictionary.registerOre("fenceWood", Blocks.DARK_OAK_FENCE);
		OreDictionary.registerOre("fenceWood", Blocks.SPRUCE_FENCE);
		OreDictionary.registerOre("fenceWood", Blocks.BIRCH_FENCE);
		OreDictionary.registerOre("fenceWood", Blocks.JUNGLE_FENCE);
		OreDictionary.registerOre("oreBlueTopaz", new ItemStack(BlockDefs.ores, 1, EnumOreType.BLUETOPAZ.ordinal()));
		OreDictionary.registerOre("oreVinteum", new ItemStack(BlockDefs.ores, 1, EnumOreType.VINTEUM.ordinal()));
		OreDictionary.registerOre("oreChimerite", new ItemStack(BlockDefs.ores, 1, EnumOreType.CHIMERITE.ordinal()));
		OreDictionary.registerOre("oreMoonstone", new ItemStack(BlockDefs.ores, 1, EnumOreType.MOONSTONE.ordinal()));
		OreDictionary.registerOre("oreSunstone", new ItemStack(BlockDefs.ores, 1, EnumOreType.SUNSTONE.ordinal()));

		OreDictionary.registerOre("blockBlueTopaz", new ItemStack(BlockDefs.blocks, 1, EnumBlockType.BLUETOPAZ.ordinal()));
		OreDictionary.registerOre("blockVinteum", new ItemStack(BlockDefs.blocks, 1, EnumBlockType.VINTEUM.ordinal()));
		OreDictionary.registerOre("blockChimerite", new ItemStack(BlockDefs.blocks, 1, EnumBlockType.CHIMERITE.ordinal()));
		OreDictionary.registerOre("blockMoonstone", new ItemStack(BlockDefs.blocks, 1, EnumBlockType.MOONSTONE.ordinal()));
		OreDictionary.registerOre("blockSunstone", new ItemStack(BlockDefs.blocks, 1, EnumBlockType.SUNSTONE.ordinal()));

		OreDictionary.registerOre("chestWood", new ItemStack(Blocks.CHEST));
		OreDictionary.registerOre("craftingTableWood", new ItemStack(Blocks.CRAFTING_TABLE));
		
		OreDictionary.registerOre("dustVinteum", new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_VINTEUM));
		OreDictionary.registerOre("arcaneAsh", new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_ARCANEASH));
		OreDictionary.registerOre("gemBlueTopaz", new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_BLUE_TOPAZ));
		OreDictionary.registerOre("gemChimerite", new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_CHIMERITE));
		OreDictionary.registerOre("gemMoonstone", new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_MOONSTONE));
		OreDictionary.registerOre("gemSunstone", new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_SUNSTONE));

	}
}

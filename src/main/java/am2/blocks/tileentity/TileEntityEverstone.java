package am2.blocks.tileentity;


import java.util.ArrayList;
import java.util.List;

import am2.ArsMagica2;
import am2.blocks.BlockEverstone;
import am2.defs.BlockDefs;
import am2.particles.AMParticle;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityEverstone extends TileEntity implements ITickable{

	private int reconstructTimer = 0;
	private IBlockState facade = null;
	private static final int reconstructMax = ArsMagica2.config.getEverstoneRepairRate();

	private boolean poweredFromEverstone = false;
	private boolean poweredFromRedstone = false;

	public void setFacade(IBlockState facade){
		this.facade = facade;
	
		if (!worldObj.isRemote){
			List<EntityPlayerMP> players = worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(pos).expand(64, 64, 64));
			for (EntityPlayerMP player : players){
				player.connection.sendPacket(getUpdatePacket());
			}
		}
		worldObj.markAndNotifyBlock(pos, worldObj.getChunkFromBlockCoords(pos), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
	}

	private void propagatePoweredByEverstone(boolean powered, ArrayList<BlockPos> completedUpdates){
		BlockPos myPosition = pos;
		if (completedUpdates.contains(myPosition)){
			return;
		}

		completedUpdates.add(myPosition);
		poweredFromEverstone = powered;
		onBreak();

		if (worldObj.isRemote){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(worldObj, "radiant", pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
			if (particle != null){
				particle.setMaxAge(20);
				particle.setDontRequireControllers();
				particle.setIgnoreMaxAge(false);
			}
		}

		for (int i = -1; i <= 1; i++){
			for (int j = -1; j <= 1; j++){
				for (int k = -1; k <= 1; k++){
					BlockPos targetPosition = pos.add(i, j, k);
					Block blockID = worldObj.getBlockState(targetPosition).getBlock();
					if (blockID == BlockDefs.everstone && !completedUpdates.contains(targetPosition)){
						TileEntityEverstone everstone = ((TileEntityEverstone)worldObj.getTileEntity(targetPosition));
						if (everstone != null)
							everstone.propagatePoweredByEverstone(powered, completedUpdates);
					}
				}
			}
		}
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}
	
	@Override
	public void update(){
		if (reconstructTimer <= 0 && worldObj.isBlockIndirectlyGettingPowered(pos) > 0){
			propagatePoweredByEverstone(true, new ArrayList<BlockPos>());
			poweredFromRedstone = true;
		}else if (poweredFromRedstone && worldObj.isBlockIndirectlyGettingPowered(pos) == 0){
			poweredFromRedstone = false;
			propagatePoweredByEverstone(false, new ArrayList<BlockPos>());
		}

		worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockEverstone.HAS_FACADE, getFacade() != null).withProperty(BlockEverstone.IS_SOLID, isSolid()), 2);
		if (reconstructTimer <= 0)
			return;

		if (worldObj.isBlockIndirectlyGettingPowered(pos) == 0 && !poweredFromEverstone){
			reconstructTimer--;
			if (worldObj.isRemote){ //
				//worldObj.scheduleBlockUpdateWithPriority(xCoord, yCoord, zCoord, BlocksCommonProxy.everstone.blockID, 0, 0);
				if (reconstructTimer < reconstructMax - 20 && reconstructTimer > 20 && worldObj.rand.nextInt(10) < 8){
					//TODO ArsMagica2.proxy.addDigParticle(worldObj, pos, getFacade() == null ? BlockDefs.everstone : getFacade(), getFacadeMeta());
				}
			}
		}
		
	}

	public int getFadeStrength(){
		if (reconstructTimer > reconstructMax / 2)
			return 0;
		return (int)(128 * ((float)(reconstructMax - reconstructTimer) / reconstructMax));
	}

	public IBlockState getFacade(){
		return facade;
	}

	public boolean isSolid(){
		return reconstructTimer == 0;
	}

	public void onBreak(){
		reconstructTimer = reconstructMax;
		if (!worldObj.isRemote){
			List<EntityPlayerMP> players = worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(pos).expand(64, 64, 64));
			for (EntityPlayerMP player : players){
				player.connection.sendPacket(getUpdatePacket());
			}
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound){
		super.readFromNBT(par1nbtTagCompound);
		if (par1nbtTagCompound.hasKey("facade")){
			this.facade = Block.getStateById(par1nbtTagCompound.getInteger("facade"));
		}
		this.poweredFromEverstone = par1nbtTagCompound.getBoolean("poweredFromEverstone");
		this.poweredFromRedstone = par1nbtTagCompound.getBoolean("poweredFromRedstone");
		this.reconstructTimer = par1nbtTagCompound.getInteger("reconstructTimer");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound par1nbtTagCompound){
		super.writeToNBT(par1nbtTagCompound);
		if (facade != null){
			par1nbtTagCompound.setInteger("facade", Block.getStateId(facade));
		}
		par1nbtTagCompound.setBoolean("poweredFromEverstone", poweredFromEverstone);
		par1nbtTagCompound.setBoolean("poweredFromRedstone", poweredFromRedstone);
		par1nbtTagCompound.setInteger("reconstructTimer", reconstructTimer);
		return par1nbtTagCompound;
	}
}

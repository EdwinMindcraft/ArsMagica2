package am2.blocks.tileentity;

import java.util.List;

import com.google.common.collect.Lists;

import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

public class TileEntityManaBattery extends TileEntityAMPower{

	private boolean active;
	public static int storageCapacity = 250000;
	private PowerTypes outputPowerType = PowerTypes.NONE;
	private int tickCounter = 0;

	public TileEntityManaBattery(){
		super(250000);
		active = false;
	}

	public PowerTypes getPowerType(){
		return outputPowerType;
	}

	public void setPowerType(PowerTypes type, boolean forceSubNodes){
		this.outputPowerType = type;
		if (worldObj != null && worldObj.isRemote)
			worldObj.markAndNotifyBlock(pos, worldObj.getChunkFromBlockCoords(pos), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
	}

	public void setActive(boolean active){
		this.active = active;
	}

	@Override
	public boolean canProvidePower(PowerTypes type){
		return true;
	}

	@Override
	public void update(){
		if (worldObj.isBlockIndirectlyGettingPowered(pos) > 0){
			this.setPowerRequests();
		}else{
			this.setNoPowerRequests();
		}

		if (this.outputPowerType == PowerTypes.NONE && !this.worldObj.isRemote){
			PowerTypes highest = PowerNodeRegistry.For(worldObj).getHighestPowerType(this);
			float amt = PowerNodeRegistry.For(worldObj).getPower(this, highest);
			if (amt > 0){
				this.outputPowerType = highest;
			}
		}

		tickCounter++;
		if (tickCounter % 600 == 0){
			worldObj.notifyBlockOfStateChange(pos, getBlockType());
			tickCounter = 0;
		}

		super.update();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("isActive", active);
		nbttagcompound.setInteger("outputType", outputPowerType.ID());
		return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		active = nbttagcompound.getBoolean("isActive");
		if (nbttagcompound.hasKey("outputType"))
			outputPowerType = PowerTypes.getByID(nbttagcompound.getInteger("outputType"));
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, getBlockMetadata(), compound);
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public int getChargeRate(){
		return 1000;
	}

	@Override
	public List<PowerTypes> getValidPowerTypes(){
		if (this.outputPowerType == PowerTypes.NONE)
			return PowerTypes.all();
		return Lists.newArrayList(outputPowerType);
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}
}

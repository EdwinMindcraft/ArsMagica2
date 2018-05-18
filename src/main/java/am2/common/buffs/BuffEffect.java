package am2.common.buffs;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BuffEffect extends PotionEffect{
	protected boolean InitialApplication;
	protected boolean HasNotified;

	public BuffEffect(Potion buffID, int duration, int amplifier){
		super(buffID, duration, amplifier);
		InitialApplication = true;
		HasNotified = ((duration / 20) > 5) ? false : true;
	}

	public boolean shouldNotify(){
		return true;
	}
	
	/**
	 * Called when the effect is created
	 * @param entityliving
	 */
	public abstract void applyEffect(EntityLivingBase entityliving);

	public abstract void stopEffect(EntityLivingBase entityliving);

	private void effectEnding(EntityLivingBase entityliving){
		stopEffect(entityliving);
	}

	@Override
	public void performEffect(EntityLivingBase entityliving){
	}
	
	/**
	 * This method is called from PlayerTickEvent
	 * to do some things, that needs client-side to work (e.g. swift swim)
	 */
	@SideOnly(Side.CLIENT)
	public void performClientEffect(EntityPlayer entityPlayer){
	}

	@Override
	public void combine(PotionEffect potioneffect){
		if (!(potioneffect instanceof BuffEffect)){
			return;
		}
		int thisAmplifier = this.getAmplifier();
		if (thisAmplifier >= potioneffect.getAmplifier()){
			super.combine(potioneffect);
			this.HasNotified = false;
		}
	}

	@Override
	public boolean onUpdate(EntityLivingBase entityliving){
		boolean bool = super.onUpdate(entityliving);
		if (InitialApplication){
			InitialApplication = false;
			applyEffect(entityliving);
		}
		else if (getDuration() == 1){
			effectEnding(entityliving);
		}else if ((getDuration() / 20) < 5 && !HasNotified && shouldNotify() && !entityliving.worldObj.isRemote){
			HasNotified = true;
		}
		performEffect(entityliving);
		return bool;
	}

	public boolean isReady(int i, int j){
		int k = 25 >> j;
		if (k > 0){
			return i % k == 0;
		}else{
			return true;
		}
	}
	
	protected abstract String spellBuffName();

	@Override
	public String getEffectName(){
		return I18n.format(this.getPotion().getRegistryName().toString());
	}
	
	@Override
	public final NBTTagCompound writeCustomPotionEffectToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("InitialApplication", InitialApplication);
		nbt.setBoolean("HasNotified", HasNotified);
		writeCustomNBT(nbt);
		return super.writeCustomPotionEffectToNBT(nbt);
	}
	
	public void writeCustomNBT(NBTTagCompound nbt) {
		
	}

	public final void readFromNBT(NBTTagCompound nbt) {
		InitialApplication = nbt.getBoolean("InitialApplication");
		HasNotified = nbt.getBoolean("HasNotified");
		readCustomNBT(nbt);
	}
	
	public void readCustomNBT(NBTTagCompound nbt) {
		
	}
}

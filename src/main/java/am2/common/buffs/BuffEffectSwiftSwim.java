package am2.common.buffs;

import am2.common.defs.PotionEffectsDefs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class BuffEffectSwiftSwim extends BuffEffect{

	public BuffEffectSwiftSwim(int duration, int amplifier){
		super(PotionEffectsDefs.SWIFT_SWIM, duration, amplifier);
	}

	@Override
	public void applyEffect(EntityLivingBase entityliving){
	}

	@Override
	public void stopEffect(EntityLivingBase entityliving){
	}

	@Override
	public void performEffect(EntityLivingBase entityliving){
		if (entityliving.isInWater()){
			if (!(entityliving instanceof EntityPlayer)){
				entityliving.motionX *= (1.133f + 0.03 * this.getAmplifier());
				entityliving.motionZ *= (1.133f + 0.03 * this.getAmplifier());
				
				if (entityliving.motionY > 0){
					entityliving.motionY *= 1.134;
				}
			}
		}
	}
	
	@Override
	public void performClientEffect(EntityPlayer entityPlayer){
		if (entityPlayer.isInWater() && !entityPlayer.capabilities.isFlying){
			entityPlayer.motionX *= (1.133f + 0.03 * this.getAmplifier());
			entityPlayer.motionZ *= (1.133f + 0.03 * this.getAmplifier());
			
			if (entityPlayer.motionY > 0){
				entityPlayer.motionY *= 1.134;
			}
		}
	}
	
	@Override
	protected String spellBuffName(){
		return "Swift Swim";
	}

}

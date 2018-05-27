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
		if (entityliving.isInWater() && !(entityliving instanceof EntityPlayer)){
			entityliving.motionX *= (1.133f + 0.03 * this.getAmplifier());
			entityliving.motionZ *= (1.133f + 0.03 * this.getAmplifier());
			
			if (entityliving.motionY > 0){
				entityliving.motionY *= 1.134;
			}
		}
	}
	
	@Override
	public void performClientEffect(EntityPlayer entityPlayer){
		//the maximum player motion in X or Z: 0.1178
		//when sprinting: 0.15319
		if (entityPlayer.isInWater() && !entityPlayer.capabilities.isFlying){
			float maxSpeed = entityPlayer.isSprinting() ? 0.15319f : 0.1178f;
			
			entityPlayer.motionX *= (1.133f + 0.03 * this.getAmplifier());
			entityPlayer.motionZ *= (1.133f + 0.03 * this.getAmplifier());
			
			//we can do it with the module of speed vector. not now
			if (entityPlayer.motionX < -maxSpeed){
				entityPlayer.motionX = -maxSpeed;
			}else if (entityPlayer.motionX > maxSpeed){
				entityPlayer.motionX = maxSpeed;
			}
			if (entityPlayer.motionZ < -maxSpeed){
				entityPlayer.motionZ = -maxSpeed;
			}else if (entityPlayer.motionZ > maxSpeed){
				entityPlayer.motionZ = maxSpeed;
			}
			
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

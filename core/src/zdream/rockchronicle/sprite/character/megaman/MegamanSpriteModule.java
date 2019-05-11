package zdream.rockchronicle.sprite.character.megaman;

import zdream.rockchronicle.core.module.sprite.SpriteModule;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanSpriteModule extends SpriteModule {
	
	MegamanInLevel parent;

	public MegamanSpriteModule(MegamanInLevel parent) {
		super(parent);
		this.parent = parent;
	}
	
	/**
	 * 获取锚点 x 坐标.
	 */
	public float getX() {
		return parent.motion.box.anchor.x;
	}
	
	/**
	 * 获取锚点 y 坐标.
	 */
	public float getY() {
		return parent.motion.box.anchor.y;
	}
	
	String motion = "stop";
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		steps++;
		
		// 是否硬直
		boolean stiffness = parent.getBoolean(new String[] {"state", "stiffness"}, false);
		if (stiffness) {
			state = motion = "stiffness";
			steps = 0;
			return;
		}
		
		// 是否在跳跃
		boolean bottomStop = parent.getBoolean(new String[] {"motion", "bottomStop"}, true);
		if (!bottomStop) {
			state = motion = "jump";
			steps = 0;
			return;
		}
		
		String curMotion = parent.getString(new String[] {"state", "motion"}, "stop");
		if (!curMotion.equals(motion)) {
			switch (curMotion) {
			case "stop":
				state = "stop";
				break;
			case "left": case "right":
				state = "walk";
				break;

			default:
				break;
			}
			this.motion = curMotion;
			steps = 0;
		}
		
	}
	
}

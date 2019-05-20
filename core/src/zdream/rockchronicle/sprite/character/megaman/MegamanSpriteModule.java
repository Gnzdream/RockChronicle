package zdream.rockchronicle.sprite.character.megaman;

import zdream.rockchronicle.core.module.sprite.BaseSpriteModule;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanSpriteModule extends BaseSpriteModule {
	
	MegamanInLevel parent;

	public MegamanSpriteModule(MegamanInLevel parent) {
		super(parent);
		this.parent = parent;
	}
	
	/**
	 * 获取锚点 x 坐标.
	 */
	public float getX() {
		return getSingleBox().anchor.x;
	}
	
	/**
	 * 获取锚点 y 坐标.
	 */
	public float getY() {
		return getSingleBox().anchor.y;
	}
	
	String motion = "normal";
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		steps++;
		
		// 是否硬直
		boolean stiffness = parent.getBoolean(new String[] {"state", "stiffness"}, false);
		if (stiffness) {
			motion = "stiffness";
			setState("stiffness");
			steps = 0;
			return;
		}
		
		// 是否在跳跃
		boolean bottomStop = parent.getBoolean(new String[] {"motion", "bottomStop"}, true);
		if (!bottomStop) {
			motion = "jump";
			setState("jump");
			steps = 0;
			return;
		}
		
		String curMotion = parent.getString(new String[] {"state", "motion"}, "stop");
		if (!curMotion.equals(motion)) {
			switch (curMotion) {
			case "stop":
				setState("normal");
				break;
			case "walk":
				setState("walk");
				break;

			default:
				break;
			}
			this.motion = curMotion;
			steps = 0;
		}
		
	}
	
}

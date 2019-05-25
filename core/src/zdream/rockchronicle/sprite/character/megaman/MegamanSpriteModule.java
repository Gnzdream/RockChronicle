package zdream.rockchronicle.sprite.character.megaman;

import zdream.rockchronicle.core.module.sprite.BaseSpriteModule;
import zdream.rockchronicle.platform.body.Box;
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
	int attackRemain = 0;
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		if (attackRemain > 0) {
			attackRemain --;
		}
		
		// 是否硬直
		boolean stiffness = parent.getBoolean(new String[] {"state", "stiffness"}, false);
		if (stiffness) {
			motion = "stiffness";
			setState("stiffness");
			select.tick(1);
			return;
		}
		
		// 攻击判定
		boolean attacking = parent.getBoolean(new String[] {"state", "attacking"}, false);
		if (attacking) {
			attackRemain = LevelWorld.STEPS_PER_SECOND / 2;
		}
		
		// 是否在攀爬
		boolean climbing = parent.getBoolean(new String[] {"climb", "climbing"}, false);
		if (climbing) {
			int upOrDown = parent.getInt(new String[] {"climb", "upOrDown"}, 0);
			motion = "climb";
			setState("climb");
			
			if (upOrDown != 0) {
				select.tick(1);
			}
			return;
		}
		
		Box box = parent.getBoxModule().getBox();
		
		// 是否在跳跃
		boolean onTheGround = parent.getBoolean(new String[] {"state", "onTheGround"}, true);
		if (!onTheGround) {
			motion = "jump";
			String s1 = (box.velocity.y >= 0) ? "jump" : "drop";
			String s2 = (box.velocity.y >= 0) ? "jump_attack" : "drop_attack";
			
			if (attackRemain > 0) {
				if (s1.equals(select.getState())) {
					select.replaceState(s2);
				} else {
					setState(s2);
				}
			} else {
				if (s2.equals(select.getState())) {
					select.replaceState(s1);
				} else {
					setState(s1);
				}
			}

			select.tick(1);
			return;
		}
		
		String curMotion = parent.getString(new String[] {"state", "motion"}, "stop");
		switch (curMotion) {
		case "stop":
			setState("normal");
			break;
		case "walk": {
			if (attackRemain > 0) {
				if ("walk".equals(select.getState())) {
					select.replaceState("walk_attack");
				} else {
					setState("walk_attack");
				}
			} else {
				if ("walk_attack".equals(select.getState())) {
					select.replaceState("walk");
				} else {
					setState("walk");
				}
			}
		} break;

		default:
			break;
		}
		this.motion = curMotion;

		select.tick(1);
	}
	
}

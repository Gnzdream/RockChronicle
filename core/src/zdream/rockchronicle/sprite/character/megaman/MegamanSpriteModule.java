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
	public void selectTexture(LevelWorld world, int index, boolean hasNext) {
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
		int climbing = parent.getInt(new String[] {"climb", "climbing"}, 0);
		if (climbing > 0) {
			switch (climbing) {
			case 1:
				motion = "climb";
				setState("climb");
				break;
			case 2: case 3: case 4: case 5: case 6: case 7:
				motion = "climb";
				setState("climb_top_0");
				break;
			case 8: case 9: case 10: case 11: case 12: case 13:
				motion = "climb";
				setState("climb_top_1");
				break;
			default:
				break;
			}
			
			int upOrDown = parent.getInt(new String[] {"climb", "upOrDown"}, 0);
			if (upOrDown == 1) {
				select.tick(1);
			} else if (upOrDown == 2) {
				select.turnBack(1);
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

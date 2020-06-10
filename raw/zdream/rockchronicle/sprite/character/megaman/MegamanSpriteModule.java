package zdream.rockchronicle.sprite.character.megaman;

import zdream.rockchronicle.core.module.sprite.BaseSpriteModule;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanSpriteModule extends BaseSpriteModule {
	
	MegamanInLevel parent;

	public MegamanSpriteModule(MegamanInLevel parent) {
		super(parent, "megaman");
		this.parent = parent;
	}
	
	/**
	 * 获取锚点 x 坐标. 单位: p
	 */
	public int getX() {
		return getSingleBox().anchorX;
	}
	
	/**
	 * 获取锚点 y 坐标.
	 */
	public int getY() {
		return getSingleBox().anchorY;
	}
	
	String motion = "normal";
	int attackRemain = 0;
	
	@Override
	public void selectTexture(LevelWorld world, int index, boolean hasNext) {
		if (attackRemain > 0) {
			attackRemain --;
		}
		
		// 是否硬直
		boolean stiffness = getBoolean("health.stiffness", false);
		if (stiffness) {
			motion = "stiffness";
			setState("stiffness");
			select.tick(1);
			return;
		}
		
		// 攻击判定
		boolean attacking = getBoolean("weapon.attacking", false);
		if (attacking) {
			attackRemain = LevelWorld.STEPS_PER_SECOND / 2;
		}
		
		// 是否在攀爬
		int climbing = getInt("climb.climbing", 0);
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
			
			int upOrDown = getInt("climb.upOrDown", 0);
			if (upOrDown == 1) {
				select.tick(1);
			} else if (upOrDown == 2) {
				select.turnBack(1);
			}
			return;
		}
		
//		Box box = parent.getBoxModule().getBox();
		
		// 是否在跳跃
		boolean onTheGround = getBoolean("state.onTheGround", true);
		if (!onTheGround) {
			motion = "jump";
			boolean direction = getInt("jump.direction", 0) >= 0;
			String s1 = (direction) ? "jump" : "drop";
			String s2 = (direction) ? "jump_attack" : "drop_attack";
			
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
		
		String curMotion = getString("state.motion", "stop");
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

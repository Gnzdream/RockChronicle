package zdream.rockchronicle.foes.megaman;

import static zdream.rockchronicle.core.foe.Box.p2block;

import zdream.rockchronicle.core.foe.SingleBoxSpritePainter;
import zdream.rockchronicle.core.world.Ticker;

public class MegamanPainter extends SingleBoxSpritePainter {
	
	final Megaman mm;
	
	public MegamanPainter(Megaman mm) {
		super(new String[] {"res/characters/megaman/sprites/megaman7sheet.json"});
		this.mm = mm;
	}
	
	/**
	 * 使用的 Sprite 的形态
	 */
	String motion = "normal";
	int attackRemain = 0;
	
	public void tick() {
		if (attackRemain > 0) {
			attackRemain --;
		}
		
		// 是否硬直
		if (mm.stiffness > 0) {
			if (mm.slideDuration > 0) {
				// TODO setState("slide");
			} else {
				motion = "stiffness";
				setState("stiffness");
			}
			super.tick();
			return;
		}
		
		// 攻击判定
		boolean attacking = mm.attacking;
		if (attacking) {
			attackRemain = Ticker.STEPS_PER_SECOND / 2;
		}
		
		// 是否在攀爬
		int climbing = mm.climbing;
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
			
			if (mm.climbDirection != 0 && mm.climbHaltRemain == 0) {
				super.tick();
			} else {
				super.tick(0);
			}
			return;
		}
		
		// 是否在滑铲
		if ("slide".equals(mm.currentPattern)) {
			setState("slide");
			
			super.tick();
			return;
		}
		
//		Box box = parent.getBoxModule().getBox();
		
		// 是否在跳跃
		boolean inAir = mm.box.inAir;
		if (inAir) {
			motion = "jump";
			boolean direction = mm.box.velocityY >= 0;
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

			super.tick();
			return;
		}
		
		int walking = mm.walking;
		
		if (walking != 0) {
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
			this.motion = "walk";
		} else {
			if (attackRemain > 0) {
				setState("normal"); // attacking TODO
			} else {
				setState("normal");
			}
			
			this.motion = "stop";
		}

		super.tick();
	}
	
	@Override
	public int getImmune() {
		return mm.immuneRemain;
	}
	
	@Override
	public int zIndex() {
		return 1000;
	}

	@Override
	public boolean getOrientation() {
		return mm.box.orientation;
	}

	@Override
	public float getBx() {
		return p2block(mm.box.anchorX);
	}

	@Override
	public float getBy() {
		return p2block(mm.box.anchorY);
	}

}

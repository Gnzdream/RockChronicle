package zdream.rockchronicle.foes.mm2shotman;

import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.SimpleBoxFoe;
import zdream.rockchronicle.core.foe.SingleBoxSpritePainter;

import static zdream.rockchronicle.core.foe.Box.*;
import static zdream.rockchronicle.core.world.Ticker.*;
import static java.lang.Math.*;

import java.util.function.Predicate;

/**
 * <p>2 代冰关那个会发抛物线子弹的圆形怪
 * <p>
 * <li>不移动, 但不是固定的怪
 * <li>这里改成: 无碰撞伤害, 但是有敌人在面前时就不停地糊脸.
 * <li>HP:5 (1280), 碰撞伤害:无
 * <li>侦测: 敌人到达面前, 高为3 长为5 的方形区域, 从普通模式转为激活模式,
 * 攻击频率都加快, 子弹速率可调.
 * <li>普通模式子弹只有一种 (圆白弹), 伤害 2.
 * 按照近 8 子弹、远 8 子弹交替运行.
 * <li>激活模式子弹有两种, 圆白弹伤害 2, 另一种分情况, (TODO 暂时不做)
 * 如果 shotman 是蓝色, 发冰弹, 伤害 1, 会触发冰冻 BUFF;
 * 如果 shotman 是红色, 发火弹, 伤害 1, 会触发点燃 BUFF;
 * </li></p>
 * 
 * @author Zdream
 * @date 2020-06-11
 */
public class MM2Shotman extends SimpleBoxFoe {

	public MM2Shotman() {
		super("mm2shotman", "foe", (byte) 2);
		
		// "width":1.375, "height":1.375, "x":-0.666667, "y":0
		box.setBox(-43691, 0, 90112, 90112); // 锚点在下底的中点偏左
		box.terrain = new IntIntMap();
		box.terrain.put(1, 2);
		box.terrain.put(2, 2);
		box.terrain.put(3, 2);
		box.flush();
		
		this.hp = 256 * 5;
	}
	
	public MM2Shotman(JsonValue data) {
		this();
		
		if (data != null) {
			JsonValue iSpecies = data.get("species");
			if (iSpecies != null) {
				this.species = iSpecies.asByte();
			}
			
			// box
			JsonValue obox = data.get("box");
			if (obox != null) {
				JsonValue v = obox.get("orientation");
				if (v != null) {
					box.orientation = v.asBoolean();
				}
				
				v = obox.get("anchor");
				if (v != null) {
					box.setAnchor(block2P(v.getFloat("x")), block2P(v.getFloat("y")));
				}
			}
		}
	}
	
	private SingleBoxSpritePainter painter;
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		String path = (species == 0) ? 
				"res/characters/mm2shotman/sprites/mm2shotman_sheet_a.json" :
				"res/characters/mm2shotman/sprites/mm2shotman_sheet_b.json";
		painter = createPainter(new String[] {path});
	}
	
	/**
	 * 0 - 红色种
	 * 1 - 蓝色种
	 */
	byte species;
	/**
	 * 上次攻击时的 age. 普通模式;
	 * 普通模式每过 1 秒就发一颗子弹,
	 * 激活模式每过 0.5 秒就发一颗子弹.
	 */
	int lastAttackAge;
	/**
	 * 当前
	 */
	int attackCount;
	/**
	 * 是否在高射炮状态
	 */
	boolean high;

	@Override
	protected void stepIfNotPause() {
		box.velocityX = box.velocityY = 0;
		
		// 计算是否被激活
		Foe enemy = foundEnemy();
		if (enemy != null) {
			// 激活
			if (high) {
				changePosture();
			} else {
				// 0.5 秒发子弹
				int duration = age - lastAttackAge;
				if (duration >= 60) {
					shotEnemy(enemy);
					resetAttackState();
				}
			}
		} else {
			int duration = age - lastAttackAge;
			if (attackCount >= 8) {
				if (duration >= 48) {
					changePosture();
				}
			}
			
			if (attackCount != 0 && duration >= 120 || attackCount == 0 && duration >= 180) {
				shot();
				resetAttackState();
			}
		}
	}
	
	private Foe foundEnemy() {
		Check ch = new Check();
		ch.runtime = runtime;
		ch.camp = camp;
		
		int px = (box.orientation) ?
				(int) (box.posX + box.posWidth + 0.2f * P_PER_BLOCK) :
				(int) (box.posX - 6 * P_PER_BLOCK);
		
		runtime.world.overlaps(
				px, (int) (box.getCenterY() - 1.5f * P_PER_BLOCK),
				5 * P_PER_BLOCK, 3 * P_PER_BLOCK, ch, true, null);
		
		return ch.targetFoe;
	}
	
	static class Check implements Predicate<Box> {
		
		Foe targetFoe = null;
		GameRuntime runtime;
		byte camp;

		@Override
		public boolean test(Box box) {
			Foe foe = runtime.findEntry(box.id);
			
			if (foe.camp == this.camp) {
				return true;
			}
			
			switch (foe.type) {
			case "leader": case "foe": case "elite":
				targetFoe = foe;
				return false;

			default:
				return true;
			}
		}
		
	}
	
	private void changePosture() {
		high = !high;
		
		painter.setState((species == 0) ?
				((high) ? "R_A_to_C" : "R_C_to_A") :
				((high) ? "B_A_to_C" : "B_C_to_A"));
		attackCount = 0;
		lastAttackAge = age;
	}
	
	private void shot() {
		int n = (box.orientation ? 1 : 0) + (high ? 2 : 0);
		MM2ShotmanBullet bullet = new MM2ShotmanBullet();
		
		int bulletX, bulletY,
			vx, initVy,
			ay = -304, // 等于洛克人的
			maxVy = 15292; // 也等于洛克人的, 每秒 28 块
		
		switch (n) {
		case 0: // 左、低炮
			bulletX = box.anchorX - (int) (0.9 * P_PER_BLOCK);
			bulletY = box.anchorY + (int) (0.8 * P_PER_BLOCK);
			vx = -16 * P_PER_BLOCK / STEPS_PER_SECOND;
			initVy = 4 * P_PER_BLOCK / STEPS_PER_SECOND;
			break;
		case 1: // 右、低炮
			bulletX = box.anchorX + (int) (0.9 * P_PER_BLOCK);
			bulletY = box.anchorY + (int) (0.8 * P_PER_BLOCK);
			vx = 16 * P_PER_BLOCK / STEPS_PER_SECOND;
			initVy = 4 * P_PER_BLOCK / STEPS_PER_SECOND;
			break;
		case 2: // 左、高炮
			bulletX = box.anchorX - (int) (0.7 * P_PER_BLOCK);
			bulletY = box.anchorY + (int) (1.5 * P_PER_BLOCK);
			vx = -8 * P_PER_BLOCK / STEPS_PER_SECOND;
			initVy = 16 * P_PER_BLOCK / STEPS_PER_SECOND;
			break;
		case 3: // 右、高炮
			bulletX = box.anchorX + (int) (0.7 * P_PER_BLOCK);
			bulletY = box.anchorY + (int) (1.5 * P_PER_BLOCK);
			vx = 8 * P_PER_BLOCK / STEPS_PER_SECOND;
			initVy = 16 * P_PER_BLOCK / STEPS_PER_SECOND;
			break;

		default:
			return; // 不可能到这里
		}
		
		bullet.set(bulletX, bulletY, vx, initVy, ay, maxVy);
		runtime.addFoe(bullet);
		
		this.attackCount++;
	}
	
	private void shotEnemy(Foe enemy) {
		
		// 喷口位置
		int srcPx = (box.orientation) ?
				box.anchorX + (int) (0.9 * P_PER_BLOCK) :
				box.anchorX - (int) (0.9 * P_PER_BLOCK);
		int srcPy = box.anchorY + (int) (0.8 * P_PER_BLOCK);
		
		// 对方位置
		int destPx = enemy.getBoxes()[0].getCenterX();
		int destPy = enemy.getBoxes()[0].getCenterY();
		
		// 双方的水平距离, 非负数
		int pDeltaX = abs(destPx - srcPx); // 非负数
		int vx = 16 * P_PER_BLOCK / STEPS_PER_SECOND; // 正数
		int time = pDeltaX / vx; // 非负数
		
		if (pDeltaX == 0 || time == 0 ||
				box.orientation && destPx < srcPx || !box.orientation && destPx > srcPx) {
			shot();
			return;
		}
		
		int ay = -304;
		int dropY = (time - 1) * time * ay / 2; // 负数
		int vy = (destPy - dropY - srcPy) / time;
		if (vy < 0) {
			vy = 0;
		}
		if (vy > vx / 2) {
			vy = vx / 2;
		}
		
//		System.out.println(time + ", " + (destPy - srcPy) + ", " + vy);
//		System.out.println(srcPy / 65536.0f + ", " + destPy / 65536.0f);
		
		MM2ShotmanBullet bullet = new MM2ShotmanBullet();
		bullet.set(srcPx, srcPy, 
				(box.orientation) ? vx : -vx,
				vy, ay, 15292);
		runtime.addFoe(bullet);
		
		this.attackCount++;
	}
	
	private void resetAttackState() {
		this.lastAttackAge = age;
	}

}

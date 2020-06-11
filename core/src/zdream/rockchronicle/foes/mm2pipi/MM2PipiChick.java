package zdream.rockchronicle.foes.mm2pipi;

import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.SimpleBoxFoe;
import zdream.rockchronicle.core.world.Ticker;

/**
 * <p>2 代飞鸟带着的蛋里面藏着的小鸟.
 * <p>
 * <li>刚出来时四散跑, 速度为 4 块/秒
 * <li>0.5秒后, 寻找最近的攻击目标位置, 朝着这个位置不转向地跑. 速度仍为 4 块/秒
 * <li>生命值: 1, 碰撞伤害: 3, 碰撞等级: 1
 * </li></p>
 * 
 * @author Zdream
 * @date 2020-06-11
 */
public class MM2PipiChick extends SimpleBoxFoe {

	public MM2PipiChick() {
		super("mm2pipi_chick", "foe", (byte) 2);
		
		box.setBox(-19115, -16384, 38229, 32768); // 锚点在下底的中点偏左
		
		hp = 256;
		damage = 256 * 3;
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		createPainter(new String[] {"res/characters/mm2birds/sprites/mm2pipi_chicks_sheet.json"});
	}

	@Override
	protected void stepIfNotPause() {
		if (age == Ticker.STEPS_PER_SECOND / 2) { // 0.5s
			findTarget();
		}
		box.setVelocity(vx, vy);
		
		runtime.world.submitMotion(box, false);
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	/**
	 * 速度, 单位: p / 步
	 */
	int vx, vy;
	
	/**
	 * 由蛋调用, 设置位置.
	 * @param eggpx
	 *   蛋的 anchorX, 单位 p
	 * @param eggpy
	 *   蛋的 anchorY, 单位 p
	 * @param vx
	 *   小鸟的初始横坐标速度, 单位: p / 步
	 * @param vy
	 *   小鸟的初始纵坐标速度, 单位: p / 步
	 */
	void setPosition(int eggpx, int eggpy, int vx, int vy) {
		box.setAnchor(eggpx, eggpy);
		this.vx = vx;
		this.vy = vy;
		box.flush();
	}
	
	/* **********
	 *   侦查   *
	 ********** */
	
	/**
	 * 找到离自己最近的 camp != 自己 camp 的 leader、foe、或者 elite
	 */
	public void findTarget() {
		int px = box.anchorX;
		int py = box.anchorY;
		
		Foe found = null;
		int targetPx = 0, targetPy = 0;
		int minDistance = Integer.MAX_VALUE;
		
		Array<Foe> foes = runtime.foes;
		for (int i = 0; i < foes.size; i++) {
			Foe foe = foes.get(i);
			
			if (foe.camp == this.camp || 
					!("leader".equals(foe.type) || "foe".equals(foe.type)
							|| "elite".equals(foe.type))) {
				continue;
			}
			
			Box[] boxes = foe.getBoxes();
			if (boxes.length == 0) {
				continue;
			}
			Box foeBox = boxes[0];
			int foePx = foeBox.posX + foeBox.posWidth / 2,
					foePy = foeBox.posY + foeBox.posHeight / 2;
			
			// 计算距离 - 直接算横纵坐标差, 这样简单
			int distance = Math.abs(px - foePx) + Math.abs(py - foePy);
			if (distance < minDistance) {
				minDistance = distance;
				found = foe;
				targetPx = foePx;
				targetPy = foePy;
			}
		}
		
		if (found != null) {
			if (px == targetPx) {
				// box.orientation 不变
				if (py == targetPy) { // 位置重合
					return;
				} else {
					this.vx = 0;
					this.vy = (targetPy > py) ? 4 * 65536 : -4 * 65536;
				}
			} else {
				double deltaX = Math.abs(px - targetPx);
				double deltaY = Math.abs(py - targetPy);
				double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY) /
						(4 * 65536 / Ticker.STEPS_PER_SECOND);
				
				this.vx = (int) ((targetPx - px) / delta);
				this.vy = (int) ((targetPy - py) / delta);
				box.orientation = vx > 0;
			}
		}
	}
	
}

package zdream.rockchronicle.foes.mm2pipi;

import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.SimpleBoxFoe;

/**
 * <p>2 代带着蛋飞的鸟.
 * <p>
 * <li>飞行速度: 水平方向 8 块/秒 (4369)
 * <li>携带一只蛋
 * <li>HP:1, 碰撞伤害:3, 碰撞等级:1
 * <li>侦测: 敌人到达横纵坐标差为 10 块时把蛋放下, 仅触发一次
 * </li></p>
 * 
 * @author Zdream
 * @date 2020-06-11
 */
public class MM2Pipi extends SimpleBoxFoe {

	public MM2Pipi() {
		super("mm2pipi", "foe", (byte) 2);
		
		box.setBox(-30037, 0, 62805, 52429); // 锚点在下底的中点偏左
		
		this.hp = 256;
		this.damage = 256 * 3;
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		createPainter(new String[] {"res/characters/mm2birds/sprites/mm2birds_sheet.json"});
		
		// 它携带一只蛋
		egg = new MM2PipiEgg(id);
		egg.setPosition(box.anchorX, box.anchorY);
		runtime.addFoe(egg);
	}
	
	public void stepIfNotPause() {
		box.velocityX = vx;
		box.velocityY = 0;
		runtime.world.submitMotion(box, false, false);
		
		// 接下来是蛋的
		if (egg != null) {
			if (egg.isDisposed()) {
				egg = null;
			} else {
				egg.setPosition(box.anchorX, box.anchorY);
			}
		}
		
		// findTarget
		if (egg != null && findTarget()) {
			// 把蛋扔下去
			throwEgg();
		}
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	int vx;
	
	/**
	 * 生怪场调用的
	 */
	public void setMotion(int px, int py, int vx, boolean orientation) {
		box.setAnchor(px, py);
		this.vx = vx;
		box.orientation = orientation;
		box.flush();
	}
	
	/* **********
	 *   携带   *
	 ********** */
	
	MM2PipiEgg egg;
	
	@Override
	protected void beforeDestroy() {
		if (egg != null) {
			egg.destroy();
		}
	}
	
	private void throwEgg() {
		egg.throwd();
		egg = null;
	}
	
	/* **********
	 *   侦查   *
	 ********** */
	
	/**
	 * 是否存在离自己 10 格距离内的 camp != 自己 camp 的 leader、foe、或者 elite
	 */
	public boolean findTarget() {
		int px = box.anchorX;
		int py = box.anchorY;
		int threshold = Box.P_PER_BLOCK * 10;
		
		Array<Foe> foes = runtime.foes;
		for (int i = 0; i < foes.size; i++) {
			Foe foe = foes.get(i);
			
			if (foe.camp == this.camp || 
					!("leader".equals(this.type) || "foe".equals(this.type)
							|| "elite".equals(this.type))) {
				continue;
			}
			
			Box[] boxes = foe.getBoxes();
			if (boxes.length == 0) {
				continue;
			}
			Box foeBox = boxes[0];
			
			// 计算距离 - 直接算横纵坐标差, 这样简单
			int distance = Math.abs(px - foeBox.anchorX) + Math.abs(py - foeBox.anchorY);
			if (threshold >= distance) {
				return true;
			}
		}
		
		return false;
	}
	
}

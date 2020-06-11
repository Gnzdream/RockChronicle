package zdream.rockchronicle.foes.mm2pipi;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.SimpleBoxFoe;
import zdream.rockchronicle.core.world.Ticker;

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
		
		outsideTrialPeriod = Ticker.STEPS_PER_SECOND;
		outsideThreshold = Ticker.STEPS_PER_SECOND / 10;
		
		this.hp = 256;
		this.damage = 256 * 3;
		
		// 它携带一只蛋
		// TODO
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		createPainter(new String[] {"res/characters/mm2birds/sprites/mm2birds_sheet.json"});
	}
	
	public void stepIfNotPause() {
		box.velocityX = vx;
		box.velocityY = 0;
		runtime.world.submitMotion(box, false);
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	int vx;
	
	public void setMotion(int px, int py, int vx, boolean orientation) {
		box.setAnchor(px, py);
		this.vx = vx;
		box.orientation = orientation;
		box.flush();
	}
	
	/* **********
	 *   侦查   *
	 ********** */
}

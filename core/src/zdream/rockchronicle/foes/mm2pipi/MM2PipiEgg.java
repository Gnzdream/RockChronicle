package zdream.rockchronicle.foes.mm2pipi;

import com.badlogic.gdx.graphics.Color;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.ShapePainter;
import zdream.rockchronicle.core.foe.SimpleBoxFoe;
import zdream.rockchronicle.core.world.Ticker;

/**
 * <p>2 代带着蛋飞的鸟.
 * <p>
 * <li>飞行速度: 水平方向 8 块/秒
 * <li>携带一只蛋
 * <li>HP:1, 碰撞伤害:3, 碰撞等级:1
 * <li>侦测: 敌人到达横纵坐标差为 10 块时把蛋放下, 仅触发一次
 * </li></p>
 * 
 * @author Zdream
 * @date 2020-06-11
 */
public class MM2PipiEgg extends SimpleBoxFoe {

	public MM2PipiEgg() {
		super("mm2pipi_egg", "foe", (byte) 2);
		
		box.setBox(-24576, -16384, 49152, 32768); // 锚点在上边的中点偏左

		outsideTrialPeriod = Ticker.STEPS_PER_SECOND;
		outsideThreshold = Ticker.STEPS_PER_SECOND / 10;
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		putPainter(new ShapePainter(box, Color.YELLOW));
	}

	@Override
	protected void stepIfNotPause() {
		// TODO Auto-generated method stub
		
	}
	
	/* **********
	 *   盒子   *
	 ********** */

}

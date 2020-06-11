package zdream.rockchronicle.foes.mm2pipi;

import com.badlogic.gdx.graphics.Color;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.ShapePainter;

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
public class MM2Pipi extends Foe {

	public MM2Pipi() {
		super("mm2pipi");
		type = "foe";
		camp = 2;
		
		box = new Box(id);
		box.setBox(-30037, 0, 62805, 52429); // 锚点在下底的中点偏左
		box.setAnchor(99999, 99999);
		boxes = new Box[] { box };
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		putPainter(new ShapePainter(box, Color.YELLOW));
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	Box box;
	Box[] boxes;

	@Override
	public Box[] getBoxes() {
		return boxes;
	}

}

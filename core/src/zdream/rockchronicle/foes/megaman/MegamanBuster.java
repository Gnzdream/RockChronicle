package zdream.rockchronicle.foes.megaman;

import com.badlogic.gdx.graphics.Color;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.ShapePainter;
import zdream.rockchronicle.core.world.Ticker;

/**
 * <p>洛克人的子弹
 * <p>
 * 默认伤害为 1, 生效一次.
 * 水平移动速度为 24 块/秒.
 * 最长寿命为 1 秒, 如果在房间外超过 0.05 秒也会被回收.
 * </p>
 * 
 * @author Zdream
 * @date 2020-06-10
 */
public class MegamanBuster extends Foe {

	public MegamanBuster() {
		super("megaman_buster");
		camp = 1;
		
		box = new Box(this.id);
		box.setBox(-10923, -8192, 21845, 16384);
		boxes = new Box[] { box };
	}
	
	ShapePainter painter;
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		putPainter(painter = new ShapePainter(box, Color.YELLOW));
	}
	
	@Override
	public void onDispose() {
		removePainter(painter);
		super.onDispose();
	}
	
	@Override
	public void step(boolean pause) {
		if (!pause) {
			// 寿命
			if (handleRemain()) {
				destroy();
				return;
			}
			// 运动
			handleMotion();
			// 判断是否打中别人
			// TODO
		}
	}

	/* **********
	 *   盒子   *
	 ********** */
	
	Box box;
	Box[] boxes;
	
	int velocityX = 13107, velocityY;

	@Override
	public Box[] getBoxes() {
		return boxes;
	}
	
	private void handleMotion() {
		box.velocityX = (box.orientation) ? velocityX : -velocityX;
		box.velocityY = velocityY;
		
		runtime.world.submitMotion(box, false);
	}
	
	/* **********
	 *   寿命   *
	 ********** */
	
	public int remain = Ticker.STEPS_PER_SECOND;
	public int outsideDuration = 0;
	
	/**
	 * @return
	 *   如果确定自毁了, 就返回 true
	 */
	private boolean handleRemain() {
		remain--;
		if (remain == 0) {
			return true;
		}
		
		if (isOutside()) {
			outsideDuration++;
			if (outsideDuration > Ticker.STEPS_PER_SECOND / 20) {
				return true;
			}
		} else {
			outsideDuration = 0;
		}
		
		return false;
	}

	/**
	 * 判断是否在房间外.
	 * @return
	 */
	public boolean isOutside() {
		box.flush();
		return runtime.world.isOutside(box.posX, box.posY, box.boxWidth, box.boxHeight);
	}

}

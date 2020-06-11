package zdream.rockchronicle.foes.mm2pipi;

import static zdream.rockchronicle.core.foe.Box.p2block;

import com.badlogic.gdx.graphics.Color;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.FoeEvent;
import zdream.rockchronicle.core.foe.ShapePainter;
import zdream.rockchronicle.core.foe.SingleBoxSpritePainter;
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
public class MM2Pipi extends Foe {

	public MM2Pipi() {
		super("mm2pipi");
		type = "foe";
		camp = 2;
		
		box = new Box(id);
		box.setBox(-30037, 0, 62805, 52429); // 锚点在下底的中点偏左
		box.setAnchor(99999, 150000);
		boxes = new Box[] { box };
		
		// 它携带一只蛋
		// TODO
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		putPainter(spainter = new ShapePainter(box, Color.YELLOW));
		putPainter(painter);
		
		addSubscribe("applyDamage", this::recieveDamage);
	}
	
	@Override
	public void onDispose() {
		removePainter(spainter);
		removePainter(painter);
		super.onDispose();
	}
	
	ShapePainter spainter;
	
	@Override
	public void step(boolean pause) {
		super.step(pause);
		
		box.velocityX = vx;
		box.velocityY = 0;
		runtime.world.submitMotion(box, false);
		
		handleRemain();
		painter.tick();
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	Box box;
	Box[] boxes;
	/**
	 * 角色销毁的保护时间, 单位: 步, 配置项
	 */
	public int outsideTrialPeriod = Ticker.STEPS_PER_SECOND;
	/**
	 * 角色判断时长, 单位: 步, 配置项
	 */
	public int outsideThreshold = Ticker.STEPS_PER_SECOND / 10;
	/**
	 * 角色在房间外的时长, 单位: 步, 状态项
	 */
	public int outsideDuration;
	public int age = 0;
	
	int vx;

	@Override
	public Box[] getBoxes() {
		return boxes;
	}
	
	public void setMotion(int px, int py, int vx, boolean orientation) {
		box.setAnchor(px, py);
		this.vx = vx;
		box.orientation = orientation;
		box.flush();
	}
	
	/**
	 * @return
	 *   如果确定自毁了, 就返回 true
	 */
	private boolean handleRemain() {
		age += 1;
		if (age > outsideTrialPeriod) {
			if (isOutside()) {
				// 如果跑到房间外
				outsideDuration ++;
			} else {
				outsideDuration = 0;
			}
			
			if (outsideDuration > outsideThreshold) {
				destroy();
				return true;
			}
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
	
	/* **********
	 *   侦查   *
	 ********** */
	
	/* **********
	 *   健康   *
	 ********** */
	
	int hp = 256; // 就是 1
	
	private void recieveDamage(FoeEvent eve) {
		int camp = eve.value.getInt("camp");
		if (camp != this.camp) {
			// 确定受伤啦
			eve.value.get("recieved").set(true);
			
			int damage = eve.value.getInt("damage");
			hp -= damage;
			if (hp <= 0) {
				this.destroy();
			}
		}

	}
	
	/* **********
	 *   绘画   *
	 ********** */
	
	class Painter extends SingleBoxSpritePainter {

		public Painter() {
			super(new String[] {"res/characters/mm2birds/sprites/mm2birds_sheet.json"});
		}

		@Override
		public int zIndex() {
			return 601;
		}

		@Override
		public boolean getOrientation() {
			return box.orientation;
		}

		@Override
		public float getBx() {
			return p2block(box.anchorX);
		}

		@Override
		public float getBy() {
			return p2block(box.anchorY);
		}
		
	}
	Painter painter = new Painter();
}

package zdream.rockchronicle.core.foe;

import static zdream.rockchronicle.core.foe.Box.p2block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.world.Ticker;

/**
 * 适用于单一盒子的小怪、精英.
 * 
 * @author Zdream
 * @date 2020-06-11
 */
public abstract class SimpleBoxFoe extends Foe {

	public SimpleBoxFoe(String name, String type, byte camp) {
		super(name);
		this.type = type;
		this.camp = camp;

		box = new Box(id);
		boxes = new Box[] { box };
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		Color color = Color.GRAY;
		switch (type) {
		case "foe": color = Color.YELLOW; break;
		case "elite": color = Color.ORANGE; break;
		case "leader": color = Color.GREEN; break;
		default: break;
		}
		
		sPainter = new ShapePainter(box, color);
		putPainter(sPainter);
		
		addSubscribe("applyDamage", this::recieveDamage);
	}
	
	@Override
	public void onDispose() {
		if (painter != null) {
			removePainter(painter);
		}
		removePainter(sPainter);
		super.onDispose();
	}
	
	@Override
	public final void step(boolean pause) {
		super.step(pause);
		
		if (!pause) {
			stepIfNotPause();
		}
		
		handleRemain();
		handleAttack();
		
		if (painter != null) {
			painter.tick();
		}
	}
	
	/**
	 * 里面处理移动
	 */
	protected abstract void stepIfNotPause();

	/* **********
	 *   盒子   *
	 ********** */
	
	protected Box box;
	private Box[] boxes;
	
	/*
	 * 销毁部分
	 */
	/**
	 * 角色销毁的保护时间, 单位: 步, 配置项
	 */
	public int outsideTrialPeriod = Ticker.STEPS_PER_SECOND;
	/**
	 * 角色判断时长, 单位: 步, 配置项.
	 * -1 表示不自动销毁
	 */
	public int outsideThreshold = -1;
	/**
	 * 角色在房间外的时长, 单位: 步, 状态项
	 */
	public int outsideDuration;
	public int age = 0;

	@Override
	public Box[] getBoxes() {
		return boxes;
	}
	
	/**
	 * @return
	 *   如果确定自毁了, 就返回 true
	 */
	private boolean handleRemain() {
		age += 1;
		if (age > outsideTrialPeriod && outsideThreshold >= 0) {
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
	 *   攻击   *
	 ********** */
	
	/**
	 * 设置为 0 就是无碰撞伤害 (但是有攻击动作).
	 * 设置为 -1 表示禁用攻击.
	 */
	protected int damage = -1;
	/**
	 * <p>伤害等级. 防御等级大于等于伤害等级, 伤害不生效.
	 * <p>一般:
	 * <li>怪碰撞伤害等级 1
	 * <li>精英怪 / BOSS 碰撞伤害等级 2
	 * <li>大多数子弹伤害等级 10
	 * <li>怪 / 精英怪 / BOSS 无敌时间防御等级 10
	 * <li>洛克人 / 安全帽无敌时间防御等级 12
	 * <li>地形的刺伤害等级 5
	 * <li>安全帽躲到壳里面的防御等级 15 (伤害不生效不等于附加效果不生效)
	 * <li>陷阱伤害等级 8
	 * </li></p>
	 */
	protected byte damageLevel = 1;
	
	private void handleAttack() {
		if (damage < 0) {
			return;
		}
		
		runtime.world.overlaps(box, this::checkCollision, true);
	}
	
	protected FoeEvent createDamageEvent() {
		FoeEvent damageEvent = new FoeEvent("applyDamage");
		JsonValue v = new JsonValue(ValueType.object);
		v.addChild("damage", new JsonValue(damage));
		v.addChild("level", new JsonValue(damageLevel));
		v.addChild("camp", new JsonValue(this.camp));
		v.addChild("recieved", new JsonValue(false));
		damageEvent.value = v;
		return damageEvent;
	}
	
	protected boolean checkCollision(Box box) {
		int targetId = box.parentId;
		
		Foe target = runtime.findEntry(targetId);
		if (target.camp != this.camp) {
			switch (target.type) {
			case "leader": case "foe": case "elite": {
				FoeEvent eve = createDamageEvent();
				target.publishNow(eve);
			} break;

			default:
				break;
			}
		}
		
		return true;
	}
	
	/* **********
	 *   健康   *
	 ********** */
	
	protected int hp; // 需要初始化
	
	private void recieveDamage(FoeEvent eve) {
		int camp = eve.value.getInt("camp");
		if (camp != this.camp) {
			// 确定受伤啦
			int damage = eve.value.getInt("damage");
			
			if (onDamageRecieved(damage, eve)) {
				eve.value.get("recieved").set(true);
				
				hp -= damage;
				if (hp <= 0) {
					this.destroy();
				}
			}
		}
	}
	
	/**
	 * 伤害提交前的钩子.
	 * @param damage
	 * @return
	 *   返回 false 可以取消这次伤害
	 */
	protected boolean onDamageRecieved(int damage, FoeEvent eve) {
		return true;
	}
	
	/* **********
	 *   绘画   *
	 ********** */
	
	protected SingleBoxSpritePainter createPainter(String[] path) {
		painter = new Painter(path);
		putPainter(painter);
		return painter;
	}
	
	class Painter extends SingleBoxSpritePainter {
		public Painter(String[] path) {
			super(path);
		}

		@Override
		public int zIndex() {
			switch (type) {
			case "foe": return 505;
			case "elite": return 555;
			case "leader": return 605;
			default: return 601;
			}
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
	protected Painter painter;
	protected ShapePainter sPainter;

}

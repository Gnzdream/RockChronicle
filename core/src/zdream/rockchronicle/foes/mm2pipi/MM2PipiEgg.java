package zdream.rockchronicle.foes.mm2pipi;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.AttackEvent;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.SimpleBoxFoe;
import zdream.rockchronicle.core.region.ITerrainStatic;

/**
 * <p>2 代飞鸟带着的蛋.
 * <p>扔下来前:
 * <li>位置跟随飞鸟. 当鸟死了之后蛋也会没.
 * <li>生命值: 1, 碰撞伤害: 2(仅下落阶段), 碰撞 camp=3
 * </li>
 * <p>扔下来后:
 * <li>开始受地形影响, 做自由落体运动. 水平方向无速度.
 * <li>不会跟着鸟死后自毁
 * <li>每步判定是否砸到东西 (包括地形和别的怪, 触发的第一步时间不判断飞鸟)
 * 一旦砸到东西, 给予 camp=3 数值 2 的无差别伤害. 如果砸到地形则不释放伤害.
 * 砸到后自己碎裂, 产生 8 只 mm2pipi_chick 和 2 个贴图 foe.
 * </li></p>
 * 
 * @author Zdream
 * @date 2020-06-11
 */
public class MM2PipiEgg extends SimpleBoxFoe {
	
	/**
	 * 鸟的 id
	 */
	public final int parentId;

	public MM2PipiEgg(int parentId) {
		super("mm2pipi_egg", "foe", (byte) 2);
		this.parentId = parentId;
		
		box.setBox(-24576, -16384, 49152, 32768); // 锚点在正中央.
		
		this.hp = 256;
		this.damage = 256 * 2;
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		createPainter(new String[] {"res/characters/mm2birds/sprites/mm2pipi_egg_sheet.json"});
	}

	@Override
	protected void stepIfNotPause() {
		if (stage == 2) {
			dropVelocity += decay;
			if (dropVelocity < maxDropVelocity) {
				dropVelocity = maxDropVelocity;
			}
			
			runtime.world.freshBox(box, false, ITerrainStatic.TERRAIN_EMPTY);
			if (box.leftTouched || box.rightTouched || box.topTouched || box.bottomTouched) {
				broken();
				return;
			}
			
			if (runtime.world.isBoxOverlap(box)) {
				broken();
				return;
			}
			
			box.setVelocityY(dropVelocity);
			runtime.world.submitMotion(box, false, this.camp, ITerrainStatic.TERRAIN_EMPTY);
		}
	}

	/* **********
	 *   阶段   *
	 ********** */
	/**
	 * <p>阶段.
	 * </p>
	 * 1: 跟随阶段
	 * 2: 自由落体阶段
	 */
	public byte stage = 1;
	
	/**
	 * 扔蛋之后, 鸟会调用它
	 */
	void throwd() {
		stage = 2;
	}
	
	@Override
	protected AttackEvent createDamageEvent() {
		AttackEvent event = super.createDamageEvent();
		event.attackCamp = 3;
		return event;
	}
	
	protected boolean needAttack(Foe target) {
		if (stage == 1) {
			return false;
		}
		
		if (target.name.equals("mm2pipi")) {
			return false;
		}
		
		switch (target.type) {
		case "leader": case "foe": case "elite":  return true;

		default:
			return false;
		}
	}
	
	@Override
	protected void onAttackFinished(int attackCount) {
		broken();
	}

	/**
	 * 碎了, 要出鸟了
	 */
	private void broken() {
		// 产生 8 只鸟
		int[][] is = new int[][] {
			{-2185, 0},
			{-1968, 948},
			{-1362, 1708},
			{-486, 2130},
			{486, 2130},
			{1362, 1708},
			{1968, 948},
			{2185, 0},
		};
		for (int i = 0; i < is.length; i++) {
			int[] xy = is[i];
			
			MM2PipiChick chick = new MM2PipiChick();
			chick.setPosition(box.anchorX, box.anchorY, xy[0], xy[1]);
			runtime.addFoe(chick);
		}
		
		// TODO 产生 2 个碎片 Foe
		
		destroy();
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	/**
	 * 当做自由落体时, 速度在每一步时间之后的速度衰减值 (delta), 配置值. 单位: p / 步
	 * 在水中会影响该衰减值导致跳跃变高.
	 * 换算后为 -304 p / 步.
	 */
	public int decay = -304;
	/**
	 * 下落时每步最快速度 (负数方向向下), 配置值. 单位: p / 步
	 * 在水中会影响该值导致向下速度变慢.
	 * 每秒 28 块, 换算后为 -15292 p / 步.
	 */
	public int maxDropVelocity = -15292;
	/**
	 * 记录上一次下落时速度
	 */
	public int dropVelocity = 0;
	
	/**
	 * 由鸟调用, 设置位置.
	 * @param pipipx
	 *   鸟的 anchorX, 单位 p
	 * @param pipipy
	 *   鸟的 anchorY, 单位 p
	 */
	void setPosition(int pipipx, int pipipy) {
		box.setAnchor(pipipx, pipipy - 16384);
	}

}

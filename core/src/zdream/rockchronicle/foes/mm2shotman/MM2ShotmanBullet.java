package zdream.rockchronicle.foes.mm2shotman;

import zdream.rockchronicle.core.foe.SimpleBoxFoe;

public class MM2ShotmanBullet extends SimpleBoxFoe {

	public MM2ShotmanBullet() {
		super("mm2shotman_bullet", "bullet", (byte) 2);

		// "width":0.333334, "height":0.333334, "x":-0.166667, "y":-0.166667
		box.setBox(-10923, -10923, 21845, 21845);
		box.flush();

		this.damage = 256 * 2;
	}
	
	/**
	 * 横向速度, 是个定值, 单位: p / 步
	 */
	public int vx;
	/**
	 * 纵向初始速度, 单位: p / 步
	 */
	public int initVy;
	/**
	 * 纵向加速度, 每步变化量, 单位: p / 步^2
	 */
	public int ay;
	/**
	 * 纵向最快速度, 正值. 纵向速度的绝对值不会大于它
	 */
	public int maxVy;
	
	/**
	 * 当前纵向速度
	 */
	public int vy;
	
	public MM2ShotmanBullet set(int anchorX, int anchorY, int vx, int initVy, int ay, int maxVy) {
		box.setAnchor(anchorX, anchorY);
		this.vx = vx;
		this.initVy = initVy;
		this.ay = ay;
		this.maxVy = maxVy;
		
		vy = initVy;
		return this;
	}

	@Override
	protected void stepIfNotPause() {
		vy += ay;
		if (vy > maxVy) {
			vy = maxVy;
		} else if (vy < -maxVy) {
			vy = -maxVy;
		}
		
		box.setVelocity(vx, vy);
		runtime.world.submitFloatBoxMotion(box);
	}
	
	@Override
	protected void onAttackFinished(int attackCount) {
		this.destroy();
	}

}

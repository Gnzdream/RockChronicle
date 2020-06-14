package zdream.rockchronicle.foes.mm5bbitter;

import zdream.rockchronicle.core.foe.SimpleBoxFoe;

public class MM2BBitterBullet extends SimpleBoxFoe {

	public MM2BBitterBullet() {
		super("mm2b_bitter_bullet", "bullet", (byte) 2);

		// "width":0.333334, "height":0.333334, "x":-0.166667, "y":-0.166667
		box.setBox(-10923, -10923, 21845, 21845);
		box.flush();

		this.damage = 256 * 4;
	}
	
	/**
	 * 横向速度, 是个定值, 单位: p / 步
	 */
	public int vx;
	
	public MM2BBitterBullet set(int anchorX, int anchorY, int vx) {
		box.setAnchor(anchorX, anchorY);
		this.vx = vx;
		return this;
	}

	@Override
	protected void stepIfNotPause() {
		box.setVelocity(vx, 0);
		runtime.world.submitFloatBoxMotion(box);
	}
	
	@Override
	protected void onAttackFinished(int attackCount) {
		this.destroy();
	}

}

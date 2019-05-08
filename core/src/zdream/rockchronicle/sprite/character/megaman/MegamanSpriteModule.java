package zdream.rockchronicle.sprite.character.megaman;

import zdream.rockchronicle.core.character.module.SpriteModule;

public class MegamanSpriteModule extends SpriteModule {
	
	Megaman parent;

	public MegamanSpriteModule(Megaman parent) {
		super(parent);
		this.parent = parent;
	}
	
	/**
	 * 获取锚点 x 坐标.
	 */
	public float getX() {
		return parent.motion.box.anchor.x;
	}
	
	/**
	 * 获取锚点 y 坐标.
	 */
	public float getY() {
		return parent.motion.box.anchor.y;
	}

}

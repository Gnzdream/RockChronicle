package zdream.rockchronicle.bullet;

import zdream.rockchronicle.core.character.CharacterEntry;

/**
 * 子弹消失时的回调函数
 * @author Zdream
 * @since v0.0.1
 */
public interface IBulletDisappearCallback<T extends CharacterEntry> {
	
	public void onDisappear(T entry);

}

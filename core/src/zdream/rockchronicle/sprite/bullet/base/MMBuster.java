package zdream.rockchronicle.sprite.bullet.base;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.sprite.bullet.IBulletDisappearCallback;

/**
 * 洛克人的基础子弹
 * @author Zdream
 */
public class MMBuster extends CharacterEntry {
	
	MMBusterMotion motion;
	MMBusterSprite sprite;
	
	private IBulletDisappearCallback<MMBuster> callback;

	public MMBuster(int id) {
		super(id);
		motion = new MMBusterMotion(this);
		sprite = new MMBusterSprite(this);
		
		this.addModule(motion);
		this.addModule(sprite);
	}

	/**
	 * 一般是 motion 调用, 它让该物体销毁
	 */
	@Override
	public void willDestroy() {
		if (callback != null) {
			callback.onDisappear(this);
		}
		
		super.willDestroy();
	}

	@Override
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		sprite.draw(batch, camera);
	}
	
	public void setDisappearCallback(IBulletDisappearCallback<MMBuster> callback) {
		this.callback = callback;
	}
	
}

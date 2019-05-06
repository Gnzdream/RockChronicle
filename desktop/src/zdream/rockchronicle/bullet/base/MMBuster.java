package zdream.rockchronicle.bullet.base;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.bullet.IBulletDisappearCallback;
import zdream.rockchronicle.core.character.CharacterEntry;

/**
 * 洛克人的基础子弹
 * @author Zdream
 */
public class MMBuster extends CharacterEntry {
	
	MMBusterMotion motion;
	MMBusterSprite sprite;
	
	private IBulletDisappearCallback<MMBuster> callback;

	public MMBuster() {
		motion = new MMBusterMotion(this);
		sprite = new MMBusterSprite(this);
		
		this.addModule(motion);
		this.addModule(sprite);
	}

	@Override
	protected void init(FileHandle file, JsonValue json) {
		super.init(file, json);
		// TODO 这里先写死
		
		motion.init(file, json);
		sprite.init(file, json);
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

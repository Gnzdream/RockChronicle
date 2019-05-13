package zdream.rockchronicle.sprite.bullet.base;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.sprite.bullet.IBulletDisappearCallback;

/**
 * 洛克人的基础子弹
 * @author Zdream
 */
public class MMBuster extends CharacterEntry {
	
	MMBusterMotion motion;
	
	private IBulletDisappearCallback<MMBuster> callback;

	public MMBuster(int id, String name) {
		super(id, name);
		motion = new MMBusterMotion(this);
	}
	
	@Override
	protected void init(FileHandle file, JsonValue value) {
		this.addModule(motion);
		super.init(file, value);
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

	public void setDisappearCallback(IBulletDisappearCallback<MMBuster> callback) {
		this.callback = callback;
	}
	
}

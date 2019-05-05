package zdream.rockchronicle.character.megaman;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.input.PlayerInput;

public class Megaman extends CharacterEntry {
	
	MegamanControlModule ctrl;
	MegamanMotionModule motion;
	MegamanSpriteModule sprite;
	
	{
		ctrl = new MegamanControlModule(this);
		motion = new MegamanMotionModule(this);
		sprite = new MegamanSpriteModule(this);
		
		this.addModule(ctrl);
		this.addModule(motion);
		this.addModule(sprite);
	}
	
	@Override
	protected void init(FileHandle file, JsonValue json) {
		super.init(file, json);
		
		// rect 碰撞块
		JsonValue rectArray = json.get("box");
		motion.initCollideRect(rectArray);
		
		ctrl.init(file, json);
		motion.init(file, json);
		sprite.init(file, json);
	}
	
	/**
	 * 绑定控制端
	 */
	public void bindController(PlayerInput input) {
		ctrl.bind(input);
	}
	
	/**
	 * 解绑控制端
	 */
	public void unbindController() {
		ctrl.unbind();
	}
	
	@Override
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		sprite.draw(batch);
	}
	
}

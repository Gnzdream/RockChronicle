package zdream.rockchronicle.sprite.character.megaman;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.input.PlayerInput;

public class MegamanInLevel extends CharacterEntry {
	
	public MegamanInLevel(int id) {
		super(id);
	}

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

package zdream.rockchronicle.sprite.character.megaman;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.input.IInputBindable;
import zdream.rockchronicle.core.input.PlayerInput;

public class MegamanInLevel extends CharacterEntry implements IInputBindable {
	
	public MegamanInLevel(int id, String name) {
		super(id, name);
	}

	MegamanControlModule ctrl;
	MegamanMotionModule motion;
	MegamanSpriteModule sprite;
	MegamanWeaponModule weapon;
	
	{
		ctrl = new MegamanControlModule(this);
		motion = new MegamanMotionModule(this);
		sprite = new MegamanSpriteModule(this);
		weapon = new MegamanWeaponModule(this);
		
		this.addModule(ctrl);
		this.addModule(motion);
		this.addModule(sprite);
		this.addModule(weapon);
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
		sprite.draw(batch, camera);
	}
	
}

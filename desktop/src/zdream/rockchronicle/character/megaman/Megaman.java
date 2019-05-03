package zdream.rockchronicle.character.megaman;

import java.util.Iterator;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.character.AbstractModule;
import zdream.rockchronicle.character.CharacterEntry;
import zdream.rockchronicle.desktop.PlayerInput;
import zdream.rockchronicle.platform.world.LevelWorld;

public class Megaman extends CharacterEntry {
	
	MegamanControlModule ctrl;
	MegamanMotionModule motion;
	MegamanSpriteModule sprite;
	
	public Megaman() {
		ctrl = new MegamanControlModule(this);
		motion = new MegamanMotionModule(this);
		sprite = new MegamanSpriteModule(this);
		
		this.addModule(ctrl);
		this.addModule(motion);
		this.addModule(sprite);
		
		ctrl.init();
		motion.init();
		sprite.init();
	}

	@Override
	protected void onLoadJson(FileHandle file, JsonValue json) {
		// rect 碰撞块
		
		JsonValue rectArray = json.get("rect");
		motion.initCollideRect(rectArray);
		
		sprite.initTexturePaths(file, json.get("textures"));
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
	public void draw(SpriteBatch batch) {
		sprite.draw(batch);
	}
	
	/**
	 * 设置位置, 单位 block
	 */
	public void setBlockPos(final int blockx, final int blocky) {
		motion.setBlockPos(blockx, blocky);
	}
	
	@Override
	public void step(LevelWorld world, int index, boolean hasNext) {
		ctrl.step(world, index, hasNext);
		motion.step(world, index, hasNext);
		
		Iterator<AbstractModule> it = modules.values().iterator();
		while (it.hasNext()) {
			AbstractModule m = it.next();
			if (m == ctrl || m == motion || m == sprite) {
				continue;
			}
			m.step(world, index, hasNext);
		}
		sprite.step(world, index, hasNext);
	}

}

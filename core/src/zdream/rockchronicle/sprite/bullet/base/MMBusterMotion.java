package zdream.rockchronicle.sprite.bullet.base;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.module.motion.MotionModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

@Deprecated
public class MMBusterMotion extends MotionModule {
	
	public final MMBuster parent;
	
	/**
	 * 剩余的飞行时间. (单位 : 步)
	 * 开始时为 1 秒的步数
	 */
	public int life = LevelWorld.STEPS_PER_SECOND;
	
	public MMBusterMotion(MMBuster mm) {
		super(mm);
		this.parent = mm;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		Box box = getSingleBox();
		if (this.orientation) {
			box.setVelocity(0.2f, 0);
		} else {
			box.setVelocity(-0.2f, 0);
		}
		box.inTerrain = false;
		parent.getBoxModule().modified();
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		Box box = getSingleBox();
		Rectangle pos = box.getPosition();
		if (!new Rectangle(0, 0, world.currentRoom.width, world.currentRoom.height).overlaps(pos)) {
			// 如果跑到房间外, 则直接删掉
			parent.willDestroy();
		} else {
			// 如果寿命到了, 则直接删掉
			life -= 1;
			if (life <= 0) {
				parent.willDestroy();
			}
		}
	}

}

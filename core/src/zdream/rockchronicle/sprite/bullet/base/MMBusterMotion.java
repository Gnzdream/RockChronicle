package zdream.rockchronicle.sprite.bullet.base;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.motion.SingleBoxMotionModule;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MMBusterMotion extends SingleBoxMotionModule {
	
	public final MMBuster parent;
	
	/**
	 * 剩余的飞行时间. (单位 : 步)
	 * 开始时为 1 秒的步数
	 */
	public int life = LevelWorld.STEPS_PER_SECOND;
	
	public MMBusterMotion(MMBuster mm) {
		super(mm);
		this.parent = mm;
		
		box.inTerrain = false;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		if (this.orientation) {
			box.setVelocity(0.2f, 0);
		} else {
			box.setVelocity(-0.2f, 0);
		}
		boxc.clear();
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		world.execVerticalMotion(box);
		world.execHorizontalMotion(box);
		
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

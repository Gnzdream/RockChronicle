package zdream.rockchronicle.bullet.base;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.MotionModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MMBusterMotion extends MotionModule {
	
	final MMBuster parent;
	public final Box box = new Box();
	
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
		box.box.set(-0.1f, -0.1f, 0.2f, 0.2f);
	}
	
	public void set(float x, float y, boolean orientation) {
		this.box.setAnchor(x, y);
		this.orientation = orientation;
		
		if (this.orientation) {
			box.setVelocity(0.2f, 0);
		} else {
			box.setVelocity(-0.2f, 0);
		}
	}

	@Override
	public void initCollideRect(JsonValue rectArray) {
		// TODO 这里先写死
	}

	@Override
	protected void createBody(LevelWorld world) {
		world.addBox(box);
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		world.execVerticalMotion(box);
		world.execHorizontalMotion(box);
		
		Rectangle pos = box.getPosition();
		if (!new Rectangle(0, 0, world.currentRoom.width, world.currentRoom.height).overlaps(pos)) {
			// 如果跑到房间外, 则直接删掉
			parent.execDestroy();
		} else {
			// 如果寿命到了, 则直接删掉
			life -= 1;
			if (life <= 0) {
				parent.execDestroy();
			}
		}
		
	}

}

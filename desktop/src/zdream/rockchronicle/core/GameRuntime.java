package zdream.rockchronicle.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.region.Region;
import zdream.rockchronicle.platform.region.Room;
import zdream.rockchronicle.platform.world.IPhysicsStep;
import zdream.rockchronicle.platform.world.LevelWorld;

public class GameRuntime {
	
	/**
	 * 现在正在显示的关卡 {@link Region}
	 */
	public Region curRegion;
	
	/**
	 * 现在显示的 {@link Room} 编号
	 */
	public int room;
	
	/**
	 * 玩家所控制的角色
	 */
	public CharacterEntry player1;
	
	/**
	 * 如果在关卡中, 关卡世界的参数
	 */
	public LevelWorld levelWorld;
	
	/**
	 * @return
	 *   正在显示的 {@link Room}
	 */
	public Room getCurrentRoom() {
		return curRegion.rooms[room];
	}
	
	/**
	 * 除了控制端以外的, 所有子弹、怪物的集合
	 */
	public final Array<CharacterEntry> entrise = new Array<>();
	private final Array<CharacterEntry> entriseWaitingForAdd = new Array<>();
	private final Array<CharacterEntry> entriseWaitingForRemove = new Array<>();
	
	public void addEntry(CharacterEntry entry) {
		entriseWaitingForAdd.add(entry);
	}
	
	public void removeEntry(CharacterEntry entry) {
		entriseWaitingForRemove.add(entry);
	}
	
	/**
	 * 每一帧更新一下. 包括角色
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void onWorldSteped(int index, boolean hasNext) {
		
		// 确定状态部分
		
		player1.determine(levelWorld, index, hasNext);
		
		for (int i = 0; i < entrise.size; i++) {
			try {
				entrise.get(i).determine(levelWorld, index, hasNext);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		
		// 进行删增工作
		entrise.addAll(entriseWaitingForAdd);
		entriseWaitingForAdd.clear();
		
		entrise.removeAll(entriseWaitingForRemove, true);
		entriseWaitingForRemove.clear();
		
		// 移动部分
		
		player1.step(levelWorld, index, hasNext);
		
		for (int i = 0; i < entrise.size; i++) {
			try {
				entrise.get(i).step(levelWorld, index, hasNext);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 类似于 {@link IPhysicsStep#onStepFinished(LevelWorld, boolean)}
	 * @param isPause
	 *   本帧是否在暂停状态
	 */
	public void onStepFinished(boolean isPause) {
		player1.onStepFinished(levelWorld, isPause);
		
		for (int i = 0; i < entrise.size; i++) {
			try {
				entrise.get(i).onStepFinished(levelWorld, isPause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawEntries(SpriteBatch batch, OrthographicCamera camera) {
		player1.draw(batch, camera);
		
		for (int i = 0; i < entrise.size; i++) {
			try {
				entrise.get(i).draw(batch, camera);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		
		if (entrise.size != lastEntriseSize) {
			lastEntriseSize = entrise.size;
			Gdx.app.log("GameRuntime", "实体个数 : " + lastEntriseSize);
		}
		if (levelWorld.count() != lastWorldCount) {
			lastWorldCount = levelWorld.count();
			Gdx.app.log("GameRuntime", "碰撞块个数 : " + lastWorldCount);
		}
	}
	
	int lastEntriseSize = 0, lastWorldCount = 0;

}

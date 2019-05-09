package zdream.rockchronicle.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.cast.CastList;
import zdream.rockchronicle.core.character.CharacterBuilder;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.region.Region;
import zdream.rockchronicle.platform.region.RegionBuilder;
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
	
	// 共用工具
	public final CastList cast = new CastList();
	public final RegionBuilder regionBuilder = new RegionBuilder();
	public final CharacterBuilder characterBuilder = new CharacterBuilder();

	public void init() {
		characterBuilder.init();
	}
	
	public void putPlayer(int seq, CharacterEntry entry) {
		switch (seq) {
		case 1:
			this.player1 = entry;
			entry.createBody(levelWorld);
			break;

		default:
			throw new IllegalArgumentException(String.format("无法设置玩家 %d 的角色", seq));
		}
	}
	
	/**
	 * 除了控制端以外的, 所有子弹、怪物的集合
	 */
	public final Array<CharacterEntry> entries = new Array<>();
	private final Array<CharacterEntry> entriesWaitingForAdd = new Array<>();
	private final Array<CharacterEntry> entriesWaitingForRemove = new Array<>();
	
	/**
	 * 用 id 来寻找角色.
	 * @param id
	 * @return
	 *   可能为 null
	 */
	public CharacterEntry findEntry(int id) {
		if (player1 != null && player1.id == id) {
			return player1;
		}
		
		for (int i = 0; i < entries.size; i++) {
			CharacterEntry entry = entries.get(i);
			if (entry.id == id) {
				return entry;
			}
		}
		
		return null;
	}
	
	public void addEntry(CharacterEntry entry) {
		entriesWaitingForAdd.add(entry);
	}
	
	public void removeEntry(CharacterEntry entry) {
		entriesWaitingForRemove.add(entry);
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
		if (player1 != null) {
			player1.determine(levelWorld, index, hasNext);
		}
		
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.get(i).determine(levelWorld, index, hasNext);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		
		// 进行删增工作
		entries.removeAll(entriesWaitingForRemove, true);
		entriesWaitingForRemove.clear();
		entries.addAll(entriesWaitingForAdd);
		entriesWaitingForAdd.forEach(entry -> entry.createBody(levelWorld));
		entriesWaitingForAdd.clear();
		
		// 移动部分
		if (player1 != null) {
			player1.step(levelWorld, index, hasNext);
		}
		
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.get(i).step(levelWorld, index, hasNext);
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
		if (player1 != null) {
			player1.onStepFinished(levelWorld, isPause);
		}
		
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.get(i).onStepFinished(levelWorld, isPause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawEntries(SpriteBatch batch, OrthographicCamera camera) {
		if (player1 != null) {
			player1.draw(batch, camera);
		}
		
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.get(i).draw(batch, camera);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		
		if (entries.size != lastEntriseSize) {
			lastEntriseSize = entries.size;
			Gdx.app.log("GameRuntime", "实体个数 : " + lastEntriseSize);
		}
		if (levelWorld.count() != lastWorldCount) {
			lastWorldCount = levelWorld.count();
			Gdx.app.log("GameRuntime", "碰撞块个数 : " + lastWorldCount);
		}
	}
	
	int lastEntriseSize = 0, lastWorldCount = 0;

}

package zdream.rockchronicle.core;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.cast.CastList;
import zdream.rockchronicle.core.character.CharacterBuilder;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.input.IInputBindable;
import zdream.rockchronicle.platform.region.Field;
import zdream.rockchronicle.platform.region.Gate;
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
	public final CharacterPaintComparator pcomp = new CharacterPaintComparator(this);

	public void init() {
		characterBuilder.init();
		regionBuilder.init();
	}
	
	/* **********
	 * 关卡数据 *
	 ********** */
	
	/**
	 * 设置当前的区域
	 * @param name
	 */
	public void setRegion(String name) {
		curRegion = regionBuilder.buildForTerrainOnly(name);
		this.room = curRegion.spawnRoom;
		levelWorld.setCurrentRoom(curRegion.rooms[this.room]);
		
		// 将场放入世界
		Room room = curRegion.rooms[this.room];
		final int length = room.fields.size;
		for (int i = 0; i < length; i++) {
			Field f = room.fields.get(i);
			
			CharacterEntry entry = characterBuilder.create(f.name, f.param);
			addEntry(entry);
		}
	}
	
	public void createWorld() {
		levelWorld.doCreate();
		levelWorld.setStepCallBack(step);
		levelWorld.doResume();
	}
	
	public void resumeWorld() {
		levelWorld.doResume();
	}
	
	public void pauseWorld() {
		levelWorld.doPause();
	}
	
	IPhysicsStep step = new IPhysicsStep() {
		@Override
		public void step(LevelWorld world, int index, boolean hasNext) {
			if (!checkRoomShift()) {
				onWorldSteped(index, hasNext);
			}
		}
		
		public void onStepFinished(LevelWorld world, boolean isPause) {
			GameRuntime.this.onStepFinished(isPause);
		};
	};
	
	/* **********
	 * 角色数据 *
	 ********** */
	
	/**
	 * 玩家所控制的角色 ID
	 */
	public int player1;
	
	public CharacterEntry getPlayer1() {
		return findEntry(player1);
	}
	
	public void putPlayer(int seq, CharacterEntry entry) {
		switch (seq) {
		case 1:
			this.player1 = entry.id;
			if (entry instanceof IInputBindable) {
				((IInputBindable) entry).bindController(RockChronicle.INSTANCE.input.p1);
			}
			addEntry(entry);
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
		for (int i = 0; i < entries.size; i++) {
			CharacterEntry entry = entries.get(i);
			if (entry.id == id) {
				return entry;
			}
		}
		
		return null;
	}
	
	/**
	 * 用 id 来从等待添加列表中寻找角色.
	 * @param id
	 * @return
	 *   可能为 null
	 */
	public CharacterEntry findEntryWaitingForAdd(int id) {
		for (int i = 0; i < entriesWaitingForAdd.size; i++) {
			CharacterEntry entry = entriesWaitingForAdd.get(i);
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
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.get(i).onStepFinished(levelWorld, isPause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawEntries(SpriteBatch batch, OrthographicCamera camera) {
		CharacterEntry[] es = entries.toArray(CharacterEntry.class);
		Arrays.sort(es, this.pcomp);
		
		for (int i = 0; i < es.length; i++) {
			try {
				es[i].draw(batch, camera);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		
		if (entries.size != lastEntriseSize || levelWorld.count() != lastWorldCount) {
			lastEntriseSize = entries.size;
			lastWorldCount = levelWorld.count();
			Gdx.app.log("GameRuntime",
					String.format("实体数: %d, 碰撞块: %d", lastEntriseSize, lastWorldCount));
		}
	}
	
	int lastEntriseSize = 0, lastWorldCount = 0;
	
	/* **********
	 * 重要事件 *
	 ********** */
	/*
	 * 比如控制角色走到房间的边缘, 需要启动切换房间的逻辑
	 */
	public boolean checkRoomShift() {
		CharacterEntry c1 = getPlayer1();
		if (c1 == null) {
			return false;
		}
		Gate[] gs = levelWorld.checkRoomShift(c1.getBoxModule().getBox());
		if (gs != null) {
			System.out.println(Arrays.toString(gs));
		}
		return false;
	}
	
}

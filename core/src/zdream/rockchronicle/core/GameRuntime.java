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
import zdream.rockchronicle.platform.region.ConnectionProperties;
import zdream.rockchronicle.platform.region.FieldDef;
import zdream.rockchronicle.platform.region.FoeDef;
import zdream.rockchronicle.platform.region.Gate;
import zdream.rockchronicle.platform.region.Region;
import zdream.rockchronicle.platform.region.RegionBuilder;
import zdream.rockchronicle.platform.region.RegionPoint;
import zdream.rockchronicle.platform.region.Room;
import zdream.rockchronicle.platform.world.IPhysicsStep;
import zdream.rockchronicle.platform.world.LevelWorld;
import zdream.rockchronicle.platform.world.RoomShiftHandler;
import zdream.rockchronicle.platform.world.SceneDesigner;

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
	public RoomShiftHandler shift;
	
	public SceneDesigner scene = new SceneDesigner(this);
	
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
		scene.init();
	}
	
	/* **********
	 * 关卡数据 *
	 ********** */
	
	/**
	 * <p>设置当前的区域, 并将有复活点的房间设置成当前房间.
	 * <p>如果该区域没有复活点, 请不要调用该方法.
	 * </p>
	 * @param name
	 */
	public void setSpawnRegion(String name) {
		Region region = regionBuilder.build(name);
		if (region.spawnRoom == -1) {
			throw new IllegalStateException(String.format("%s 的出生点位没有确定", region));
		}
		
		setRoom(region.rooms[region.spawnRoom]);
	}
	
	/**
	 * 移屏时, 请保证清除其它角色之后再调用该方法.
	 * @param room
	 */
	public void setRoom(Room room) {
		this.room = room.index;
		curRegion = room.region;
		levelWorld.setCurrentRoom(room);
		
		// 如果该房间有连接到其它区域, 需要初始化相邻的区域
		// 扫描点
		Array<RegionPoint> ps = curRegion.points;
		for (int i = 0; i < ps.size; i++) {
			RegionPoint p = ps.get(i);
			if (!room.contain(p.x, p.y)) {
				continue;
			}
			if (p.conn != null) {
				ConnectionProperties conn = p.conn;
				Region destRegion = regionBuilder.build(conn.destRegionName);
				RegionPoint point = destRegion.findPoint(conn.destPoint);
				if (point == null) {
					continue; // 找不到这个点
				}
				Room destRoom = destRegion.of(point.x, point.y);
				if (destRoom == null) {
					continue; // 这个点不属于任何房间
				}
				regionBuilder.createGate(room, destRoom, p, point);
			}
		}
		
		Room r = curRegion.rooms[this.room];
		// 将场放入世界
		int length = r.fields.size;
		for (int i = 0; i < length; i++) {
			FieldDef f = r.fields.get(i);
			
			CharacterEntry entry = characterBuilder.create(f.name, f.param);
			addEntry(entry);
		}
		
		// 将怪放入世界
		length = r.foes.size;
		for (int i = 0; i < length; i++) {
			FoeDef f = r.foes.get(i);
			
			CharacterEntry entry = characterBuilder.create(f.name, f.param);
			addEntry(entry);
		}
		
		// 其它
		scene.onRoomUpdated();
	}
	
	public void createWorld() {
		levelWorld = new LevelWorld();
		shift = new RoomShiftHandler();
		
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
	
	public void tick(float deltaTime) {
		if (shift.durationShift()) {
			shift.tickShift(deltaTime);
		}	
		levelWorld.doPhysicsStep(deltaTime);
	}
	
	IPhysicsStep step = new IPhysicsStep() {
		@Override
		public void step(LevelWorld world, int index, boolean hasNext) {
			if (!checkRoomShift()) {
				onWorldSteped(index, hasNext);
			}
		}
		
		public void stepPaused(LevelWorld world) {
			GameRuntime.this.stepPaused();
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
	 * 所有子弹、怪物的集合
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
		handleAddAndRemove();
		
		// 移动部分
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.get(i).step(levelWorld, index, hasNext);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handleAddAndRemove() {
		entries.removeAll(entriesWaitingForRemove, true);
		entriesWaitingForRemove.clear();
		entries.addAll(entriesWaitingForAdd);
		entriesWaitingForAdd.forEach(entry -> entry.createBody(levelWorld));
		entriesWaitingForAdd.clear();
	}

	/**
	 * 类似于 {@link IPhysicsStep#stepPaused(LevelWorld, boolean)}
	 */
	public void stepPaused() {
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.get(i).stepPaused(levelWorld);
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
		Gate gate = levelWorld.checkRoomShift(c1.getBoxModule().getBox());
		if (gate != null && shift.checkShift(c1, gate)) {
			shift.doShift(gate);
			return true;
		}
		return false;
	}
	
}

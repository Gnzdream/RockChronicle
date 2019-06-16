package zdream.rockchronicle.core;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;

import zdream.rockchronicle.cast.CastList;
import zdream.rockchronicle.core.character.CharacterEntry;
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
		return levelWorld.currentRoom;
	}
	
	// 共用工具
	public final CastList cast = new CastList();
	public final RegionBuilder regionBuilder = new RegionBuilder();
	public final CharacterPaintComparator pcomp = new CharacterPaintComparator(this);

	public void init() {
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
		levelWorld.setCurrentRoom(room);
		
		// 如果该房间有连接到其它区域, 需要初始化相邻的区域
		// 扫描点
		Array<RegionPoint> ps = room.region.points;
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
		
		// 将场放入世界
		int length = room.fields.size;
		for (int i = 0; i < length; i++) {
			FieldDef f = room.fields.get(i);
			
			CharacterEntry entry = levelWorld.createEntry(f.name, f.param);
			levelWorld.addEntry(entry);
		}
		
		// 将怪放入世界
		length = room.foes.size;
		for (int i = 0; i < length; i++) {
			FoeDef f = room.foes.get(i);
			
			CharacterEntry entry = levelWorld.createEntry(f.name, f.param);
			levelWorld.addEntry(entry);
		}
		
		// 其它
		scene.onRoomUpdated();
	}
	
	public void createWorld() {
		levelWorld = new LevelWorld();
		levelWorld.init();
		shift = new RoomShiftHandler();
		
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
	 * 每一帧更新一下. 包括角色
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void onWorldSteped(int index, boolean hasNext) {
		DelayedRemovalArray<CharacterEntry> entries = levelWorld.entries;
		
		// 确定状态部分
		entries.begin();
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.items[i].determine(levelWorld, index, hasNext);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		entries.end();
		
		// 移动部分
		entries.begin();
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.items[i].step(levelWorld, index, hasNext);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		entries.end();
	}
	
	/**
	 * 类似于 {@link IPhysicsStep#stepPaused(LevelWorld, boolean)}
	 */
	public void stepPaused() {
		DelayedRemovalArray<CharacterEntry> entries = levelWorld.entries;
		for (int i = 0; i < entries.size; i++) {
			try {
				entries.get(i).stepPaused(levelWorld);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawEntries(SpriteBatch batch, OrthographicCamera camera) {
		DelayedRemovalArray<CharacterEntry> entries = levelWorld.entries;
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
		CharacterEntry c1 = levelWorld.getPlayer1();
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

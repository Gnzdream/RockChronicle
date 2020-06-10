package zdream.rockchronicle.core;

import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.IFoePainter;
import zdream.rockchronicle.core.region.Region;
import zdream.rockchronicle.core.region.RegionBuilder;
import zdream.rockchronicle.core.region.Room;
import zdream.rockchronicle.core.world.SceneDesigner;
import zdream.rockchronicle.core.world.Ticker;

public class GameRuntime {
	
	public void init() {
		regionBuilder.init();
		scene.init();
		
		ticker.setStepListener((pause) -> this.step(pause));
	}
	
	public void tick(float delta) {
		ticker.tick(delta);
	}
	
	/*
	 * 补充的方法在这里
	 */
	
	/* **********
	 *  计时器  *
	 ********** */
	/*
	 * 计时器和暂停管理等
	 * 包括世界状态:
	 * 1. 房间切换
	 * 2. 自由控制
	 */
	public Ticker ticker = new Ticker();
	
	public void step(boolean pause) {
		// 移动部分
		for (int i = 0; i < foes.size; i++) {
			try {
				foes.get(i).step(pause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		
		// 进行删增工作
		handleFoeAddAndRemove();
		
		// 确定状态部分
		for (int i = 0; i < foes.size; i++) {
			try {
				foes.get(i).submit(pause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}
	
	/* **********
	 *   地图   *
	 ********** */
	
	/**
	 * 现在正在显示的关卡 {@link Region}
	 */
	public Region curRegion;
	
	/**
	 * 现在显示的 {@link Room} 编号
	 */
	public int room;
	
	public final RegionBuilder regionBuilder = new RegionBuilder();

	/**
	 * 创建一个 {@link Region}, 设置为当前活动的地区, 并且当前房间为默认的初始房间
	 * @param name
	 *   这个初始的 {@link Region} 的名字
	 */
	public void setCurrentRegion(String name) {
		this.curRegion = regionBuilder.build(name);
		setCurrentRoom(curRegion.spawnRoom);
	}

	public Room getCurrentRoom() {
		return curRegion.rooms[room];
	}
	
	/**
	 * 当前房间发生替换
	 * @param room
	 */
	public void setCurrentRoom(int room) {
		this.room = room;
		this.scene.onRoomUpdated();
	}
	
	/* **********
	 *   画面   *
	 ********** */
	
	public SceneDesigner scene = new SceneDesigner(this);

	/* **********
	 *   角色   *
	 ********** */
	
	public Array<Foe> foes = new Array<>();
	public Array<Box> boxes = new Array<>();
	public Array<IFoePainter> painters = new Array<>();
	private final Array<Foe> foesWaitingForAdd = new Array<>();
	private final Array<Foe> foesWaitingForRemove = new Array<>();
	
	/**
	 * 用 id 来寻找角色.
	 * @param id
	 * @return
	 *   可能为 null
	 */
	public Foe findEntry(int id) {
		for (int i = 0; i < foes.size; i++) {
			Foe entry = foes.get(i);
			if (entry.id == id) {
				return entry;
			}
		}
		return null;
	}
	
	public void addFoe(Foe entry) {
		if (entry != null)
			foesWaitingForAdd.add(entry);
	}
	
	public void removeFoe(Foe entry) {
		if (entry != null)
			foesWaitingForRemove.add(entry);
	}
	
	private void handleFoeAddAndRemove() {
		foes.removeAll(foesWaitingForRemove, true);
		foesWaitingForRemove.clear();
		foes.addAll(foesWaitingForAdd);
		foesWaitingForAdd.forEach((foe) -> foe.init(this));
		foesWaitingForAdd.clear();
	}

	public void addBox(Box box) {
		boxes.add(box);
	}

	public void removeBox(Box box) {
		boxes.removeValue(box, true);
	}

	public void addPainter(IFoePainter painter) {
		painters.add(painter);
	}

	public void removePainter(IFoePainter painter) {
		painters.removeValue(painter, true);
	}
	
}

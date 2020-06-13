package zdream.rockchronicle.core.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.region.FieldDef;
import zdream.rockchronicle.core.region.FoeDef;
import zdream.rockchronicle.core.region.Gate;
import zdream.rockchronicle.core.region.Room;

import static zdream.rockchronicle.core.foe.Box.*;

import static zdream.rockchronicle.core.region.Gate.*;

/**
 * <p>场景设计师
 * <p>对现在世界进入的房间, 将所有的 {@link OrthogonalTiledMapRenderer} 摆放至指定位置;
 * 管理镜头位置. 将场景中的怪一一放到指定位置;
 * <p>负责房间切换
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-19 (created)
 *   2019-05-19 (last modified)
 */
public class SceneDesigner {
	
	private final GameRuntime runtime;
	
	/**
	 * 摄像机
	 */
	public final OrthographicCamera camera = new OrthographicCamera();
	
	/**
	 * 摄像机瞄准的位置, 以当前房间为参照
	 */
	public Vector2 centerPoint = new Vector2();
	
	/**
	 * 渲染当前 Tiled 地图的, 因此会常驻且不会被回收
	 */
	public OrthogonalTiledMapRenderer mainRender;
	
	/**
	 * 地图的渲染列表, 渲染其它区域的. 当出现跨 Region 的渲染时启用
	 */
	public Array<MapRendererEntry> renders = new Array<>();
	
	/**
	 * 创建怪的始作俑者
	 */
	public FoeBuilder foeBuilder = new FoeBuilder();
	
	public SceneDesigner(GameRuntime runtime) {
		this.runtime = runtime;
	}
	
	class MapRendererEntry {
		OrthogonalTiledMapRenderer render;
		Gate gate;
	}
	
	public void init() {
		RockChronicle game = RockChronicle.INSTANCE;
		
		camera.setToOrtho(false, game.width, game.height); // y 轴方向朝上
		mainRender = new OrthogonalTiledMapRenderer(null, 1f / Config.INSTANCE.blockWidth);
	}
	
	public void onRoomChanged() {
		Room room = runtime.getCurrentRoom();
		
		// 清空原来的渲染列表, 暂时不考虑重用.
		for (int i = 1; i < renders.size; i++) {
			MapRendererEntry entry = renders.get(i);
			entry.render.dispose();
		}
		renders.clear();
		
		mainRender.setMap(room.region.tmx);
		
		// 按照房间的大门列表补充新的
		for (int i = 0; i < room.gates.size; i++) {
			Gate g = room.gates.get(i);
			if (g.destRoom.region.equals(room.region)) {
				continue;
			}
			MapRendererEntry entry = new MapRendererEntry();
			entry.render = new OrthogonalTiledMapRenderer(
					g.destRoom.region.tmx, 1f / Config.INSTANCE.blockWidth);
			entry.gate = g;
			renders.add(entry);
		}
		
		// 开始放怪
		putFoesIntoWorld(room);
	}

	/* **********
	 * 房间切换 *
	 ********** */
	
	public RoomShiftParam param;
	
	public void doShift(Gate gate) {
		param = new RoomShiftParam(gate);
		
		param.entries.add(runtime.player1);
		param.entriesPos = new int[param.entries.size][];
		param.phase2EntryWidth = new int[param.entries.size];
		
		/*
		 * <p>确定移屏的相关参数. 移屏都是摄像机在动.
		 * <p>移屏, 分两个阶段; 第一是摄像机在原房间摆正, 第二是摄像机从原房间移向目标房间.
		 * 如果关卡设计合理的话, 是没有第一阶段的.
		 * </p>
		 */
		RockChronicle app = RockChronicle.INSTANCE;
		Room srcRoom = gate.srcRoom, destRoom = gate.destRoom;
		
		// TODO 如果 destRoom 来自其它的区域, 还需要修改数据
		
		Vector2 pos = new Vector2(camera.position.x - app.width / 2.0f,
				camera.position.y - app.height / 2.0f); // 相对于当前房间
		param.currentPos.set(pos);
		switch (gate.direction) {
		case DIRECTION_LEFT: 
		case DIRECTION_RIGHT: {
			
			float dy;
			PHASE1 : { // 阶段一: 定 y 轴
				// 下面计算镜头的框的底边纵坐标的范围
				int offsety = srcRoom.offsety;
				int y1start = 0,
						y1end = y1start + srcRoom.height - (int) camera.viewportHeight;
				if (y1start == y1end) {
					param.phase1Pos.set(pos.x, dy = y1start);
					break PHASE1;
				}
				
				int y2start = destRoom.offsety + gate.offsetYOfRegion - offsety,
						y2end = y2start + destRoom.height - (int) camera.viewportHeight;
				if (y2start == y2end) {
					param.phase1Pos.set(pos.x, dy = y2start);
					break PHASE1;
				}
				
				if (pos.y >= y1start && pos.y <= y1end && pos.y >= y2start && pos.y <= y2end) {
					param.phase1Pos.set(pos.x, dy = pos.y);
					break PHASE1;
				}
				if (pos.y < y1start || pos.y < y2start) {
					param.phase1Pos.set(pos.x, dy = Math.max(y1start, y2start));
					break PHASE1;
				}
				if (pos.y > y1end || pos.y > y2end) {
					param.phase1Pos.set(pos.x, dy = Math.min(y1end, y2end));
					break PHASE1;
				}
				// 如果运行到这里, 那就是出错了
				throw new IllegalStateException("移屏计算出错: 阶段一");
			}
			
			{ // 阶段二: 定 x 轴
				int offsetx = srcRoom.offsetx;
				if (gate.direction == DIRECTION_RIGHT) {
					param.phase2Pos.set(destRoom.offsetx - gate.offsetXOfRegion - offsetx, dy);
					param.phase2CameraWidth = param.phase2Pos.x - param.phase1Pos.x;
					
					for (int i = 0; i < param.entries.size; i++) {
						Foe entry = param.entries.get(i);
						Box box = entry.getBoxes()[0];
						int pxLeft = box.posX;
						int pAnchorX = box.anchorX;
						int pAnchorY = box.anchorY;
						// 向右移屏后, 角色左边需要等于 0.5
						int pxDestLeft = block2P(destRoom.offsetx - gate.offsetXOfRegion - srcRoom.offsetx + 0.5f);
						
						param.phase2EntryWidth[i] = pxDestLeft - pxLeft;
						param.entriesPos[i] = new int[]{ pAnchorX + param.phase2EntryWidth[i], pAnchorY };
					}
				} else {
					param.phase2Pos.set(destRoom.offsetx - gate.offsetXOfRegion + destRoom.width
							- camera.viewportWidth - offsetx, dy);
					param.phase2CameraWidth = param.phase1Pos.x - param.phase2Pos.x;
					
					for (int i = 0; i < param.entries.size; i++) {
						Foe entry = param.entries.get(i);
						Box box = entry.getBoxes()[0];
						int pxRight = box.posX + box.posWidth; // 相对于 srcRoom
						int pAnchorX = box.anchorX;
						int pAnchorY = box.anchorY;
						// 向左移屏后, 角色右边需要等于目标房间的宽 - 0.5f
						int pxDestRight = block2P(-0.5f);
						
						param.phase2EntryWidth[i] = pxRight - pxDestRight; // 保持正数
						param.entriesPos[i] = new int[]{ pAnchorX - param.phase2EntryWidth[i], pAnchorY };
					}
				}
			}
		} break;
		
		case DIRECTION_BOTTOM:
		case DIRECTION_TOP: {
			float dx;
			PHASE1 : { // 阶段一: 定 x 轴
				// 下面计算镜头的框的底边纵坐标的范围
				int offsetx = srcRoom.offsetx;
				int x1start = 0,
						x1end = x1start + srcRoom.width - (int) camera.viewportWidth;
				if (x1start == x1end) {
					param.phase1Pos.set(dx = x1start, pos.y);
					break PHASE1;
				}
				
				int x2start = destRoom.offsetx + gate.offsetXOfRegion - offsetx,
						x2end = x2start + destRoom.height - (int) camera.viewportWidth;
				if (x2start == x2end) {
					param.phase1Pos.set(dx = x2start, pos.y);
					break PHASE1;
				}
				
				if (pos.x >= x1start && pos.x <= x1end && pos.x >= x2start && pos.x <= x2end) {
					param.phase1Pos.set(dx = pos.x, pos.y);
					break PHASE1;
				}
				if (pos.x < x1start || pos.x < x2start) {
					param.phase1Pos.set(dx = Math.max(x1start, x2start), pos.y);
					break PHASE1;
				}
				if (pos.x > x1end || pos.x > x2end) {
					param.phase1Pos.set(dx = Math.min(x1end, x2end), pos.y);
					break PHASE1;
				}
				// 如果运行到这里, 那就是出错了
				throw new IllegalStateException("移屏计算出错: 阶段一");
			}
			
			{ // 阶段二: 定 y 轴
				int offsety = srcRoom.offsety;
				if (gate.direction == DIRECTION_BOTTOM) { // 向下
					param.phase2Pos.set(dx, destRoom.offsety - gate.offsetYOfRegion
							+ destRoom.height - camera.viewportHeight - offsety);
					param.phase2CameraWidth = param.phase1Pos.y - param.phase2Pos.y;
					
					for (int i = 0; i < param.entries.size; i++) {
						Foe entry = param.entries.get(i);
						Box box = entry.getBoxes()[0];
						int pyTop = box.posY + box.posHeight;
						int pAnchorX = box.anchorX;
						int pAnchorY = box.anchorY;
						// 向下移屏后, 角色上边需要等于目标房间高 - 0.1
						int pxDestTop = block2P(-0.1f);
						
						param.phase2EntryWidth[i] = pyTop - pxDestTop;
						param.entriesPos[i] = new int[] {pAnchorX, pAnchorY - param.phase2EntryWidth[i]};
					}
				} else {
					param.phase2Pos.set(dx, destRoom.offsety - gate.offsetYOfRegion - offsety);
					param.phase2CameraWidth = param.phase2Pos.y - param.phase1Pos.y;
					
					for (int i = 0; i < param.entries.size; i++) {
						Foe entry = param.entries.get(i);
						Box box = entry.getBoxes()[0];
						int pyBottom = box.posY; // 相对于 srcRoom
						int pAnchorX = box.anchorX;
						int pAnchorY = box.anchorY;
						// 向下移屏后, 角色上边需要等于目标房间高 - 0.1
						int pxDestBottom = block2P(srcRoom.height + 0.1f);

						// 向上移屏后, 角色下边需要等于 0.1
						param.phase2EntryWidth[i] = pxDestBottom - pyBottom;
						param.entriesPos[i] = new int[] {pAnchorX, pAnchorY + param.phase2EntryWidth[i]};
					}
				}
			}
		} break;
		
		default:
			break;
		}

		runtime.roomShiftingStarted();
		param.phase = 1;
	}
	
	public void tickShift() {
		switch (param.phase) {
		case 1:
			shiftPhase1();
			break;
		case 2:
			shiftPhase2();
			break;
		case 3:
			// 后续
			shiftPhase3();
			break;

		default:
//			throw new IllegalStateException("移屏阶段数据出错: " + param.phase);
		}
	}

	/*
	 * 镜头移动 1 步时间
	 * 移屏速度现在是定死的, 每秒 40 格 (每步 0.333333 格).
	 */
	private void shiftPhase1() {
		if (doShiftRoom(param.phase1Pos)) {
			param.phase = 2;
		}
	}
	
	/**
	 * 阶段二: 镜头移动 + 角色移动
	 */
	private void shiftPhase2() {
		if (doShiftRoom(param.phase2Pos)) {
			param.phase = 3; // 移屏全部结束, 需要设置新的房间
		}
	}
	
	private boolean doShiftRoom(Vector2 toPos) {
		boolean bx = param.currentPos.x != toPos.x;
		float delta = bx ? toPos.x - param.currentPos.x : toPos.y - param.currentPos.y;
		float step = 25 * Ticker.WORLD_STEP; // 上面的每秒 25 格, 单位: 格
		boolean finished = false;
		
		if (delta > 0) {
			if (delta < step) {
				param.currentPos.set(toPos);
				finished = true;
				delta = 0;
			} else {
				if (bx) {
					param.currentPos.x += step;
				} else {
					param.currentPos.y += step;
				}
				delta += step;
			}
		} else if (delta < 0) {
			if (delta > -step) {
				param.currentPos.set(toPos);
				finished = true;
			} else {
				if (bx) {
					param.currentPos.x -= step;
				} else {
					param.currentPos.y -= step;
				}
				delta -= step;
			}
		} else {
			// 该阶段直接结束
			finished = true;
		}
		
		// 更新角色位置
		if (param.phase == 2) {
			float remain = Math.abs(delta) / param.phase2CameraWidth;
			for (int i = 0; i < param.entries.size; i++) {
				Foe entry = param.entries.get(i);
				Box box = entry.getBoxes()[0];
				
				if (bx) {
					int px;
					if (delta >= 0) {
						px = (int) (param.entriesPos[i][0] - remain * param.phase2EntryWidth[i]);
					} else {
						px = (int) (param.entriesPos[i][0] + remain * param.phase2EntryWidth[i]);
					}
					box.setAnchorX(px);
				} else {
					int py;
					if (delta >= 0) {
						py = (int) (param.entriesPos[i][1] - remain * param.phase2EntryWidth[i]);
					} else {
						py = (int) (param.entriesPos[i][1] + remain * param.phase2EntryWidth[i]);
					}
					box.setAnchorY(py);
				}
			}
		}
		
		// 更新镜头位置
		RockChronicle app = RockChronicle.INSTANCE;
		camera.position.x = app.width / 2.0f + param.currentPos.x;
		camera.position.y = app.height / 2.0f + param.currentPos.y;
		
		return finished;
	}

	private void shiftPhase3() {
		RockChronicle app = RockChronicle.INSTANCE;
		GameRuntime runtime = app.runtime;
		
		// 清空 entry
		IntSet set = new IntSet(param.entries.size);
		param.entries.forEach(ch -> set.add(ch.id));
		
		Foe[] foes = runtime.foes.toArray(Foe.class);
		for (int i = 0; i < foes.length; i++) {
			Foe foe = foes[i];
			if (!set.contains(foe.id)) {
				runtime.destroyFoeNow(foe);
			}
		}
		
		Room srcRoom = param.gate.srcRoom;
		Room destRoom = param.gate.destRoom;
		runtime.setCurrentRoom(destRoom);
		
		// 设置角色位置
		int pDeltaX = block2P(destRoom.offsetx - srcRoom.offsetx);
		int pDeltaY = block2P(destRoom.offsety - srcRoom.offsety);
		if (srcRoom.region != destRoom.region) {
			Gate gate = param.gate;
			pDeltaX -= block2P(gate.offsetXOfRegion);
			pDeltaY -= block2P(gate.offsetYOfRegion);
		}
		for (int i = 0; i < param.entries.size; i++) {
			Foe entry = param.entries.get(i);
			Box box = entry.getBoxes()[0];
			box.addAnchorX(-pDeltaX);
			box.addAnchorY(-pDeltaY);
		}
		
		// 镜头位置的更新将交给 SceneDesigner
		camera.position.x = app.width / 2.0f;
		camera.position.y = app.height / 2.0f;
		
		// 结束
		runtime.roomShiftingFinished();
		this.param = null;
	}
	
	/**
	 * 询问现在是否在房间切换中
	 * @return
	 */
	public boolean durationShift() {
		return param != null;
	}

	/* **********
	 *   镜头   *
	 ********** */
	
	public void updateCamera() {
		if (!durationShift()) {
			// 根据玩家位置改变镜头
			Foe entry = runtime.player1;
			if (entry != null) {
				Box box = entry.getBoxes()[0];
				centerPoint.x = p2block(box.anchorX);
				centerPoint.y = p2block(box.anchorY + 1);
				
				Room curRoom = runtime.getCurrentRoom();
				float halfWidth = camera.viewportWidth / 2;
				float halfHeight = camera.viewportHeight / 2;
				
				float xstart = halfWidth;
				float xend = curRoom.width - halfWidth;
				float ystart = halfHeight;
				float yend = curRoom.height - halfHeight;
				if (centerPoint.x < xstart) {
					centerPoint.x = xstart;
				} else if (centerPoint.x > xend) {
					centerPoint.x = xend;
				}
				if (centerPoint.y < ystart) {
					centerPoint.y = ystart;
				} else if (centerPoint.y > yend) {
					centerPoint.y = yend;
				}
				camera.position.set(centerPoint, 0);
			}
		}
		
		// 更新 MapRender
		fixMapRender();
	}

	/**
	 * 在界面启动时, 和每帧
	 * 修正 MapRender 渲染的瓦片地图的位置
	 */
	private void fixMapRender() {
		// 当前区域
		Room curRoom = runtime.getCurrentRoom();
		
		mainRender.setView(camera);
		Rectangle viewBounds = mainRender.getViewBounds();
		viewBounds.setX(viewBounds.x + curRoom.offsetx);
		viewBounds.setY(viewBounds.y + curRoom.offsety);
		
		float ox = -camera.position.x + camera.viewportWidth / 2.0f;
		float oy = -camera.position.y + camera.viewportHeight / 2.0f;
		float dx = ox - curRoom.offsetx;
		float dy = oy - curRoom.offsety;
		
		mainRender.getBatch().getProjectionMatrix()
			.translate (dx, dy, 0);
		
		// 其它区域
		for (int i = 0; i < renders.size; i++) {
			MapRendererEntry entry = renders.get(i);
			OrthogonalTiledMapRenderer render = entry.render;
			Gate gate = entry.gate;
			
			render.setView(camera);
			Rectangle viewBounds0 = render.getViewBounds();
			viewBounds0.setX(viewBounds0.x + curRoom.offsetx + gate.offsetXOfRegion);
			viewBounds0.setY(viewBounds0.y + curRoom.offsety + gate.offsetYOfRegion);
			
			render.getBatch().getProjectionMatrix()
				.translate(ox - curRoom.offsetx - gate.offsetXOfRegion,
						oy - curRoom.offsety - gate.offsetYOfRegion,
						0);
		}
	}
	
	/**
	 * TODO 这部分渲染工作需要移到 PainterManager 里面
	 */
	public void renderMap() {
		mainRender.render();
		
		for (int i = 0; i < renders.size; i++) {
			renders.get(i).render.render();
		}
	}
	
	/**
	 * @return 单位: p
	 */
	public int[] getCameraBound() {
		Rectangle viewBounds = mainRender.getViewBounds();
		Room curRoom = runtime.getCurrentRoom();
		int[] rets = new int[4];
		
		rets[0] = block2P(viewBounds.x - curRoom.offsetx);
		rets[1] = block2P(viewBounds.y - curRoom.offsety);
		rets[2] = block2P(viewBounds.width);
		rets[3] = block2P(viewBounds.height);
		
		return rets;
	}
	
	/* **********
	 *   生怪   *
	 ********** */
	/*
	 * 怪、大门连接点、道具等等
	 */

	private void putFoesIntoWorld(Room room) {
//		Region curRegion = room.region;
//		RegionBuilder regionBuilder = runtime.world.regionBuilder;
//		
		// 将场放入世界
		int length = room.fields.size;
//		System.out.println("--> SceneDesigner.putFoesIntoWorld()");
		for (int i = 0; i < length; i++) {
			FieldDef f = room.fields.get(i);
			
			Foe foe = foeBuilder.create(f.name, f.param);
			if (foe == null) {
				continue;
			}
			runtime.addFoe(foe);
		}
		
		// 将怪放入世界
		length = room.foes.size;
		for (int i = 0; i < length; i++) {
			FoeDef f = room.foes.get(i);
			
			Foe foe = foeBuilder.create(f.name, f.param);
			if (foe == null) {
				continue;
			}
			runtime.addFoe(foe);
		}
	}
	
}

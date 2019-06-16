package zdream.rockchronicle.platform.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.region.Gate;
import zdream.rockchronicle.platform.region.Room;

/**
 * <p>场景设计师
 * <p>对现在世界进入的房间, 将所有的 {@link OrthogonalTiledMapRenderer} 摆放至指定位置;
 * 管理镜头位置.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-19 (created)
 *   2019-05-19 (last modified)
 */
public class SceneDesigner {
	
	private GameRuntime runtime;
	
	/**
	 * 摄像机
	 */
	public OrthographicCamera camera = new OrthographicCamera();
	
	/**
	 * 摄像机瞄准的位置, 以当前房间为参照
	 */
	public Vector2 centerPoint = new Vector2();
	
	/**
	 * 渲染当前地图的, 因此会常驻且不会被回收
	 */
	public OrthogonalTiledMapRenderer mainRender;
	
	/**
	 * 地图的渲染列表, 渲染其它区域的
	 */
	public Array<MapRendererEntry> renders = new Array<>();
	
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
	
	public void onRoomUpdated() {
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
	}
	
	public void updateCamera() {
		if (!runtime.shift.durationShift()) {
			// 根据玩家位置改变镜头
			CharacterEntry entry = runtime.levelWorld.getPlayer1();
			if (entry != null) {
				Rectangle rect = entry.getBoxModule().getBox().getPosition();
				rect.getCenter(centerPoint);
				
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
}

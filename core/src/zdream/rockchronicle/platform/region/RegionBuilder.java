package zdream.rockchronicle.platform.region;

import static java.util.Objects.requireNonNull;
import static zdream.rockchronicle.platform.region.Terrains.terrainCode;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.utils.FilePathUtil;

public class RegionBuilder {

	/**
	 * 处理从文件中读取 tmx 数据的类
	 */
	private TmxMapLoader localLoader = new TmxMapLoader(new LocalFileHandleResolver());
	private JsonReader jreader = new JsonReader();
	
	public final int defRoomWidth = 25;
	public final int defRoomHeight = 14;
	
	public static final String TERRAIN_ONLY_LAYER_NAME = "TerrainOnly";
	
	/**
	 * 地形的纹理. 见文件 res/terrain/terrain.png
	 */
	private Texture terrainTexture;
	public static final String TERRAIN_PATH =
			Paths.get("res", "level", "terrain", "terrain.png").toString();

	public Region build(RegionDef def) {
		requireNonNull(def, "def == null");
		Region region = new Region();
		
		// tmx
		String path = def.tmxPath;
		region.tmx = localLoader.load(FilePathUtil.relativeFileHandle(def.basePath, path).path());
		
		initSymbolMap(region);
		// TODO
		
		
		return region;
	}
	
	public Region build(String basePath) {
		RegionDef def = new RegionDef(basePath);
		
		JsonValue json = jreader.parse(Gdx.files.local(basePath));
		def.tmxPath = json.getString("tmxPath");
		
		return build(def);
	}
	
	/**
	 * 创建仅能看到地形符号的关卡地图, 其它图层均被隐藏
	 * @param basePath
	 * @return
	 */
	public Region buildForTerrainOnly(String basePath) {
		RegionDef def = new RegionDef(basePath);
		
		JsonValue json = jreader.parse(Gdx.files.local(basePath));
		def.tmxPath = json.getString("tmxPath");
		
		Region r = build(def);
		createTerrainOnlyLayer(r);

		return r;
	}
	
	/* **********
	 *  初始化  *
	 ********** */
	
	/**
	 * <p>初始化 Symbol 层
	 * <p>tiledMap 中默认有 symbol 这个图块层, 而 symbol 层是不能被 render 的.
	 * 这里设置为不可见
	 * </p>
	 */
	void initSymbolMap(Region region) {
		TiledMap t = region.tmx;
		MapLayer l = t.getLayers().get("Symbol");
		l.setVisible(false);
		
		// 下面用于检查 Symbol 层的合规性
		if (!(l instanceof TiledMapTileLayer)) {
			throw new RuntimeException("Symbol 层必须是 Tiled 层");
		}
		TiledMapTileLayer layer = (TiledMapTileLayer) l;
		ArrayList<Rectangle> rects = new ArrayList<>();
		
		final int width = layer.getWidth(), height = layer.getHeight();
		// 出生点位
		int spawnx = -1, spawny = -1;
		
		/*
		 * 寻找思路: 寻找左下角标识, 因为每个房间 (room) 的大小是 25x14 的整数倍,
		 * 由此寻找剩余右上、右下、左下的标识.
		 */
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Cell cell = layer.getCell(x, y);
				if (cell == null) {
					continue;
				}
				
				switch (cell.getTile().getProperties().get("type").toString()) {
				case "room-corner3": {
					// 找到 room 左下角
					Rectangle rect = new Rectangle(x, y, 0, 0);
					checkRoom(x, y, rect, layer);
					// TODO 缺少检查 rect
					rects.add(rect);
				} break;
				case "player-spawn":
					spawnx = x;
					spawny = y;
					break;

				default:
					break;
				}
			}
		}
		
		if (spawnx == -1) {
			throw new RuntimeException("出生点位没有确定");
		}
		
		Room[] rooms = region.rooms = new Room[rects.size()];
		boolean spawnCheck = false;
		float spawnxf = spawnx + 0.5f, spawnyf = spawny + 0.5f;
		
		for (int i = 0; i < rooms.length; i++) {
			// 将 Room 信息打包
			Room r = new Room();
			Rectangle rect = rects.get(i);
			
			r.index = i;
			r.offsetx = (int) rect.x;
			r.offsety = (int) rect.y;
			r.width = (int) rect.width;
			r.height = (int) rect.height;
			
			rooms[i] = r;
			
			// 判断出生点在哪个 Room 当中
			if (spawnCheck) {
				continue;
			}
			
			if (rect.contains(spawnxf, spawnyf)) {
				spawnCheck = true;
				region.spawnRoom = i;
				region.spawnx = spawnx - r.offsetx;
				region.spawny = spawny - r.offsety;
			}
			
			// 读取地形数据
			readTerrains(r, t);
		}
		
		if (region.spawnRoom == -1) {
			throw new RuntimeException("出生点位没有确定");
		}
	}
	
	private void checkRoom(int x, int y, Rectangle rect, TiledMapTileLayer layer) {
		final int xstep = this.defRoomWidth, ystep = this.defRoomHeight;
		final int width = layer.getWidth(), height = layer.getHeight();
		
		// 右边界, 上边界
		int xx = x + xstep - 1, yy = y + ystep - 1;
		
		// 找 room 的另三个角
		// 右下角
		for (; ; xx += xstep) {
			if (xx >= width) {
				throw new RuntimeException("左下角点在 [" + x + "," + y + "] 的 Room 没有对应的右边界");
			}
			Cell cell = layer.getCell(xx, y);
			if (cell == null) {
				continue;
			}
			if ("room-corner4".equals(cell.getTile().getProperties().get("type"))) {
				break; // 已确定 xx
			}
		}
		
		// 左上角
		for (; ; yy += xstep) {
			if (yy >= height) {
				throw new RuntimeException("左下角点在 [" + x + "," + y + "] 的 Room 没有对应的上边界");
			}
			Cell cell = layer.getCell(x, yy);
			if (cell == null) {
				continue;
			}
			if ("room-corner1".equals(cell.getTile().getProperties().get("type"))) {
				break; // 已确定 yy
			}
		}
		
		// 检查右上角
		Cell cell = layer.getCell(xx, yy);
		if (cell == null || !"room-corner2".equals(cell.getTile().getProperties().get("type"))) {
			throw new RuntimeException("左下角点在 [" + x + "," + y + "] 的 Room 没有对应的右上角点");
		}
		
		rect.setWidth(xx - x + 1);
		rect.setHeight(yy - y + 1);
	}
	
	/**
	 * 为每一个确定的房间读取地形数据
	 * @param r
	 * @param tiledMap
	 */
	private void readTerrains(Room r, TiledMap tiledMap) {
		MapLayers ls = tiledMap.getLayers();
		final int len = ls.getCount();
		
		final int width = r.width, height = r.height;
		r.terrains = new int[width][height];
		
		for (int i = 0; i < len; i++) {
			MapLayer l = ls.get(i);
			
			if (l.getName().equals("Symbol")) {
				continue;
			}
			
			if (!(l instanceof TiledMapTileLayer)) {
				continue;
			}
			
			TiledMapTileLayer layer = (TiledMapTileLayer) l;
			
			for (int xx = 0; xx < width; xx++) { // xx 为在该房间中的相对 x
				int x = r.offsetx + xx; // x 是绝对值
				for (int yy = 0; yy < height; yy++) { // yy 为在该房间中的相对 y
					int y = r.offsety + yy; // y 是绝对值
					
					Cell cell = layer.getCell(x, y);
					if (cell == null) {
						continue;
					}
					
					MapProperties pro = cell.getTile().getProperties();
					if (pro == null) {
						continue;
					}
					
					Object o = pro.get("terrain");
					if (o == null) {
						continue;
					}
					
					r.terrains[xx][yy] = terrainCode(o.toString());
				}
			}
		}
	}
	
	/**
	 * 为 Region 的 TMX 地图创建一个图层, 只显示地形的图层.
	 * 然后该图层置于顶层, 原来名为 terrain 的图层不显示.
	 */
	private void createTerrainOnlyLayer(Region r) {
		TiledMap tmx = r.tmx;
		
		// 先将 texture data 放进去
		if (terrainTexture == null) {
			terrainTexture = new Texture(Gdx.files.local(TERRAIN_PATH), false);
			terrainTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		}
		requireNonNull(terrainTexture);
		
		// 获取最大的 firstgid
		int firstgid = 1;
		for (Iterator<TiledMapTileSet> it0 = tmx.getTileSets().iterator(); it0.hasNext();) {
			TiledMapTileSet set = it0.next();
			for (Iterator<TiledMapTile> it1 = set.iterator(); it1.hasNext();) {
				TiledMapTile tile = it1.next();
				if (tile.getId() >= firstgid) {
					firstgid = tile.getId() + 1;
				}
			}
		}
		
		// Tiled Set
		TiledMapTileSet tileset = new TiledMapTileSet();
		tileset.setName("terrain");
		tileset.getProperties().put("firstgid", firstgid);
		
		{
			MapProperties props = tileset.getProperties();
//			props.put("imagesource", imageSource);
			
			// TODO 常量 : 每一个块的长和高为 24 像素
			int tilewidth = 24, tileheight = 24;
			int width = terrainTexture.getWidth(), height = terrainTexture.getHeight();
			props.put("imagewidth", width);
			props.put("imageheight", height);
			props.put("tilewidth", tilewidth);
			props.put("tileheight", tileheight);
			props.put("margin", 0);
			props.put("spacing", 0);
			
			int stopWidth = width - tilewidth;
			int stopHeight = height - tileheight;

			int id = firstgid;

			for (int y = 0; y <= stopHeight; y += tileheight) {
				for (int x = 0; x <= stopWidth; x += tilewidth) {
					TextureRegion tileRegion = new TextureRegion(terrainTexture, x, y, tilewidth, tileheight);
					TiledMapTile tile = new StaticTiledMapTile(tileRegion);
					tile.setId(id);
					tile.setOffsetX(0);
					tile.setOffsetY(0);
					tileset.putTile(id++, tile);
				}
			}
		}
		tmx.getTileSets().addTileSet(tileset);
		
		// Tiled Layer
		TiledMapTileLayer srcLayer = (TiledMapTileLayer) tmx.getLayers().get("Terrain");
		TiledMapTileLayer layer = new TiledMapTileLayer(
				srcLayer.getWidth(), srcLayer.getHeight(),
				(int) srcLayer.getTileWidth(), (int) srcLayer.getTileHeight());
		layer.setName(TERRAIN_ONLY_LAYER_NAME);
		
		for (int x = 0; x < srcLayer.getWidth(); x++) {
			for (int y = 0; y < srcLayer.getHeight(); y++) {
				Cell srcCell = srcLayer.getCell(x, y);
				if (srcCell == null) {
					continue;
				}
				MapProperties pro = srcCell.getTile().getProperties();
				if (pro == null) {
					continue;
				}
				
				Object o = pro.get("terrain");
				if (o == null) {
					continue;
				}
				int terrainCode = terrainCode(o.toString());
				
				Cell destCell = new Cell();
				destCell.setTile(tileset.getTile(firstgid + terrainCode));
				layer.setCell(x, y, destCell);
			}
		}
		layer.setVisible(true);
		srcLayer.setVisible(false);
		tmx.getLayers().add(layer);
		
	}
	
}

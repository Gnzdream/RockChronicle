package zdream.rockchronicle.platform.region;

import static java.util.Objects.requireNonNull;
import static zdream.rockchronicle.platform.region.Terrains.terrainCode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
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
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.utils.FilePathUtil;
import zdream.rockchronicle.utils.JsonUtils;

public class RegionBuilder {
	
	/* **********
	 *  初始化  *
	 ********** */
	
	private final ObjectMap<String, RegionDef> levels = new ObjectMap<>();
	
	public void init() {
		rescan(Paths.get("res", "level"), new String[] {"symbol", "terrain"});
		
		Gdx.app.log("RegionBuilder", String.format("扫描关卡共 %d 个", levels.size));
	}
	
	/**
	 * <p>初始化区域创建工具, 加载所有的关卡相关的初始化 json 文件
	 * <p>所有的相关文件在文件夹 [dir] 中的文件的一级目录中
	 * (满足 [dir]/?/?.json), 且 json 文件包含的说明有:
	 * <li>name : (string, 必需) 说明人物名称
	 * <li>class : (string, 必需) 说明人物创建的全类名
	 * <li>modules : (object{string : string}, 不必需)
	 *     如果模块需要按照某些模板来创建的话, 则将 key=模块属性 (比如 "sprite", "control" 等)
	 *     value=模块模板名称 放入 modules 中.
	 * </li>
	 * @param dir
	 *   搜索的路径
	 * @param ignores
	 *   忽略的一级目录名称
	 */
	private void rescan(Path path, String[] ignores) {
		FileHandle f = Gdx.files.local(path.toString());
		
		if (!f.exists() || !f.isDirectory()) {
			throw new IllegalStateException(path + " 无法读取角色创建文件信息");
		}
		
		FileHandle[] children = f.list();
		for (int i = 0; i < children.length; i++) {
			FileHandle child = children[i];
			
			if (!child.isDirectory()) {
				continue;
			}
			// 查看是否在忽略名单中
			String fname = child.name();
			boolean ignore = false;
			for (int j = 0; j < ignores.length; j++) {
				if (fname.equals(ignores[j])) {
					ignore = true;
					break;
				}
			}
			if (ignore) continue;
			
			// 扫描
			FileHandle[] files = child.list();
			for (int j = 0; j < files.length; j++) {
				FileHandle fjson = files[j];
				
				if (fjson.isDirectory()) {
					continue;
				}
				
				String ext = fjson.extension();
				if (!"json".equals(ext.toLowerCase())) {
					continue;
				}
				
				try {
					JsonValue v = jreader.parse(fjson);
					
					RegionDef def = new RegionDef();
					def.name = v.getString("name");
					def.path = fjson.path();
					def.data = v.toJson(OutputType.minimal);
					
					levels.put(def.name, def);
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean has(String name) {
		return levels.containsKey(name);
	}


	/* **********
	 * 构造部分 *
	 ********** */

	/**
	 * 处理从文件中读取 tmx 数据的类
	 */
	private TmxMapLoader localLoader = new TmxMapLoader(new LocalFileHandleResolver());
	private JsonReader jreader = JsonUtils.jreader;
	
	public final int defRoomWidth = 25;
	public final int defRoomHeight = 14;
	
	public static final String TERRAIN_ONLY_LAYER_NAME = "TerrainOnly";
	
	/**
	 * 地形的纹理. 见文件 res/terrain/terrain.png
	 */
	private Texture terrainTexture;
	public static final String TERRAIN_PATH =
			Paths.get("res", "level", "terrain", "terrain.png").toString();
	
	public Region build(String name) {
		RegionDef def = levels.get(name);
		if (def == null) {
			throw new NullPointerException(String.format("不存在 %s 的关卡数据", name));
		}
		
		RegionBundle bundle = new RegionBundle(def);
		
		return build(bundle);
	}
	
	/**
	 * 创建仅能看到地形符号的关卡地图, 其它图层均被隐藏
	 * @param basePath
	 * @return
	 */
	public Region buildForTerrainOnly(String name) {
		RegionDef def = levels.get(name);
		if (def == null) {
			throw new NullPointerException(String.format("不存在 %s 的关卡数据", name));
		}
		
		RegionBundle bundle = new RegionBundle(def);
		Region r = build(bundle);
		createTerrainOnlyLayer(r);

		return r;
	}

	private Region build(RegionBundle bundle) {
		JsonValue json = jreader.parse(Gdx.files.local(bundle.def.path));
		parseRegionDefJson(bundle, json);
		
		// tmx
		String path = bundle.tmxPath;
		bundle.region.tmx = localLoader.load(FilePathUtil.relativeFileHandle(bundle.def.path, path).path());
		
		scanSymbolTiledSet(bundle);
		
		initSymbolMap(bundle);
		initFieldMap(bundle);
		// 图与图之间进行衔接的门
		initGate(bundle);
		
		return bundle.region;
	}

	/* **********
	 *  初始化  *
	 ********** */

	/**
	 * 扫描图块 symbol
	 * @param bundle
	 */
	private void scanSymbolTiledSet(RegionBundle bundle) {
		TiledMap t = bundle.region.tmx;
		
		Iterator<TiledMapTileSet> it = t.getTileSets().iterator();
		int startId = 1;
		boolean seek = false;
		for (; it.hasNext();) {
			TiledMapTileSet set = it.next();
			if ("symbol".equals(set.getName())) {
				seek = true;
				break;
			} else {
				startId += set.size();
			}
		}
		
		if (!seek) {
			throw new IllegalArgumentException(String.format("无法解析 %s 文件的地形数据: 没有 symbol 层", bundle.tmxPath));
		}
		
		bundle.setSymbolStartId(startId);
	}
	
	private void parseRegionDefJson(RegionBundle bundle, JsonValue json) {
		bundle.tmxPath = json.getString("tmxPath");
		
		// fields
		JsonValue afields = json.get("fields");
		if (afields != null && afields.size > 0) {
			for (JsonValue ofieldPair = afields.child; ofieldPair != null; ofieldPair = ofieldPair.next) {
				String key = ofieldPair.getString("key");
				JsonValue param = ofieldPair.get("param");
				bundle.fields.put(key, param);
			}
		}
	}
	
	/**
	 * <p>初始化 Symbol 层
	 * <p>tiledMap 中默认有 "Symbol" 这个图块层, 而 Symbol 层是不能被 render 的.
	 * 这里设置为不可见
	 * </p>
	 */
	void initSymbolMap(RegionBundle bundle) {
		TiledMap t = bundle.region.tmx;
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
				
				int id = cell.getTile().getId();
				if (id == bundle.symbolCorner3Id) {
					// 找到 room 左下角
					Rectangle rect = new Rectangle(x, y, 0, 0);
					checkRoom(x, y, rect, layer, bundle);
					// TODO 缺少检查 rect
					rects.add(rect);
				} else if (id == bundle.symbolSpawnId) {
					spawnx = x;
					spawny = y;
				}
			}
		}
		
		if (spawnx == -1) {
			throw new RuntimeException("出生点位没有确定");
		}
		
		Room[] rooms = bundle.region.rooms = new Room[rects.size()];
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
				bundle.region.spawnRoom = i;
				bundle.region.spawnx = spawnx - r.offsetx;
				bundle.region.spawny = spawny - r.offsety;
			}
			
			// 读取地形数据
			readTerrains(r, t);
		}
		
		if (bundle.region.spawnRoom == -1) {
			throw new RuntimeException("出生点位没有确定");
		}
	}
	
	private void checkRoom(int x, int y, Rectangle rect, TiledMapTileLayer layer, RegionBundle bundle) {
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
			if (cell.getTile().getId() == bundle.symbolCorner4Id) {
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
			if (cell.getTile().getId() == bundle.symbolCorner1Id) {
				break; // 已确定 yy
			}
		}
		
		// 检查右上角
		Cell cell = layer.getCell(xx, yy);
		if (cell == null || !(cell.getTile().getId() == bundle.symbolCorner2Id)) {
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
	 * 然后该图层置于顶层, 原来名为 "Terrain" 的图层不显示.
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
	
	/**
	 * <p>按照 TMX 文件内容所述, 对 "Field" 层进行解析.
	 * <p>Field 层在 TMX 文件中被定义成一个对象层, 它描述了所有在这个关卡中的场的位置及大小.
	 * </p>
	 * @param region
	 */
	private void initFieldMap(RegionBundle bundle) {
		TiledMap tmx = bundle.region.tmx;
		MapLayer l = tmx.getLayers().get("Fields");
		if (l == null) {
			return;
		}
		l.setVisible(false);
		
		MapObjects objs = l.getObjects();
		final int size = objs.getCount();
		for (int i = 0; i < size; i++) {
			MapObject obj = objs.get(i);
			String name = obj.getName();
			
			JsonValue param = bundle.fields.get(name);
			if (param == null) {
				continue;
			}
			
			if (obj instanceof RectangleMapObject) {
				RectangleMapObject robj = (RectangleMapObject) obj;
				Rectangle rect = robj.getRectangle();
				
				float x = rect.x / Config.INSTANCE.blockWidth;
				float y = rect.y / Config.INSTANCE.blockHeight;
				float w = rect.width / Config.INSTANCE.blockWidth;
				float h = rect.height / Config.INSTANCE.blockHeight;
				
				// 确定该场属于哪个房间
				Room[] rooms = bundle.region.rooms;
				for (int j = 0; j < rooms.length; j++) {
					Room room = rooms[j];
					if (room.overlaps(x, y, w, h)) {
						Field f = createFieldForRoom(room, x, y, w, h, param);
						room.fields.add(f);
					}
				}
			}
		}
	}
	
	/**
	 * 甄别并创建大门的数据
	 * @param bundle
	 */
	private void initGate(RegionBundle bundle) {
		Room[] rooms = bundle.region.rooms;
		
		for (int i = 0; i < rooms.length; i++) {
			Room room = rooms[i];
			int xstart = room.offsetx; // 看左边界
			int xend = xstart + room.width - 1;
			int ystart = room.offsety;
			int yend = ystart + room.height - 1;
			
			// top
			// TODO 由于要看梯子, 所以这里省略
			
			for (int j = i + 1; j < rooms.length; j++) {
				Room room2 = rooms[j];
				
				int xstart2 = room2.offsetx;
				int xend2 = xstart2 + room2.width - 1;
				int ystart2 = room2.offsety;
				int yend2 = ystart2 + room2.height - 1;
				
				if (xstart - 1 == xend2) {
					if (ystart2 < yend && yend2 > ystart) { // room 的左边界接壤 room2
						createGateLeft(room, room2, bundle);
					}
				} else if (xend + 1 == xstart2) {
					if (ystart2 < yend && yend2 > ystart) { // room 的右边界接壤 room2
						createGateLeft(room2, room, bundle);
					}
				} // else TODO 上下接壤
			}
		}
		
	}
	
	/**
	 * room1 的左边接壤 room2
	 * @param room1
	 * @param room2
	 * @param bundle
	 */
	private void createGateLeft(Room room1, Room room2, RegionBundle bundle) {
		System.out.println(String.format("房间 %d 左边接壤 %d", room1.index, room2.index));
		
		int offsety1 = room1.offsety;
		int offsety2 = room2.offsety;
		
		int ystart = Math.min(offsety1, offsety2);
		int yend = Math.min(offsety1 + room1.height - 1, offsety2 + room2.height - 1);
		System.out.println(String.format("y : [%d - %d]", ystart, yend));
		
		for (int y = ystart; y < yend; y++) {
			
		}
		
	}
	
	private Field createFieldForRoom(Room room, float x, float y, float w, float h, JsonValue param) {
		Field f = new Field();
		
		f.param = JsonUtils.clone(param);
		f.room = room.index;
		
		f.rect.x = x - room.offsetx;
		f.rect.y = y - room.offsety;
		f.rect.width = w;
		f.rect.height = h;
		
		f.name = f.param.getString("name");
		
		// 利用 param 中的 top: no_border 参数改写 rect 数值
		JsonValue obox = param.get("box");
		if (obox != null) {
			JsonValue oboxTop = obox.get("top");
			if (oboxTop != null) {
				String top = oboxTop.asString();
				if ("no_border".equals(top)) {
					int roomHeight = room.height;
					f.rect.height = roomHeight * 2 - f.rect.y;
				}
			}
		}
		
		// 写入 f.param
		obox = f.param.get("box");
		if (obox == null) {
			obox = new JsonValue(ValueType.object);
			f.param.addChild(obox);
		}
		JsonValue orect = new JsonValue(ValueType.object);
		obox.addChild("rect", orect);
		orect.addChild("x", new JsonValue(f.rect.x));
		orect.addChild("y", new JsonValue(f.rect.y));
		orect.addChild("width", new JsonValue(f.rect.width));
		orect.addChild("height", new JsonValue(f.rect.height));
		
		return f;
	}
	
}

package zdream.rockchronicle.platform.region;

import static java.util.Objects.requireNonNull;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * <p>在 {@link Region} 生成前的过渡产物,
 * 一个 RegionBundle 是一个 region 的 json 数据的替代物
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 *   2019-05-12 (last modified)
 */
class RegionBundle {
	
	public RegionBundle(RegionDef def) {
		requireNonNull(def, "def == null");
		this.def = def;
	}
	
	/**
	 * 该 region 所在的路径.
	 */
	public final RegionDef def;
	
	/**
	 * 属性 tmxPath.
	 * 以 {@link #basePath} 作为基础路径, basePath + tmxPath 获得 tmx 文件的路径.
	 */
	public String tmxPath;

	Region region = new Region();
	ObjectMap<String, JsonValue> fields = new ObjectMap<>();
	
	/*
	 * 辅助部分
	 */
	public void setSymbolStartId(final int id) {
		symbolCorner1Id = id;
		symbolCorner2Id = id + 1;
		symbolCorner3Id = id + 10;
		symbolCorner4Id = id + 11;
		
		symbolSpawnId = id + 2;
		symbolToTopId = id + 3;
		symbolDropId = id + 4;
		
		symbolToLeftId = id + 12;
		symbolToBottomId = id + 13;
		symbolToRightId = id + 14;
	}
	
	/**
	 * Symbol 图块的 ID
	 */
	public int symbolCorner1Id = -1,
			symbolCorner2Id = -1,
			symbolCorner3Id = -1,
			symbolCorner4Id = -1,
			symbolSpawnId = -1,
			symbolToTopId = -1,
			symbolToBottomId = -1,
			symbolToLeftId = -1,
			symbolToRightId = -1,
			symbolDropId = -1;

}

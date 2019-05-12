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
	
	public RegionBundle(String basePath) {
		requireNonNull(basePath, "basePath == null");
		this.basePath = basePath;
	}
	
	/**
	 * 该 region 所在的路径.
	 */
	public final String basePath;
	
	/**
	 * 属性 tmxPath.
	 * 以 {@link #basePath} 作为基础路径, basePath + tmxPath 获得 tmx 文件的路径.
	 */
	public String tmxPath;

	Region region = new Region();
	ObjectMap<String, JsonValue> fields = new ObjectMap<>();

}

package zdream.rockchronicle.platform.body;

import zdream.rockchronicle.platform.region.ITerrainStatic;

/**
 * 地形的 body 参数
 * 
 * @author Zdream
 * @date 2019-04-28
 */
public class TerrainParam extends BodyParam implements ITerrainStatic {

	public TerrainParam(int x, int y, int terrain) {
		super(BodyType.Terrain);
		this.x = x;
		this.y = y;
		this.terrain = terrain;
	}
	
	public int x, y;
	public int terrain;
	
	
	@Override
	public final boolean isTerrain() {
		return true;
	}
	
	@Override
	public final TerrainParam getAsTerrain() {
		return this;
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d)-%d", x, y, terrain);
	}

}

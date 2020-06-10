package zdream.rockchronicle.platform.body;

public class BodyParam {
	
	public final BodyType type;

	public BodyParam(BodyType type) {
		this.type = type;
	}
	
	/**
	 * 询问该 body 是地形吗
	 * @return
	 */
	public boolean isTerrain() {
		return false;
	}
	
	public TerrainParam getAsTerrain() {
		throw new IllegalStateException("类 " + this + " 不能转化成地形物体参数");
	}

}

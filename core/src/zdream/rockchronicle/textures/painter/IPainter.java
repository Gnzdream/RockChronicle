package zdream.rockchronicle.textures.painter;

/**
 * <p>绘画接口
 * <p>定义 zIndex:
 * 后景低于 -1000; 图块与地形为 -999 至 -1; 精灵为 1 至 500; 图标等为 1000 以上
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-17 (created)
 *   2019-05-17 (last modified)
 */
public interface IPainter {
	
	public void draw();

}

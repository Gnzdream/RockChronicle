package zdream.rockchronicle.core.input;

/**
 * <p>控制信号绑定方
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-17 (created)
 *   2019-05-17 (last modified)
 */
public interface IInputBindable {
	
	public void bindController(PlayerInput input);
	public void unbindController();

}

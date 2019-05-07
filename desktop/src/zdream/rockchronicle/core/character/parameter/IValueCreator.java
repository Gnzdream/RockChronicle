package zdream.rockchronicle.core.character.parameter;

/**
 * 数据的创建者
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-07 (create)
 */
public interface IValueCreator<T> {
	
	/**
	 * 获取创建的数据
	 * @return
	 */
	public T get();

}

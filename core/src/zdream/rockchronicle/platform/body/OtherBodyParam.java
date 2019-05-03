package zdream.rockchronicle.platform.body;

/**
 * 其它 body 参数
 * 
 * @author Zdream
 * @date 2019-04-28
 */
public class OtherBodyParam extends BodyParam {

	private OtherBodyParam() {
		super(BodyType.Other);
	}
	
	public static final OtherBodyParam INSTANCE = new OtherBodyParam();

}

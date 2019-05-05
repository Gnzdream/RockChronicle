package zdream.rockchronicle.core.input;

public interface IControlListener {
	
	public void onKeyPressed(int mapkey, PlayerInput in);

	public void onKeyReleased(int mapkey, PlayerInput in);

}

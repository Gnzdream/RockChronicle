package zdream.rockchronicle.sprite.leader.megaman;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.input.IInputBindable;
import zdream.rockchronicle.core.input.PlayerInput;
import zdream.rockchronicle.platform.body.Box;

public class Megaman extends CharacterEntry implements IInputBindable {

	public Megaman(int id) {
		super(id, "leader");
		this.box = new Box(id);
		this.boxes = new Box[] { box };
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	Box box;
	Box[] boxes;

	@Override
	public Box[] getBoxes() {
		return boxes;
	}
	
	/* **********
	 *   控制   *
	 ********** */

	@Override
	public void bindController(PlayerInput input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unbindController() {
		// TODO Auto-generated method stub
		
	}

}

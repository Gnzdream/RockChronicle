package zdream.rockchronicle.textures;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;

public class Textures {
	
	public static final Texture white;

	static {
		Pixmap p = new Pixmap(48, 48, Format.RGB888);
		p.setColor(1, 1, 1, 1);
		p.fillRectangle(0, 0, 48, 48);
		white = new Texture(p);
	}

}

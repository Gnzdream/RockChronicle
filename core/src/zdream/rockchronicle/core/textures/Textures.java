package zdream.rockchronicle.core.textures;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;

public class Textures {
	
	public static final Texture white;

	static {
		Pixmap p = new Pixmap(72, 72, Format.RGB888);
		p.setColor(1, 1, 1, 1);
		p.fillRectangle(0, 0, 72, 72);
		white = new Texture(p);
	}

}

package zdream.rockchronicle.desktop;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.JsonReader;

import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.character.CharacterBuilder;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.platform.region.RegionBuilder;
import zdream.rockchronicle.screen.MainMenuScreen;

public class RockChronicleDesktop extends Game {
	
	public static final RockChronicleDesktop INSTANCE;
	
	static {
		INSTANCE = new RockChronicleDesktop();
	}

	public RockChronicleDesktop() {
		projectPath = System.getProperty("user.dir");
		input = new InputCenter();
		runtime = new GameRuntime();
	}
	
	// 其它数据
	public int width = 25;
	public int height = 14;
	public int widthInPixel;
	public int heightInPixel;
	
	// 类
	public final String projectPath;
	public final InputCenter input;
	public final GameRuntime runtime;

	// 共用工具
	public final JsonReader jreader = new JsonReader();
	public final RegionBuilder regionBuilder = new RegionBuilder();
	public final CharacterBuilder characterBuilder = new CharacterBuilder();
	
	// 与引擎相关的
	
	public SpriteBatch batch;
	
	public BitmapFont font;

	@Override
	public void create() {
		// 初始化
		initConfig();
		initControl();
		characterBuilder.init();
		
		widthInPixel = width * Config.INSTANCE.blockWidth;
		heightInPixel = height * Config.INSTANCE.blockHeight;
		
		// 其它
		batch = new SpriteBatch();
		this.setScreen(new MainMenuScreen());
		
		// font
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("res\\font\\msyhbd.ttc"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		
		parameter.size = 16;
		parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "这是";
		
		font = generator.generateFont(parameter);
		generator.dispose();
		
		// , FreeTypeFontGenerator.DEFAULT_CHARS+ "歌唱我们亲爱的祖国,从今走向繁荣富强", false
		
		// font = new UnicodeFont("res\\font\\msyhbd.ttc", "");
	}
	
	private void initControl() {
		input.init();
	}
	
	private void initConfig() {
		Config.INSTANCE.init(jreader.parse(Gdx.files.local("res\\conf\\conf.json")));
	}

}

package zdream.rockchronicle;

import java.nio.file.Paths;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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

/**
 * 游戏主体
 * 
 * @author Zdream
 */
public class RockChronicle {
	
	public static RockChronicle INSTANCE;
	
	public static void pushGame(Game game) {
		if (INSTANCE == null) {
			INSTANCE = new RockChronicle(game);
		}
	}
	
	RockChronicle(Game game) {
		this.game = game;
		projectPath = System.getProperty("user.dir");
		input = new InputCenter();
		runtime = new GameRuntime();
	}
	
	// 其它数据
	public int width = 25;
	public int height = 14;
	public int widthInPixel;
	public int heightInPixel;
	
	public final Game game;
	
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

	public void create() {
		// 初始化
		initConfig();
		initControl();
		characterBuilder.init();
		
		widthInPixel = width * Config.INSTANCE.blockWidth;
		heightInPixel = height * Config.INSTANCE.blockHeight;
		
		// 其它
		batch = new SpriteBatch();
		game.setScreen(new MainMenuScreen());
		
		// font
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("res\\font\\msyhbd.ttc"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		
		parameter.size = 16;
		parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + chineseCharacter();
		
		font = generator.generateFont(parameter);
		generator.dispose();
		
		// , FreeTypeFontGenerator.DEFAULT_CHARS+ "歌唱我们亲爱的祖国,从今走向繁荣富强", false
		
		// font = new UnicodeFont("res\\font\\msyhbd.ttc", "");
	}
	
	private String chineseCharacter() {
		FileHandle f = Gdx.files.local(Paths.get("res", "conf", "chinese_character.txt").toString());
		String str = f.readString("UTF-8");
		return str.replaceAll("\\\n", "");
	}
	
	private void initControl() {
		input.init();
	}
	
	private void initConfig() {
		Config.INSTANCE.init(jreader.parse(Gdx.files.local("res\\conf\\conf.json")));
	}
	
}

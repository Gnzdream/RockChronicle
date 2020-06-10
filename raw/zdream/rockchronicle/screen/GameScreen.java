package zdream.rockchronicle.screen;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.textures.TextureSheet;

public class GameScreen implements Screen {
	
	final RockChronicle app;

	Texture dropImage;
	Sound dropSound;
	Music rainMusic;
	OrthographicCamera camera;
	Rectangle bucket;
	Array<Rectangle> raindrops;
	long lastDropTime;
	int dropsGathered;
	
	// 这个手柄可以玩玩
	Controller controller;
	PovDirection direction = PovDirection.center;
	int speed = 0;
	
	TextureSheet megaman;

	public GameScreen() {
		Gdx.app.log(getClass().getSimpleName(), "进入 GameScreen");
		this.app = RockChronicle.INSTANCE;
		

		// 这里试一下读 json 文件
		megaman = TextureSheet.createSheet(Gdx.files.local("res\\megaman\\megaman7sheet.json"));
		
		
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.local("res\\test\\drop.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.local("res\\test\\drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.local("res\\test\\Hornet Man.mp3"));
		rainMusic.setLooping(true);

		// create the camera and the SpriteBatch
		// 这里的设置使 y 轴是向上的
		camera = new OrthographicCamera();
		camera.setToOrtho(false, app.width, app.height);

		// create a Rectangle to logically represent the bucket
		bucket = new Rectangle();
		bucket.x = 0; // center the bucket horizontally
		bucket.y = 0; // bottom left corner of the bucket is 20 pixels above
						// the bottom screen edge
		// 这里可以确定纹理的锚点就是左下角 (在 y 轴方向向下的直角坐标系中)
		
		bucket.width = 56;
		bucket.height = 64;

		// create the raindrops array and spawn the first raindrop
		raindrops = new Array<Rectangle>();
		spawnRaindrop();
		
		
		// 这里的代码只是试试手柄
		System.out.println("我们一起来试试手柄");
		
		Array<Controller> controllers = Controllers.getControllers();
		if (controllers.size == 1) {
			Gdx.app.log(getClass().getSimpleName(), controllers.get(0).getName());
			this.controller = controllers.get(0);
			this.controller.addListener(l);
		} else {
			System.out.println("手柄个数: " + controllers.size);
			for (int i = 0; i < controllers.size; i++) {
				Gdx.app.log(getClass().getSimpleName(), controller.getName());
			}
			Controllers.addListener(l);
		}
		
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 576 - 64);
		raindrop.y = 432;
		raindrop.width = 48;
		raindrop.height = 48;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}
	
	void toLeft() {
		speedUp();
		
		bucket.x -= speed;
		if (bucket.x < 0)
			bucket.x = 0;
	}
	
	void toRight() {
		speedUp();
		
		bucket.x += speed;
		if (bucket.x > 576 - 64)
			bucket.x = 576 - 64;
	}
	
	void speedUp() {
		if (speed < 10) {
			speed += 2;
		}
	}

	@Override
	public void render(float delta) {
		// clear the screen with a dark blue color. The
		// arguments to glClearColor are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		app.batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the bucket and
		// all drops
		app.batch.begin();
		app.font.draw(app.batch, "Drops 你收集了: " + dropsGathered, 0, 432);
		// app.batch.draw(megaman.textureEntry.get("normal").region, bucket.x, bucket.y, bucket.width, bucket.height);
		for (Rectangle raindrop : raindrops) {
			app.batch.draw(dropImage, raindrop.x, raindrop.y, raindrop.width, raindrop.height);
		}
		app.batch.end();

		// process user input
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}
		
		
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			if (!Gdx.input.isKeyPressed(Keys.RIGHT))
				toLeft();
		} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			toRight();
		} else if (controller != null) CONTROLLER: {// 手柄
			if (direction == PovDirection.center) {
				break CONTROLLER;
			}
			
			switch (direction) {
			case northWest: case west: case southWest:
				toLeft();
				break;
				
			case northEast: case east: case southEast:
				toRight();
				break;

			default:
				break;
			}
		}

		// make sure the bucket stays within the screen bounds
		
		

		// check if we need to create a new raindrop
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
			spawnRaindrop();

		// move the raindrops, remove any that are beneath the bottom edge of
		// the screen or that hit the bucket. In the later case we increase the 
		// value our drops counter and add a sound effect.
		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0)
				iter.remove();
			if (raindrop.overlaps(bucket)) {
				dropsGathered++;
				dropSound.play();
				iter.remove();
			}
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		// start the playback of the background music
		// when the screen is shown
		rainMusic.play();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		dropImage.dispose();
//		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
	}
	
	ControllerListener l = new ControllerListener() {
		
		@Override
		public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
			System.out.println("ySliderMoved");
			return false;
		}
		
		@Override
		public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
			System.out.println("xSliderMoved");
			return false;
		}
		
		@Override
		public boolean povMoved(Controller controller, int povCode, PovDirection value) {
			direction = value;
			speed = 0;
			return false;
		}
		
		@Override
		public void disconnected(Controller controller) {
			System.out.println("disconnected");
		}
		
		@Override
		public void connected(Controller controller) {
			System.out.println("connected");
			
		}
		
		@Override
		public boolean buttonUp(Controller controller, int buttonCode) {
			System.out.println("buttonUp: " + buttonCode);
			return false;
		}
		
		@Override
		public boolean buttonDown(Controller controller, int buttonCode) {
			System.out.println("buttonDown: " + buttonCode);
			return false;
		}
		
		@Override
		public boolean axisMoved(Controller controller, int axisCode, float value) {
			System.out.println("axisMoved: " + axisCode + " -> " + value);
			return false;
		}
		
		@Override
		public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
			System.out.println("accelerometerMoved");
			return false;
		}
	};

}

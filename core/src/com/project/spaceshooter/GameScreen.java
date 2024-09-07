package com.project.spaceshooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

class GameScreen implements Screen{
    boolean isGameStarted = false;
    boolean isGameOver = false;
    ScoreDatabaseManager scoreManager;
    //screen
    private Camera camera;
    private Viewport viewport;

    //graphics
    private SpriteBatch batch;
    private TextureAtlas textureAtlas;
    private Texture explosionTexture;
    private Texture heartTexture;
    private Texture backgroundTexture;
    private Texture playGameTexture;
    private Texture restartButtonTexture;
    private Texture exitButtonTexture;
    private Texture historyButtonTexture;
    private TextureRegion[] backgrounds;

    private TextureRegion playerShipTextureRegion, playerShieldTextureRegion, enemyShipTextureRegion, enemyShieldTextureRegion, playerLaserTextureRegion, enemyLaserTextureRegion;

    //timing
    private float[] backgroundOffsets = {0,0,0,0};
    private  float backgroundMaxScrollingSpeed;
    private float timeBetweenEnemySpawns = 1.3f;
    private float enemySpawnTimer = 0;

    //world parameters
    private final int WORLD_WIDTH = 72;
    private final int WORLD_HEIGHT = 128;
    private final float TOUCH_MOVEMENT_THRESHOLD = 0.1f;

    //game objects
    private PlayerShip playerShip;
    private LinkedList<EnemyShip> enemyShipList;
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;
    private LinkedList<Explosion> explosionList;

    private int score = 0;

    Rectangle playGameBounds;
    Rectangle restartButtonBounds;
    Rectangle exitButtonBounds;
    Rectangle historyButtonBounds;

    //Display
    BitmapFont font;
    float hudVerticalMargin, hudLeftX, hudRightX, hudCentreX, hudRow1Y, hudRow2Y, hudSectionWidth;


    GameScreen() {
        scoreManager = new ScoreDatabaseManager();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        //set up the texture bg
        textureAtlas = new TextureAtlas("images.atlas");

        backgrounds = new TextureRegion[4];
        backgrounds[0] = textureAtlas.findRegion("Starscape00");
        backgrounds[1] = textureAtlas.findRegion("Starscape01");
        backgrounds[2] = textureAtlas.findRegion("Starscape02");
        backgrounds[3] = textureAtlas.findRegion("Starscape03");

        //backgroundHeight = WORLD_HEIGHT * 2;
        backgroundMaxScrollingSpeed = (float)(WORLD_HEIGHT)/4;

        //texture regions ship laser
        playerShipTextureRegion = textureAtlas.findRegion("playerShip1_orange");
        enemyShipTextureRegion = textureAtlas.findRegion("enemyBlue1");
        playerShieldTextureRegion = textureAtlas.findRegion("shield2");
        enemyShieldTextureRegion = textureAtlas.findRegion("shield1");
        enemyShieldTextureRegion.flip(false, true);
        playerLaserTextureRegion = textureAtlas.findRegion("laserBlue03");
        enemyLaserTextureRegion = textureAtlas.findRegion("laserRed03");

        //explosion
        explosionTexture = new Texture("explosion.png");

        //set up game objects
        playerShip = new PlayerShip(48, 5, 0.4f, 4, 45,0.5f,10, 10,WORLD_HEIGHT*2/7, WORLD_HEIGHT/4, playerShipTextureRegion, playerShieldTextureRegion, playerLaserTextureRegion);

        enemyShipList = new LinkedList<>();
        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();
        explosionList = new LinkedList<>();

        batch = new SpriteBatch();

        prepareHUD();
    }

    private void prepareHUD() {
        //create a bitmapfont from our font file
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("EdgeOfTheGalaxyPoster-3zRAp.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        heartTexture = new Texture("heart.png");
        fontParameter.size = 72;
        fontParameter.borderWidth = 3.6f;
        fontParameter.color = new Color(1, 1, 1, 0.3f);
        fontParameter.borderColor = new Color(0, 0, 0, 0.3f);

        font = fontGenerator.generateFont(fontParameter);

        //scale the font to fit world
        font.getData().setScale(0.08f);

        //calculate hud margins
        hudVerticalMargin = font.getCapHeight() / 2;
        hudLeftX = hudVerticalMargin;
        hudRightX = WORLD_WIDTH * 2 / 3 - hudLeftX;
        hudCentreX = WORLD_WIDTH / 3;
        hudRow1Y = WORLD_HEIGHT - hudVerticalMargin;
        hudRow2Y = hudRow1Y - hudVerticalMargin - font.getCapHeight();
        hudSectionWidth = WORLD_WIDTH / 3;
    }

    @Override
    public void show() {
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));

        backgroundTexture = new Texture("bg.jpeg");
        playGameTexture = new Texture("nutplay.png");
        restartButtonTexture = new Texture("restart.png");
        exitButtonTexture = new Texture("quit.png");
        historyButtonTexture = new Texture("score.png");

        // Tính toán vị trí và kích thước của các nút
        float buttonWidth = WORLD_WIDTH / 3f;
        float buttonHeight = WORLD_HEIGHT / 10f;
        float buttonX = (WORLD_WIDTH - buttonWidth) / 2;
        float buttonY = (WORLD_HEIGHT - buttonHeight) / 2;

        restartButtonBounds = new Rectangle(buttonX, buttonY + buttonHeight, buttonWidth, buttonHeight);
        exitButtonBounds = new Rectangle(buttonX, buttonY - buttonHeight, buttonWidth, buttonHeight);

        // Đặt kích thước cho nút "Play Game"
        float playButtonWidth = WORLD_WIDTH / 3f; // Đặt kích thước theo tỷ lệ với WORLD_WIDTH
        float playButtonHeight = WORLD_HEIGHT / 11f; // Đặt kích thước theo tỷ lệ với WORLD_HEIGHT
        float playButtonX = (WORLD_WIDTH - playButtonWidth) / 2;
        float playButtonY = (WORLD_HEIGHT - playButtonHeight) / 2;
        playGameBounds = new Rectangle(playButtonX, playButtonY, playButtonWidth, playButtonHeight);

        float historyButtonWidth = WORLD_WIDTH / 3f;
        float historyButtonHeight = WORLD_HEIGHT / 10f;
        float historyButtonX = (WORLD_WIDTH - historyButtonWidth) / 2;
        float historyButtonY = (WORLD_HEIGHT - historyButtonHeight) / 2 - historyButtonHeight * 2; // Adjust the vertical position
        historyButtonBounds = new Rectangle(historyButtonX, historyButtonY, historyButtonWidth, historyButtonHeight);

    }


    @Override
    public void render(float deltaTime) {
        batch.begin();

        batch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (!isGameStarted) {
            batch.draw(playGameTexture, playGameBounds.x, playGameBounds.y, playGameBounds.width, playGameBounds.height);

            // Kiểm tra sự kiện chạm vào nút "Play Game"
            if (Gdx.input.isTouched()) {
                // Chuyển đổi tọa độ chạm vào
                Vector3 touchPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPoint);
                float touchX = touchPoint.x;
                float touchY = touchPoint.y;

                // In ra tọa độ chạm vào để kiểm tra
             //   Gdx.app.log("Touch", "X: " + touchX + ", Y: " + touchY);

                // Kiểm tra xem người dùng đã chạm vào nút "Play Game" hay không
                if (playGameBounds.contains(touchX, touchY)) {
                    // Bắt đầu trò chơi
                    isGameStarted = true;
                   // Gdx.app.log("Game", "Game started!");
                }
            }
        }
        else if (isGameOver) {
            // Hiển thị màn hình thua
            renderGameOverScreen();
            if (showHistoricalScores) {
                renderHistoricalScores(); // Hiển thị thông tin điểm số lịch sử
            }
        }

        else {
            //scrolling background
            renderBackground(deltaTime);

            //ship lasers
            playerShip.update(deltaTime);
            spawnEnemyShips(deltaTime);

            ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()) {
                EnemyShip enemyShip = enemyShipListIterator.next();
                moveEnemy(enemyShip, deltaTime);
                //ship lasers
                enemyShip.update(deltaTime);
                //enemy ships
                enemyShip.draw(batch);

            }

            //player ships
            playerShip.draw(batch);

            //Lasers
            renderLasers(deltaTime);

            //explosions
            renderExplosions(deltaTime);

            //detect collisions between lasers and ships
            detectCollisions();

            //move
            detectInput(deltaTime);  //move player

            //hud render
            updateAndRenderHUD();

            if (playerShip.lives <= 0) {
                isGameOver = true;
            }
        }
        batch.end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    private Stage stage;
    private boolean isScoreSaved = false;
    private boolean showHistoricalScores = false;


    private void renderGameOverScreen() {
        if (!isScoreSaved) {
            // Lưu điểm số xuống cơ sở dữ liệu
            scoreManager.saveScore(score);

            // Đặt biến isScoreSaved thành true để chỉ lưu điểm số một lần
            isScoreSaved = true;
        }

        // Tính toán vị trí của nút "Restart"
        float restartButtonWidth = WORLD_WIDTH / 3f;
        float restartButtonHeight = WORLD_HEIGHT / 10f;
        float restartButtonX = (WORLD_WIDTH - restartButtonWidth) / 2;
        float restartButtonY = (WORLD_HEIGHT - restartButtonHeight) / 2.6f + restartButtonHeight;
        Rectangle restartButtonBounds = new Rectangle(restartButtonX, restartButtonY, restartButtonWidth, restartButtonHeight);
        batch.draw(restartButtonTexture, restartButtonBounds.x, restartButtonBounds.y, restartButtonBounds.width, restartButtonBounds.height);

        // Tính toán vị trí của nút "Exit"
        float exitButtonWidth = WORLD_WIDTH / 3f;
        float exitButtonHeight = WORLD_HEIGHT / 10f;
        float exitButtonX = (WORLD_WIDTH - exitButtonWidth) / 2;
        float exitButtonY = (WORLD_HEIGHT - exitButtonHeight) / 2 - exitButtonHeight;
        Rectangle exitButtonBounds = new Rectangle(exitButtonX, exitButtonY, exitButtonWidth, exitButtonHeight);
        batch.draw(exitButtonTexture, exitButtonBounds.x, exitButtonBounds.y, exitButtonBounds.width, exitButtonBounds.height);

        // Vẽ điểm số
        font.draw(batch, "Score: " + score, hudCentreX, hudRow1Y, hudSectionWidth, Align.center, false);

        // Tính toán vị trí của nút "History Score"
        float historyButtonWidth = WORLD_WIDTH / 3f;
        float historyButtonHeight = WORLD_HEIGHT / 10f;
        float historyButtonX = (WORLD_WIDTH - historyButtonWidth) / 2;
        float historyButtonY = (WORLD_HEIGHT - historyButtonHeight) / 2 + historyButtonHeight; // Điều chỉnh vị trí dọc
        Rectangle historyButtonBounds = new Rectangle(historyButtonX, historyButtonY, historyButtonWidth, historyButtonHeight);
        batch.draw(historyButtonTexture, historyButtonBounds.x, historyButtonBounds.y, historyButtonBounds.width, historyButtonBounds.height);

        // Xử lý sự kiện chạm vào nút "Restart" và "Exit"
        if (Gdx.input.justTouched()) {
            Vector3 touchPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPoint);
            float touchX = touchPoint.x;
            float touchY = touchPoint.y;

            // Kiểm tra nếu người chơi chạm vào nút "Restart"
            if (restartButtonBounds.contains(touchX, touchY)) {
                // Bắt đầu lại trò chơi
                restartGame();
            }

            // Kiểm tra nếu người chơi chạm vào nút "Exit"
            if (exitButtonBounds.contains(touchX, touchY)) {
                // Thoát ứng dụng
                Gdx.app.exit();
            }

            // Kiểm tra nếu người chơi chạm vào nút "History Score"
            if (historyButtonBounds.contains(touchX, touchY)) {
                // Nếu đang hiển thị thông tin điểm số lịch sử, hãy tắt nó đi
                if (showHistoricalScores) {
                    showHistoricalScores = false;
                } else {
                    // Nếu không, hãy hiển thị nó
                    Map<Integer, String> historicalScores = scoreManager.getHistoricalScores();
                    updateHistoricalScoresText(historicalScores);
                    showHistoricalScores = true;
                }
            }

            if (showHistoricalScores) {
                Map<Integer, String> historicalScores = scoreManager.getHistoricalScores();
                updateHistoricalScoresText(historicalScores);
                renderHistoricalScores(); // Vẽ thông tin điểm số lịch sử lên màn hình
            }
        }
    }

    private String historicalScoresText = "";

    private void updateHistoricalScoresText(Map<Integer, String> historicalScores) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, String> entry : historicalScores.entrySet()) {
            builder.append("Score: ").append(entry.getKey()).append(", Timestamp: ").append(entry.getValue()).append("\n");
        }
        historicalScoresText = builder.toString();
    }
    private void renderHistoricalScores() {
        font.draw(batch, historicalScoresText, 1f, 40f); // Thay x và y bằng vị trí bạn muốn vẽ
    }


//    private void showScoreHistory() {
//        // Tạo một skin mới
//        Skin skin = new Skin(); uiskin.json
//
//
//        // Thêm dialog vào Stage
//        stage.addActor(scoreHistoryDialog);
//
//        // Gọi setInputProcessor để stage có thể nhận input từ người dùng
//        Gdx.input.setInputProcessor(stage);
//    }



    private void restartGame() {
        // Thiết lập lại tất cả các biến trạng thái trò chơi
        isGameOver = false;
        score = 0;

        // Thiết lập lại tàu người chơi
        playerShip.reset();

        // Xóa tất cả tàu kẻ địch còn tồn tại
        enemyShipList.clear();

        // Xóa tất cả các laser còn tồn tại
        playerLaserList.clear();
        enemyLaserList.clear();

        // Xóa tất cả các vụ nổ còn tồn tại
        explosionList.clear();
    }


    private void updateAndRenderHUD() {
        //render top row
        font.draw(batch, "Score", hudLeftX, hudRow1Y, hudSectionWidth, Align.left, false);
        font.draw(batch, "Shield", hudCentreX, hudRow1Y, hudSectionWidth, Align.center, false);
        font.draw(batch, "Lives", hudRightX, hudRow1Y, hudSectionWidth, Align.right, false);

        //render second row
        font.draw(batch, String.format(Locale.getDefault(), "%06d", score), hudLeftX, hudRow2Y, hudSectionWidth, Align.left, false);
        font.draw(batch, String.format(Locale.getDefault(), "%02d", playerShip.shield), hudCentreX, hudRow2Y, hudSectionWidth, Align.center, false);
        // Kích thước mới của trái tim (nhỏ hơn)
        float heartSize = hudVerticalMargin * 2f; // Ví dụ, bạn có thể sử dụng một hệ số như 0.5 để thu nhỏ trái tim

        // Vẽ số trái tim với màu mặc định
        for (int i = 0; i < playerShip.lives; i++) {
            float heartX = hudRightX - (i + 1) * hudSectionWidth / 4 + heartSize * 5.5f; // Tính toán vị trí x của trái tim
            float heartY = hudRow2Y - hudVerticalMargin * 2; // Tính toán vị trí y của trái tim
            batch.draw(heartTexture, heartX, heartY, heartSize, heartSize); // Vẽ trái tim với kích thước và vị trí mới
        }

        // Vẽ số trái tim bằng màu khác khi mất mạng
        for (int i = playerShip.lives; i < playerShip.lives; i++) {
            float heartX = hudRightX - (i + 1) * hudSectionWidth;
            float heartY = hudRow2Y - hudVerticalMargin;
            batch.setColor(1, 0, 0, 1); // Đặt màu cho trái tim khi mất mạng
            batch.draw(heartTexture, heartX, heartY, heartSize, heartSize);
            batch.setColor(1, 1, 1, 1); // Đặt lại màu về mặc định
        }
    }

        private void spawnEnemyShips(float deltaTime) {
        enemySpawnTimer += deltaTime;

        if (enemySpawnTimer > timeBetweenEnemySpawns) {
            enemyShipList.add(new EnemyShip(32, 1, 0.3f, 5, 50, 0.8f, 10, 10, SpaceShooterGame.random.nextFloat() * (WORLD_WIDTH - 10) + 5, WORLD_HEIGHT - 5, enemyShipTextureRegion, enemyShieldTextureRegion, enemyLaserTextureRegion));
            enemySpawnTimer -= timeBetweenEnemySpawns;
        }
    }

    private void renderLasers(float deltaTime) {
        //lasers
        //create new lasers
        //player lasers
        if (playerShip.canFireLaser()){
            Laser[] lasers = playerShip.fireLasers();
            for (Laser laser : lasers){
                playerLaserList.add(laser);
            }
        }

        //enemy lasers
        ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
        while (enemyShipListIterator.hasNext()) {
            EnemyShip enemyShip = enemyShipListIterator.next();
            if (enemyShip.canFireLaser()) {
                Laser[] lasers = enemyShip.fireLasers();
                    enemyLaserList.addAll(Arrays.asList(lasers));

            }
        }

        // draw lasers
        //remove old lasers
        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed * deltaTime;
            if (laser.boundingBox.y > WORLD_HEIGHT) {
                iterator.remove();
            }
        }
        iterator = enemyLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed * deltaTime;
            if (laser.boundingBox.y + laser.boundingBox.height < 0) {
                iterator.remove();
            }
        }
    }

    private void detectCollisions() {
        // Duyệt qua mỗi laser của người chơi, kiểm tra xem nó có va chạm với tàu kẻ địch không
        ListIterator<Laser> laserListIterator = playerLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()) {
                EnemyShip enemyShip = enemyShipListIterator.next();

                if (enemyShip.intersects(laser.boundingBox)) {
                    // Tiếp xúc với tàu kẻ địch
                    if (enemyShip.hitAndCheckDestroy(laser)) {
                        enemyShipListIterator.remove();
                        explosionList.add(new Explosion(explosionTexture, new Rectangle(enemyShip.boundingBox), 0.7f));
                        score += 100;
                    }
                    laserListIterator.remove();
                    break;
                }
            }
        }

        // Duyệt qua mỗi laser của kẻ địch, kiểm tra xem nó có va chạm với tàu người chơi không
        laserListIterator = enemyLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            if (playerShip.intersects(laser.boundingBox)) {
                // Tiếp xúc với tàu người chơi
                if (playerShip.hitAndCheckDestroy(laser)) {
                    explosionList.add(new Explosion(explosionTexture, new Rectangle(playerShip.boundingBox), 1.6f));
                    playerShip.shield = 5;

                    // Giảm số mạng của người chơi khi bị đạn của kẻ địch đánh trúng
                    playerShip.lives--;

                    // Kiểm tra nếu người chơi hết mạng, thực hiện các hành động cần thiết ở đây
                    if (playerShip.lives <= 0) {
                        // Thực hiện các hành động khi người chơi thua cuộc
                    }
                }
                laserListIterator.remove();
            }
        }
    }


    private void renderExplosions(float deltaTime) {
        ListIterator<Explosion> explosionListIterator = explosionList.listIterator();
        while (explosionListIterator.hasNext()) {
            Explosion explosion = explosionListIterator.next();
            explosion.update(deltaTime);
            if (explosion.isFinished()) {
                explosionListIterator.remove();
            }
            else {
                explosion.draw(batch);
            }
        }
    }

    private void renderBackground(float deltaTime) {
        backgroundOffsets[0] += deltaTime * backgroundMaxScrollingSpeed / 8;
        backgroundOffsets[1] += deltaTime * backgroundMaxScrollingSpeed / 4;
        backgroundOffsets[2] += deltaTime * backgroundMaxScrollingSpeed / 2;
        backgroundOffsets[3] += deltaTime * backgroundMaxScrollingSpeed ;

        for(int layer = 0; layer < backgroundOffsets.length; layer++){
            if (backgroundOffsets[layer] > WORLD_HEIGHT) {
                backgroundOffsets[layer] = 0;
            }
            batch.draw(backgrounds[layer],0, -backgroundOffsets[layer], WORLD_WIDTH, WORLD_HEIGHT);
            batch.draw(backgrounds[layer],0, -backgroundOffsets[layer] + WORLD_HEIGHT, WORLD_WIDTH, WORLD_HEIGHT);
        }
    }

    public void detectInput(float deltaTime) {
        //keyboard

        //strategy: determine the max distance the ship can move
        //check each key that matters and move accordingly

        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
        upLimit = (float)WORLD_HEIGHT/2 - playerShip.boundingBox.y - playerShip.boundingBox.height;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0) {
            playerShip.translate(Math.min(playerShip.movementSpeed * deltaTime, rightLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0) {
            playerShip.translate(0f, Math.min(playerShip.movementSpeed * deltaTime, upLimit));
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0) {
            playerShip.translate(Math.max(-playerShip.movementSpeed * deltaTime, leftLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0) {
            playerShip.translate(0f, Math.max(-playerShip.movementSpeed * deltaTime, downLimit));
        }

        //touch input
        if (Gdx.input.isTouched()) {
            //get the screen position of the touch
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            //convert to world postion
            Vector2 touchPoint = new Vector2(xTouchPixels, yTouchPixels);
            touchPoint = viewport.unproject(touchPoint);

            //calculate the x and y differences
            Vector2 playerShipCentre = new Vector2(
                    playerShip.boundingBox.x + playerShip.boundingBox.width/2, playerShip.boundingBox.y + playerShip.boundingBox.height/2);

            float touchDistance = touchPoint.dst(playerShipCentre);

            if (touchDistance > TOUCH_MOVEMENT_THRESHOLD) {
                float xTouchDifference = touchPoint.x - playerShipCentre.x;
                float yTouchDifference = touchPoint.y - playerShipCentre.y;

                //scale to the maximum speed of the ship
                float xMove = xTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;
                float yMove = yTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;

                if (xMove > 0) xMove = Math.min(xMove, rightLimit);
                else xMove = Math.max(xMove, leftLimit);

                if (yMove > 0) yMove = Math.min(yMove, upLimit);
                else yMove = Math.max(yMove, downLimit);

                playerShip.translate(xMove, yMove);
            }
        }
    }

    public void moveEnemy(EnemyShip enemyShip, float deltaTime) {
        //strategy: determine the max distance the ship can move

        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -enemyShip.boundingBox.x;
        downLimit = (float)WORLD_HEIGHT/2 - enemyShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - enemyShip.boundingBox.x - enemyShip.boundingBox.width;
        upLimit = WORLD_HEIGHT - enemyShip.boundingBox.y - enemyShip.boundingBox.height;

        float xMove = enemyShip.getDirectionVector().x * enemyShip.movementSpeed * deltaTime;
        float yMove = enemyShip.getDirectionVector().y * enemyShip.movementSpeed * deltaTime;

        if (xMove > 0) xMove = Math.min(xMove, rightLimit);
        else xMove = Math.max(xMove, leftLimit);

        if (yMove > 0) yMove = Math.min(yMove, upLimit);
        else yMove = Math.max(yMove, downLimit);

        enemyShip.translate(xMove, yMove);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        playGameTexture.dispose();
    }
}

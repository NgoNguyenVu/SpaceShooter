package com.project.spaceshooter;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

class EnemyShip extends Ship {

    Vector2 directionVector;
    float timeSinceLastDirectionChange = 0;
    float directionChangeFrequency = 0.75f;

    public EnemyShip(float movementSpeed, int shield, float laserWidth, float laserHeight, float laserMovementSpeed, float timeBetweenShots, float width, float height, float xCentre, float yCentre, TextureRegion shipTextureRegion, TextureRegion shieldTextureRegion, TextureRegion laserTextureRegion) {
        super(movementSpeed, shield, laserWidth, laserHeight, laserMovementSpeed, timeBetweenShots, width, height, xCentre, yCentre, shipTextureRegion, shieldTextureRegion, laserTextureRegion);

        directionVector = new Vector2(0, -1);
    }

    public Vector2 getDirectionVector() {
        return  directionVector;
    }

    private void randomDirectionVector() {
        double bearing = SpaceShooterGame.random.nextDouble() * 6.283185;   // 2*pi
        directionVector.x = (float)Math.sin(bearing);
        directionVector.y = (float)Math.cos(bearing);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        timeSinceLastDirectionChange += deltaTime;
        if (timeSinceLastDirectionChange > directionChangeFrequency) {
            randomDirectionVector();
            timeSinceLastDirectionChange -= directionChangeFrequency;
        }
    }

    @Override
    public Laser[] fireLasers(){
        Laser[] laser = new Laser[2];
        laser[0] = new Laser( boundingBox.x + boundingBox.width * 0.27f, boundingBox.y - laserHeight, laserWidth, laserHeight, laserMovementSpeed, laserTextureRegion);
        laser[1] = new Laser( boundingBox.x + boundingBox.width * 0.7f, boundingBox.y - laserHeight, laserWidth, laserHeight, laserMovementSpeed, laserTextureRegion);

        timeSinceLastShot = 0;

        return laser;
    }
}

package com.project.spaceshooter;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

class PlayerShip extends Ship {

    int lives;

    public PlayerShip(float movementSpeed, int shield, float laserWidth, float laserHeight, float laserMovementSpeed, float timeBetweenShots, float width, float height, float xCentre, float yCentre, TextureRegion shipTextureRegion, TextureRegion shieldTextureRegion, TextureRegion laserTextureRegion) {
        super(movementSpeed, shield, laserWidth, laserHeight, laserMovementSpeed, timeBetweenShots, width, height, xCentre, yCentre, shipTextureRegion, shieldTextureRegion, laserTextureRegion);
        lives = 3;
    }

    @Override
    public Laser[] fireLasers(){
        Laser[] laser = new Laser[2];
        laser[0] = new Laser(boundingBox.x + boundingBox.width * 0.04f, boundingBox.y + boundingBox.height * 0.44f , laserWidth, laserHeight, laserMovementSpeed, laserTextureRegion);
        laser[1] = new Laser(boundingBox.x + boundingBox.width * 0.94f, boundingBox.y + boundingBox.height * 0.44f , laserWidth, laserHeight, laserMovementSpeed, laserTextureRegion);

        timeSinceLastShot = 0;

        return laser;
    }

    public void reset() {
        lives = 3; // Thiết lập số mạng về giá trị ban đầu
    }
}

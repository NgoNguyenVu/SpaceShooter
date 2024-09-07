package com.project.spaceshooter;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.Base64;

public class Laser {

    //position and dimensions
    Rectangle boundingBox;

    //laser physical character
    float movementSpeed;

    //graphics
    TextureRegion textureRegion;

    public Laser(float xCentre, float yBottom, float width, float height, float movementSpeed, TextureRegion textureRegion) {

        this.boundingBox = new Rectangle( xCentre - width/2, yBottom, width, height);


        this.movementSpeed = movementSpeed;
        this.textureRegion = textureRegion;

    }

    public void draw(Batch batch){
        batch.draw(textureRegion, boundingBox.x - boundingBox.width/2, boundingBox.y, boundingBox.width, boundingBox.height);
    }
}

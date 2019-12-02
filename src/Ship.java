import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PApplet.*;
import static processing.core.PConstants.TWO_PI;

public class Ship {
    static PApplet t;
    static ArrayList<Asteroid> asteroids;
    PVector pos;
    PVector vel;
    PVector acc;
    boolean debug = false;
    float fuel = 1;
    float rotation = 0;
    final float speed = 0.15f;
    boolean boosting = false;
    int lifeSpan;
    boolean dead = false;
    Ray[] rays;
    float maxSpeed = 10;
    int inputCount;
    int cooldown = 0;
    Laser laser;
    int asteroidNum;

    Ship(PApplet pApplet, int raysNum, int asteroidNum) {
        t = pApplet;
        this.asteroidNum = asteroidNum;
        pos = new PVector(t.width / 2, t.height / 2);
        vel = new PVector();
        acc = new PVector();
        rays = new Ray[raysNum];
        inputCount = rays.length * 2 + 1;

        float angle = TWO_PI / rays.length;
        for (int i = 0; i < rays.length; i++) {
            rays[i] = new Ray(t, pos, i * angle);
        }
        laser = new Laser(t, this);
        createAsteroids();
    }

    void draw() {
        if (boosting) {
            t.fill(255, 50, 50);
        } else {
            t.fill(255);
        }
        t.stroke(0);
        t.noStroke();

        t.pushMatrix();
        {
            float cx = 1.2f / 3;
            float cy = (0.5f + 0.8f) / 3;
            t.translate(pos.x, pos.y);
            t.scale(20);
            t.rotate(rotation);
            t.beginShape();
            {
                t.vertex(-cx, -cy);
                t.vertex(-cx, 1f - cy);
                t.vertex(1.2f - cx, 0.5f - cy);
            }
            t.endShape();
        }
        t.popMatrix();
    }

    public Res step(int action) {
        float reward = 0;
        t.background(51);
        switch (action) {
            case 0:
                boost();
                break;
            case 1:
                rotate(speed);
                break;
            case 2:
                rotate(-speed);
                break;
            case 3:
                beam();
        }

        checkCollision(pos);
        update();
        reward = updateLaser();
        updateAsteroids();
        draw();
        boolean done = checkDone();
        if (dead) reward -= 1;
        return new Res(reward, done);
    }

    public int getNumAction() {
        return 4;
    }

    float updateLaser() {
        t.strokeWeight(3);
        float reward = laser.beam(asteroids);
        t.strokeWeight(1);
        if (isRecharge()) {
            fuel = min(fuel + 0.01f, 1);
        }
        laser.shutdown();
        t.text(fuel, 200, 200);
        return reward;
    }

    int chargeStart = 0;

    void resetCharge() {
        chargeStart = t.frameCount;
    }

    boolean isRecharge() {
        return t.frameCount - chargeStart > 100;
    }

    boolean canShot(){
        return cooldown <= 0;
    }

    void beam() {
        boolean hit = false;
        resetCharge();
        if (canShot()) {
            laser.beam();
            cooldown = 30;
//            fuel -= 0.02f;
        }
    }

    boolean checkDone() {
        dead = checkCollision(pos) != null;
        return lifeSpan > 2000 || dead;
    }

    void updateAsteroids() {
        for (Asteroid asteroid : asteroids) {
            asteroid.draw();
            asteroid.update();
        }
        if (t.random(1) < 0.005) {
            float x = t.random(t.width);
            float y = t.random(1) < 0.5f ? 0 : t.height;
            asteroids.add(new Asteroid(t, x, y));
        }
    }

    void createAsteroids() {
        asteroids = new ArrayList<>();
        for (int i = 0; i < asteroidNum; i++) {
            float x = t.random(t.width);
            float y = t.random(1) < 0.5f ? 0 : t.height;
            asteroids.add(new Asteroid(t, x, y));
        }
    }


    HitData getState(Ray ray) {
        PVector closest = null;
        Asteroid hit = null;
        float record = Float.POSITIVE_INFINITY;
        for (Asteroid a : asteroids) {
            for (Boundary b : a.edges) {
                final PVector pt = ray.cast(b);
                if (pt != null) {
                    hit = a;
                    final float d = PVector.dist(pos, pt);
                    if (d < record) {
                        record = d;
                        closest = pt;
                    }
                }
            }
        }
        return new HitData(hit, closest);
    }

    class HitData{
        Asteroid asteroid;
        PVector intersection;
        HitData(Asteroid asteroid, PVector intersection){
            this.asteroid = asteroid;
            this.intersection = intersection;
        }
    }

    public float[] getState() {
        float[] inputs = new float[inputCount];
        t.stroke(255);
        for (int i = 0; i < rays.length; i+=2) {
            Ray ray = rays[i];
            HitData hitData = getState(ray);
            if (hitData.intersection != null) {
                float d = pos.dist(hitData.intersection);
//                inputs[i] = map(d, 0, t.width, 1, 0);
                d /= 10;
                d = d < 1 ? 1 : d;
                inputs[i] = 1/d;
                PVector towards = PVector.sub(pos, hitData.asteroid.pos);
                towards.normalize();
                float redShift = hitData.asteroid.vel.dot(towards);
                inputs[i+1] = redShift;
                if(debug){
                    t.line(pos.x, pos.y, hitData.intersection.x, hitData.intersection.y);
                }
            } else {
                inputs[i] = 0;
            }
            ray.rotate(rotation);
        }
        inputs[inputCount - 1] = canShot() ? 1 : 0;
        return inputs;
    }

    void update() {
        lifeSpan++;
        pos.add(vel);
        vel.mult(0.98f);
        vel.limit(maxSpeed);
        wrapEdges(pos);
        cooldown = t.max(0, cooldown - 1);
        boosting = false;
    }

    static Asteroid checkCollision(PVector pos) {
        for (Asteroid a : asteroids) {
            if (a.checkCollision(pos)) {
                return a;
            }
        }
        return null;
    }

    void rotate(float a) {
        rotation += a;
    }

    void boost() {
        PVector force = PVector.fromAngle(rotation).setMag(0.5f);
        vel.add(force);
        boosting = true;
    }

    void printArray(float[] arr) {
        t.print('[');

        for (int i = 0; i < arr.length; ++i) {
            t.print(arr[i] + (i < arr.length - 1 ? ", " : ""));
        }

        println(']');
    }

    static void wrapEdges(PVector pos) {
        if (pos.x > t.width) {
            pos.x = 0;
        } else if (pos.x < 0) {
            pos.x = t.width;
        }

        if (pos.y > t.height) {
            pos.y = 0;
        } else if (pos.y < 0) {
            pos.y = t.height;
        }
    }

    class Res {
        final float reward;
        final boolean done;

        Res(float reward, boolean done) {
            this.reward = reward;
            this.done = done;
        }
    }
}

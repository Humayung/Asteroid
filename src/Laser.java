import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import static java.lang.Math.max;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;

public class Laser {
    Ray laser;
    PApplet t;
    PVector tip;
    Ship ship;
    PVector pos;
    int lifespan = 255;
    boolean off = false;
    final int range = 450;
    boolean beam = false;

    Laser(PApplet t, Ship ship) {
        this.t = t;
        this.ship = ship;
        pos = ship.pos;
        tip = ship.pos;
        laser = new Ray(t, pos, ship.rotation);
    }

    float beam(ArrayList<Asteroid> asteroids) {
        float reward = 0;
        t.stroke(255, 50, 50, lifespan);
        laser.rotate(ship.rotation);
        PVector pt = cast(asteroids);
        if (pt != null) {
            t.line(pt.x, pt.y, pos.x, pos.y);
            if(beam) {
                reward = 0.08f;
            }
        }else{
            float a = ship.rotation;
            float x = pos.x + cos(a) * range;
            float y = pos.y + sin(a) * range;
            t.line(pos.x, pos.y, x, y);
            if(beam){
                reward = -0.01f;
            }
        }
        lifespan = max(lifespan - 10, 0);
        off = lifespan <= 0;
        return reward;
    }

    PVector cast(ArrayList<Asteroid> asteroids) {
        PVector closest = null;
        Asteroid hit = null;
        float record = Float.POSITIVE_INFINITY;
        for (Asteroid a : asteroids) {
            float distance = pos.dist(a.pos);
            if(distance < range) {
                for (Boundary b : a.edges) {
                    final PVector pt = laser.cast(b);
                    if (pt != null) {
                        final float d = PVector.dist(pos, pt);
                        if (d < record) {
                            hit = a;
                            record = d;
                            closest = pt;
                        }
                    }

                }
            }
        }
        if(hit != null && beam){
            boolean die = hit.disintegrate();
            if(die){
                ArrayList<Asteroid> chunks = hit.split();
                if(chunks != null) {
                    for (Asteroid a : chunks) {
                        if (a != null) {
                            asteroids.add(a);
                        }
                    }
                }
                asteroids.remove(hit);
            }
        }
        return closest;
    }


    void beam() {
        lifespan = 255;
        beam = true;
    }

    void shutdown() {
        beam = false;
    }

}




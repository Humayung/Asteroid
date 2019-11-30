import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;
import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.TWO_PI;

public class Asteroid {
    PVector pos;
    PVector vel;
    PVector acc;
    float health = 255;

    Boundary[] edges;
    float radius;
    float majorRadius;
    final int npoints = 5;
    static PApplet t;

    Asteroid(PApplet t, float x, float y) {
        this(t, new PVector(x, y), 50);
    }

    Asteroid(PApplet t, PVector pos, float radius){
        this.radius = radius;
        majorRadius = radius * (1 + 0.5f);
        this.pos = pos.copy();
        vel = PVector.random2D().mult(t.random(1, 3));
        acc = new PVector();
        edges = new Boundary[npoints];
        Asteroid.t = t;
        createEdges();

    }

    void createEdges() {
        float angle = TWO_PI / npoints;//set the angle between vertexes
        float sx, sy;
        PVector end1, end2;
        for (float a = 0; a < TWO_PI; a += angle) {//draw each vertex of the polygon
            int index = (int) (a / angle);
            if (index == 0) {
                sx = pos.x + t.random(-radius / 2, radius / 2) + PApplet.cos(a) * radius;
                sy = pos.y + t.random(-radius / 2, radius / 2) + PApplet.sin(a) * radius;
                end1 = new PVector(sx, sy);
            } else {
                end1 = edges[index - 1].endPoint[1].copy();
            }

            if (index == npoints - 1) {
                end2 = edges[0].endPoint[0].copy();
            } else {
                sx = pos.x + t.random(-radius / 2, radius / 2) + PApplet.cos(a + angle) * radius;
                sy = pos.y + t.random(-radius / 2, radius / 2) + PApplet.sin(a + angle) * radius;
                end2 = new PVector(sx, sy);
            }
            edges[index] = new Boundary(t, end1, end2);
        }
    }

    void draw() {
        t.stroke(255);
        t.beginShape();
        t.fill(255, 255 - health);
        for (Boundary b : edges) {
            t.vertex(b.endPoint[0].x, b.endPoint[0].y);
        }
        t.endShape(CLOSE);
    }

    boolean disintegrate(){
        health -= 50;
        return health <= 0;
    }

    void update() {
        pos.add(vel);
        for (Boundary b : edges) {
            b.move(vel);
        }
        wrapEdges();
    }


    boolean checkCollision(PVector pos) {
        int nvert = edges.length;
        int i, j;
        boolean c = false;
        float testx = pos.x;
        float testy = pos.y;
        for (i = 0, j = nvert - 1; i < nvert; j = i++) {
            Boundary a = edges[i];
            Boundary b = edges[j];
            if (((a.endPoint[0].y > testy) != (b.endPoint[0].y > testy)) &&
                    (testx < (b.endPoint[0].x - a.endPoint[0].x) * (testy - a.endPoint[0].y) / (b.endPoint[0].y - a.endPoint[0].y) + a.endPoint[0].x))
                c = !c;
        }
        return c;

    }

    ArrayList<Asteroid> split(){
        ArrayList<Asteroid> children = new ArrayList<>();
        if(radius > 20) {
            children.add(new Asteroid(t, pos, radius / 2f));
            children.add(new Asteroid(t, pos, radius / 2f));
            return children;
        }else{
            return null;
        }

    }

    void wrapEdges() {
        if (pos.x - majorRadius > t.width) {
            pos.x -= t.width + majorRadius * 2;
            for (Boundary b : edges) {
                for (PVector p : b.endPoint) {
                    p.x -= t.width + majorRadius * 2;
                }
            }
        } else if (pos.x + majorRadius < 0) {
            pos.x += t.width + majorRadius * 2;
            for (Boundary b : edges) {
                for (PVector p : b.endPoint) {
                    p.x += t.width + majorRadius * 2;
                }
            }
        }

        if (pos.y - majorRadius > t.height) {
            pos.y -= t.height + majorRadius * 2;
            for (Boundary b : edges) {
                for (PVector p : b.endPoint) {
                    p.y -= t.width + majorRadius * 2;
                }
            }
        } else if (pos.y + majorRadius < 0) {
            pos.y += t.height + majorRadius * 2;
            for (Boundary b : edges) {
                for (PVector p : b.endPoint) {
                    p.y += t.height + majorRadius * 2;
                }
            }
        }
    }
}

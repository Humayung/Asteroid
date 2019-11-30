import processing.core.PApplet;
import processing.core.PVector;

public class Boundary {
    PVector[] endPoint;
    static PApplet t;

    Boundary(PApplet target, PVector endPoint1, PVector endPoint2) {
        endPoint = new PVector[]{endPoint1, endPoint2};
        if (t == null) t = target;
    }

    void move(PVector vel) {
        endPoint[0].add(vel);
        endPoint[1].add(vel);
    }

}

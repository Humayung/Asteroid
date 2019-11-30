import processing.core.PApplet;
import processing.core.PVector;

public class Ray {
    PVector pos;
    PVector dir;

    static PApplet t;
    float angle;

    Ray(PApplet target, PVector pos, float angle){
        this.pos = pos;
        this.angle = angle;
        dir = PVector.fromAngle(angle);
        if(t == null) {
            t = target;
        }
    }

    void rotate(float angle){
        dir = PVector.fromAngle(angle + this.angle);
    }

    PVector cast(Boundary b){
        final float x1 = b.endPoint[0].x;
        final float y1 = b.endPoint[0].y;
        final float x2 = b.endPoint[1].x;
        final float y2 = b.endPoint[1].y;

        final float x3 = pos.x;
        final float y3 = pos.y;
        final float x4 = dir.x + pos.x;
        final float y4 = dir.y + pos.y;

        final float den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if(den == 0) return null;
        final float t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
        final float u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;

        if(t > 0 && t < 1 && u > 0){
            PVector pt = new PVector();
            pt.x = x1 + t * (x2 - x1);
            pt.y = y1 + t * (y2 - y1);
            return  pt;
        }else{
            return null;
        }
    }
}

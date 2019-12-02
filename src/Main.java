import processing.core.PApplet;

public class Main extends PApplet {
    Ship ship;
    public static void main(String[] args){
        PApplet.main("Main", args);
    }

    public void settings(){
        size(1366/2, 768, P2D);
    }

    public void setup(){
        ship = new Ship(this, 128, 13);
        ship.debug = true;
    }
    public void draw(){
        int action = getKey();
        Ship.Res res = null;
        if(action < 4) {
            res = ship.step(action);
            if (res.reward != 0) {
                System.out.println(res.reward);
            }
            if (res.done) reset();
            ship.getState();
        }
    }

    void reset(){
        ship = new Ship(this, 128, 13);
    }

    int getKey(){
        if(keyPressed){
            if(keyCode == UP) return 0;
            if(keyCode == RIGHT) return 1;
            if(keyCode == LEFT) return 2;
            if(key == ' ') return 3;
        }
        return 9;
    }
}

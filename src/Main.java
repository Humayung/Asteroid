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
        ship = new Ship(this, 64, 13);
    }
    public void draw(){
        int action = getKey();
        Ship.Res res = ship.step(action);
        if(res.reward != 0){
            System.out.println(res.reward);
        }
        if(res.done) reset();
        ship.getState();
    }

    void reset(){
        ship = new Ship(this, 40, 13);
    }

    int getKey(){
        if(keyPressed){
            if(keyCode == UP) return 1;
            if(keyCode == RIGHT) return 2;
            if(keyCode == LEFT) return 3;
            if(key == ' ') return 4;
        }
        return 0;
    }
}

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch_Dividing_Aggregating_Walkers extends PApplet {

// the simulation settings
int ENV_SIZE = 512;
int NB_INITIAL_WALKERS = 1;

float STEP_SIZE = 1.0f;
float TURN_CHANCES = 0.02f;
float TURN_ANGLE = PI / 4;

float DIVISION_CHANCES = 0.01f;
float DIVISION_ANGLE = PI / 4;
boolean DISCRETE_DIV_ANGLE = true;


ArrayList<Walker> walkers;


public PVector getTorusPosition (PVector position) {
  PVector pos = position.copy();
  if (pos.x < 0) pos.x = ENV_SIZE + pos.x;
  if (pos.x > ENV_SIZE) pos.x %= ENV_SIZE;
  if (pos.y < 0) pos.y = ENV_SIZE + pos.y;
  if (pos.y > ENV_SIZE) pos.y = pos.y %= ENV_SIZE;
  return pos;
}

public void setup () {
  
  background(0, 0, 0);
  walkers = new ArrayList<Walker>();

  // seed the world with walkers
  // their position, number, angle has a major impact on the emerging patterns
  for (int i = 0; i < NB_INITIAL_WALKERS; i++) {
    // line distribution
    float x = PApplet.parseFloat(ENV_SIZE) * .25f + PApplet.parseFloat(ENV_SIZE) * .5f * PApplet.parseFloat(i) / NB_INITIAL_WALKERS;
    float y = PApplet.parseFloat(ENV_SIZE) * .5f;
    float ang = random(0, TWO_PI);
    walkers.add(
      new Walker(x, y, 0)
    );
  }
}

public void draw () {
  ArrayList<Walker> newWalkers = new ArrayList<Walker>();

  for (Walker w : walkers) {
    // 1. walking step
    w.update();

    // 2. division step
    float r = random(0, 1);
    if (r < DIVISION_CHANCES) {
      float nAngle = w.ang + (DISCRETE_DIV_ANGLE ? round(random(0, 1))*2-1 : random(-1, 1)) * DIVISION_ANGLE;
      Walker nWalker = new Walker(w.pos.x, w.pos.y, nAngle);
      newWalkers.add(nWalker);
    }

    w.draw();
  }

  // adds the new walkers to the active list
  for (Walker w : newWalkers) {
    walkers.add(w);
  }

  // checks for dead walkers
  loadPixels();
  for (int i = walkers.size()-1; i >= 0; i--) {
    Walker w = walkers.get(i);
    // turn the walker coordinates into an index to sample the environment color
    // to do that we compute the "next" walker position
    PVector dir = new PVector(cos(w.ang), sin(w.ang));
    PVector npos = w.pos.copy().add(dir.mult(2 * STEP_SIZE));
    npos = getTorusPosition(npos);

    // sample aggregate color
    int idx = PApplet.parseInt(npos.x) + PApplet.parseInt(npos.y) * ENV_SIZE;
    int pixel = pixels[idx];
    float red = red(pixel);
    
    // kill the walker if it will run on some aggregate
    if (red > 200.0f) {
      walkers.remove(i);
      // draw its last step to fill the gap
      w.lastPos = w.pos;
      w.pos = npos;
      w.draw();
    }
  }
}
class Walker {
  PVector pos;
  PVector lastPos;
  float ang;
  
  public Walker (float x, float y, float ang) {
    this.lastPos = new PVector(x, y);
    this.pos = new PVector(x, y);
    this.ang = ang;
  }
  
  public void update () {
    lastPos = pos.copy();

    // the walker has a random chances to turn
    if (random(0, 1) < TURN_CHANCES) {
      this.ang+= TURN_ANGLE * (round(random(0, 1)) * 2.f - 1.f);
    }

    // move along the direction
    PVector dir = new PVector(cos(ang), sin(ang));
    pos.add(dir.mult(STEP_SIZE));
    
    // makes sure that the walkers stays within the window area
    pos = getTorusPosition(pos);
  }
  
  public void draw () {
    stroke(255, 255, 255);
    strokeWeight(2);
    
    // if the line is too long (because of torus world), we shorthen it
    PVector line = lastPos.copy().sub(pos);

    if (line.mag() < 4*STEP_SIZE) {
      line(lastPos.x, lastPos.y, pos.x, pos.y);
    }
  }
}
  public void settings() {  size(512, 512); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_Dividing_Aggregating_Walkers" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

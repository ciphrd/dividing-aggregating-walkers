// the simulation settings
int ENV_SIZE = 512;
int NB_INITIAL_WALKERS = 1;

float STEP_SIZE = 1.0f;
float TURN_CHANCES = 0.02;
float TURN_ANGLE = PI / 4;

float DIVISION_CHANCES = 0.01;
float DIVISION_ANGLE = PI / 4;
boolean DISCRETE_DIV_ANGLE = true;


ArrayList<Walker> walkers;


PVector getTorusPosition (PVector position) {
  PVector pos = position.copy();
  if (pos.x < 0) pos.x = ENV_SIZE + pos.x;
  if (pos.x > ENV_SIZE) pos.x %= ENV_SIZE;
  if (pos.y < 0) pos.y = ENV_SIZE + pos.y;
  if (pos.y > ENV_SIZE) pos.y = pos.y %= ENV_SIZE;
  return pos;
}

void setup () {
  size(512, 512);
  background(0, 0, 0);
  walkers = new ArrayList<Walker>();

  // seed the world with walkers
  // their position, number, angle has a major impact on the emerging patterns
  for (int i = 0; i < NB_INITIAL_WALKERS; i++) {
    // line distribution
    float x = float(ENV_SIZE) * .25 + float(ENV_SIZE) * .5 * float(i) / NB_INITIAL_WALKERS;
    float y = float(ENV_SIZE) * .5;
    float ang = random(0, TWO_PI);
    walkers.add(
      new Walker(x, y, 0)
    );
  }
}

void draw () {
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
    int idx = int(npos.x) + int(npos.y) * ENV_SIZE;
    int pixel = pixels[idx];
    float red = red(pixel);
    
    // kill the walker if it will run on some aggregate
    if (red > 200.0) {
      walkers.remove(i);
      // draw its last step to fill the gap
      w.lastPos = w.pos;
      w.pos = npos;
      w.draw();
    }
  }
}

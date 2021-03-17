class Walker {
  PVector pos;
  PVector lastPos;
  float ang;
  
  public Walker (float x, float y, float ang) {
    this.lastPos = new PVector(x, y);
    this.pos = new PVector(x, y);
    this.ang = ang;
  }
  
  void update () {
    lastPos = pos.copy();

    // the walker has a random chances to turn
    if (random(0, 1) < TURN_CHANCES) {
      this.ang+= TURN_ANGLE * (round(random(0, 1)) * 2. - 1.);
    }

    // move along the direction
    PVector dir = new PVector(cos(ang), sin(ang));
    pos.add(dir.mult(STEP_SIZE));
    
    // makes sure that the walkers stays within the window area
    pos = getTorusPosition(pos);
  }
  
  void draw () {
    stroke(255, int(255. * DEPOSIT_RATE));
    strokeWeight(1);
    
    // if the line is too long (because of torus world), we shorthen it
    PVector line = lastPos.copy().sub(pos);

    if (line.mag() < 4*STEP_SIZE) {
      line(lastPos.x, lastPos.y, pos.x, pos.y);
    }
  }
}

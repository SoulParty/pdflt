package lt.nortal.pdflt.domain;

public class Point {
  private float x;
  private float y;

  public Point() {
    this.x = 0;
    this.y = 0;
  }

  public Point(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public float getX() {
    return x;
  }


  public float getY() {
    return y;
  }

}

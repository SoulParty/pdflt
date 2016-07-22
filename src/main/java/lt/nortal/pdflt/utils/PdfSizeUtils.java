package lt.nortal.pdflt.utils;

import lt.nortal.pdflt.domain.Point;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;

public class PdfSizeUtils {

  private PdfSizeUtils() {
  }
  
  public static Point getPointWithRotation(Point point, PdfReader reader, int pageNumber) {
    int rotation = reader.getPageRotation(pageNumber);
    Rectangle pageSize = reader.getPageSizeWithRotation(pageNumber);
    switch (rotation) {
      case 90:
        point = new Point(point.getY(), pageSize.getTop()-point.getX());
        break;
      case 180:
        point = new Point(pageSize.getRight()-point.getX(), pageSize.getTop()-point.getY());
        break;
      case 270:
        point = new Point(pageSize.getRight()-point.getY(), point.getX());
        break;
    }
    return point;
  }

  public static Rectangle getRectangleWithRotation(Rectangle rect, PdfReader reader, int pageNumber) {
    int rotation = reader.getPageRotation(pageNumber);
    Rectangle pageSize = reader.getPageSizeWithRotation(pageNumber);
    switch (rotation) {
      case 90:
        rect = new Rectangle(
            rect.getBottom(), 
            pageSize.getTop() - rect.getLeft(), 
            rect.getTop(), 
            pageSize.getTop() - rect.getRight());
        break;
      case 180:
        rect = new Rectangle(
            pageSize.getRight() - rect.getLeft(),
            pageSize.getTop() - rect.getBottom(),
            pageSize.getRight() - rect.getRight(), 
            pageSize.getTop() - rect.getTop());
        break;
      case 270:
        rect = new Rectangle(
            pageSize.getRight() - rect.getBottom(),
            rect.getLeft(),
            pageSize.getRight() - rect.getTop(), rect.getRight());
        break;
    }
    rect.normalize();

    return rect;
  }

  public static Rectangle getCropBoxWithRotation(PdfReader reader, int pageNumber) {
    return getRectangleWithRotation(reader.getCropBox(pageNumber), reader, pageNumber);
  }

}

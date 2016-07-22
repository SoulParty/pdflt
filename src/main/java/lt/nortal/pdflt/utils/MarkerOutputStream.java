package lt.nortal.pdflt.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkerOutputStream extends FilterOutputStream {

  private byte[] marker;
  private int matchedCount;
  private long position;
  private List<Long> markerPositions;
  private List<Long> unmodifiableMarkerPositions;
  
  public MarkerOutputStream(OutputStream out, byte[] marker) {
    super(out);
    this.marker = marker;
    this.matchedCount = 0;
    this.position = 0L;
    this.markerPositions = new ArrayList<Long>();
    this.unmodifiableMarkerPositions = Collections.unmodifiableList(this.markerPositions);
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    checkNextByte((byte)b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    for (int i=0; i<len; i++) {
      checkNextByte(b[off+i]);
    }
  }

  public List<Long> getMarkerPositions() {
    return unmodifiableMarkerPositions;
  }

  public boolean isMatched() {
    return !markerPositions.isEmpty();
  }
  
  private void checkNextByte(byte b) {
    if (b == marker[matchedCount]) {
      matchedCount++;
      if (matchedCount == marker.length) {
        markerPositions.add(position);
        position++;
        partialRematch();
      }
    } else {
      position++;
      if (matchedCount > 0) {
        partialRematch();
        checkNextByte(b);
      }
    }
  }

  private void partialRematch() {
    int previouslyMatchedCount = matchedCount;
    matchedCount = 0;
    for (int i=1; i<previouslyMatchedCount; i++) {
      checkNextByte(marker[i]);
    }
  }

}

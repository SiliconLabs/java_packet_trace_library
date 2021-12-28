package com.silabs.pti.extcap;

import java.io.File;
import java.io.IOException;

import com.silabs.na.pcap.IPcapOutput;
import com.silabs.na.pcap.LinkType;
import com.silabs.na.pcap.Pcap;

/**
 * Class that facilitates the capturing from WSTK into a pcap file.
 * 
 * @author timotej
 *
 */
public class ExtcapCapture {

  private String ifc, fifo, filter;

  public ExtcapCapture(String ifc, String fifo, String filter) {
    this.ifc = ifc;
    this.fifo = fifo;
    this.filter = filter;
  }

  public void capture(IExtcapInterface ec) throws IOException {
    ec.log("capture: start");
    try (IPcapOutput output = Pcap.openForWriting(new File(fifo))) {
      output.writeInterfaceDescriptionBlock(LinkType.ETHERNET, Pcap.RESOLUTION_MICROSECONDS);
      for (int i = 0; i < 1000; i++) {
        output.writeEnhancedPacketBlock(0, 10 * i, new byte[] { (byte) i, (byte) (i + 1) });
        try {
          Thread.sleep(200);
        } catch (Exception e) {
        }
      }
    } catch (IOException e) {
      String msg = e.getMessage();
      if ( !msg.equals("Broken pipe")) {
        ec.log("capture: exception in writing pcap file: " + e.getMessage());
      }
    } finally {
      ec.log("capture: stop");
    }
  }
}

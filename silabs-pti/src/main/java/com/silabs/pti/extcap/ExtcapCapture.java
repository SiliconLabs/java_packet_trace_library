package com.silabs.pti.extcap;

import java.io.File;
import java.io.IOException;

import com.silabs.na.pcap.IPcapOutput;
import com.silabs.na.pcap.LinkType;
import com.silabs.na.pcap.Pcap;

/**
 * Class that facilitates the capturing from WSTK into a pcap file.
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
  
  public void capture() throws IOException {
    try (IPcapOutput output = Pcap.openForWriting(new File(fifo))) {
      output.writeInterfaceDescriptionBlock(LinkType.ETHERNET, Pcap.RESOLUTION_MICROSECONDS);
      for ( int i=0; i<1000; i++ ) {
        output.writeEnhancedPacketBlock(i, i, new byte[] {1,2,3,4,5,6,7,8,9,10});
        try { Thread.sleep(200); } catch (Exception e) {}
      }
    }
  }
}

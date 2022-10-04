package com.silabs.pti;

public class MainRunner implements Runnable {
  Main m = null;

  public MainRunner(final String[] args) {
    m = new Main(args);
  }

  @Override
  public void run() {
    if (m.cli().shouldExit())
      System.exit(m.cli().exitCode());

    m.run();
  }
}

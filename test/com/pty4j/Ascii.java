package com.pty4j;

public class Ascii {
  /**
   * End of Text: A communication control character used to terminate a sequence of characters
   * started with STX and transmitted as an entity.
   */
  public final static byte ETX = 3;

  public final static char ETX_CHAR = (char)ETX;

  /**
   * Bell ('\a'): A character for use when there is a need to call for human attention. It may
   * control alarm or attention devices.
   */
  public final static byte BEL = 7;

  public final static char BEL_CHAR = (char)BEL;

  /**
   * Backspace ('\b'): A format effector which controls the movement of the printing position one
   * printing space backward on the same printing line. (Applicable also to display devices.)
   */
  public final static byte BS = 8;
}

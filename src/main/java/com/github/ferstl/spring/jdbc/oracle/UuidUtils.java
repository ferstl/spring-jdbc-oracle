package com.github.ferstl.spring.jdbc.oracle;

import java.util.UUID;

/**
 * Utility class for converting UUID &lt;-&gt; byte[16].
 */
final class UuidUtils {

  private UuidUtils() {
    throw new IllegalArgumentException("not instantiable");
  }

  /**
   * Converts a raw 128 bit value to a {@link UUID}.
   *
   * @param raw the 128 bit value of a UUID, possibly {@code null}
   * @return a UUID with value {@code raw}
   *         {@code null} if {@code raw} is null
   */
  static UUID fromByteArray(byte[] raw) {
    if (raw == null) {
      return null;
    }
    if (raw.length != 16) {
      throw new IllegalArgumentException("unexpected data length: " + raw.length);
    }
    long mostSigBits = toLong(raw, 0);
    long leastSigBits = toLong(raw, 8);
    return new UUID(mostSigBits, leastSigBits);
  }

  /**
   * Converts the 128 bit value of a {@link UUID} to a {@code byte[16]}.
   *
   * @param uuid the UUID to convert, possibly {@code null}
   * @return the byte[16] representation of the value of {@code uuid},
   *         {@code null} if {@code uuid} is null
   */
  static byte[] toByteArray(UUID uuid) {
    if (uuid == null) {
      return null;
    } else {
      byte[] raw = new byte[16];
      long mostSignificantBits = uuid.getMostSignificantBits();
      storeAt(mostSignificantBits, raw, 0);
      long leastSignificantBits = uuid.getLeastSignificantBits();
      storeAt(leastSignificantBits, raw, 8);
      return raw;
    }
  }

  /**
   * Reads a 64 bit value from a {@code byte[]} using network order.
   *
   * @param b the array from which to read
   * @param start the array index at which to start reading
   * @return the value
   * @throws IllegalArgumentException if {@code start} is negative or
   *                                  there are not at least 8 elements
   *                                  {@code b} starting at {@code start}
   * @throws NullPointerException if {@code b} is {@code null}
   */
  private static long toLong(byte[] b, int start) {
    if (start < 0) {
      throw new IllegalArgumentException("start is negative");
    }
    if ((start + 7) >= b.length) {
      throw new IllegalArgumentException("array too short");
    }
    return (Byte.toUnsignedLong(b[start]) << 56)
            | (Byte.toUnsignedLong(b[start + 1]) << 48)
            | (Byte.toUnsignedLong(b[start + 2]) << 40)
            | (Byte.toUnsignedLong(b[start + 3]) <<  32)
            | (Byte.toUnsignedLong(b[start + 4]) << 24)
            | (Byte.toUnsignedLong(b[start + 5]) <<  16)
            | (Byte.toUnsignedLong(b[start + 6]) << 8)
            | Byte.toUnsignedLong(b[start + 7]);
  }


  /**
   * Stores a 64 bit value to a {@code byte[]} using network order.
   *
   * @param l the value to write
   * @param b the array to which to store
   * @param start the array index at which to start writing
   * @throws IllegalArgumentException if {@code start} is negative or
   *                                  there are not at least 8 elements
   *                                  {@code b} starting at {@code start}
   * @throws NullPointerException if {@code b} is {@code null}
   */
  private static void storeAt(long l, byte[] b, int start) {
    if (start < 0) {
      throw new IllegalArgumentException("start is negative");
    }
    if ((start + 7) >= b.length) {
      throw new IllegalArgumentException("array too short");
    }
    b[start] = (byte) ((l & (0xFFl << 56)) >>> 56);
    b[start + 1] = (byte) ((l & (0xFFl << 48)) >>> 48);
    b[start + 2] = (byte) ((l & (0xFFl << 40)) >>> 40);
    b[start + 3] = (byte) ((l & (0xFFl << 32)) >>> 32);
    b[start + 4] = (byte) ((l & (0xFFl << 24)) >>> 24);
    b[start + 5] = (byte) ((l & (0xFFl << 16)) >>> 16);
    b[start + 6] = (byte) ((l & (0xFFl << 8)) >>> 8);
    b[start + 7] = (byte) (l & 0xFFl);
  }

}

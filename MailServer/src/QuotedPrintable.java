package networkProject;

import java.io.UnsupportedEncodingException;

/**
 * A class containing static methods to perform decoding from <b>quoted
 * printable</b> content transfer encoding and to encode into
 */
public class QuotedPrintable {

	private static byte HT = 0x09; // \t
	private static byte LF = 0x0A; // \n
	private static byte CR = 0x0D; // \r

	/**
	 * A method to decode quoted printable encoded data. It overrides the same input
	 * byte array to save memory. Can be done because the result is surely smaller
	 * than the input.
	 * 
	 * @param qp
	 *            a byte array to decode.
	 * @return the length of the decoded array.
	 */
	public static int decode(final byte[] qp) {
		final int qplen = qp.length;
		int retlen = 0;

		for (int i = 0; i < qplen; i++) {
			// Handle encoded chars
			if (qp[i] == '=') {
				if (qplen - i > 2)
					// The sequence can be complete, check it
					if (qp[i + 1] == QuotedPrintable.CR && qp[i + 2] == QuotedPrintable.LF) {
						// soft line break, ignore it
						i += 2;
						continue;

					} else if (QuotedPrintable.isHexDigit(qp[i + 1]) && QuotedPrintable.isHexDigit(qp[i + 2])) {
						// convert the number into an integer, taking
						// the ascii digits stored in the array.
						qp[retlen++] = (byte) (QuotedPrintable.getHexValue(qp[i + 1]) * 16
								+ QuotedPrintable.getHexValue(qp[i + 2]));

						i += 2;
						continue;
					}
			} else
			// Log.error("decode: Invalid sequence = " + qp[i + 1]
			// + qp[i + 2]);

			// RFC 2045 says to exclude control characters mistakenly
			// present (unencoded) in the encoded stream.
			// As an exception, we keep unencoded tabs (0x09)
			if ((qp[i] >= 0x20 && qp[i] <= 0x7f) || qp[i] == QuotedPrintable.HT || qp[i] == QuotedPrintable.CR
					|| qp[i] == QuotedPrintable.LF)
				qp[retlen++] = qp[i];
		}

		return retlen;
	}

	private static boolean isHexDigit(final byte b) {
		return ((b >= 0x30 && b <= 0x39) || (b >= 0x41 && b <= 0x46));
	}

	private static byte getHexValue(final byte b) {
		return (byte) Character.digit((char) b, 16);
	}

	/**
	 * 
	 * @param qp
	 *            Byte array to decode
	 * @param enc
	 *            The character encoding of the decoded string
	 * @return The decoded string.
	 */
	public static String decode(final byte[] qp, final String enc) {
		final int len = QuotedPrintable.decode(qp);
		try {
			return new String(qp, 0, len, enc);
		} catch (final UnsupportedEncodingException e) {
			// Log.error("qp.decode: " + enc + " not supported. " + e.toString());
			return new String(qp, 0, len);
		}
	}

	/**
	 * A method to encode data in quoted printable
	 * 
	 * @param content
	 *            The string to be encoded
	 * @return the encoded string.
	 * @throws Exception
	 * 
	 *             public static byte[] encode(String content, String enc) throws
	 *             Exception { // TODO: to be implemented (has to return a String)
	 *             throw new Exception("This method is not implemented!"); }
	 */
	/**
	 * A method to encode data in quoted printable
	 * 
	 * @param content
	 *            The string to be encoded
	 * @return the encoded string.
	 * @throws Exception
	 * 
	 *             public static byte[] encode(byte[] content) throws Exception { //
	 *             TODO: to be implemented (has to return a String) throw new
	 *             Exception("This method is not implemented!"); }
	 */

}
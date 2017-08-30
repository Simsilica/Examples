package example.map;

/**
    General utility class for byte to hex conversions.
    All methods are referenced in a static context.
*/
public class ConvertHex {

    /**
        Given a byte, return a String containing the hexadecimal equivalent.
        @param b byte to process
        @return String containing the hexadecimal equivalent of the provided byte
    */
    public static String byteToHex( byte b ) {
        if( (b & 0xf0) == 0 ) {
            return 0 + Integer.toHexString( b & 0xFF );
        } else {
            return Integer.toHexString( b & 0xFF );
        }
    }

    /**
        Given an integer representation of a byte, return a String containing the
        hexadecimal equivalent.
        @param theByte byte to process
        @return String containing the hexadecimal equivalent of the provided byte
    */
    public static String byteToHex( int theByte ) {
        if( (theByte & 0x00F0) == 0 ) {
            return 0 + Integer.toHexString( theByte & 0xFF );
        } else {
            return Integer.toHexString( theByte & 0xFF );
        }
    }

    /**
        Given a string containing hexadecimals, returns the byte equivalent in a byte array

        @param s Source string to be decoded.
        @return Byte array representing the original hex values in the string.
    */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }
}
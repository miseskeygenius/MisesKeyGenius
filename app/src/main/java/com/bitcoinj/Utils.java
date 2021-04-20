package com.bitcoinj;

import android.os.Build;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Utils
{
    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    /**
     * <p>
     * The regular {@link BigInteger#toByteArray()} includes the sign bit of the number and
     * might result in an extra byte addition. This method removes this extra byte.
     * </p>
     * <p>
     * Assuming only positive numbers, it's possible to discriminate if an extra byte
     * is added by checking if the first element of the array is 0 (0000_0000).
     * Due to the minimal representation provided by BigInteger, it means that the bit sign
     * is the least significant bit 0000_000<b>0</b> .
     * Otherwise the representation is not minimal.
     * For example, if the sign bit is 0000_00<b>0</b>0, then the representation is not minimal due to the rightmost zero.
     * </p>
     * @param b the integer to format into a byte array
     * @param numBytes the desired size of the resulting byte array
     * @return numBytes byte long array.
     */
    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        checkArgument(b.signum() >= 0, "b must be positive or zero");
        checkArgument(numBytes > 0, "numBytes must be positive");
        byte[] src = b.toByteArray();
        byte[] dest = new byte[numBytes];
        boolean isFirstByteOnlyForSign = src[0] == 0;
        int length = isFirstByteOnlyForSign ? src.length - 1 : src.length;
        checkArgument(length <= numBytes, "The given number does not fit in " + numBytes);
        int srcPos = isFirstByteOnlyForSign ? 1 : 0;
        int destPos = numBytes - length;
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    /**
     * <p>The "compact" format is a representation of a whole number N using an unsigned 32 bit number similar to a
     * floating point format. The most significant 8 bits are the unsigned exponent of base 256. This exponent can
     * be thought of as "number of bytes of N". The lower 23 bits are the mantissa. Bit number 24 (0x800000) represents
     * the sign of N. Therefore, N = (-1^sign) * mantissa * 256^(exponent-3).</p>
     *
     * <p>Satoshi's original implementation used BN_bn2mpi() and BN_mpi2bn(). MPI uses the most significant bit of the
     * first byte as sign. Thus 0x1234560000 is compact 0x05123456 and 0xc0de000000 is compact 0x0600c0de. Compact
     * 0x05c0de00 would be -0x40de000000.</p>
     *
     * <p>Bitcoin only uses this "compact" format for encoding difficulty targets, which are unsigned 256bit quantities.
     * Thus, all the complexities of the sign bit and using base 256 are probably an implementation accident.</p>
     */
    public static BigInteger decodeCompactBits(long compact) {
        int size = ((int) (compact >> 24)) & 0xFF;
        byte[] bytes = new byte[4 + size];
        bytes[3] = (byte) size;
        if (size >= 1) bytes[4] = (byte) ((compact >> 16) & 0xFF);
        if (size >= 2) bytes[5] = (byte) ((compact >> 8) & 0xFF);
        if (size >= 3) bytes[6] = (byte) (compact & 0xFF);
        return decodeMPI(bytes, true);
    }

    /** Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in big endian format. */
    public static long readUint32BE(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xffl) << 24) |
                ((bytes[offset + 1] & 0xffl) << 16) |
                ((bytes[offset + 2] & 0xffl) << 8) |
                (bytes[offset + 3] & 0xffl);
    }
    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     * @param hasLength can be set to false if the given array is missing the 4 byte length field
     */
    public static BigInteger decodeMPI(byte[] mpi, boolean hasLength) {
        byte[] buf;
        if (hasLength) {
            int length = (int) readUint32BE(mpi, 0);
            buf = new byte[length];
            System.arraycopy(mpi, 4, buf, 0, length);
        } else
            buf = mpi;
        if (buf.length == 0)
            return BigInteger.ZERO;
        boolean isNegative = (buf[0] & 0x80) == 0x80;
        if (isNegative)
            buf[0] &= 0x7f;
        BigInteger result = new BigInteger(buf);
        return isNegative ? result.negate() : result;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String HEXencode(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Calculates RIGTMOST160(SHA3(input)). This is used in Ethereum address
     * calculations. *
     *
     * @param input
     *            - data
     * @return - 20 right bytes of the hash keccak of the data
     */
    public static byte[] sha3omit12(byte[] input) {
         Keccak.Digest256 digest256 = new Keccak.Digest256();
        byte[] hash = digest256.digest(input);
        byte[] result = new byte[hash.length-12];
        System.arraycopy(hash, 12, result, 0, hash.length-12);
        return result;
    }
}

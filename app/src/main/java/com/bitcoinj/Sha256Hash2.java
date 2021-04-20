package com.bitcoinj;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Hash2
{

   /* s must be an even-length string.
    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }*/

    byte[] bytes;

    public static byte[] hash(byte[] bytes)
    {
        MessageDigest digest = null;

        try { digest = MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException e) { e.printStackTrace(); }

        return digest.digest(bytes);
    }

    public Sha256Hash2(byte[] input)
    {
        this.bytes = hash(input);
    }


    public Sha256Hash2(String string)
    {
        new Sha256Hash2(string.getBytes(Charset.forName("UTF-8")));
    }

    public Sha256Hash2(CharSequence chars)
    {
        new Sha256Hash2(chars.toString());
    }

    public char[] toCharArray()
    {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return hexChars;
    }

    public byte[] toByteArray() { return bytes; }
    public String toString() { return new String(toCharArray()); }
}

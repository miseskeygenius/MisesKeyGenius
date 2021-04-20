package com.miseskeygenius;

import com.bitcoinj.Base58;
import com.bitcoinj.DeterministicKey;
import com.bitcoinj.DumpedPrivateKey;
import com.bitcoinj.ECKey;
import com.bitcoinj.HDKeyDerivation;
import com.bitcoinj.MainNetParams;
import com.bitcoinj.MnemonicCode;
import com.bitcoinj.Script;
import com.bitcoinj.Sha256Hash;

import com.bitcoinj.Utils;
import com.qrcode.ErrorCorrectionLevel;
import com.qrcode.Mode;
import com.qrcode.QRCode;

import java.util.Arrays;

import static com.bitcoinj.Bech32.encode;

public class MisesBip32  {

      // INPUT
    private DeterministicKey masterPrivateKey;
    private String path;
    private int addressFrom;
    private int addressTo;
    public boolean lookForAddresses;

    public boolean inputOk()
    {
        return masterPrivateKey!=null & path!=null & addressFrom>=0 & addressFrom<=addressTo;
    }

    // is the key public or private?
    private int ppKey;
    public static final int PRIVATE_KEY = 0;
    public static final int PUBLIC_ADDRESS = 1;

    public int coin;
    public static final int BITCOIN_LEGACY = 0;
    public static final int BITCOIN_SEGWIT = 1;
    public static final int ETHEREUM = 2;

    // OUTPUT
    public String keyList;
    public String keyListNumbers;

    public MisesBip32(String mpk, String path, String addressNumbers)
    {
        setMasterPrivateKey(mpk);
        setPath(path);
        setAddressNumbers(addressNumbers);
    }

    public static String generateSeed(String mnemonics, String passphrase, int mode) {

        if (mode==MainActivity.MODE_MNEMONICS)
            return new String(toCharArray(MnemonicCode.toSeed(mnemonics)));

        else if (mode==MainActivity.MODE_PASSPHRASE)
            return Sha256Hash.hash(passphrase);

        else // MODE_MNEMONICS_PLUS_PASSPHRASE
            return new String(toCharArray(MnemonicCode.toSeed(mnemonics, passphrase)));
    }

    public static boolean mnemonicsOk(String mnemonics) {
        // Check lowercase characters and words join by single space
        if (mnemonics.matches("[a-z]+( [a-z]+)*"))
            return MnemonicCode.checkMnemonics(Arrays.asList(mnemonics.split(" ")));

        else return false;
    }
    public static boolean seedOk(String seed) {
        // Check seed even lenght and chars
        return seed.length()%2==0 & seed.matches("[0-9A-Fa-f]+");
    }

    public static String getMasterPrivateKey(String seed) {
        // seed is suposed to be already checked

        // generate Master Private Key from seed
        DeterministicKey mpk = HDKeyDerivation.createMasterPrivateKey(hexStringToByteArray(seed));
        return mpk.serializePrivB58(MainNetParams.get());
    }

    public boolean setMasterPrivateKey(String mpkString) {
        boolean ok = true;
        // generate Master Private Key from serialized string
        try {
            this.masterPrivateKey = DeterministicKey.deserializeB58(MainNetParams.get(), mpkString);
            if (!this.masterPrivateKey.hasPrivKey())
                throw new IllegalArgumentException ("Parent key must have private key bytes for this method");
        } catch (Exception e) {
            ok = false;
        }
        return ok;
    }

    public boolean setPath(String path) {
        boolean ok = pathOk(path);
        if (ok) {
            this.path = path;
            // tells if we are looking for an extended key, or addresses
            lookForAddresses = path.matches("[Mm]/.*[a-z].*");
        }
        return ok;
    }

    private static boolean pathOk(String path) {
        return path.matches("[mM](/[0-9]+['H]?)*(/([a-z]['H]?)?)?");
    }

    public boolean setAddressNumbers(String string) {

        boolean ok = false;
        if (string.matches("[0-9]+-[0-9]+")) {

            this.addressFrom = Integer.parseInt(string.substring(0, string.indexOf('-')));
            this.addressTo = Integer.parseInt(string.substring(string.indexOf('-') + 1));

            ok = addressFrom>=0 & addressFrom<=addressTo;
        }
        else if (string.matches("[0-9]+")) {
            this.addressFrom = Integer.parseInt(string);
            this.addressTo = this.addressFrom;

            ok = addressFrom>=0;
        }
        return ok;
    }

    public int getNAdresses(){

        int nAdresses;
        if (!lookForAddresses) nAdresses=1;

        else {
            nAdresses = addressTo - addressFrom + 1;
            if (nAdresses>1){
                int maxNAdresses = getMaxNAdresses();
                if (nAdresses>maxNAdresses) nAdresses = maxNAdresses;
            }
        }
        return nAdresses;
    }

    private static final int prvBtcKeySize = 52;
    private static final int legacyAddressSize = 34;
    //private static final int nestedAddressSize = 34;
    private static final int segwitAddressSize = 42;
    private static final int prvEthKeySize = 66;
    private static final int ethAddressSize = 42;

    private int getAddressLength()
    {
        int result;
        if (coin==BITCOIN_LEGACY) {
            if (isPrivateKey()) result = prvBtcKeySize;
            else result = legacyAddressSize;
        }
        else if (coin==BITCOIN_SEGWIT) {
            if (isPrivateKey()) result = prvBtcKeySize;
            else result = segwitAddressSize;
        }
        else { // coin==ETHEREUM
            if (isPrivateKey()) result = prvEthKeySize;
            else result = ethAddressSize;
        }
        return result;
    }


    // fix num of addresses according to max QR capacity
    public int getMaxNAdresses() {

        int maxLength = QRCode.getMaxLength(Mode.MODE_8BIT_BYTE, ErrorCorrectionLevel.L);
        return (maxLength/(getAddressLength()+2))-1;
    }

    public void setPpKey(int pp) { this.ppKey=pp; }

    public boolean isPrivateKey() {
        return this.ppKey==PRIVATE_KEY;
    }

    private static boolean carryOn(){
        return !Thread.currentThread().isInterrupted();
    }

    public void processDerivation()
    {
        if (!inputOk()) {
            Thread.currentThread().interrupt();
            return;
        }

        boolean hardenedAddresses = path.endsWith("'") | path.endsWith("H");

        String extendedKeyPath = path;

        if (lookForAddresses)
            // remove everything after last '/'
            if (path.lastIndexOf('/')!=-1) extendedKeyPath = path.substring(0, path.lastIndexOf('/'));

        DeterministicKey extendedKey = DeterministicKey.computeDerivation(this.masterPrivateKey, extendedKeyPath);

        if (lookForAddresses) {

            // get addresses from extendedKey
            // convert key dk to WIF format

            int addressLength = getAddressLength();
            int nAddresses = getNAdresses();

            StringBuilder keyBuffer = new StringBuilder(addressLength*nAddresses);
            StringBuilder numbersBuffer = new StringBuilder(addressLength*nAddresses);


            for (int i = 0; i<nAddresses & carryOn(); i++) {

                int a = addressFrom+i;
                String addressPath = "m/" + a;
                if (hardenedAddresses) addressPath += "H";

                DeterministicKey addressKey = DeterministicKey.computeDerivation(extendedKey, addressPath);

                String key;

                if (isPrivateKey()) {
                    if (coin==ETHEREUM) key = getEthPrvKey(addressKey);
                    else key = getBtcPrvKey(addressKey);
                }
                else // public address
                {
                    if (coin==BITCOIN_LEGACY) key = getLegacyAddress(addressKey);
                    else if (coin==BITCOIN_SEGWIT) key = getSegwitAddress(addressKey);
                    else key = getEthereumAddress(addressKey);
                }

                keyBuffer.append(key);
                keyBuffer.append("\n");

                numbersBuffer.append(a).append(": ");
                if (a<10) numbersBuffer.append(" ");
                if (key.length()<addressLength) numbersBuffer.append(String.format("%" + addressLength + "s", key));
                else numbersBuffer.append(key);
                numbersBuffer.append("\n");
            }

            keyList = keyBuffer.toString();
            keyListNumbers = numbersBuffer.toString();
        }

        else { // looking for a extended key

            if (isPrivateKey())
            {
                if (coin==BITCOIN_SEGWIT) keyList = getZPrv(extendedKey);
                else keyList = getXPrv(extendedKey);
            }
            else // public extended key
            {
                if (coin==BITCOIN_SEGWIT) keyList = getZPub(extendedKey);
                else keyList = getXPub(extendedKey);
            }
        }
    }

    private static String getEthPrvKey(DeterministicKey key) {
        return "0x" + new String(toCharArray(key.getPrivKeyBytes()));
    }

    private static String getBtcPrvKey(DeterministicKey key)
    {
        ECKey eckey = ECKey.fromPrivate(key.getPrivKeyBytes33());
        DumpedPrivateKey dpk = eckey.getPrivateKeyEncoded(MainNetParams.get());
        return dpk.toBase58();
    }

    private static String getLegacyAddress(DeterministicKey key)
    {
         return Base58.encodeChecked(0, key.getPubKeyHash());
    }

    private static String getEthereumAddress(DeterministicKey key)
    {
        ECKey ec = ECKey.fromPrivate(key.getPrivKeyBytes33(), true);
        byte[] pubBytes = ec.getPubKeyPoint().getEncoded(false);
        byte[] removedFirst = new byte[pubBytes.length - 1];
        System.arraycopy(pubBytes, 1, removedFirst, 0, pubBytes.length - 1);
        byte[] sha33 = Utils.sha3omit12(removedFirst);
        return "0x" + new String(toCharArray(sha33));
    }

    private static String getSegwitAddress(DeterministicKey key)
    {
        // get compressed public key
        ECKey ec = ECKey.fromPrivate(key.getPrivKeyBytes33(), true);
        byte[] compressedPub = ec.getPubKey();

        // calculate RIPEMD160 hash
        byte[] rip = ECKey.sha256hash160(compressedPub);

        byte[] squash = squash20bytes(rip);

        // add 0 at the start of the array
        byte[] squash0 = new byte[squash.length+1];
        squash0[0] = 0;
        System.arraycopy(squash, 0, squash0, 1, squash.length);

        return encode("bc", squash0);
    }

    private static byte[] squash20bytes (byte[] bytes) {

        int loop = bytes.length / 5;
        byte[] result = new byte[loop*8];

        for (int i = 0; i < loop; i++) {
            byte[] bytes5 = new byte[5];
            System.arraycopy(bytes, i * 5, bytes5, 0, 5);
            byte[] squash = squash5bytes(bytes5);
            System.arraycopy(squash, 0, result, i * 8, 8);
        }
        return result;
    }

    private static byte[] squash5bytes(byte[] bytes){

        // bytes.length must be 5
        byte[] result = new byte[8];

        // shift has precedence over logical operators
        result[0] = (byte)(bytes[0]>>3 & 31);
        result[1] = (byte)((bytes[0]<<2 & 31) + (bytes[1]>>6 & 3));
        result[2] = (byte)(bytes[1]>>1 & 31);
        result[3] = (byte)((bytes[1]<<4 & 31) + (bytes[2]>>4 & 15));
        result[4] = (byte)((bytes[2]<<1 & 31) + (bytes[3]>>7 & 1));
        result[5] = (byte)(bytes[3]>>2 & 31);
        result[6] = (byte)((bytes[3]<<3 & 31) + (bytes[4]>>5 & 7));
        result[7] = (byte)(bytes[4] & 31);

        return result;
    }

    private static String getXPrv(DeterministicKey key)
    {
        return key.serializePrivB58(MainNetParams.get());
    }

    private static String getXPub(DeterministicKey key)
    {
        return key.serializePubB58(MainNetParams.get());
    }

    private static String getZPrv(DeterministicKey key)
    {
        return key.serializePrivB58(MainNetParams.get(), Script.ScriptType.P2WPKH);
    }

    private static String getZPub(DeterministicKey key)
    {
        return key.serializePubB58(MainNetParams.get(), Script.ScriptType.P2WPKH);
    }

    // convert an hexadecimal in string format to byte array
    private static byte[] hexStringToByteArray(String s)
    {
        // s must be an even-length string
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static char[] toCharArray(byte[] input)
    {
        if (input==null) return new char[0];

        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[input.length * 2];

        for (int j = 0; j < input.length; j++)
        {
            int v = input[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return hexChars;
    }
}

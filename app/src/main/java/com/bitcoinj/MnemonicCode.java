package com.bitcoinj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MnemonicCode {

    private static final int PBKDF2_ROUNDS = 2048;

    /**
     * Convert mnemonic word list to seed.
     */
    public static byte[] toSeed(String mnemonics) {
        return toSeed(mnemonics, "");
    }

    public static byte[] toSeed(String mnemonics, String passphrase) {
        //checkNotNull(passphrase, "A null passphrase is not allowed.");

        // To create binary seed from mnemonic, we use PBKDF2 function
        // with mnemonic sentence (in UTF-8) used as a password and
        // string "mnemonic" + passphrase (again in UTF-8) used as a
        // salt. Iteration count is set to 4096 and HMAC-SHA512 is
        // used as a pseudo-random function. Desired length of the
        // derived key is 512 bits (= 64 bytes).

        String salt = "mnemonic" + passphrase;
        return PBKDF2SHA512.derive(mnemonics, salt, PBKDF2_ROUNDS, 64);
    }

    /**
     *  Given 11 words, get the possible word 12 that match the checksum
     */
    public static List<String> getWord12(List<String> words) {

        List<String> validWords = new ArrayList<>();

        // the number of words must be 11
        if (words.size() != 11) return null;

        for (String word12 : Dictionary.english)
        {
            words.add(11, word12);
            if (checkMnemonics(words)) validWords.add(word12);
            words.remove(11);
        }
        return validWords;
    }

    // look up all the words in the diccionary
    public static boolean checkDictionary(List<String> words)
    {
        for (String word : words) {
            // Find the word index in the wordlist.
            int ndx = Collections.binarySearch(Dictionary.english, word);
            // check if the word exist in the dictionary
            if (ndx < 0) return false;
        }
        return true;
    }


    // check to see if a mnemonic word list is valid.
    public static boolean checkMnemonics(List<String> words)
    {
        // word list size must be multiple of three words
        if (words.size() % 3 > 0) return false;

        // word list is empty
        if (words.size() == 0) return false;


        // construct concatenation of original entropy and checksum.
        int concatBitsLength = words.size() * 11;
        boolean[] concatBits = new boolean[concatBitsLength];

        int wordIndex = 0;
        for (String word : words)
        {
            // Find the words index in the wordlist.
            int ndx = Collections.binarySearch(Dictionary.english, word);

            // check if the word exist in the dictionary
            if (ndx < 0) return false;

            // Set the next 11 bits to the value of the index.
            for (int i = 0; i < 11; ++i)
                concatBits[(wordIndex * 11) + i] = (ndx & (1 << (10 - i))) != 0;

            wordIndex++;
        }

        // every three words, there is 1 checksum bit
        int checksumBitsLength = concatBitsLength / 33;
        int entropyBitsLength = concatBitsLength - checksumBitsLength;

        // extract original entropy as bytes.
        byte[] entropy = new byte[entropyBitsLength / 8];
        for (int i = 0; i < entropy.length; ++i)
            for (int j = 0; j < 8; ++j)
                if (concatBits[(i * 8) + j])
                    entropy[i] |= 1 << (7 - j);

        // Take the digest of the entropy.
        byte[] hash = Sha256Hash.hash(entropy);
        boolean[] hashBits = bytesToBits(hash);

        // Check all the checksum bits.
        for (int i = 0; i < checksumBitsLength; ++i)
            if (concatBits[entropyBitsLength + i] != hashBits[i]) return false;

        return true;
    }


    private static boolean[] bytesToBits(byte[] data) {
        boolean[] bits = new boolean[data.length * 8];
        for (int i = 0; i < data.length; ++i)
            for (int j = 0; j < 8; ++j)
                bits[(i * 8) + j] = (data[i] & (1 << (7 - j))) != 0;
        return bits;
    }
}

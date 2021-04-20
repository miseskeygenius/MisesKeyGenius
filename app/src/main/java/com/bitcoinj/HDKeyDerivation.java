package com.bitcoinj;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.math.BigInteger;

import static com.bitcoinj.Utils.checkArgument;

public class HDKeyDerivation
{
    /**
     * Child derivation may fail (although with extremely low probability); in such case it is re-attempted.
     * This is the maximum number of re-attempts (to avoid an infinite loop in case of bugs etc.).
     */
    public static final int MAX_CHILD_DERIVATION_ATTEMPTS = 100;

    /**
     * Generates a new deterministic key from the given seed, which can be any arbitrary byte array. However resist
     * the temptation to use a string as the seed - any key derived from a password is likely to be weak and easily
     * broken by attackers (this is not theoretical, people have had money stolen that way). This method checks
     * that the given seed is at least 64 bits long.
     *
     * throws HDDerivationException if generated master key is invalid (private key not between 0 and n inclusive)
     */
    public static DeterministicKey createMasterPrivateKey(byte[] seed)
    {
        // Calculate I = HMAC-SHA512(key="Bitcoin seed", msg=S)
        byte[] i = HDUtils.hmacSha512(HDUtils.createHmacSha512Digest("Bitcoin seed".getBytes()), seed);
        // Split I into two 32-byte sequences, Il and Ir.
        // Use Il as master secret key, and Ir as master chain code.
        ///checkState(i.length == 64, i.length);
        byte[] il = Arrays.copyOfRange(i, 0, 32);
        byte[] ir = Arrays.copyOfRange(i, 32, 64);
        Arrays.fill(i, (byte)0);
        DeterministicKey masterPrivKey = createMasterPrivKeyFromBytes(il, ir);
        Arrays.fill(il, (byte)0);
        Arrays.fill(ir, (byte)0);
        // Child deterministic keys will chain up to their parents to find the keys.
        masterPrivKey.setCreationTimeSeconds(System.currentTimeMillis()/1000); ///Utils.currentTimeSeconds();
        return masterPrivKey;
    }

    /// @throws HDDerivationException if privKeyBytes is invalid (not between 0 and n inclusive).

    public static DeterministicKey createMasterPrivKeyFromBytes(byte[] privKeyBytes, byte[] chainCode) ///throws HDDerivationException
    {
        BigInteger priv = new BigInteger(1, privKeyBytes);
        ///assertNonZero(priv, "Generated master key is invalid.");
        ///assertLessThanN(priv, "Generated master key is invalid.");
        return new DeterministicKey(HDPath.m(), chainCode, priv, null);
    }

    /**
     * Derives a key given the "extended" child number, ie. the 0x80000000 bit of the value that you
     * pass for {@code childNumber} will determine whether to use hardened derivation or not.
     * Consider whether your code would benefit from the clarity of the equivalent, but explicit, form
     * of this method that takes a {@code ChildNumber} rather than an {@code int}, for example:
     * {@code deriveChildKey(parent, new ChildNumber(childNumber, true))}
     * where the value of the hardened bit of {@code childNumber} is zero.
     */
    public static DeterministicKey deriveChildKey(DeterministicKey parent, int childNumber) {
        return deriveChildKey(parent, new ChildNumber(childNumber));
    }

    public enum PublicDeriveMode {
        NORMAL,
        WITH_INVERSION
    }

    /**
     * @throws HDDerivationException if private derivation is attempted for a public-only parent key, or
     * if the resulting derived key is invalid (eg. private key == 0).
     */
    public static DeterministicKey deriveChildKey(DeterministicKey parent, ChildNumber childNumber) throws HDDerivationException {
        ///if (!parent.hasPrivKey())
       ///     return deriveChildKeyFromPublic(parent, childNumber, PublicDeriveMode.NORMAL);
       /// else
            return deriveChildKeyFromPrivate(parent, childNumber);
    }

    public static class RawKeyBytes {
        public final byte[] keyBytes, chainCode;

        public RawKeyBytes(byte[] keyBytes, byte[] chainCode) {
            this.keyBytes = keyBytes;
            this.chainCode = chainCode;
        }
    }

    public static DeterministicKey deriveChildKeyFromPrivate(DeterministicKey parent, ChildNumber childNumber)
            throws HDDerivationException {
        RawKeyBytes rawKey = deriveChildKeyBytesFromPrivate(parent, childNumber);
        return new DeterministicKey(parent.getPath().extend(childNumber), rawKey.chainCode,
                new BigInteger(1, rawKey.keyBytes), parent);
    }

    public static RawKeyBytes deriveChildKeyBytesFromPrivate(DeterministicKey parent,
                                                             ChildNumber childNumber) throws HDDerivationException {
        checkArgument(parent.hasPrivKey(), "Parent key must have private key bytes for this method.");
        byte[] parentPublicKey = parent.getPubKeyPoint().getEncoded(true);
        ///checkState(parentPublicKey.length == 33, "Parent pubkey must be 33 bytes, but is " + parentPublicKey.length);
        ByteBuffer data = ByteBuffer.allocate(37);
        if (childNumber.isHardened()) {
            data.put(parent.getPrivKeyBytes33());
        } else {
            data.put(parentPublicKey);
        }
        data.putInt(childNumber.i());
        byte[] i = HDUtils.hmacSha512(parent.getChainCode(), data.array());
        ///checkState(i.length == 64, i.length);
        byte[] il = Arrays.copyOfRange(i, 0, 32);
        byte[] chainCode = Arrays.copyOfRange(i, 32, 64);
        BigInteger ilInt = new BigInteger(1, il);
        ///assertLessThanN(ilInt, "Illegal derived key: I_L >= n");
        final BigInteger priv = parent.getPrivKey();
        BigInteger ki = priv.add(ilInt).mod(ECKey.CURVE.getN());
        ///assertNonZero(ki, "Illegal derived key: derived private key equals 0.");
        return new RawKeyBytes(ki.toByteArray(), chainCode);
    }
}

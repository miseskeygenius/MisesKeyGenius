package com.bitcoinj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * <p>
 * The following format is often used to represent some type of data (e.g. key or hash of key):
 * </p>
 *
 * <pre>
 * [prefix] [data bytes] [checksum]
 * </pre>
 * <p>
 * and the result is then encoded with some variant of base. This format is most commonly used for addresses and private
 * keys exported using Bitcoin Core's dumpprivkey command.
 * </p>
 */
public abstract class PrefixedChecksummedBytes implements Serializable, Cloneable {
    protected final transient NetworkParameters params;
    protected final byte[] bytes;

    protected PrefixedChecksummedBytes(NetworkParameters params, byte[] bytes) {
        this.params = params;
        this.bytes = bytes;
    }

    /**
     * @return network this data is valid for
     */
    public final NetworkParameters getParameters() {
        return params;
    }

    //esto funcionara??
    private static int objectsHash(Object... values) {
        return Arrays.hashCode(values);
    }

    @Override
    public int hashCode() {
        return objectsHash(params, Arrays.hashCode(bytes));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrefixedChecksummedBytes other = (PrefixedChecksummedBytes) o;
        return this.params.equals(other.params) && Arrays.equals(this.bytes, other.bytes);
    }

    /**
     * This implementation narrows the return type to {@link PrefixedChecksummedBytes}
     * and allows subclasses to throw {@link CloneNotSupportedException} even though it
     * is never thrown by this implementation.
     */
    @Override
    public PrefixedChecksummedBytes clone() throws CloneNotSupportedException {
        return (PrefixedChecksummedBytes) super.clone();
    }

    // Java serialization

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(params.getId());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            Field paramsField = PrefixedChecksummedBytes.class.getDeclaredField("params");
            paramsField.setAccessible(true);
            ///paramsField.set(this, checkNotNull(NetworkParameters.fromID(in.readUTF())));
            paramsField.set(this, NetworkParameters.fromID(in.readUTF()));
            paramsField.setAccessible(false);
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }
}
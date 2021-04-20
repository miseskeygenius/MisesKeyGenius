package com.bitcoinj;


/**
 * The encryption type of the wallet.
 *
 * The encryption type is UNENCRYPTED for wallets where the wallet does not support encryption - wallets prior to
 * encryption support are grandfathered in as this wallet type.
 * When a wallet is ENCRYPTED_SCRYPT_AES the keys are either encrypted with the wallet password or are unencrypted.
 */
public enum EncryptionType {
    UNENCRYPTED,                 // All keys in the wallet are unencrypted
    ENCRYPTED_SCRYPT_AES;        // All keys are encrypted with a passphrase based KDF of scrypt and AES encryption
}
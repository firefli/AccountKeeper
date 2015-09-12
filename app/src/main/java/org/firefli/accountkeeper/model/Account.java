package org.firefli.accountkeeper.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.GetChars;

import org.firefli.accountkeeper.security.EncryptionManager;

import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Created by firefli on 11/29/14.
 */
public class Account implements Parcelable {

    private String name;
    private byte[] ePass;

    public Account() {}

    public Account(String name, char[] pass, EncryptionManager eManager) throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        this.name = name;
        setPassword(eManager, pass);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasPassword() {
        return ePass != null;
    }

    public char[] getPassword(EncryptionManager eManager) throws EncryptionManager.EncryptionManagerNeedsKeyException, GeneralSecurityException {
        return eManager.decrypt(ePass);
    }

    public void setPassword(EncryptionManager eManager, GetChars password) throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
        int pwdLength = password.length();
        char[] pwd = new char[pwdLength];
        password.getChars(0, pwdLength, pwd, 0);
        setPassword(eManager, pwd);
    }

    public void setPassword(EncryptionManager eManager, char[] password) throws EncryptionManager.EncryptionManagerNeedsKeyException, GeneralSecurityException {
        this.ePass = eManager.encrypt(password);
    }

    public byte[] getRawPwd() {
        return ePass;
    }

    public void setRawPwd(byte[] rawPwd) {
        ePass = rawPwd;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Account) {
            Account otherAcct = (Account)obj;
            return this.name.equals(otherAcct.name) && Arrays.equals(this.ePass, otherAcct.ePass);
        } else {
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Account(Parcel in) {
        name = in.readString();
        ePass = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByteArray(ePass);
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}

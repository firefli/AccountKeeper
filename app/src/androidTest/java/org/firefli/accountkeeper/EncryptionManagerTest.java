package org.firefli.accountkeeper;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import org.firefli.accountkeeper.security.EncryptionManager;

import java.security.GeneralSecurityException;

/**
 * Created by firefli on 11/30/2014.
 */
public class EncryptionManagerTest extends TestCase {

    private EncryptionManager manager;

    @Before
    protected void setUp() throws GeneralSecurityException{
        manager = new EncryptionManager();
        manager.setKey(new char[]{'t','e','s','t'});
    }

    @Test
    protected void testEncrypt() throws GeneralSecurityException {
        byte[] encryptedData = manager.encrypt("I need to be encrypted".toCharArray());
        Assert.assertNotNull(encryptedData);
        Assert.assertNotSame(encryptedData.length, 0);
    }

}

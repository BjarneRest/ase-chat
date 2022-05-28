package de.bjarnerest.asechat.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bjarnerest.asechat.helper.HashingHelper;
import org.junit.jupiter.api.Test;

public class HashingHelperTest {

  @Test
  void testHashing() {

    String str = "karlsruhe";
    String hashed = HashingHelper.hashSha512WithSalt(str);

    // Test verification
    assertFalse(HashingHelper.verifySha512WithSalt("bye", hashed));
    assertTrue(HashingHelper.verifySha512WithSalt(str, hashed));


  }

  @Test
  void testVerification() {

    String str1 = "hello";
    String hash1 = "0382FA4BC54FF3D2C5ACE6A820457E68:11D5C63D3999ECDF0DA5CF23CEE55B55C9693311729A2CD6D4E2F82A5C72EA575A415CB8BA8F4E9D540EC9C36981D72A4AAFB59F6E1DC92AA565D3F49AE412BD";

    assertFalse(HashingHelper.verifySha512WithSalt("wrong", hash1));
    assertTrue(HashingHelper.verifySha512WithSalt(str1, hash1));

    String str2 = "elephant";
    String hash2 = "AE89B2EFAABEEE3F9D55871D4DF1F5D2:D0E84DD98F5645E27A98AACE19D18C49F697F91D6F80B0DF339B5873CA8D9DD65B7907ACA3615A12DCD85657CA6E137BC1C08BB059AF25037F0B57B845B0549F";

    assertFalse(HashingHelper.verifySha512WithSalt("false", hash2));
    assertTrue(HashingHelper.verifySha512WithSalt(str2, hash2));

  }


}

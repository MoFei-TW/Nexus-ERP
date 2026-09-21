package com.mofei.erp;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class HashGenTest {

    @Test
    void printHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("HASH_ADMIN=" + encoder.encode("admin"));
        System.out.println("HASH_0000000=" + encoder.encode("0000000"));
    }
}

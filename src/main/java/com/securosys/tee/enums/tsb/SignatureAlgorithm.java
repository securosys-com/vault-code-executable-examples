/**
 * Copyright (c)2005 Securosys SA, authors: Tomasz Madej
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 **/
package com.securosys.tee.enums.tsb;


import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.exceptions.tsb.BusinessReason;

public enum SignatureAlgorithm {
    SHA224_WITH_RSA_PSS("SHA224withRSA/PSS"),
    SHA256_WITH_RSA_PSS("SHA256withRSA/PSS"),
    SHA384_WITH_RSA_PSS("SHA384withRSA/PSS"),
    SHA512_WITH_RSA_PSS("SHA512withRSA/PSS"),
    NONE_WITH_DSA("NONEwithDSA"),
    SHA224_WITH_DSA("SHA224withDSA"),
    SHA256_WITH_DSA("SHA256withDSA"),
    SHA384_WITH_DSA("SHA384withDSA"),
    SHA512_WITH_DSA("SHA512withDSA"),
    NONE_WITH_RSA("NONEwithRSA"),
    SHA224_WITH_RSA("SHA224withRSA"),
    SHA256_WITH_RSA("SHA256withRSA"),
    SHA384_WITH_RSA("SHA384withRSA"),
    SHA512_WITH_RSA("SHA512withRSA"),
    NONESHA224_WITH_RSA("NONESHA224withRSA"),
    NONESHA256_WITH_RSA("NONESHA256withRSA"),
    NONESHA384_WITH_RSA("NONESHA384withRSA"),
    NONESHA512_WITH_RSA("NONESHA512withRSA"),
    NONE_WITH_ECDSA("NONEwithECDSA"),
    SHA1_WITH_ECDSA("SHA1withECDSA"),
    SHA224_WITH_ECDSA("SHA224withECDSA"),
    SHA256_WITH_ECDSA("SHA256withECDSA"),
    SHA384_WITH_ECDSA("SHA384withECDSA"),
    SHA512_WITH_ECDSA("SHA512withECDSA"),
    SHA3224_WITH_ECDSA("SHA3224withECDSA"),
    SHA3256_WITH_ECDSA("SHA3256withECDSA"),
    SHA3384_WITH_ECDSA("SHA3384withECDSA"),
    SHA3512_WITH_ECDSA("SHA3512withECDSA"),
    SHA256_WITH_ECDSA_DETERMINISTIC("SHA256withECDDSA"), // RFC6979
    DOUBLE_SHA256_WITH_ECDSA("DOUBLE_SHA256_WITH_ECDSA"),
    EDDSA("EdDSA"),
    KECCAK224_WITH_ECDSA("KECCAK224withECDSA"),
    KECCAK256_WITH_ECDSA("KECCAK256withECDSA"),
    KECCAK384_WITH_ECDSA("KECCAK384withECDSA"),
    KECCAK512_WITH_ECDSA("KECCAK512withECDSA"),
    ISS_KERL("ISS_KERL"),
    SHA1_WITH_RSA("SHA1withRSA"),
    SHA1_WITH_DSA("SHA1withDSA"),
    NONESHA1_WITH_RSA("NONESHA1withRSA"),
    SHA1_WITH_RSA_PSS("SHA1withRSA/PSS"),
    BLS("BLS"),
    DILITHIUM("Dilithium"),
    SPHINCS_PLUS("SphincsPlus"),
    KYBER("Kyber");

    private String algorithm;

    SignatureAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public static SignatureAlgorithm fromAlgorithm(String algorithm) {
        for (SignatureAlgorithm signatureAlgorithm : values()) {
            if (signatureAlgorithm.getAlgorithm().equalsIgnoreCase(algorithm)) {
                return signatureAlgorithm;
            }
        }
        String msg = String.format("algorithm='%s' can not be mapped to SignatureAlgorithm", algorithm);
        throw new BusinessException(msg, BusinessReason.ERROR_INVALID_VALUE_FOR_ENUM);
    }

    public String getAlgorithm() {
        return algorithm;
    }
}

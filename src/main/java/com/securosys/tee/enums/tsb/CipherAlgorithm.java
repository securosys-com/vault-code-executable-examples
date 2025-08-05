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

public enum CipherAlgorithm {
    RSA_PADDING_OAEP_WITH_SHA512("RSApaddingOAEPwithSHA512"),
    RSA("RSA"),
    RSA_PADDING_OAEP_WITH_SHA224("RSApaddingOAEPwithSHA224"),
    RSA_PADDING_OAEP_WITH_SHA256("RSApaddingOAEPwithSHA256"),
    RSA_PADDING_OAEP_WITH_SHA1("RSApaddingOAEPwithSHA1"),
    RSA_PADDING_OAEP("RSApaddingOAEP"),
    RSA_PADDING_OAEP_WITH_SHA384("RSApaddingOAEPwithSHA384"),
    RSA_NO_PADDING("RSAnopadding"),
    AES_GCM("AES_GCM"),
    AES_CTR("AES_CTR"),
    AES_ECB("AES_ECB"),
    AES_CBC_NO_PADDING("AES_CBC_NoPadding"),
    AES("AES"),
    CHACHA20("ChaCha20"),
    CHACHA20_AEAD("ChaCha20AEAD"),
    CAMELLIA("Camellia"),
    CAMELLIA_CBC_NO_PADDING("CAMELLIA_CBC_NoPadding"),
    CAMELLIA_ECB("CAMELLIA_ECB"),
    TDEA_CBC("TDEA_CBC"),
    TDEA_ECB("TDEA_ECB"),
    TDEA_CBC_NO_PADDING("TDEA_CBC_NoPadding");

    private String cipher;

    CipherAlgorithm(String cipher) {
        this.cipher = cipher;
    }

    public static CipherAlgorithm fromAlgorithm(String algorithm) {
        for (CipherAlgorithm cipherAlgorithm : values()) {
            if (cipherAlgorithm.getAlgorithm().equalsIgnoreCase(algorithm)) {
                return cipherAlgorithm;
            }
        }
        String msg = String.format("algorithm='%s' can not be mapped to CipherAlgorithm", algorithm);
        throw new BusinessException(msg, BusinessReason.ERROR_INVALID_VALUE_FOR_ENUM);
    }

    public String getAlgorithm() {
        return cipher;
    }
}

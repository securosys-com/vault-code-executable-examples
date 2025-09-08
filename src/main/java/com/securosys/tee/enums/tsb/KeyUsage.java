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

public enum KeyUsage {
    DIGITAL_SIGNATURE("digitalSignature"),
    CONTENT_COMMITMENT("contentCommitment"),
    KEY_ENCIPHERMENT("keyEncipherment"),
    DATA_ENCIPHERMENT("dataEncipherment"),
    KEY_AGREEMENT("keyAgreement"),
    KEY_CERT_SIGN("keyCertSign"),
    CRL_SIGN("cRLSign"),
    ENCIPHER_ONLY("encipherOnly"),
    DECIPHER_ONLY("decipherOnly");

    private String keyUsage;

    KeyUsage(String keyUsage) {
        this.keyUsage = keyUsage;
    }

    public static KeyUsage fromKeyUsageString(String keyUsage) {
        for (KeyUsage usage : values()) {
            if (usage.getKeyUsage().equalsIgnoreCase(keyUsage)) {
                return usage;
            }
        }
        String msg = String.format("keyUsage='%s' can not be mapped to KeyUsage", keyUsage);
        throw new BusinessException(msg, BusinessReason.ERROR_INVALID_VALUE_FOR_ENUM);
    }

    public String getKeyUsage() {
        return this.keyUsage;
    }
}

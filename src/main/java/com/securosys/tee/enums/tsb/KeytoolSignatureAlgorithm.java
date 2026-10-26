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

public enum KeytoolSignatureAlgorithm {
	SHA224_WITH_RSA("SHA224withRSA"),
	SHA256_WITH_RSA("SHA256withRSA"),
	SHA384_WITH_RSA("SHA384withRSA"),
	SHA512_WITH_RSA("SHA512withRSA"),
	SHA256_WITH_ECDSA("SHA256withECDSA"),
	SHA384_WITH_ECDSA("SHA384withECDSA"),
	SHA512_WITH_ECDSA("SHA512withECDSA");

    private String algorithm;

    KeytoolSignatureAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public static KeytoolSignatureAlgorithm fromAlgorithm(String algorithm) {
        for (KeytoolSignatureAlgorithm signatureAlgorithm : values()) {
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

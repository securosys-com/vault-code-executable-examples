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

public enum ExtendedKeyUsage {
    ANY_EXTENDED_KEY_USAGE("anyExtendedKeyUsage"),
    SERVER_AUTH("serverAuth"),
    CLIENT_AUTH("clientAuth"),
    CODE_SIGNING("codeSigning"),
    EMAIL_PROTECTION("emailProtection"),
    TIME_STAMPING("timeStamping"),
    OCSP_SIGNING("OCSPSigning");

    private String extKeyUsage;

    ExtendedKeyUsage(String extKeyUsage) {
        this.extKeyUsage = extKeyUsage;
    }

    public static ExtendedKeyUsage fromExtKeyUsageString(String extKeyUsage) {
        for (ExtendedKeyUsage usage : values()) {
            if (usage.getExtKeyUsage().equalsIgnoreCase(extKeyUsage)) {
                return usage;
            }
        }
        String msg = String.format("extendedKeyUsage='%s' can not be mapped to KeyUsage", extKeyUsage);
        throw new BusinessException(msg, BusinessReason.ERROR_INVALID_VALUE_FOR_ENUM);
    }

    public String getExtKeyUsage() {
        return this.extKeyUsage;
    }
}

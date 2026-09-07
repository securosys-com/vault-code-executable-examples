/**
 * Copyright (c)2026 Securosys SA, authors: Tomasz Madej
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
package com.securosys.tee.utils;

import com.securosys.primus.jce.PrimusEncoding;
import com.securosys.tee.enums.tsb.PayloadType;
import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.exceptions.tsb.BusinessReason;

import java.util.Base64;

public class EncodeDecodeUtil {

    public static byte[] decodeByType(String payload, PayloadType type) {
        if(type == null)
            return base64Decode(payload);

        switch (type.name().toUpperCase()) {
            case "HEX":
                try{
                    if (payload.length() % 2 != 0) {
                        throw new BusinessException(
                                "Invalid hex payload: length must be even (got " + payload.length() + " characters)",
                                BusinessReason.ERROR_ENCODING_EXCEPTION
                        );
                    }
                    if (!payload.matches("[0-9A-Fa-f]+")) {
                        throw new BusinessException(
                                "Invalid hex payload: contains non-hexadecimal characters",
                                BusinessReason.ERROR_ENCODING_EXCEPTION
                        );
                    }
                    return PrimusEncoding.hexDecode(payload);
                } catch (NumberFormatException e) {
                    throw new BusinessException("The 'payload' is not valid 'hex' encoded.", BusinessReason.ERROR_ENCODING_EXCEPTION);
                }
            default:
                return base64Decode(payload);
        }
    }

    public static byte[] base64Decode(String payload) {
        try {
            return Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("The 'payload' is not valid 'base64' encoded.", BusinessReason.ERROR_ENCODING_EXCEPTION);
        }
    }
}
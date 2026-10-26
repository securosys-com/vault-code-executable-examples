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
import com.securosys.primus.jce.encoding.DERObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Arrays;

public final class SignatureUtil {


    public static byte[] extractRSfromDERSignature(byte[] sig) {
        byte[] underifiedSignature = PrimusEncoding.underifyRS(sig);
        byte[] r = Arrays.copyOfRange(underifiedSignature, 0, underifiedSignature.length / 2);
        byte[] s = Arrays.copyOfRange(underifiedSignature, underifiedSignature.length / 2, underifiedSignature.length);
        return cat(r, s);
    }
    public static byte[] cat(byte[] a, byte[] b) {
        if (b == null) {
            return a;
        } else if (a == null) {
            return b;
        } else if (b.length == 0) {
            return a;
        } else if (a.length == 0) {
            return b;
        }
        final byte[] bytes = new byte[a.length + b.length];
        System.arraycopy(a, 0, bytes, 0, a.length);
        System.arraycopy(b, 0, bytes, a.length, b.length);
        return bytes;
    }



}
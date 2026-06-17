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
package com.securosys.tee.hsm;

import com.securosys.primus.jce.PrimusProvider;
import com.securosys.primus.jce.PrimusRolloverDeriveKey;
import com.securosys.primus.jce.spi0.Pkcs11StatusIds;
import com.securosys.primus.jce.spi0.SpiException;
import com.securosys.tee.dto.tsb.request.*;
import com.securosys.tee.dto.tsb.response.EncryptRequestResponseDto;
import com.securosys.tee.dto.tsb.response.SignResult;
import com.securosys.tee.dto.tsb.response.SignedKeyAttributesDto;
import com.securosys.tee.enums.tsb.CipherAlgorithm;
import com.securosys.tee.enums.tsb.PayloadType;
import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.exceptions.tsb.BusinessReason;
import com.securosys.tee.utils.EncodeDecodeUtil;
import com.securosys.tee.utils.HsmUtil;
import com.securosys.tee.utils.SignatureUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import java.security.Key;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public interface HsmAdapter {
    void login();
    void logout();
    List<String> enumerateKeys();
    void createKey(String keyName, char[] password,String keyType,String keySize, AttributesDto attributesDto) throws BusinessException;
    SignedKeyAttributesDto getKeyAttributes(String keyName, char[] password);
    byte[] sign(SignRequestDto signRequest) throws Throwable;
    byte[] decrypt(DecryptPayload decryptPayload);
    EncryptRequestResponseDto encrypt(EncryptRequestDto encryptRequest);
    boolean verify(VerifySignatureRequestDto verifySignatureRequest);
}
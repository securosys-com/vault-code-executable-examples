/**
 * Copyright (c)2025 Securosys SA, authors: Tomasz Madej
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
package com.securosys.tee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securosys.primus.jce.spi0.NotFoundException;
import com.securosys.tee.db.DatabaseAdapter;
import com.securosys.tee.db.DatabaseFactory;
import com.securosys.tee.dto.CryptoOperation;
import com.securosys.tee.dto.JvmInput;
import com.securosys.tee.dto.tsb.request.*;
import com.securosys.tee.dto.tsb.response.EncryptRequestResponseDto;
import com.securosys.tee.dto.tsb.response.SignedKeyAttributesDto;
import com.securosys.tee.enums.tsb.CipherAlgorithm;
import com.securosys.tee.enums.tsb.PayloadType;
import com.securosys.tee.enums.tsb.SignatureAlgorithm;
import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.hsm.HsmAdapter;
import com.securosys.tee.hsm.HsmJceClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class HsmExecutable {

    public static void main(String[] args) throws Throwable {
        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        JvmInput jvmInput = mapper.readValue(input, JvmInput.class);
        HsmAdapter hsmAdapter = new HsmJceClient(jvmInput.getHsmConnection());
        hsmAdapter.login();
        CryptoOperation cryptoOperation = mapper.readValue(jvmInput.getInput(), CryptoOperation.class);

        if(!hsmAdapter.isKeyExists(cryptoOperation.getKeyName())) {
            AttributesDto attributesDto = new AttributesDto();
            attributesDto.setDestroyable(true);
            attributesDto.setDecrypt(true);
            attributesDto.setEncrypt(true);
            attributesDto.setSign(true);
            attributesDto.setVerify(true);
            attributesDto.setExtractable(false);
            attributesDto.setSensitive(false);
            hsmAdapter.createKey(cryptoOperation.getKeyName(),null,"RSA","2048",attributesDto);
        }
        //SIGN
        SignRequestDto signRequestDto = new SignRequestDto();
        signRequestDto.setPayload(cryptoOperation.getPayload());
        signRequestDto.setSignKeyName(cryptoOperation.getKeyName());
        signRequestDto.setSignatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA);
        signRequestDto.setPayloadType(PayloadType.UNSPECIFIED);
        byte[] signature = hsmAdapter.sign(signRequestDto);

        //Verify
        VerifySignatureRequestDto verifySignatureRequestDto = new VerifySignatureRequestDto();
        verifySignatureRequestDto.setPayload(cryptoOperation.getPayload());
        verifySignatureRequestDto.setSignKeyName(cryptoOperation.getKeyName());
        verifySignatureRequestDto.setSignature(Base64.getEncoder().encodeToString(signature));
        verifySignatureRequestDto.setSignatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA);

        boolean valid = hsmAdapter.verify(verifySignatureRequestDto);

        //Encrypt
        EncryptRequestDto encryptRequestDto = new EncryptRequestDto();
        encryptRequestDto.setPayload(cryptoOperation.getPayload());
        encryptRequestDto.setEncryptKeyName(cryptoOperation.getKeyName());
        encryptRequestDto.setCipherAlgorithm(CipherAlgorithm.RSA);
        EncryptRequestResponseDto encrypted = hsmAdapter.encrypt(encryptRequestDto);
        //Decrypt
        DecryptPayload decryptPayload = new DecryptPayload();
        decryptPayload.setEncryptedPayload(encrypted.getEncryptedPayload());
        decryptPayload.setCipherAlgorithm(CipherAlgorithm.RSA);
        decryptPayload.setDecryptKeyName(cryptoOperation.getKeyName());
        byte[] decrypted = hsmAdapter.decrypt(decryptPayload);

        StringBuilder sb = new StringBuilder("TEST SIGN/VERIFY, ENCRYPT/DECRYPT").append("\n\n");
        sb.append("KEY: ").append(cryptoOperation.getKeyName()).append("\n");
        sb.append("PAYLOAD: ").append(cryptoOperation.getPayload()).append("\n\n");

        sb.append("SIGN/VERIFY: ").append("\n");
        sb.append("ALGORITHM: ").append(SignatureAlgorithm.SHA256_WITH_RSA.getAlgorithm()).append("\n");
        sb.append("SIGNATURE: ").append(Base64.getEncoder().encodeToString(signature)).append("\n");
        sb.append("IS VALID: ").append(valid).append("\n\n");

        sb.append("ENCRYPT/DECRYPT: ").append("\n");
        sb.append("ALGORITHM: ").append(CipherAlgorithm.RSA.getAlgorithm()).append("\n");
        sb.append("ENCRYPTED: ").append(encrypted.getEncryptedPayload()).append("\n");
        sb.append("DECRYPTED: ").append(Base64.getEncoder().encodeToString(decrypted));

        hsmAdapter.logout();
        System.out.write(sb.toString().getBytes());
        System.out.flush();

        System.exit(0);
    }
}

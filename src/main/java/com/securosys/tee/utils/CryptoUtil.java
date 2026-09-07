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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.securosys.primus.jce.PrimusAccess;
import com.securosys.primus.jce.PrimusAccessBlob;
import com.securosys.primus.jce.PrimusAccessGroup;
import com.securosys.primus.jce.PrimusAccessToken;
import com.securosys.primus.jce.PrimusApprovalToken;
import com.securosys.primus.jce.PrimusAuthorizationToken;
import com.securosys.primus.jce.PrimusSignature;
import com.securosys.primus.jce.PrimusEncoding;
import com.securosys.tee.dto.tsb.ModifyPolicyDto;
import com.securosys.tee.dto.tsb.request.SignatureDto;
import com.securosys.tee.enums.tsb.CipherAlgorithm;
import com.securosys.tee.enums.tsb.HsmRequestType;
import com.securosys.tee.enums.tsb.SignatureAlgorithm;
import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.exceptions.tsb.BusinessReason;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class offers various helper methods for using crypto operations in Java. Generally this class should only offer methods
 * that are not specific to the JCE. But it may offer methods that uses JCE specific objects like a BLS public key which is
 * a public key object. In this case the method does not separate the different public keys but works for all public keys in
 * general.
 */
public final class CryptoUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(CryptoUtil.class);

	private static final String RNG_ALGORITHM = "SHA1PRNG";

	private static final String X509_CERTIFICATE_TYPE = "X.509";





	public static String getKeyTypeForCipherAlgorithm(CipherAlgorithm cipherAlgorithm) {
		switch(cipherAlgorithm) {
			case RSA_PADDING_OAEP_WITH_SHA512:
			case RSA:
			case RSA_PADDING_OAEP_WITH_SHA224:
			case RSA_PADDING_OAEP_WITH_SHA256:
			case RSA_PADDING_OAEP_WITH_SHA1:
			case RSA_PADDING_OAEP:
			case RSA_PADDING_OAEP_WITH_SHA384:
			case RSA_NO_PADDING:
			case RSA_PADDING_PKCS:
				return "RSA";
			case AES_GCM:
			case AES_CTR:
			case AES_ECB:
			case AES_CBC_NO_PADDING:
			case AES:
				return "AES";
			case CHACHA20:
			case CHACHA20_AEAD:
				return "ChaCha20";
			case CAMELLIA:
			case CAMELLIA_CBC_NO_PADDING:
			case CAMELLIA_ECB:
				return "Camellia";
			case TDEA_CBC:
			case TDEA_CBC_NO_PADDING:
			case TDEA_ECB:
				return "TDEA";
			default:
				return "NOT_SUPPORTED";
		}
	}




}

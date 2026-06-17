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

import com.securosys.primus.jce.*;
import com.securosys.primus.jce.pqc.DilithiumMode;
import com.securosys.primus.jce.pqc.KyberMode;
import com.securosys.primus.jce.pqc.SphincsPlusMode;
import com.securosys.primus.jce.spec.EdPrivateKeyImpl;
import com.securosys.primus.jce.spec.EdPublicKeyImpl;
import com.securosys.tee.dto.tsb.ModifyPolicyDto;
import com.securosys.tee.dto.tsb.PolicyDto;
import com.securosys.tee.dto.tsb.request.CreateKeyDto;
import com.securosys.tee.dto.tsb.request.ImportKeyDto;
import com.securosys.tee.dto.tsb.request.KeyImportDto;
import com.securosys.tee.dto.tsb.response.KeyAttributesDto;
import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.exceptions.tsb.BusinessReason;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class offers helper methods for HSM specific operations. This includes all methods that depend on features offered by
 * the JCE. Additionally, this also includes methods that only make sense in conjunction with the JCE like crafting a specific
 * input for a JCE method.
 */
public final class HsmUtil {

	private static final String BIP32_PATH_SEPARATOR = "/";
	public static final String EXTERNAL_KEYSTORE_SKA_KEY_TYPE = "SKA";
	public static final String EXTERNAL_KEYSTORE_NON_SKA_KEY_TYPE = "NONSKA";
	public static final String DERIVATION_TYPE_ADA = "ADA";
	public static final String DERIVATION_TYPE_SLIP_0010 = "SLIP-10";
	public static final String DERIVATION_TYPE_ADA_OR_SLIP_0010 = "ADA/SLIP-10";

	private static final Logger LOGGER = LoggerFactory.getLogger(HsmUtil.class);

	public static OffsetDateTime getDateFromTimestamp(byte[] timestamp) {
		final PrimusTimestamp primusTimestamp = new PrimusTimestamp(timestamp);
		return OffsetDateTime.ofInstant(Instant.ofEpochSecond(primusTimestamp.getSecondsSinceEpoch()), ZoneId.systemDefault());
	}

	public static boolean isSkaKey(String keyName){
		String[][] keyType = PrimusKeyTypes.getKeyTypes(keyName);
		for (int i = 0; i < keyType.length; i++) {
			for (int j = 0; j < keyType[i].length; j++) {
				if (keyType[i][j].startsWith("Eka")) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Get key types for an alias.
	 *      *
	 *      * Returns an array of which each entry corresponds to a HSM object.
	 *      * Such entries represent pairs of object type (e.g. "PrivateKey", "PublicKey", "Certificate") and algorithm type (e.g. "DSA").
	 *      * If there is no such alias, an empty list will be returned.
	 *      *
	 *      * Known key types:
	 *      * PrivateKey, PublicKey, SecretKey, Certificate, DataObject, EkaPrivateKey, RksPrivateKey.
	 *      *
	 *      * Known algorithm types:
	 *      * RSA, DSA, DH, DHX942, EC, ED25519, ECCKD, ISS, BLS,
	 *      * RSAEKA, DSAEKA, ECEKA, ED25519EKA, ECCKDEKA, ISSEKA, BLSEKA,
	 *      * RSARKS, ECRKS,
	 *      * AES, CAMELLIA, TDEA, CHACHA20, POLY1305,
	 *      * HMACSHA1, HMACSHA224, HMACSHA256, HMACSHA384, HMACSHA512, HMACSHA3224, HMACSHA3256, HMACSHA3384, HMACSHA3512,
	 *      * UNSPECIFIED
	 */
	public static String getKeyTypeAlgorithm(String keyName){
		String[][] keyType = PrimusKeyTypes.getKeyTypes(keyName);
		for (int i = 0; i < keyType.length; i++) {
			if(keyType[i][0].equals("SecretKey") || keyType[i][0].equals("PrivateKey") || keyType[i][0].equals("EkaPrivateKey") || keyType[i][0].equals("RksPrivateKey"))
				return keyType[i][1];
		}

		return null;
	}

	public static int mapCryptoCurrency(String cryptoCurrencyFormat) {
		switch(cryptoCurrencyFormat){
			case "BTC":
				return PrimusCryptoCurrencies.BITCOIN;
			case "ETH":
				return PrimusCryptoCurrencies.ETHEREUM;
			case "XLM":
				return PrimusCryptoCurrencies.STELLAR;
			case "XRP":
				return PrimusCryptoCurrencies.RIPPLE;
			case "IOTA":
				return PrimusCryptoCurrencies.IOTA;
			default:
				throw new BusinessException("Cryptocurrency '" + cryptoCurrencyFormat + "' is not supported",
						BusinessReason.ERROR_UNSUPPORTED_CRYPTOCURRENCY);
		}
	}

	public static boolean containsBIP32Path(String keyName) {
		return keyName.contains(BIP32_PATH_SEPARATOR);
	}

	/**
	 * Returns the master key name part of a BIP32 keyname incl. derivation path. Throws exception if the master key name
	 * portion is empty.
	 * @param keyNameWithPath a BIP32 keyname concatenated with derivation path (e.g. mykey/0'/1)
	 * @return the master key name (e.g. mykey)
	 */
	public static String getBIP32MasterKeyName(String keyNameWithPath) {
		String masterKeyName = StringUtils.substringBefore(keyNameWithPath, BIP32_PATH_SEPARATOR);
		if(masterKeyName == null || masterKeyName.isBlank() || !keyNameWithPath.contains(BIP32_PATH_SEPARATOR)) {
			throw new BusinessException("BIP32 / SLIP10 key name '" + keyNameWithPath + "' does not contain master key name and path",
					BusinessReason.ERROR_INVALID_KEY_NAME);
		}
		else {
			return masterKeyName;
		}
	}

	/**
	 * Returns the derivation path part of a BIP32 keyname incl. derivation path. Throws exception if the derivation path
	 * portion is empty.
	 * @param keyNameWithPath a BIP32 keyname concatenated with derivation path (e.g. mykey/0'/1)
	 * @return the master key name (e.g. 0'/1)
	 */
	public static String getBIP32DerivationPath(String keyNameWithPath) {
		return getBIP32DerivationPath(keyNameWithPath, true);
	}

	public static String getBIP32DerivationPath(String keyNameWithPath, Boolean throwException) {
		String derivationPath = StringUtils.substringAfter(keyNameWithPath, BIP32_PATH_SEPARATOR);
		if(throwException){
			if(derivationPath == null || derivationPath.isBlank()) {
				throw new BusinessException("BIP32 / SLIP-10 key name '" + keyNameWithPath + "' does not contain master key name and path",
						BusinessReason.ERROR_INVALID_KEY_NAME);
			}
		}

		return derivationPath;
	}

	public static String getBip32ParentIndex(String keyNameWithPath) {
		return StringUtils.substringBeforeLast(keyNameWithPath, BIP32_PATH_SEPARATOR);
	}

	public static String[] getBip32Indexes(String keyNameWithPath) {
		return StringUtils.split(keyNameWithPath, BIP32_PATH_SEPARATOR);
	}

	public static int getBip32ChildIndex(String keyNameWithPath) {
		String lastPart = StringUtils.substringAfterLast(keyNameWithPath, BIP32_PATH_SEPARATOR);
		try {
            lastPart = lastPart.replace("'", "");
			return Integer.parseInt(lastPart);
		} catch (NumberFormatException e) {
			throw new BusinessException("Error parsing childIndex, last part of derivation-path is not a number: " + keyNameWithPath, BusinessReason.ERROR_KEY_ATTRIBUTES_INVALID);
		}
	}

	public static int getBip32Depth(String keyNameWithPath) {
		return StringUtils.countMatches(keyNameWithPath, BIP32_PATH_SEPARATOR);
	}

	public static boolean isSkaKey(KeyAttributesDto keyAttributes) {
		return isPolicySet(keyAttributes.getPolicy());
	}

	public static boolean isSkaKey(CreateKeyDto createKeyRequest) {
		return isPolicySet(createKeyRequest.getPolicy());
	}

	public static boolean isSkaKey(KeyImportDto keyImportRequest) {
		return isPolicySet(keyImportRequest.getPolicy());
	}

	public static boolean isSkaKey(ImportKeyDto importKeyRequest) {
		return isPolicySet(importKeyRequest.getPolicy());
	}

	private static boolean isPolicySet(PolicyDto policy) {
		return policy != null;
	}

	public static boolean isAsymmetricAlgorithm(String algorithm) {
		// search for the KeyPairGenerator services to figure out what types of keys can be generated.
		Provider primusProvider = new PrimusProvider();
		List<Provider.Service> keyPairGeneratorServices = getProviderServices(primusProvider, "KeyPairGenerator");

		// now collect all asymmetric key algorithms in a list
		Set<String> primusKeyPairGeneratorAlgorithms =
				keyPairGeneratorServices.stream()
						.map(Provider.Service::getAlgorithm)
						.filter(algo -> !algo.equals("DH")) //DH is not supported for SKA keys
						.collect(Collectors.toCollection(HashSet::new));

		//ED is not explicitly listed in the Provider Services. The reason for that probably is that it is created with setting
		// the algorithm to EC. This is why we have to explicitly allow ED here.
		primusKeyPairGeneratorAlgorithms.add("ED");

		// PQC Algorithm's do not have standard algOID's that's why we map all to the primus-kpg.
		getDilithiumParameterSpec().keySet().forEach(x -> primusKeyPairGeneratorAlgorithms.add(x));
		getSphincsPlusParameterSpec().keySet().forEach(x -> primusKeyPairGeneratorAlgorithms.add(x));
		getFips203MLKEMParameterSpec().keySet().forEach(x -> primusKeyPairGeneratorAlgorithms.add(x));

		// LOGGER.debug("Supported Algorithms: {}", primusKeyPairGeneratorAlgorithms);

		return primusKeyPairGeneratorAlgorithms.contains(algorithm);
	}

	public static boolean isSymmetricAlgorithm(String algorithm) {
		// search for the KeyGenerator services to figure out what types of keys can be generated.
		Provider primusProvider = new PrimusProvider();
		List<Provider.Service> keyGeneratorServices = getProviderServices(primusProvider, "KeyGenerator");

		// now collect all symmetric key algorithms in a list
		Set<String> primusKeyGeneratorAlgorithms =
				keyGeneratorServices.stream()
						.map(Provider.Service::getAlgorithm)
						//MAC algorithms are not supported for now
						.filter(algo -> !algo.equals("Poly1305"))
						.collect(Collectors.toCollection(HashSet::new));
		// LOGGER.debug("Supported Algorithms: {}", primusKeyGeneratorAlgorithms);

		return primusKeyGeneratorAlgorithms.contains(algorithm);
	}

	public static void checkPolicyAttachedToSymmetricKey(String algorithm){
		if(HsmUtil.isSymmetricAlgorithm(algorithm)){
			LOGGER.warn("Create symmetric key request received with key policy, this is not allowed. " +
					"The policy is ignored and the request is processed.");
		}
	}


	private static List<Provider.Service> getProviderServices(Provider provider, String serviceName) {
		List<Provider.Service> services =
				provider.getServices().stream()
						.filter(x -> x.getType().equals(serviceName)).collect(Collectors.toList());
		// services.forEach(x -> LOGGER.debug("KeyGeneratorService: {}", x));
		return services;
	}

	public static String getEcCurveNameForOID(String oid) {
		Map<String, String> curves = getSupportedEcCurves();
		curves.putAll(getSupportedEdCurves());
		LOGGER.debug("looking for curve name for oid: '{}'", oid);
		String curve = curves.get(oid);
		LOGGER.debug("curve for '{}' is '{}'", oid, curve);
		return curve;
	}

	public static Map<String, DilithiumMode> getDilithiumParameterSpec(){
		Map<String, DilithiumMode> map = new HashMap<>();
		map.put("DILITHIUM_L2", DilithiumMode.dilithiumMode4x4r3);
		map.put("DILITHIUM_L3", DilithiumMode.dilithiumMode6x5r3);
		map.put("DILITHIUM_L5", DilithiumMode.dilithiumMode8x7r3);
		map.put("ML-DSA-44", DilithiumMode.dilithiumMode44);
		map.put("ML-DSA-65", DilithiumMode.dilithiumMode65);
		map.put("ML-DSA-87", DilithiumMode.dilithiumMode87);
		return map;
	}

	public static Map<String, DilithiumMode> getFips204MLDSAParameterSpec(){
		Map<String, DilithiumMode> map = new HashMap<>();
		map.put("ML-DSA-44", DilithiumMode.dilithiumMode44);
		map.put("ML-DSA-65", DilithiumMode.dilithiumMode65);
		map.put("ML-DSA-87", DilithiumMode.dilithiumMode87);
		return map;
	}

	public static List<String> getFips204MLDSA_SLHDSAPreHashSignatureAlgorithms(){
		List<String> map = new ArrayList<>();
		map.add("SHA2-224");
		map.add("SHA2-256");
		map.add("SHA2-384");
		map.add("SHA2-512");
		map.add("SHA3-224");
		map.add("SHA3-256");
		map.add("SHA3-384");
		map.add("SHA3-512");
		map.add("SHAKE-128");
		map.add("SHAKE-256");
        map.add("ML-DSA-M");
		return map;
	}

	public static Map<String, SphincsPlusMode> getSphincsPlusParameterSpec(){
		Map<String, SphincsPlusMode> map = new HashMap<>();
		map.put("SPHINCS_PLUS_SHAKE_L1", SphincsPlusMode.sphincsPlusMode128ShakeFastr3);
		map.put("SPHINCS_PLUS_SHAKE_L3", SphincsPlusMode.sphincsPlusMode192ShakeFastr3);
		map.put("SPHINCS_PLUS_SHAKE_L5", SphincsPlusMode.sphincsPlusMode256ShakeFastr3);
		map.put("SLH-DSA-SHA2-128s", SphincsPlusMode.sphincsPlusMode128Sha2Small);
		map.put("SLH-DSA-SHA2-128f", SphincsPlusMode.sphincsPlusMode128Sha2Fast);
		map.put("SLH-DSA-SHA2-192s", SphincsPlusMode.sphincsPlusMode192Sha2Small);
		map.put("SLH-DSA-SHA2-192f", SphincsPlusMode.sphincsPlusMode192Sha2Fast);
		map.put("SLH-DSA-SHA2-256s", SphincsPlusMode.sphincsPlusMode256Sha2Small);
		map.put("SLH-DSA-SHA2-256f", SphincsPlusMode.sphincsPlusMode256Sha2Fast);
		map.put("SLH-DSA-SHAKE-128s", SphincsPlusMode.sphincsPlusMode128ShakeSmall);
		map.put("SLH-DSA-SHAKE-128f", SphincsPlusMode.sphincsPlusMode128ShakeFast);
		map.put("SLH-DSA-SHAKE-192s", SphincsPlusMode.sphincsPlusMode192ShakeSmall);
		map.put("SLH-DSA-SHAKE-192f", SphincsPlusMode.sphincsPlusMode192ShakeFast);
		map.put("SLH-DSA-SHAKE-256s", SphincsPlusMode.sphincsPlusMode256ShakeSmall);
		map.put("SLH-DSA-SHAKE-256f", SphincsPlusMode.sphincsPlusMode256ShakeFast);
		return map;
	}

	public static Map<String, SphincsPlusMode> getFips205SLHDSAParameterSpec(){
		Map<String, SphincsPlusMode> map = new HashMap<>();
		map.put("SLH-DSA-SHA2-128s", SphincsPlusMode.sphincsPlusMode128Sha2Small);
		map.put("SLH-DSA-SHA2-128f", SphincsPlusMode.sphincsPlusMode128Sha2Fast);
		map.put("SLH-DSA-SHA2-192s", SphincsPlusMode.sphincsPlusMode192Sha2Small);
		map.put("SLH-DSA-SHA2-192f", SphincsPlusMode.sphincsPlusMode192Sha2Fast);
		map.put("SLH-DSA-SHA2-256s", SphincsPlusMode.sphincsPlusMode256Sha2Small);
		map.put("SLH-DSA-SHA2-256f", SphincsPlusMode.sphincsPlusMode256Sha2Fast);
		map.put("SLH-DSA-SHAKE-128s", SphincsPlusMode.sphincsPlusMode128ShakeSmall);
		map.put("SLH-DSA-SHAKE-128f", SphincsPlusMode.sphincsPlusMode128ShakeFast);
		map.put("SLH-DSA-SHAKE-192s", SphincsPlusMode.sphincsPlusMode192ShakeSmall);
		map.put("SLH-DSA-SHAKE-192f", SphincsPlusMode.sphincsPlusMode192ShakeFast);
		map.put("SLH-DSA-SHAKE-256s", SphincsPlusMode.sphincsPlusMode256ShakeSmall);
		map.put("SLH-DSA-SHAKE-256f", SphincsPlusMode.sphincsPlusMode256ShakeFast);
		return map;
	}

	public static Map<String, KyberMode> getFips203MLKEMParameterSpec() {
		Map<String, KyberMode> map = new HashMap<>();
		map.put("ML-KEM-512", KyberMode.KyberMode512);
		map.put("ML-KEM-768", KyberMode.KyberMode768);
		map.put("ML-KEM-1024", KyberMode.KyberMode1024);
		return map;
	}

	public static String getPqcSignAlgorithm(String keyAlgorithm){
		Map<String, String> map = new HashMap<>();
		map.put("SphincsPlus-shake-128f-r3.1", "SphincsPlus");
		map.put("SphincsPlus-shake-192f-r3.1", "SphincsPlus");
		map.put("SphincsPlus-shake-256f-r3.1", "SphincsPlus");
		map.put("SLH-DSA-SHA2-128s", "SphincsPlus");
		map.put("SLH-DSA-SHA2-128f", "SphincsPlus");
		map.put("SLH-DSA-SHA2-192s", "SphincsPlus");
		map.put("SLH-DSA-SHA2-192f", "SphincsPlus");
		map.put("SLH-DSA-SHA2-256s", "SphincsPlus");
		map.put("SLH-DSA-SHA2-256f", "SphincsPlus");
		map.put("SLH-DSA-SHAKE-128s", "SphincsPlus");
		map.put("SLH-DSA-SHAKE-128f", "SphincsPlus");
		map.put("SLH-DSA-SHAKE-192s", "SphincsPlus");
		map.put("SLH-DSA-SHAKE-192f", "SphincsPlus");
		map.put("SLH-DSA-SHAKE-256s", "SphincsPlus");
		map.put("SLH-DSA-SHAKE-256f", "SphincsPlus");
		map.put("Dilithium-4x4-r3", "Dilithium");
		map.put("Dilithium-6x5-r3", "Dilithium");
		map.put("Dilithium-8x7-r3", "Dilithium");
		map.put("ML-DSA-44", "ML-DSA");
		map.put("ML-DSA-65", "ML-DSA");
		map.put("ML-DSA-87", "ML-DSA");
		map.put("HSS-LMS", "LMS");
		return map.get(keyAlgorithm);
	}

	public static String getPqcKeyAlgorithm(String keyAlgorithm){
		Map<String, String> map = new HashMap<>();
		map.put("ML-KEM-512", "ML-KEM");
		map.put("ML-KEM-768", "ML-KEM");
		map.put("ML-KEM-1024", "ML-KEM");
		map.put("SPHINCS_PLUS_SHAKE_L1", "SphincsPlus");
		map.put("SPHINCS_PLUS_SHAKE_L3", "SphincsPlus");
		map.put("SPHINCS_PLUS_SHAKE_L5", "SphincsPlus");
		map.put("SLH-DSA-SHA2-128s", "SLH-DSA");
		map.put("SLH-DSA-SHA2-128f", "SLH-DSA");
		map.put("SLH-DSA-SHA2-192s", "SLH-DSA");
		map.put("SLH-DSA-SHA2-192f", "SLH-DSA");
		map.put("SLH-DSA-SHA2-256s", "SLH-DSA");
		map.put("SLH-DSA-SHA2-256f", "SLH-DSA");
		map.put("SLH-DSA-SHAKE-128s", "SLH-DSA");
		map.put("SLH-DSA-SHAKE-128f", "SLH-DSA");
		map.put("SLH-DSA-SHAKE-192s", "SLH-DSA");
		map.put("SLH-DSA-SHAKE-192f", "SLH-DSA");
		map.put("SLH-DSA-SHAKE-256s", "SLH-DSA");
		map.put("SLH-DSA-SHAKE-256f", "SLH-DSA");
		map.put("DILITHIUM_L2", "Dilithium");
		map.put("DILITHIUM_L3", "Dilithium");
		map.put("DILITHIUM_L5", "Dilithium");
		map.put("ML-DSA-44", "ML-DSA");
		map.put("ML-DSA-65", "ML-DSA");
		map.put("ML-DSA-87", "ML-DSA");
		map.put("LMS", "LMS");
		return map.get(keyAlgorithm);
	}

	private static Map<String, String> getSupportedEcCurves() {
		Provider[] providers = Security.getProviders("AlgorithmParameters.EC");
		Provider provider = providers[0];
		String curves = provider.getService("AlgorithmParameters", "EC").getAttribute("SupportedCurves");
		Map<String, String> map = Arrays.stream(curves.split("\\|"))
				.map(s -> s.replace("[", "").replace("]", ""))
				.map(s -> s.split(","))
				.collect(Collectors.toMap(a -> a[a.length - 1], a -> a[0]));

		map.put("1.3.132.0.32", "secp224k1");
		map.put("1.3.132.0.33", "secp224r1");
		map.put("1.3.132.0.10", "secp256k1");
		map.put("1.2.840.10045.3.1.7", "secp256r1");
		map.put("1.3.132.0.34", "secp384r1");
		map.put("1.3.132.0.35", "secp521r1");

		map.put("1.2.840.10045.3.1.1", "x962p239v1");
		map.put("1.2.840.10045.3.1.2", "x962p239v2");
		map.put("1.2.840.10045.3.1.3", "x962p239v3");

		map.put("1.3.36.3.3.2.8.1.1.1", "brainpool224r1");
		map.put("1.3.36.3.3.2.8.1.1.7", "brainpool256r1");
		map.put("1.3.36.3.3.2.8.1.1.9", "brainpool320r1");
		map.put("1.3.36.3.3.2.8.1.1.11", "brainpool384r1");
		map.put("1.3.36.3.3.2.8.1.1.13", "brainpool512r1");

		map.put("1.2.250.1.223.101.256.1", "frp256v1");

		LOGGER.debug("supported curves: {}", map);
		return map;
	}

	public static String getEdCurveNameForOID(String oid) {
		Map<String, String> curves = getSupportedEdCurves();
		LOGGER.debug("looking for curve name for oid: '{}'", oid);
		String curve = curves.get(oid);
		LOGGER.debug("curve for '{}' is '{}'", oid, curve);
		return curve;
	}

	/*
	This method manually defines a mapping between the ED curves and the curve OID. It can't be loaded the same as the EC curves
	as the available providers do not contain these curve OIDs.
	 */
	private static Map<String, String> getSupportedEdCurves() {
		Map<String, String> map = new HashMap<>();
		map.put("1.3.101.112", "ed25519");
		return map;
	}

}
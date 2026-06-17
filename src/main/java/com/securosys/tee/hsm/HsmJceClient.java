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


import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;

import com.securosys.primus.jce.*;
import com.securosys.primus.jce.encoding.StringEncoding;
import com.securosys.primus.jce.spi0.*;
import com.securosys.tee.dto.tsb.PolicyDto;
import com.securosys.tee.dto.tsb.request.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.securosys.primus.jce.encoding.DEREncodingException;
import com.securosys.tee.dto.HsmConnection;
import com.securosys.tee.dto.tsb.response.EncryptRequestResponseDto;
import com.securosys.tee.dto.tsb.response.KeyAttributesDto;
import com.securosys.tee.dto.tsb.response.SignedKeyAttributesDto;
import com.securosys.tee.enums.tsb.CipherAlgorithm;
import com.securosys.tee.enums.tsb.PayloadType;
import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.exceptions.tsb.BusinessReason;
import com.securosys.tee.utils.AuthorizationExceptionHandler;
import com.securosys.tee.utils.CryptoUtil;
import com.securosys.tee.utils.EncodeDecodeUtil;
import com.securosys.tee.utils.HsmUtil;
import com.securosys.tee.utils.SignatureUtil;

import lombok.Data;

@Data
public class HsmJceClient implements HsmAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HsmJceClient.class);
    private static final String JCE_PROVIDER = PrimusProvider.getProviderName();
    private static final String CLIENT_ID = "TEE_EXECUTABLE_JCE_CLIENT";

    private static final String KEYSTORE_TYPE = PrimusProvider.getKeyStoreTypeName();

    private HsmConnection hsmConnection;

    public HsmJceClient(HsmConnection hsmConnection) {
        Security.addProvider(new PrimusProvider());

        this.hsmConnection = hsmConnection;
    }

    @Override
    public void login() {
        if (PrimusLogin.isLoggedIn()) {
            return;
        }
        final String userClientId = CLIENT_ID + "_" + hsmConnection.getUserSecret();

        PrimusConfiguration.disassociateClientId(userClientId);
        PrimusConfiguration.setClientId(userClientId);
        PrimusConfiguration.setHsmHost(hsmConnection.getHost(),hsmConnection.getPort(),hsmConnection.getUser());
        PrimusLogin.login(hsmConnection.getUser(),hsmConnection.getUserSecret().toCharArray());
    }

    @Override
    public void logout() {
        while(PrimusLogin.isLoggedIn()) {
            PrimusLogin.logout();
        }
    }

    @Override
    public List<String> enumerateKeys() {
            this.login();
            try {
                KeyStore primusKeyStore = KeyStore.getInstance(KEYSTORE_TYPE, JCE_PROVIDER);
                primusKeyStore.load(null);
                return Collections.list(primusKeyStore.aliases());
            }
            catch (Exception e) {
                throw new BusinessException("Could not enumerate keys on HSM", BusinessReason.ERROR_IO, e);
            }
        }

    @Override
    public void createKey(String keyName, char[] password, String keyType, String keySize, AttributesDto attributesDto) throws BusinessException {
        CreateKeyDto createKey = new CreateKeyDto();
        createKey.setAlgorithm(keyType);
        createKey.setKeySize(Integer.valueOf(keySize));
        createKey.setLabel(keyName);
        createKey.setAttributes(attributesDto);
        createKey.setPolicy(null);
//            createKey.setPolicy(new PolicyDto());
//            PolicyDto.KeyStatus keyStatus = new PolicyDto.KeyStatus();
//            keyStatus.setBlocked(false);
//            createKey.getPolicy().setKeyStatus(keyStatus);
        this.createNonSkaKey(createKey);

    }
    private void createNonSkaKey(CreateKeyDto createKey) {
        login();


        String keyAlgorithm = createKey.getAlgorithm();
        Key key;
        if (HsmUtil.isAsymmetricAlgorithm(keyAlgorithm)) {
            KeyPair keyPair = createAsymmetricKey(createKey);
            key = keyPair.getPrivate();
        } else {
            throw new BusinessException("Unsupported algorithm: '" + keyAlgorithm + "'",
                    BusinessReason.ERROR_INPUT_VALIDATION_FAILED);
        }

        String keyId = createKey.getId();
        if (keyId != null) {
            setKeyId(keyId, key); // TODO: sfe, 2022-11-04, TSB-5120
        }
    }
    private static void setKeyId(String id, Key key) {
        PrimusKeyFields.setSharedKeyValue(key, StringEncoding.encode(id), PrimusKeyFields.ID);
    }

    private KeyPair createAsymmetricKey(CreateKeyDto createKey) {
        // fill in the policy based on the input
        PolicyDto policy = createKey.getPolicy();
        PrimusAccess primusAccess = null;

        // create signing key
        final String keyName = createKey.getLabel();

        final KeyPair keyPair;
        try {
            PolicyDto.KeyStatus keyStatus = HsmUtil.isSkaKey(createKey) ? policy.getKeyStatus() : null;
            setFlagsAndAccess(createKey.getAttributes(), keyStatus, createKey.getAddressFormat(), primusAccess);
            KeyPairGenerator primusKeyPairGenerator = PrimusKeyPairGeneratorFactory.getKeyPairGenerator(createKey);

            LOGGER.debug("generating key with name: '{}'", keyName);
            keyPair = PrimusName.generateKeyPair(primusKeyPairGenerator, keyName, createKey.getPassword());
            final PrivateKey privateKey = keyPair.getPrivate();
            LOGGER.debug("generated private key: '{}'", privateKey);
            final PublicKey signKeyPublic = keyPair.getPublic();
            LOGGER.debug("generated public key: '{}'", signKeyPublic);
            LOGGER.debug("persisting key: '{}'", keyName);
            PrimusName.persistPublicKey(keyPair.getPublic(), keyName);

            return keyPair;
        } catch (KeyStoreException e) {
            throw new BusinessException("Could not persist public key.", BusinessReason.ERROR_IN_HSM, e);
        } catch (DuplicateEntryException e) {
            throw new BusinessException("Could not create key. The key name is already in use.",
                    BusinessReason.ERROR_KEY_ALREADY_EXISTING, e);
        } finally {
            PrimusKeyAttributes.clearKeyCapabilityFlags();
        }
    }
    private static void setKeyAccessFlags(PrimusKeyAttributes.AccessAttribute[] accessAttributes) {
        for (PrimusKeyAttributes.AccessAttribute attribute : accessAttributes) {
            PrimusKeyAttributes.setKeyAccessFlag(attribute.attribute, attribute.value);
        }
    }
    private static void setKeyCapabilityFlags(PrimusKeyAttributes.CapabilityAttribute[] capabilityAttributes) {
        for (PrimusKeyAttributes.CapabilityAttribute attribute : capabilityAttributes) {
            PrimusKeyAttributes.setKeyCapabilityFlag(attribute.attribute, attribute.value);
        }
    }
    private static PrimusKeyAttributes.CapabilityAttribute[] getCapabilityAttributes(AttributesDto attributes) {
        LOGGER.debug("processing key attributes for capability flags: {}", attributes);
        List<PrimusKeyAttributes.CapabilityAttribute> primusCapabilityAttributes = new ArrayList<>();

        primusCapabilityAttributes.add(new PrimusKeyAttributes.CapabilityAttribute(PrimusKeyAttributes.CAPABILITY_DECRYPT,
                attributes.getDecrypt()));

        primusCapabilityAttributes.add(new PrimusKeyAttributes.CapabilityAttribute(PrimusKeyAttributes.CAPABILITY_DERIVE,
                attributes.isDerive()));

        primusCapabilityAttributes.add(new PrimusKeyAttributes.CapabilityAttribute(PrimusKeyAttributes.CAPABILITY_SIGN,
                attributes.getSign()));

        primusCapabilityAttributes.add(new PrimusKeyAttributes.CapabilityAttribute(PrimusKeyAttributes.CAPABILITY_UNWRAP,
                attributes.getUnwrap()));

        if (attributes.getEncrypt() != null) {
            primusCapabilityAttributes.add(new PrimusKeyAttributes.CapabilityAttribute(PrimusKeyAttributes.CAPABILITY_ENCRYPT,
                    attributes.getEncrypt()));
        }

        if (attributes.getVerify() != null) {
            primusCapabilityAttributes.add(new PrimusKeyAttributes.CapabilityAttribute(PrimusKeyAttributes.CAPABILITY_VERIFY,
                    attributes.getVerify()));
        }

        if (attributes.getWrap() != null) {
            primusCapabilityAttributes.add(new PrimusKeyAttributes.CapabilityAttribute(PrimusKeyAttributes.CAPABILITY_WRAP,
                    attributes.getWrap()));
        }

        return primusCapabilityAttributes.toArray(PrimusKeyAttributes.CapabilityAttribute[]::new);
    }
    private static void setFlagsAndAccess(AttributesDto attributes, PolicyDto.KeyStatus keyStatus, AddressFormatDto addressFormat, PrimusAccess primusAccess) {
        if(keyStatus!=null) {
            setKeyAccessFlags(getAccessAttributes(attributes, keyStatus.getBlocked()));
        }else{
            setKeyAccessFlags(getAccessAttributes(attributes, false));
        }
        setKeyCapabilityFlags(getCapabilityAttributes(attributes));
        PrimusAccess.setAccess(primusAccess);

        if (addressFormat != null
                && addressFormat.getFormat() != null
                && !addressFormat.getFormat().isBlank()) {
            PrimusCryptoCurrencies.setCryptoCurrency(HsmUtil.mapCryptoCurrency(addressFormat.getFormat()));
        }
    }
    /**
     The attributes copyable, sensitive and neverExtractable are not set on purpose.
     */
    private static PrimusKeyAttributes.AccessAttribute[] getAccessAttributes(AttributesDto attributes,
                                                                             boolean keyStatus) {
        LOGGER.debug("processing key attributes for access flags: {}", attributes);
        List<PrimusKeyAttributes.AccessAttribute> primusAccessAttributes = new ArrayList<>();
        primusAccessAttributes.add(new PrimusKeyAttributes.AccessAttribute(PrimusKeyAttributes.ACCESS_BLOCKED,
                keyStatus));

        // take care, logic inverted
        primusAccessAttributes.add(new PrimusKeyAttributes.AccessAttribute(PrimusKeyAttributes.ACCESS_INDESTRUCTIBLE,
                !attributes.isDestroyable()));


        primusAccessAttributes.add(new PrimusKeyAttributes.AccessAttribute(PrimusKeyAttributes.ACCESS_MODIFIABLE,
                attributes.isModifiable()));


        primusAccessAttributes.add(new PrimusKeyAttributes.AccessAttribute(PrimusKeyAttributes.ACCESS_EXTRACTABLE,
                attributes.isExtractable()));


        if (attributes.getSensitive() != null) {
            primusAccessAttributes.add(new PrimusKeyAttributes.AccessAttribute(PrimusKeyAttributes.ACCESS_SENSITIVE,
                    attributes.getSensitive()));
        }

        return primusAccessAttributes.toArray(PrimusKeyAttributes.AccessAttribute[]::new);
    }


    @Override
    public SignedKeyAttributesDto getKeyAttributes(String keyName, char[] keyPassword) {
        login();
        String attestationKeyName = hsmConnection.getAttestationKeyName();
        byte[][] signature = new byte[1][];
        String xml;
        boolean hasRolloverCapability = false; // not an hsm or jce-capability
        Long derivationValue = null; // derive index is the hsm free-field: 119

        try {
            Key key = loadKeyFromKeyName(keyName, keyPassword);

            xml = PrimusAttestation.getSignedAttributes(attestationKeyName, key, signature);

            SignedKeyAttributesDto keyAttributes = new SignedKeyAttributesDto();
            keyAttributes.setXml(xml);
            keyAttributes.setJson(createJsonKeyAttributes(xml));
            keyAttributes.setXmlSignature(Base64.getEncoder().encodeToString(signature[0]));
            keyAttributes.setAttestationKeyName(attestationKeyName);
            return keyAttributes;
        }
        catch (NotFoundException e) {
            String msg = String.format("An attestation key with the name '%s' does not exist.", attestationKeyName);
            throw new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
        }

    }

    @Override
    public byte[] sign(SignRequestDto signRequest) throws Throwable {
        login();
        PrivateKey signKey;
        String keyName = signRequest.getSignKeyName();
        char[] keyPassword = signRequest.getKeyPassword();
        signKey = loadPrivateKeyFromKeyName(keyName, keyPassword);

        return createSignature(signKey,
                keyPassword,
                signRequest.getPayload(),
                hsmConnection.getAttestationKeyName(),
                signRequest.getSignatureAlgorithm().getAlgorithm(),
                signRequest.getSignatureType(),
                keyName,
                signRequest.getPayloadType());
                }

    @Override
    public byte[] decrypt(DecryptPayload decryptPayload) {
        login();
        Key decryptKey = loadKeyFromKeyName(
                decryptPayload.getDecryptKeyName(),
                decryptPayload.getKeyPassword());
        return decrypt(decryptKey, decryptPayload);

    }

    @Override
    public EncryptRequestResponseDto encrypt(EncryptRequestDto encryptRequest) {
            String cipherAlgorithm = encryptRequest.getCipherAlgorithm().getAlgorithm();
            Key encryptKey = loadEncryptKey(encryptRequest.getEncryptKeyName(), encryptRequest.getKeyPassword(),
                    cipherAlgorithm);
            try {
                EncryptRequestResponseDto resp = new EncryptRequestResponseDto();

                final Cipher cipher = Cipher.getInstance(cipherAlgorithm, PrimusProvider.getProviderName());
                if (cipherAlgorithm.equals(CipherAlgorithm.RSA_PADDING_OAEP.getAlgorithm())) {
                    cipher.init(Cipher.ENCRYPT_MODE, encryptKey, OAEPParameterSpec.DEFAULT);
                }
                else if(cipherAlgorithm.equals(CipherAlgorithm.AES_GCM.getAlgorithm()) && encryptRequest.getTagLength() != 0){
                    final byte[] iv = new byte[12];
                    (new SecureRandom()).nextBytes(iv);
                    final GCMParameterSpec gcmSpec = new GCMParameterSpec(encryptRequest.getTagLength(), iv);
                    cipher.init(Cipher.ENCRYPT_MODE, encryptKey, gcmSpec);
                }
                else {
                    cipher.init(Cipher.ENCRYPT_MODE, encryptKey);
                }
                if(encryptRequest.getAdditionalAuthenticationData() != null) {
                    String encodedAad = encryptRequest.getAdditionalAuthenticationData();
                    cipher.updateAAD(Base64.getDecoder().decode(encodedAad));
                }
                byte[] payload = Base64.getDecoder().decode(encryptRequest.getPayload());
                byte[] encryptedPayload = cipher.doFinal(payload);
                byte[] iv = cipher.getIV();

                resp.setEncryptedPayload(Base64.getEncoder().encodeToString(encryptedPayload));
                if(iv != null) {
                    resp.setInitializationVector(Base64.getEncoder().encodeToString(iv));
                }
                if(cipherAlgorithm.equals(CipherAlgorithm.AES_GCM.getAlgorithm())){
                    byte[] mac = Arrays.copyOfRange(encryptedPayload, encryptedPayload.length - (encryptRequest.getTagLength() / Byte.SIZE), encryptedPayload.length);
                    resp.setMessageAuthenticationCode(Base64.getEncoder().encodeToString(mac));
                    byte[] encPayloadWithoutMac = Arrays.copyOfRange(encryptedPayload, 0, (encryptedPayload.length - encryptRequest.getTagLength() / Byte.SIZE));
                    resp.setEncryptedPayloadWithoutMessageAuthenticationCode(Base64.getEncoder().encodeToString(encPayloadWithoutMac));
                }

                return resp;
            }
            catch (SpiException e) {
                if (e.getStatus() == Pkcs11StatusIds.KEY_FUNCTION_NOT_PERMITTED) {
                    throw new BusinessException("Key can not be used to encrypt the request as the encrypt attribute of the key is "
                            + "set to false.", BusinessReason.ERROR_KEY_ATTRIBUTES_INVALID, e);
                } else if(e.getStatus() == Pkcs11StatusIds.GENERAL_ERROR && e.getSuppressed() != null && e.getSuppressed().length > 0
                        && e.getSuppressed()[0].getMessage().equals("maybe: key size too small for mode")){
                    String msg = String.format("The payload can not be encrypted as the key is too small for mode and given payload size.");
                    throw new BusinessException(msg, BusinessReason.ERROR_INVALID_PAYLOAD);
                }
                List<Integer> listOfSupportedTagLength = List.of(128, 120, 112, 104, 96, 64);
                if (encryptRequest.getTagLength() != 0 && !listOfSupportedTagLength.contains(Integer.valueOf(encryptRequest.getTagLength())))
                    throw new BusinessException("Error specified tagLength not supported.", BusinessReason.ERROR_INVALID_TAGLENGTH);
                throw createEncryptErrorException(e);
            }
            catch (Exception e) {
                throw createEncryptErrorException(e);
            }


    }

    @Override
    public boolean verify(VerifySignatureRequestDto verifySignatureRequest) {
        boolean signatureValid;
        byte[] decPayload = EncodeDecodeUtil.decodeByType(verifySignatureRequest.getPayload(), null);
        if(verifySignatureRequest.getSignatureAlgorithm().getAlgorithm().startsWith("HMACSHA256")) {
            Key hmacKey = loadKeyFromKeyName(verifySignatureRequest.getSignKeyName(), verifySignatureRequest.getMasterKeyPassword());
            signatureValid = verifyHmacSignature((SecretKey) hmacKey, verifySignatureRequest.getSignatureAlgorithm().getAlgorithm(), decPayload, Base64.getDecoder().decode(verifySignatureRequest.getSignature()));
        }
        else {
            PublicKey signKey = loadPublicKeyFromKeyName(verifySignatureRequest.getSignKeyName());
            signatureValid = verifySignature(signKey, verifySignatureRequest.getSignatureAlgorithm().getAlgorithm(), decPayload, Base64.getDecoder().decode(verifySignatureRequest.getSignature()));
        }
        return signatureValid;
    }
    private static boolean verifyHmacSignature(SecretKey signKey, String signAlgorithm, byte[] payload, byte[] signature) {
        try {
            Mac mac = Mac.getInstance(signAlgorithm, PrimusProvider.getProviderName());
            mac.init(signKey);
            mac.update(payload);
            return Arrays.equals(signature, mac.doFinal());
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new BusinessException("Error verifying signature", BusinessReason.ERROR_IN_HSM, e);
        }
    }

    private static boolean verifySignature(PublicKey signKey,
                                           String signatureAlgorithm,
                                           byte[] payload,
                                           byte[] signature) {
        try {
            Signature verifier = Signature.getInstance(signatureAlgorithm, JCE_PROVIDER);
            verifier.initVerify(signKey);

            verifier.update(payload);
            return verifier.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException | NoSuchProviderException | InvalidKeyException e) {
            throw new BusinessException("Error verifying signature: " + e.getMessage(), BusinessReason.ERROR_IN_HSM, e);
        } finally {
            System.clearProperty("com.securosys.primus.jce.dilithiumInternal");
        }
    }

    private static byte[] decrypt(Key decryptKey, DecryptPayload decryptPayload) {
        String cipherAlgorithm = decryptPayload.getCipherAlgorithm().getAlgorithm();
        try {
            final Cipher cipher = Cipher.getInstance(cipherAlgorithm, PrimusProvider.getProviderName());
            if(decryptPayload.getInitializationVector() == null) {
                if (cipherAlgorithm.equals(CipherAlgorithm.RSA_PADDING_OAEP.getAlgorithm())) {
                    cipher.init(Cipher.DECRYPT_MODE, decryptKey, OAEPParameterSpec.DEFAULT);
                }
                else {
                    cipher.init(Cipher.DECRYPT_MODE, decryptKey);
                }
            }
            else {
                byte[] iv = Base64.getDecoder().decode(decryptPayload.getInitializationVector());
                if (cipherAlgorithm.equals(CipherAlgorithm.CHACHA20_AEAD.getAlgorithm())) {
                    GCMParameterSpec gcmSpec = new GCMParameterSpec(0, iv);
                    cipher.init(Cipher.DECRYPT_MODE, decryptKey, gcmSpec);
                } else if(decryptPayload.getTagLength() != null
                        && cipherAlgorithm.equals(CipherAlgorithm.AES_GCM.getAlgorithm())
                        && decryptPayload.getTagLength() != 0){
                    GCMParameterSpec gcmSpec = new GCMParameterSpec(decryptPayload.getTagLength(), iv);
                    cipher.init(Cipher.DECRYPT_MODE, decryptKey, gcmSpec);
                }
                else {
                    IvParameterSpec ivSpec = new IvParameterSpec(iv); //NOSONAR: The iv is cryptographically secure
                    cipher.init(Cipher.DECRYPT_MODE, decryptKey, ivSpec);
                }
            }
            if(decryptPayload.getAdditionalAuthenticationData() != null) {
                byte[] aad = Base64.getDecoder().decode(decryptPayload.getAdditionalAuthenticationData());
                cipher.updateAAD(aad);
            }
            byte[] encryptedPayload = Base64.getDecoder().decode(decryptPayload.getEncryptedPayload());
            return cipher.doFinal(encryptedPayload);
        }
        catch(AuthorizationException e) {
            throw AuthorizationExceptionHandler.process(e);
        }
        catch (SpiException e) {
            if (e.getStatus() == Pkcs11StatusIds.KEY_FUNCTION_NOT_PERMITTED) {
                LOGGER.error("Key can not be used to decrypt the request as the decrypt attribute of the key is set to false.");
            } else if(e.getMessage().contains("status: MissingParameter")) {
                if(decryptPayload.getTagLength() != 0 && stringIsNullOrEmpty(decryptPayload.getInitializationVector())){
                    throw new BusinessException("Tag length is specified, need initializationVector as well", BusinessReason.ERROR_IN_HSM, e);
                }
            }
            List<Integer> listOfSupportedTagLength = List.of(128, 120, 112, 104, 96, 64);
            if (decryptPayload.getTagLength() != null
                    && decryptPayload.getTagLength() != 0
                    && !listOfSupportedTagLength.contains(Integer.valueOf(decryptPayload.getTagLength())))
                throw new BusinessException("Error specified tagLength not supported.", BusinessReason.ERROR_INVALID_TAGLENGTH);
            throw createDecryptErrorException(e);
        }
        catch (Exception e) {
            throw createDecryptErrorException(e);
        }
    }
    private static Key loadEncryptKey(String encryptKeyName, char[] keyPassword, String cipherAlgorithm) {
        CipherAlgorithm algorithm = CipherAlgorithm.fromAlgorithm(cipherAlgorithm);
        String keyType = CryptoUtil.getKeyTypeForCipherAlgorithm(algorithm);
        if (HsmUtil.isAsymmetricAlgorithm(keyType)) {
            return loadPublicKeyFromKeyName(encryptKeyName);
        }
        else {
            return loadKeyFromKeyName(encryptKeyName, keyPassword);
        }
    }
    private static PublicKey loadPublicKeyFromKeyName(String keyName) {
        try {
            PublicKey directPublicKey = PrimusPrimitives.getPublicKeyDirectly(keyName);
                if (directPublicKey != null) return directPublicKey;
                KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE, JCE_PROVIDER);
                keyStore.load(null);
                Certificate certificate = keyStore.getCertificate(keyName);
                if (certificate != null) {
                    return certificate.getPublicKey();
                } else {
                    String msg = String.format("A key with the name '%s' does not exist.", keyName);
                    throw new BusinessException(msg, BusinessReason.ERROR_KEY_NOT_EXISTENT);
                }

        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 CertificateException | KeyStoreException | IOException e) {
            String msg = "Could not load key. Access to HSM keystore is not working properly.";
            throw new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
        }
    }
    private static BusinessException createDecryptErrorException(Exception e) {
        return new BusinessException("Error decrypting payload", BusinessReason.ERROR_IN_HSM, e);
    }

    private static KeyAttributesDto createJsonKeyAttributes(String xml) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return xmlMapper.readValue(xml, KeyAttributesDto.class);
        }
        catch (IOException e) {
            throw new BusinessException("Could not create json object from xml.",
                    BusinessReason.ERROR_DATA_INVALID_CONSTELLATION, e);
        }
    }

    private static Key loadKeyFromKeyName(String keyName, char[] keyPassword) {
        try {
            Key key;

            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE, JCE_PROVIDER);
            keyStore.load(null);
            key = keyStore.getKey(keyName, keyPassword);

            if (key == null) {
                String msg = String.format("A key with the name '%s' does not exist.", keyName);
                throw new BusinessException(msg, BusinessReason.ERROR_KEY_NOT_EXISTENT);
            }
            else {
                return key;
            }
        }
        catch (UnrecoverableKeyException e) {
            if(e.getCause() instanceof WrongKeyPasswordException) {
                String msg = String.format("Key password mismatch for key '%s'.", keyName);
                throw new BusinessException(msg, BusinessReason.ERROR_KEY_PASSWORD_MISMATCH, e);
            }
            throw createKeystoreAccessFailingException(e);
        }
        catch (IOException | KeyStoreException | NoSuchProviderException | CertificateException | NoSuchAlgorithmException e) {
            throw createKeystoreAccessFailingException(e);
        }
    }
    private static BusinessException createKeystoreAccessFailingException(Exception e) {
        String msg = "Could not load key. Access to HSM keystore is not working properly.";
        return new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
    }
    private static byte[] createSignature(PrivateKey signKey,
                                              char[] keyPassword,
                                              String payload,
                                              String attestationKeyName,
                                              String signatureAlgorithm,
                                              String signatureType,
                                              String signKeyName,
                                              PayloadType payloadType) throws Throwable {
        try {
            LOGGER.debug("setting signature algorithm '{}'", signatureAlgorithm);

            byte[] decPayload = EncodeDecodeUtil.decodeByType(payload, payloadType);
            // get signature
            Signature signature = Signature.getInstance(signatureAlgorithm, JCE_PROVIDER);

            // init sign
            signature.initSign(signKey);

            // set message
            signature.update(decPayload);

            // sign
            byte[] derSignature = signature.sign();

            // SchnorrBip0340 Support with AuxiliaryRandomData
            if(signatureType == null || signatureType.equals("DER") || signatureType.equals("RAW")) {
                if(signatureType != null && signatureType.equals("RAW")) {
                    try{
                        byte[] rs = SignatureUtil.extractRSfromDERSignature(derSignature);
                        byte[] r = Arrays.copyOfRange(rs, 0, rs.length / 2);
                        byte[] s = Arrays.copyOfRange(rs, rs.length / 2, rs.length);
                        return SignatureUtil.cat(r, s);
                    } catch (DEREncodingException e) {
                        throw new BusinessException(e.getMessage(), BusinessReason.ERROR_ENCODING_EXCEPTION, e);
                    }
                }
                return derSignature;

            }
        }
        catch(PrimusAuthorizationInsufficientException e) {
            String msg = "Error creating signature. The provided approvals are insufficient.";
            throw new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
        }
        catch (SpiException e) {
            if(e instanceof com.securosys.primus.jce.spi0.NotFoundException){
                throw new Throwable(BusinessReason.ERROR_KEY_NOT_EXISTENT.getReason());
            }
            else if (e.getStatus() == Pkcs11StatusIds.KEY_FUNCTION_NOT_PERMITTED) {
                LOGGER.error("Key can not be used to sign the request as the sign attribute of the key is set to false.");
            }
            throw createSignErrorException(e);
        }
        catch (Exception e) {
            throw createSignErrorException(e);
        }
        return null;
    }
    private static BusinessException createSignErrorException(Exception e) {
        return new BusinessException("Error creating signature: " + e.getMessage(), BusinessReason.ERROR_IN_HSM, e);
    }
    private static PrivateKey loadPrivateKeyFromKeyName(String keyName, char[] keyPassword) {
        Key key = loadKeyFromKeyName(keyName, keyPassword);
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        else {
            String msg = "Private key can not be loaded as the referenced key is not an asymmetric key.";
            throw new BusinessException(msg, BusinessReason.ERROR_INVALID_KEY_TYPE);
        }
    }
    private static BusinessException createEncryptErrorException(Exception e) {
        return new BusinessException("Error encrypting payload", BusinessReason.ERROR_IN_HSM, e);
    }

    private static boolean stringIsNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }



}


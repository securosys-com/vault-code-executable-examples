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
package com.securosys.tee.hsm;

import com.securosys.primus.jce.PrimusProvider;
import com.securosys.primus.jce.PrimusSpecs;
import com.securosys.primus.jce.pqc.DilithiumModeParameterSpec;
import com.securosys.primus.jce.pqc.KyberModeParameterSpec;
import com.securosys.primus.jce.pqc.SphincsPlusModeParameterSpec;
import com.securosys.tee.dto.tsb.request.CreateKeyDto;
import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.exceptions.tsb.BusinessReason;
import com.securosys.tee.utils.HsmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PrimusKeyPairGeneratorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimusKeyPairGeneratorFactory.class);

    private static final String JCE_PROVIDER = PrimusProvider.getProviderName();

    public static KeyPairGenerator getKeyPairGenerator(CreateKeyDto createKey) {
        final String keyAlgorithm = createKey.getAlgorithm();

        try {
            KeyPairGenerator primusKeyPairGenerator;
            String pqcAlgorithm = HsmUtil.getPqcKeyAlgorithm(keyAlgorithm);
            if ("ED".equals(keyAlgorithm)) {
                //An ED key is created with the algorithm set to EC and with an ED curve specified.
                primusKeyPairGenerator = KeyPairGenerator.getInstance("EC", JCE_PROVIDER);
            }  else {
                primusKeyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm, JCE_PROVIDER);
            }

            if ("EC".equals(keyAlgorithm)) {
                AlgorithmParameterSpec algorithmParameters;

                if (isCustomCurve(createKey.getCurveOid())) {
                    algorithmParameters = deriveAlgorithmParametersFromCustomCurve(createKey.getCurveOid());
                } else {
                    algorithmParameters = deriveAlgorithmParametersFromCurveOid(createKey.getCurveOid());
                }

                if (createKey.getAttributes().isDerive() && createKey.getAttributes().isBip32()) {
                    LOGGER.debug("Creating a an EC key with derive=true. enabling BIP32 derivation");
                    primusKeyPairGenerator.initialize(new PrimusSpecs.CkdEnabled(algorithmParameters));
                } else {
                    LOGGER.debug("Creating a an EC key with derive=false.");
                    primusKeyPairGenerator.initialize(algorithmParameters);
                }
            }
            if ("ED".equals(keyAlgorithm)) {
                //If the original algorithm was ED other curves are supported
                String curve = HsmUtil.getEdCurveNameForOID(createKey.getCurveOid());
                if (curve == null) {
                    throw new BusinessException("Unsupported Curve: '" + createKey.getCurveOid() + "'",
                            BusinessReason.ERROR_INPUT_VALIDATION_FAILED);
                }
                if (createKey.getAttributes().isDerive() && createKey.getAttributes().isBip32()) {
                    LOGGER.debug("Creating a an ED key with derive=true. enabling BIP32 derivation");
                    primusKeyPairGenerator.initialize(new PrimusSpecs.CkdEnabled(new ECGenParameterSpec(curve)));
                } else {
                    LOGGER.debug("Creating a an ED key with derive=false.");
                    primusKeyPairGenerator.initialize(new ECGenParameterSpec(curve));
                }
            }
            if (List.of("RSA", "DSA").contains(keyAlgorithm)) {
                final Integer keySize = createKey.getKeySize();
                primusKeyPairGenerator.initialize(keySize);
            }
            if (keyAlgorithm.equals("ISS")) {
                primusKeyPairGenerator.initialize(new PrimusSpecs.IssGenParameterSpec(createKey.getKeySize()));
            }

            return primusKeyPairGenerator;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            String msg = String.format("Could not initialize key pair generator for algorithm '%s'.", keyAlgorithm);
            throw new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
        }
    }

    private static ECGenParameterSpec deriveAlgorithmParametersFromCurveOid(String curveOid) {
        // figure out what curve to use:
        String curve = HsmUtil.getEcCurveNameForOID(curveOid);

        if (curve == null) {
            throw new BusinessException("Unsupported Curve: '" + curveOid + "'",
                    BusinessReason.ERROR_INPUT_VALIDATION_FAILED);
        }

        return new ECGenParameterSpec(curve);
    }

    private static ECParameterSpec deriveAlgorithmParametersFromCustomCurve(String curveOid) {
        Map<String, String> curveParameters = extractCurveParametersFromCurveOid(curveOid);
        return new ECParameterSpec(
                new EllipticCurve(
                        new ECFieldFp(new BigInteger(curveParameters.get("p"))),
                        new BigInteger(curveParameters.get("a")),
                        new BigInteger(curveParameters.get("b"))),
                new ECPoint(
                        new BigInteger(curveParameters.get("x")),
                        new BigInteger(curveParameters.get("y"))),
                new BigInteger(curveParameters.get("g")),
                Integer.parseInt(curveParameters.get("h")));
    }

    /**
     * Returns a map containing the curve parameter as the key and the parameter value as the value.
     */
    private static Map<String, String> extractCurveParametersFromCurveOid(String curveOid) {
        String[] customCurveParts = curveOid.split("\\.");

        Map<String, String> curveParameters = new HashMap<>();
        for (String curveParameter : customCurveParts) {
            String parameter = curveParameter.substring(0, 1);
            String value = curveParameter.substring(1);
            curveParameters.put(parameter, value);
        }

        return curveParameters;
    }

    private static boolean isCustomCurve(String curveOid) {
        String customCurveRegex = "p[0-9]+.a[0-9]+.b[0-9]+.x[0-9]+.y[0-9]+.g[0-9]+.h[0-9]+";
        Pattern customCurvePattern = Pattern.compile(customCurveRegex);
        return customCurvePattern.matcher(curveOid).matches();
    }

}

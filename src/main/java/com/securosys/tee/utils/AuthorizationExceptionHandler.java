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

import com.securosys.primus.jce.spi0.AuthorizationException;
import com.securosys.primus.jce.spi0.StatusIds;
import com.securosys.tee.exceptions.tsb.BusinessException;
import com.securosys.tee.exceptions.tsb.BusinessReason;

public final class AuthorizationExceptionHandler {

	/**
	 * Processes an AuthorizationException thrown by the JCE and generates an exception corresponding to the status id
	 * in the AuthorizationException
	 * @param e The AuthorizationException thrown by the JCE
	 */
	public static BusinessException process(AuthorizationException e) {
		if(e.getStatus() == StatusIds.EXTENDED_KEY_ATTRIBUTES_OBJECT_BLOCKED) {
			String msg = "The key operation failed as the key is blocked";
			return new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
		}
		if(e.getStatus() == StatusIds.EXTENDED_KEY_ATTRIBUTES_TOKEN_NOT_FOUND) {
			String msg = "The key operation failed with an authorization error. The provided approvals might be insufficient.";
			return new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
		}
		if(e.getStatus() == StatusIds.EXTENDED_KEY_ATTRIBUTES_PARAMETERS_INVALID){
			String msg = "The key operation failed with an authorization error. The extended Key Attribute Parameters are invalid.";
			return new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
		}
		String msg = String.format("The key operation failed with an authorization error. "
				+ "The status id provided by the JCE is: %s", e.getStatus());
		return new BusinessException(msg, BusinessReason.ERROR_IN_HSM, e);
	}

}

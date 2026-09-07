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
package com.securosys.tee.enums.tsb;

public enum HsmRequestType {
	SIGN(Values.SIGN),
	BLOCK(Values.BLOCK),
	UNBLOCK(Values.UNBLOCK),
	MODIFY(Values.MODIFY),
	DECRYPT(Values.DECRYPT),
	UNWRAP(Values.UNWRAP),
	CSRSIGN(Values.CSRSIGN),
	CRTSIGN(Values.CRTSIGN),
	SELFSIGN(Values.SELFSIGN);

	private String value;

	HsmRequestType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}

	// Kind of a work-around in order to be able to use the enum also for the
	// @DiscriminatorValue annotation in the sub-classes.
	public static class Values {

		public static final String SIGN = "sign";

		public static final String BLOCK = "block";

		public static final String UNBLOCK = "unblock";

		public static final String MODIFY = "modify";

		public static final String DECRYPT = "decrypt";

		public static final String UNWRAP = "unwrap";

		public static final String CSRSIGN = "csrsign";

		public static final String CRTSIGN = "crtsign";

		public static final String SELFSIGN = "selfsign";
	}
}

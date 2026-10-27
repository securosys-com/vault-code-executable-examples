/**
 * Copyright (c)2005 Securosys SA, authors: Tomasz Madej
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
package com.securosys.tee.exceptions.tsb;

import java.io.Serializable;

/**
 * @author AdNovum Informatik AG
 */
public interface Reason extends Serializable {

    /**
     * returns the reason (like "res.error.impl") which is describes which entry in a message-file to display the user.
     * Like this we can display a nice message to the user depending on the problem. For example "the lastname you entered was
     * too long"
     *
     * @return the reason.
     */
    String getReason();

    /**
     * describes the type of error that occurred. This code is used to decide how to handle the error, how the flow is supposed
     * to work.
     * For example
     * errorCode="1" means "backend down" and the UI only can fail.
     * errorCode="100" means "some business problem" and can be handled
     * errorCode="112" could mean "there was a problem on field x" and be handled differently.
     *
     * @return the error-code
     */
    int getErrorCode();

}

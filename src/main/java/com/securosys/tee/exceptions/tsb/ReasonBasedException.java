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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public abstract class ReasonBasedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReasonBasedException.class);

    // generated unique ID to identify the exception
    private final String id;

    private final Reason reason;

    ReasonBasedException(String message, Reason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        if (cause instanceof ReasonBasedException) {
            ReasonBasedException businessException = (ReasonBasedException) cause;
            id = businessException.getId();
        } else {
            id = generateErrorId();
            LOGGER.info("caused by:", cause);
        }
    }

    ReasonBasedException(String message, Reason reason) {
        super(message);
        this.reason = reason;
        id = generateErrorId();
    }

    /**
     * Creates information where this exception was thrown.
     */
    private static String whereAmI() {
        int maxDepth = 6;
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder msg = new StringBuilder();
        int depth = Math.min(stack.length, maxDepth);
        for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
            StackTraceElement currentStack = stack[currentDepth];
            String fileName = currentStack.getFileName();

            if (fileName == null) {
                fileName = "Unknown Source";
            } else {
                fileName += ":" + currentStack.getLineNumber();
            }

            msg.append(currentStack.getClassName()).append(".").append(currentStack.getMethodName()).append("(").append(fileName)
                    .append(") ");
        }

        return msg.toString();
    }

    public Reason getReason() {
        return reason;
    }

    public String getId() {
        return id;
    }

    public int getErrorCode() {
        return reason.getErrorCode();
    }

    /**
     * Generates a new errorId and logs it (and the place it was created) to the logfile
     *
     * @return the errorId
     */
    private String generateErrorId() {
        String tid = UUID.randomUUID().toString();
        String stackTrace = whereAmI();

        String stringForIdGeneration = toStringForIdGeneration(tid);
        LOGGER.info(stringForIdGeneration);
        LOGGER.info("at {}", stackTrace);

        return tid;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("id", id);
        tsb.append("reason", reason);
        tsb.append("message", getMessage());
        return tsb.toString();
    }

    /**
     * this toString like method lets us pass the tid instead of reading from the member-variable.
     * At the time this method is called, id has not yet been assigned. As it is only possible to assign id (because
     * it is declared final) in static{}-block or a constructor we have to use this little workaround.
     * We can not have generateErrorId assign it
     */
    private String toStringForIdGeneration(String tid) {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("id", tid);
        tsb.append("reason", reason);
        tsb.append("message", getMessage());
        return tsb.toString();
    }
}

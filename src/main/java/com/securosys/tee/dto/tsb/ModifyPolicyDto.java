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
package com.securosys.tee.dto.tsb;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.securosys.tee.validation.tsb.DivisibleBy60;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Base class for policy without key-status field (as this field is not needed/supported
 * when modifying the policy of a key)
 */
@Schema(description = "The new policy that shall be applied to a key. Contains the rules to use this key for signing a payload"
        + "in a sign request, the rules to block and unblock this key, and the rules to modify the policy of this key. If a "
        + "rule is empty the associated operation can be performed without any approvals.")
public class ModifyPolicyDto {

    @Valid
    @JacksonXmlProperty(localName = "rule_use")
    private Rule ruleUse;

    @Valid
    @JacksonXmlProperty(localName = "rule_block")
    private Rule ruleBlock;

    @Valid
    @JacksonXmlProperty(localName = "rule_unblock")
    private Rule ruleUnblock;

    @Valid
    @JacksonXmlProperty(localName = "rule_modify")
    private Rule ruleModify;

    public Rule getRuleUse() {
        return ruleUse;
    }

    public void setRuleUse(Rule ruleUse) {
        this.ruleUse = ruleUse;
    }

    public Rule getRuleBlock() {
        return ruleBlock;
    }

    public void setRuleBlock(Rule ruleBlock) {
        this.ruleBlock = ruleBlock;
    }

    public Rule getRuleUnblock() {
        return ruleUnblock;
    }

    public void setRuleUnblock(Rule ruleUnblock) {
        this.ruleUnblock = ruleUnblock;
    }

    public Rule getRuleModify() {
        return ruleModify;
    }

    public void setRuleModify(Rule ruleModify) {
        this.ruleModify = ruleModify;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModifyPolicyDto that = (ModifyPolicyDto) o;
        return Objects.equals(ruleUse, that.ruleUse) &&
                Objects.equals(ruleBlock, that.ruleBlock) &&
                Objects.equals(ruleUnblock, that.ruleUnblock) &&
                Objects.equals(ruleModify, that.ruleModify);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleUse, ruleBlock, ruleUnblock, ruleModify);
    }

    @Override
    public String toString() {
        return "ModifyPolicyDto{" +
                "ruleUse=" + ruleUse +
                ", ruleBlock=" + ruleBlock +
                ", ruleUnblock=" + ruleUnblock +
                ", ruleModify=" + ruleModify +
                '}';
    }

    public enum ApprovalType {
        certificate, // NOSONAR, needs to be lowercase to match the XML format which is lowercase
        public_key // NOSONAR, and if using @JsonProperty to map to lower case, the generated swagger documentation is wrong
    }

    @Schema(description = "A rule contains multiple tokens. If all groups in a token reach their quorum of approvals the rule "
            + "is fulfilled and the associated operation is performed on the HSM.")
    public static class Rule {

        @Valid
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "token")
        @NotNull
        @Schema(description = "The list of tokens in the rule.")
        private List<Token> tokens;

        public List<Token> getTokens() {
            return tokens;
        }

        public void setTokens(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public String toString() {
            return "Rule{" +
                    "tokens=" + tokens +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Rule rule = (Rule) o;
            return Objects.equals(tokens, rule.tokens);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tokens);
        }
    }

    @Schema(description = "Contains multiple groups of approvals. A token is only valid for a specified time window. The time "
            + "windows can be disabled by setting timelock and timeout to 0 which makes the token immediately active and never "
            + "expiring.")
    public static class Token {

        @JacksonXmlProperty(isAttribute = true)
        @Schema(description = "The name of the token.")
        private String name;

        @DivisibleBy60
        @NotNull
        @Schema(description = "The amount of time in seconds after which the token is valid starting from the creation time of "
                + "the request. The value must be a multiple of 60 as the seconds must represent full minutes. When the "
                + "timelock is set to 0 the token is immediately active.")
        private Integer timelock;

        @DivisibleBy60
        @NotNull
        @Schema(description = "The amount of time in seconds after which the token is no longer valid starting from the "
                + "creation time of the request. The value must be a multiple of 60 as the seconds must represent full minutes. "
                + "When the timeout is set to 0 the token is forever valid.")
        private Integer timeout;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "group")
        @Schema(description = "The list of groups in the token.", nullable = true)
        private List<Group> groups;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getTimelock() {
            return timelock;
        }

        public void setTimelock(Integer timelock) {
            this.timelock = timelock;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> groups) {
            this.groups = groups;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "name='" + name + '\'' +
                    ", timelock=" + timelock +
                    ", timeout=" + timeout +
                    ", groups=" + groups +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Token token = (Token) o;
            return Objects.equals(name, token.name) &&
                    Objects.equals(timelock, token.timelock) &&
                    Objects.equals(timeout, token.timeout) &&
                    Objects.equals(groups, token.groups);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, timelock, timeout, groups);
        }
    }

    @Schema(description = "Contains a list of approvals. Approval clients in this list can approve the request to reach the "
            + "quorum of the group.")
    public static class Group {

        @JacksonXmlProperty(isAttribute = true)
        @Schema(description = "The name of the group.")
        private String name;

        @NotNull
        @Schema(description = "The quorum of the group.")
        private Integer quorum;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "approval")
        @NotNull
        @Schema(description = "The list of approval clients that can send an approval.")
        private List<Approval> approvals;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getQuorum() {
            return quorum;
        }

        public void setQuorum(Integer quorum) {
            this.quorum = quorum;
        }

        public List<Approval> getApprovals() {
            return approvals;
        }

        public void setApprovals(List<Approval> approvals) {
            this.approvals = approvals;
        }

        @Override
        public String toString() {
            return "Group{" +
                    "name='" + name + '\'' +
                    ", quorum=" + quorum +
                    ", approvals=" + approvals +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Group group = (Group) o;
            return Objects.equals(name, group.name) &&
                    Objects.equals(quorum, group.quorum) &&
                    Objects.equals(approvals, group.approvals);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, quorum, approvals);
        }
    }

    @Schema(description = "Contains information about the approval client that can send an approval.")
    public static class Approval {

        @JacksonXmlProperty(isAttribute = true)
        @NotNull
        @Schema(description = "The type of the approval.")
        private ApprovalType type;

        @JacksonXmlProperty(isAttribute = true)
        @Schema(description = "The name of the approval. Only supported if type is 'public_key'. If type is 'certificate' the "
                + "common name in the certificate is used as name.")
        private String name;

        @JacksonXmlText
        @NotEmpty
        @Schema(description = "Either the public key if the type of the approval is public_key or a certificate (base64 "
                + "encoded) if the type is certificate.")
        private String value;

        public ApprovalType getType() {
            return type;
        }

        public void setType(ApprovalType type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Approval{" +
                    "type='" + type + '\'' +
                    ", name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Approval approval = (Approval) o;
            return Objects.equals(type, approval.type) &&
                    Objects.equals(name, approval.name) &&
                    Objects.equals(value, approval.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name, value);
        }
    }
}

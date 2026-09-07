package com.securosys.tee.dto.tsb.request;

import com.securosys.tee.validation.tsb.Base64Encoded;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class DecapsulateRequestDto {

	@NotEmpty
	@Schema(description = "Name of the key to be used for the decapsulation process.")
	private String decapsulationKeyName;

	@Schema(description = "Password of the decapsulation key.", type = "string", example = "string")
	private char[] keyPassword;

	@NotEmpty
	@Base64Encoded
	@Schema(description = "The encapsulated key.", format = "base64")
	private String ciphertext;

	@Base64Encoded
	@Schema(description = "Additional meta data that will be provided to the approval client.", format = "base64")
	private String metaData;

	@Base64Encoded
	@Schema(description = "Signature for the meta data.", format = "base64")
	private String metaDataSignature;
}
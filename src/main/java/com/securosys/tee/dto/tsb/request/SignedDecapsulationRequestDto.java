package com.securosys.tee.dto.tsb.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignedDecapsulationRequestDto {

	@NotNull
	@Valid
	private DecapsulateRequestDto decapsulationRequest;

	@Valid
	private SignatureDto requestSignature;

}

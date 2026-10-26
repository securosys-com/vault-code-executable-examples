package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"os"
)

// The JVM serializes byte[] input as a Base64 string in the outer JSON.
func execute(input []byte) ([]byte, error) {
	var envelope struct {
		Input []byte `json:"input"`
	}
	if err := json.Unmarshal(input, &envelope); err != nil {
		return nil, fmt.Errorf("invalid input envelope: %w", err)
	}
	var task struct {
		ApprovalToBeSigned string `json:"approvalToBeSigned"`
	}
	if err := json.Unmarshal(envelope.Input, &task); err != nil {
		return nil, fmt.Errorf("invalid task: %w", err)
	}
	if task.ApprovalToBeSigned == "" {
		return nil, fmt.Errorf("ApprovalToBeSigned is not found in input")
	}
	// Java's basic Base64 decoder accepts omitted trailing padding.
	decoder := base64.StdEncoding
	if len(task.ApprovalToBeSigned)%4 != 0 {
		decoder = base64.RawStdEncoding
	}
	return decoder.DecodeString(task.ApprovalToBeSigned)
}

func run() error {
	input, err := io.ReadAll(os.Stdin)
	if err != nil {
		return err
	}
	output, err := execute(input)
	if err != nil {
		return err
	}
	_, err = os.Stdout.Write(output)
	return err
}

func main() {
	if err := run(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}

// SPDX-License-Identifier: Apache-2.0

package export_wasi_cli_0_2_8_run

import (
	"fmt"
	"os"

	witTypes "go.bytecodealliance.org/pkg/wit/types"
	"securosys/tee-docker/examples/database-wasm/internal/application"
)

// Run implements wasi:cli/run@0.2.8. The componentize-go generated export
// links this symbol into the command world.
func Run() witTypes.Result[witTypes.Unit, witTypes.Unit] {
	if err := application.Run(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		return witTypes.Err[witTypes.Unit, witTypes.Unit](witTypes.Unit{})
	}
	return witTypes.Ok[witTypes.Unit, witTypes.Unit](witTypes.Unit{})
}

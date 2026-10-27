package export_wasi_cli_0_2_8_run

import (
	"fmt"
	"os"

	witTypes "go.bytecodealliance.org/pkg/wit/types"
	"securosys/tee-docker/examples/tsb-dataobject-wasm/internal/application"
)

func Run() witTypes.Result[witTypes.Unit, witTypes.Unit] {
	if err := application.Run(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		return witTypes.Err[witTypes.Unit, witTypes.Unit](witTypes.Unit{})
	}
	return witTypes.Ok[witTypes.Unit, witTypes.Unit](witTypes.Unit{})
}
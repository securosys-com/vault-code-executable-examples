package tsbjson

import "encoding/json"

func EscapeString(value string) string {
	quoted, _ := json.Marshal(value)
	return string(quoted[1 : len(quoted)-1])
}

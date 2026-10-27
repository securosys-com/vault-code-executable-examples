"""Exercise the compiled WASI module using Node.js (20+)."""
import base64
import json
from pathlib import Path
import subprocess

directory = Path(__file__).resolve().parent
module = directory.parent / "build/wasm/default-app.wasm"


def envelope(task):
    return json.dumps({
        "input": base64.b64encode(json.dumps(task).encode()).decode(),
        "timestamp": "2026-09-17T00:00:00Z",
        "database": None,
    }).encode()


def run(data):
    return subprocess.run(
        ["node", str(directory / "run.mjs"), str(module)],
        input=data, capture_output=True, check=False,
    )


for payload in [b"Hello WASM", bytes(range(256)), "Zażółć gęślą".encode()]:
    for padded in [True, False]:
        approval = base64.b64encode(payload).decode()
        if not padded:
            approval = approval.rstrip("=")
        result = run(envelope({"approvalToBeSigned": approval, "id": "example"}))
        assert result.returncode == 0, result.stderr
        assert result.stdout == payload, result.stdout

for data in [b"{", b"{}", envelope({}), envelope({"approvalToBeSigned": ""}),
             envelope({"approvalToBeSigned": "%%%"})]:
    result = run(data)
    assert result.returncode != 0, data
    assert result.stdout == b"", result.stdout

print("Passed: 6 success cases and 5 invalid-input cases on compiled WASM")

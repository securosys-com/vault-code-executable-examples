import { readFile } from 'node:fs/promises';
import { WASI } from 'node:wasi';

const path = process.argv[2];
if (!path) {
  console.error('Usage: node run.mjs <module.wasm> < input.json');
  process.exit(1);
}
const wasi = new WASI({
  version: 'preview1',
  args: [path],
  env: {},
  preopens: {},
  returnOnExit: true,
});
const module = await WebAssembly.compile(await readFile(path));
const instance = await WebAssembly.instantiate(module, {
  wasi_snapshot_preview1: wasi.wasiImport,
});
process.exitCode = wasi.start(instance);

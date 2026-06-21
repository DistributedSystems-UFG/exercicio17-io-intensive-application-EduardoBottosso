from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
PROTO_DIR = ROOT / "src" / "main" / "proto"
PROTO = PROTO_DIR / "SistemaDeEstoques.proto"
SAIDA = ROOT / "cliente-python" / "gerado"
SAIDA.mkdir(parents=True, exist_ok=True)

comando = [
    sys.executable,
    "-m",
    "grpc_tools.protoc",
    f"-I{PROTO_DIR}",
    f"--python_out={SAIDA}",
    f"--pyi_out={SAIDA}",
    f"--grpc_python_out={SAIDA}",
    str(PROTO),
]

print("Executando:", " ".join(comando))
subprocess.run(comando, check=True)
print("Stubs gerados em:", SAIDA)

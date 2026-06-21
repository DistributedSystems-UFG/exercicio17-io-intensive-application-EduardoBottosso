from __future__ import annotations

import argparse
import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

import grpc

DIRETORIO_CLIENTE = Path(__file__).resolve().parent
DIRETORIO_GERADO = DIRETORIO_CLIENTE / "gerado"
sys.path.insert(0, str(DIRETORIO_GERADO))

try:
    import SistemaDeEstoques_pb2 as pb2
    import SistemaDeEstoques_pb2_grpc as pb2_grpc
except ImportError as exc:
    raise SystemExit(
        "Stubs gRPC não encontrados. Execute: python cliente-python/gerar_stubs.py"
    ) from exc

MARMITAS = {
    "P": pb2.MARMITA_P,
    "M": pb2.MARMITA_M,
    "G": pb2.MARMITA_G,
}

INSUMOS = {
    "ARROZ": pb2.ARROZ,
    "FEIJAO": pb2.FEIJAO,
    "MACARRAO": pb2.MACARRAO,
    "CARNE": pb2.CARNE,
    "VERDURAS": pb2.VERDURAS,
    "EMBALAGEM_P": pb2.EMBALAGEM_P,
    "EMBALAGEM_M": pb2.EMBALAGEM_M,
    "EMBALAGEM_G": pb2.EMBALAGEM_G,
}


def criar_stub(host: str, porta: int):
    canal = grpc.insecure_channel(f"{host}:{porta}")
    return canal, pb2_grpc.SistemaDeEstoquesStub(canal)


def imprimir_marmitas(estoque) -> None:
    print("Marmitas:")
    print(f"  P: {estoque.quantidade_p}")
    print(f"  M: {estoque.quantidade_m}")
    print(f"  G: {estoque.quantidade_g}")


def imprimir_insumos(estoque) -> None:
    print("Insumos:")
    print(f"  Arroz: {estoque.porcoes_arroz}")
    print(f"  Feijão: {estoque.porcoes_feijao}")
    print(f"  Macarrão: {estoque.porcoes_macarrao}")
    print(f"  Carne: {estoque.porcoes_carne}")
    print(f"  Verduras: {estoque.porcoes_verduras}")
    print(f"  Embalagens P: {estoque.embalagens_p}")
    print(f"  Embalagens M: {estoque.embalagens_m}")
    print(f"  Embalagens G: {estoque.embalagens_g}")


def consultar(host: str, porta: int, atraso_ms: int = 0) -> None:
    canal, stub = criar_stub(host, porta)
    try:
        resposta = stub.ConsultarEstoqueGeral(
            pb2.ConsultaEstoqueRequest(atraso_simulado_ms=atraso_ms),
            timeout=15,
        )
        imprimir_marmitas(resposta.marmitas)
        imprimir_insumos(resposta.insumos)
        print(f"Thread no servidor: {resposta.thread_processamento}")
    finally:
        canal.close()


def ajustar_marmita(host: str, porta: int, tipo: str, variacao: int) -> None:
    tipo = tipo.upper()
    if tipo not in MARMITAS:
        raise ValueError("Tipo de marmita inválido. Use P, M ou G.")

    canal, stub = criar_stub(host, porta)
    try:
        resposta = stub.AjustarEstoqueMarmita(
            pb2.AjusteEstoqueMarmitaRequest(
                tipo=MARMITAS[tipo],
                variacao=variacao,
            ),
            timeout=10,
        )
        imprimir_operacao(resposta)
    finally:
        canal.close()


def ajustar_insumo(host: str, porta: int, tipo: str, variacao: int) -> None:
    tipo = tipo.upper()
    if tipo not in INSUMOS:
        raise ValueError("Insumo inválido: " + ", ".join(INSUMOS))

    canal, stub = criar_stub(host, porta)
    try:
        resposta = stub.AjustarEstoqueInsumo(
            pb2.AjusteEstoqueInsumoRequest(
                tipo=INSUMOS[tipo],
                variacao=variacao,
            ),
            timeout=10,
        )
        imprimir_operacao(resposta)
    finally:
        canal.close()


def definir_marmitas(host: str, porta: int, p: int, m: int, g: int) -> None:
    canal, stub = criar_stub(host, porta)
    try:
        resposta = stub.DefinirEstoqueMarmitas(
            pb2.DefinirEstoqueMarmitasRequest(
                estoque=pb2.EstoqueMarmitasData(
                    quantidade_p=p,
                    quantidade_m=m,
                    quantidade_g=g,
                )
            ),
            timeout=10,
        )
        imprimir_operacao(resposta)
    finally:
        canal.close()


def definir_insumos(host: str, porta: int, valores: dict[str, int]) -> None:
    canal, stub = criar_stub(host, porta)
    try:
        resposta = stub.DefinirEstoqueInsumos(
            pb2.DefinirEstoqueInsumosRequest(
                estoque=pb2.EstoqueInsumosData(
                    porcoes_arroz=valores["arroz"],
                    porcoes_feijao=valores["feijao"],
                    porcoes_macarrao=valores["macarrao"],
                    porcoes_carne=valores["carne"],
                    porcoes_verduras=valores["verduras"],
                    embalagens_p=valores["embalagem_p"],
                    embalagens_m=valores["embalagem_m"],
                    embalagens_g=valores["embalagem_g"],
                )
            ),
            timeout=10,
        )
        imprimir_operacao(resposta)
    finally:
        canal.close()


def zerar(host: str, porta: int, alvo: str) -> None:
    canal, stub = criar_stub(host, porta)
    try:
        if alvo == "marmitas":
            resposta = stub.ZerarEstoqueMarmitas(pb2.OperacaoVaziaRequest(), timeout=10)
        else:
            resposta = stub.ZerarEstoqueInsumos(pb2.OperacaoVaziaRequest(), timeout=10)
        imprimir_operacao(resposta)
    finally:
        canal.close()


def imprimir_operacao(resposta) -> None:
    print(resposta.mensagem)
    print(f"Quantidade atual/total: {resposta.quantidade_atual}")
    print(f"Thread no servidor: {resposta.thread_processamento}")


def requisicao_teste(host: str, porta: int, atraso_ms: int, numero: int):
    inicio = time.perf_counter()
    canal, stub = criar_stub(host, porta)
    try:
        resposta = stub.ConsultarEstoqueGeral(
            pb2.ConsultaEstoqueRequest(atraso_simulado_ms=atraso_ms),
            timeout=max(15, atraso_ms / 1000 + 10),
        )
        duracao = time.perf_counter() - inicio
        return numero, duracao, resposta.thread_processamento
    finally:
        canal.close()


def teste_concorrencia(
    host: str,
    porta: int,
    quantidade: int,
    atraso_ms: int,
    clientes: int,
) -> None:
    if quantidade <= 0 or clientes <= 0:
        raise ValueError("Quantidade e clientes devem ser maiores que zero.")

    print(
        f"Enviando {quantidade} requisições para {host}:{porta}, "
        f"com atraso de {atraso_ms} ms e {clientes} clientes concorrentes..."
    )

    inicio_total = time.perf_counter()
    resultados = []
    with ThreadPoolExecutor(max_workers=clientes) as pool:
        futures = [
            pool.submit(requisicao_teste, host, porta, atraso_ms, numero)
            for numero in range(1, quantidade + 1)
        ]
        for future in as_completed(futures):
            resultados.append(future.result())

    duracao_total = time.perf_counter() - inicio_total
    for numero, duracao, thread in sorted(resultados):
        print(f"  Requisição {numero:02d}: {duracao:.3f}s | {thread}")

    print(f"Tempo total: {duracao_total:.3f}s")


def executar_menu(host: str, porta: int) -> None:
    while True:
        print("\n=== Cliente do Sistema de Estoques ===")
        print(f"Servidor: {host}:{porta}")
        print("1 - Consultar estoque geral")
        print("2 - Ajustar estoque de marmita")
        print("3 - Ajustar estoque de insumo")
        print("4 - Definir todo o estoque de marmitas")
        print("5 - Definir todo o estoque de insumos")
        print("6 - Zerar estoque de marmitas")
        print("7 - Zerar estoque de insumos")
        print("8 - Testar requisições concorrentes")
        print("0 - Sair")

        opcao = input("Escolha: ").strip()
        try:
            if opcao == "1":
                consultar(host, porta)
            elif opcao == "2":
                tipo = input("Tipo (P/M/G): ").strip()
                variacao = int(input("Variação (+ adiciona, - retira): "))
                ajustar_marmita(host, porta, tipo, variacao)
            elif opcao == "3":
                print("Tipos: " + ", ".join(INSUMOS))
                tipo = input("Insumo: ").strip()
                variacao = int(input("Variação (+ adiciona, - retira): "))
                ajustar_insumo(host, porta, tipo, variacao)
            elif opcao == "4":
                p = int(input("Quantidade P: "))
                m = int(input("Quantidade M: "))
                g = int(input("Quantidade G: "))
                definir_marmitas(host, porta, p, m, g)
            elif opcao == "5":
                valores = {
                    "arroz": int(input("Porções de arroz: ")),
                    "feijao": int(input("Porções de feijão: ")),
                    "macarrao": int(input("Porções de macarrão: ")),
                    "carne": int(input("Porções de carne: ")),
                    "verduras": int(input("Porções de verduras: ")),
                    "embalagem_p": int(input("Embalagens P: ")),
                    "embalagem_m": int(input("Embalagens M: ")),
                    "embalagem_g": int(input("Embalagens G: ")),
                }
                definir_insumos(host, porta, valores)
            elif opcao == "6":
                zerar(host, porta, "marmitas")
            elif opcao == "7":
                zerar(host, porta, "insumos")
            elif opcao == "8":
                quantidade = int(input("Número de requisições: "))
                atraso = int(input("Atraso por requisição em ms: "))
                clientes = int(input("Número de clientes concorrentes: "))
                teste_concorrencia(host, porta, quantidade, atraso, clientes)
            elif opcao == "0":
                return
            else:
                print("Opção inválida.")
        except (ValueError, grpc.RpcError) as exc:
            imprimir_erro(exc)


def imprimir_erro(exc: Exception) -> None:
    if isinstance(exc, grpc.RpcError):
        print(f"Erro gRPC: {exc.code().name} - {exc.details()}")
    else:
        print(f"Erro: {exc}")


def criar_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Cliente Python do Sistema de Estoques gRPC")
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--porta", type=int, default=50051)

    subparsers = parser.add_subparsers(dest="comando")

    consulta = subparsers.add_parser("consultar")
    consulta.add_argument("--atraso-ms", type=int, default=0)

    marmita = subparsers.add_parser("ajustar-marmita")
    marmita.add_argument("tipo", choices=["P", "M", "G"])
    marmita.add_argument("variacao", type=int)

    insumo = subparsers.add_parser("ajustar-insumo")
    insumo.add_argument("tipo", choices=list(INSUMOS))
    insumo.add_argument("variacao", type=int)

    definir_m = subparsers.add_parser("definir-marmitas")
    definir_m.add_argument("p", type=int)
    definir_m.add_argument("m", type=int)
    definir_m.add_argument("g", type=int)

    definir_i = subparsers.add_parser("definir-insumos")
    definir_i.add_argument("--arroz", type=int, required=True)
    definir_i.add_argument("--feijao", type=int, required=True)
    definir_i.add_argument("--macarrao", type=int, required=True)
    definir_i.add_argument("--carne", type=int, required=True)
    definir_i.add_argument("--verduras", type=int, required=True)
    definir_i.add_argument("--embalagem-p", type=int, required=True)
    definir_i.add_argument("--embalagem-m", type=int, required=True)
    definir_i.add_argument("--embalagem-g", type=int, required=True)

    subparsers.add_parser("zerar-marmitas")
    subparsers.add_parser("zerar-insumos")

    teste = subparsers.add_parser("teste")
    teste.add_argument("--requisicoes", type=int, default=8)
    teste.add_argument("--atraso-ms", type=int, default=1000)
    teste.add_argument("--clientes", type=int, default=8)

    return parser


def main() -> int:
    parser = criar_parser()
    args = parser.parse_args()

    try:
        if args.comando is None:
            executar_menu(args.host, args.porta)
        elif args.comando == "consultar":
            consultar(args.host, args.porta, args.atraso_ms)
        elif args.comando == "ajustar-marmita":
            ajustar_marmita(args.host, args.porta, args.tipo, args.variacao)
        elif args.comando == "ajustar-insumo":
            ajustar_insumo(args.host, args.porta, args.tipo, args.variacao)
        elif args.comando == "definir-marmitas":
            definir_marmitas(args.host, args.porta, args.p, args.m, args.g)
        elif args.comando == "definir-insumos":
            definir_insumos(
                args.host,
                args.porta,
                {
                    "arroz": args.arroz,
                    "feijao": args.feijao,
                    "macarrao": args.macarrao,
                    "carne": args.carne,
                    "verduras": args.verduras,
                    "embalagem_p": args.embalagem_p,
                    "embalagem_m": args.embalagem_m,
                    "embalagem_g": args.embalagem_g,
                },
            )
        elif args.comando == "zerar-marmitas":
            zerar(args.host, args.porta, "marmitas")
        elif args.comando == "zerar-insumos":
            zerar(args.host, args.porta, "insumos")
        elif args.comando == "teste":
            teste_concorrencia(
                args.host,
                args.porta,
                args.requisicoes,
                args.atraso_ms,
                args.clientes,
            )
        return 0
    except (ValueError, grpc.RpcError) as exc:
        imprimir_erro(exc)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())

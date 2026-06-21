package br.ufg.marmitaria.estoques.servico;

import br.ufg.marmitaria.estoques.dominio.Insumo;
import br.ufg.marmitaria.estoques.dominio.TamanhoMarmita;
import br.ufg.marmitaria.estoques.grpc.AjusteEstoqueInsumoRequest;
import br.ufg.marmitaria.estoques.grpc.AjusteEstoqueMarmitaRequest;
import br.ufg.marmitaria.estoques.grpc.ConsultaEstoqueRequest;
import br.ufg.marmitaria.estoques.grpc.DefinirEstoqueInsumosRequest;
import br.ufg.marmitaria.estoques.grpc.DefinirEstoqueMarmitasRequest;
import br.ufg.marmitaria.estoques.grpc.EstoqueGeralResponse;
import br.ufg.marmitaria.estoques.grpc.EstoqueInsumosData;
import br.ufg.marmitaria.estoques.grpc.EstoqueInsumosResponse;
import br.ufg.marmitaria.estoques.grpc.EstoqueMarmitasData;
import br.ufg.marmitaria.estoques.grpc.EstoqueMarmitasResponse;
import br.ufg.marmitaria.estoques.grpc.OperacaoEstoqueResponse;
import br.ufg.marmitaria.estoques.grpc.OperacaoVaziaRequest;
import br.ufg.marmitaria.estoques.grpc.SistemaDeEstoquesGrpc;
import br.ufg.marmitaria.estoques.grpc.TipoInsumo;
import br.ufg.marmitaria.estoques.grpc.TipoMarmita;
import br.ufg.marmitaria.estoques.modelo.EstoqueInsumos;
import br.ufg.marmitaria.estoques.modelo.EstoqueMarmitas;
import br.ufg.marmitaria.estoques.modelo.SnapshotEstoques;
import br.ufg.marmitaria.estoques.repositorio.EstoqueArquivoRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SistemaDeEstoquesService extends SistemaDeEstoquesGrpc.SistemaDeEstoquesImplBase {
    private static final Logger LOGGER = Logger.getLogger(SistemaDeEstoquesService.class.getName());
    private static final int ATRASO_MAXIMO_MS = 10_000;

    private final ExecutorService executor;
    private final EstoqueArquivoRepository repository;

    public SistemaDeEstoquesService(
            ExecutorService executor,
            EstoqueArquivoRepository repository) {
        this.executor = executor;
        this.repository = repository;
    }

    @Override
    public void consultarEstoqueGeral(
            ConsultaEstoqueRequest request,
            StreamObserver<EstoqueGeralResponse> responseObserver) {
        executarRpc("ConsultarEstoqueGeral", responseObserver, () -> {
            simularAtraso(request.getAtrasoSimuladoMs());
            SnapshotEstoques snapshot = repository.consultarGeral();
            return EstoqueGeralResponse.newBuilder()
                    .setMarmitas(paraProto(snapshot.getMarmitas()))
                    .setInsumos(paraProto(snapshot.getInsumos()))
                    .setThreadProcessamento(Thread.currentThread().getName())
                    .build();
        });
    }

    @Override
    public void consultarEstoqueMarmitas(
            ConsultaEstoqueRequest request,
            StreamObserver<EstoqueMarmitasResponse> responseObserver) {
        executarRpc("ConsultarEstoqueMarmitas", responseObserver, () -> {
            simularAtraso(request.getAtrasoSimuladoMs());
            return EstoqueMarmitasResponse.newBuilder()
                    .setEstoque(paraProto(repository.consultarMarmitas()))
                    .setThreadProcessamento(Thread.currentThread().getName())
                    .build();
        });
    }

    @Override
    public void consultarEstoqueInsumos(
            ConsultaEstoqueRequest request,
            StreamObserver<EstoqueInsumosResponse> responseObserver) {
        executarRpc("ConsultarEstoqueInsumos", responseObserver, () -> {
            simularAtraso(request.getAtrasoSimuladoMs());
            return EstoqueInsumosResponse.newBuilder()
                    .setEstoque(paraProto(repository.consultarInsumos()))
                    .setThreadProcessamento(Thread.currentThread().getName())
                    .build();
        });
    }

    @Override
    public void ajustarEstoqueMarmita(
            AjusteEstoqueMarmitaRequest request,
            StreamObserver<OperacaoEstoqueResponse> responseObserver) {
        executarRpc("AjustarEstoqueMarmita", responseObserver, () -> {
            TamanhoMarmita tamanho = mapearTamanho(request.getTipo());
            int quantidade = repository.ajustarMarmita(tamanho, request.getVariacao());
            return respostaOperacao(
                    "Estoque da marmita " + tamanho + " atualizado.",
                    quantidade);
        });
    }

    @Override
    public void ajustarEstoqueInsumo(
            AjusteEstoqueInsumoRequest request,
            StreamObserver<OperacaoEstoqueResponse> responseObserver) {
        executarRpc("AjustarEstoqueInsumo", responseObserver, () -> {
            Insumo insumo = mapearInsumo(request.getTipo());
            int quantidade = repository.ajustarInsumo(insumo, request.getVariacao());
            return respostaOperacao(
                    "Estoque do insumo " + insumo + " atualizado.",
                    quantidade);
        });
    }

    @Override
    public void definirEstoqueMarmitas(
            DefinirEstoqueMarmitasRequest request,
            StreamObserver<OperacaoEstoqueResponse> responseObserver) {
        executarRpc("DefinirEstoqueMarmitas", responseObserver, () -> {
            if (!request.hasEstoque()) {
                throw new IllegalArgumentException("O estoque de marmitas deve ser informado.");
            }
            int total = repository.definirMarmitas(paraModelo(request.getEstoque()));
            return respostaOperacao("Estoque de marmitas definido.", total);
        });
    }

    @Override
    public void definirEstoqueInsumos(
            DefinirEstoqueInsumosRequest request,
            StreamObserver<OperacaoEstoqueResponse> responseObserver) {
        executarRpc("DefinirEstoqueInsumos", responseObserver, () -> {
            if (!request.hasEstoque()) {
                throw new IllegalArgumentException("O estoque de insumos deve ser informado.");
            }
            int total = repository.definirInsumos(paraModelo(request.getEstoque()));
            return respostaOperacao("Estoque de insumos definido.", total);
        });
    }

    @Override
    public void zerarEstoqueMarmitas(
            OperacaoVaziaRequest request,
            StreamObserver<OperacaoEstoqueResponse> responseObserver) {
        executarRpc("ZerarEstoqueMarmitas", responseObserver, () -> {
            repository.zerarMarmitas();
            return respostaOperacao("Estoque de marmitas zerado.", 0);
        });
    }

    @Override
    public void zerarEstoqueInsumos(
            OperacaoVaziaRequest request,
            StreamObserver<OperacaoEstoqueResponse> responseObserver) {
        executarRpc("ZerarEstoqueInsumos", responseObserver, () -> {
            repository.zerarInsumos();
            return respostaOperacao("Estoque de insumos zerado.", 0);
        });
    }

    private <T> void executarRpc(
            String nomeOperacao,
            StreamObserver<T> responseObserver,
            TarefaRpc<T> tarefa) {
        try {
            executor.execute(() -> {
                String thread = Thread.currentThread().getName();
                LOGGER.info(() -> nomeOperacao + " processada por " + thread);

                try {
                    T resposta = tarefa.executar();
                    responseObserver.onNext(resposta);
                    responseObserver.onCompleted();
                } catch (IllegalArgumentException e) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .withCause(e)
                            .asRuntimeException());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    responseObserver.onError(Status.CANCELLED
                            .withDescription("A operação foi interrompida.")
                            .withCause(e)
                            .asRuntimeException());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Erro de acesso aos arquivos de estoque.", e);
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("Falha ao acessar os arquivos de estoque.")
                            .withCause(e)
                            .asRuntimeException());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Erro inesperado no serviço.", e);
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("Erro inesperado no servidor.")
                            .withCause(e)
                            .asRuntimeException());
                }
            });
        } catch (RejectedExecutionException e) {
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription("O servidor está encerrando e não aceita novas requisições.")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    private static void simularAtraso(int atrasoMs) throws InterruptedException {
        if (atrasoMs < 0 || atrasoMs > ATRASO_MAXIMO_MS) {
            throw new IllegalArgumentException(
                    "O atraso simulado deve estar entre 0 e " + ATRASO_MAXIMO_MS + " ms.");
        }
        if (atrasoMs > 0) {
            Thread.sleep(atrasoMs);
        }
    }

    private static OperacaoEstoqueResponse respostaOperacao(String mensagem, int quantidadeAtual) {
        return OperacaoEstoqueResponse.newBuilder()
                .setSucesso(true)
                .setMensagem(mensagem)
                .setQuantidadeAtual(quantidadeAtual)
                .setThreadProcessamento(Thread.currentThread().getName())
                .build();
    }

    private static EstoqueMarmitasData paraProto(EstoqueMarmitas estoque) {
        return EstoqueMarmitasData.newBuilder()
                .setQuantidadeP(estoque.getQuantidadeP())
                .setQuantidadeM(estoque.getQuantidadeM())
                .setQuantidadeG(estoque.getQuantidadeG())
                .build();
    }

    private static EstoqueInsumosData paraProto(EstoqueInsumos estoque) {
        return EstoqueInsumosData.newBuilder()
                .setPorcoesArroz(estoque.getPorcoesArroz())
                .setPorcoesFeijao(estoque.getPorcoesFeijao())
                .setPorcoesMacarrao(estoque.getPorcoesMacarrao())
                .setPorcoesCarne(estoque.getPorcoesCarne())
                .setPorcoesVerduras(estoque.getPorcoesVerduras())
                .setEmbalagensP(estoque.getEmbalagensP())
                .setEmbalagensM(estoque.getEmbalagensM())
                .setEmbalagensG(estoque.getEmbalagensG())
                .build();
    }

    private static EstoqueMarmitas paraModelo(EstoqueMarmitasData estoque) {
        return new EstoqueMarmitas(
                estoque.getQuantidadeP(),
                estoque.getQuantidadeM(),
                estoque.getQuantidadeG());
    }

    private static EstoqueInsumos paraModelo(EstoqueInsumosData estoque) {
        return new EstoqueInsumos(
                estoque.getPorcoesArroz(),
                estoque.getPorcoesFeijao(),
                estoque.getPorcoesMacarrao(),
                estoque.getPorcoesCarne(),
                estoque.getPorcoesVerduras(),
                estoque.getEmbalagensP(),
                estoque.getEmbalagensM(),
                estoque.getEmbalagensG());
    }

    private static TamanhoMarmita mapearTamanho(TipoMarmita tipo) {
        return switch (tipo) {
            case MARMITA_P -> TamanhoMarmita.P;
            case MARMITA_M -> TamanhoMarmita.M;
            case MARMITA_G -> TamanhoMarmita.G;
            case TIPO_MARMITA_NAO_ESPECIFICADO, UNRECOGNIZED ->
                    throw new IllegalArgumentException("Informe um tamanho de marmita válido.");
        };
    }

    private static Insumo mapearInsumo(TipoInsumo tipo) {
        return switch (tipo) {
            case ARROZ -> Insumo.ARROZ;
            case FEIJAO -> Insumo.FEIJAO;
            case MACARRAO -> Insumo.MACARRAO;
            case CARNE -> Insumo.CARNE;
            case VERDURAS -> Insumo.VERDURAS;
            case EMBALAGEM_P -> Insumo.EMBALAGEM_P;
            case EMBALAGEM_M -> Insumo.EMBALAGEM_M;
            case EMBALAGEM_G -> Insumo.EMBALAGEM_G;
            case TIPO_INSUMO_NAO_ESPECIFICADO, UNRECOGNIZED ->
                    throw new IllegalArgumentException("Informe um insumo válido.");
        };
    }

    @FunctionalInterface
    private interface TarefaRpc<T> {
        T executar() throws Exception;
    }
}

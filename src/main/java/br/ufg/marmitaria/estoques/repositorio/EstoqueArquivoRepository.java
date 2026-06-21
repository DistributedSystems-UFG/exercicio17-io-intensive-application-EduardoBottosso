package br.ufg.marmitaria.estoques.repositorio;

import br.ufg.marmitaria.estoques.dominio.Insumo;
import br.ufg.marmitaria.estoques.dominio.TamanhoMarmita;
import br.ufg.marmitaria.estoques.modelo.EstoqueInsumos;
import br.ufg.marmitaria.estoques.modelo.EstoqueMarmitas;
import br.ufg.marmitaria.estoques.modelo.SnapshotEstoques;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EstoqueArquivoRepository {
    private final Path arquivoMarmitas;
    private final Path arquivoInsumos;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public EstoqueArquivoRepository(Path diretorioDados) throws IOException {
        Path diretorio = diretorioDados.toAbsolutePath().normalize();
        Files.createDirectories(diretorio);

        this.arquivoMarmitas = diretorio.resolve("estoque_marmitas.json");
        this.arquivoInsumos = diretorio.resolve("estoque_insumos.json");
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        inicializarArquivosAusentes();
    }

    public SnapshotEstoques consultarGeral() throws IOException {
        lock.readLock().lock();
        try {
            return new SnapshotEstoques(lerMarmitasSemLock(), lerInsumosSemLock());
        } finally {
            lock.readLock().unlock();
        }
    }

    public EstoqueMarmitas consultarMarmitas() throws IOException {
        lock.readLock().lock();
        try {
            return lerMarmitasSemLock();
        } finally {
            lock.readLock().unlock();
        }
    }

    public EstoqueInsumos consultarInsumos() throws IOException {
        lock.readLock().lock();
        try {
            return lerInsumosSemLock();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int ajustarMarmita(TamanhoMarmita tamanho, int variacao) throws IOException {
        lock.writeLock().lock();
        try {
            EstoqueMarmitas estoque = lerMarmitasSemLock();
            int quantidadeAtual = obterQuantidadeMarmita(estoque, tamanho);
            int novaQuantidade = somarSemEstouro(quantidadeAtual, variacao);
            validarNaoNegativo(novaQuantidade, "A quantidade de marmitas não pode ficar negativa.");

            definirQuantidadeMarmita(estoque, tamanho, novaQuantidade);
            gravarAtomico(arquivoMarmitas, estoque);
            return novaQuantidade;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int ajustarInsumo(Insumo insumo, int variacao) throws IOException {
        lock.writeLock().lock();
        try {
            EstoqueInsumos estoque = lerInsumosSemLock();
            int quantidadeAtual = obterQuantidadeInsumo(estoque, insumo);
            int novaQuantidade = somarSemEstouro(quantidadeAtual, variacao);
            validarNaoNegativo(novaQuantidade, "A quantidade do insumo não pode ficar negativa.");

            definirQuantidadeInsumo(estoque, insumo, novaQuantidade);
            gravarAtomico(arquivoInsumos, estoque);
            return novaQuantidade;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int definirMarmitas(EstoqueMarmitas estoque) throws IOException {
        validarEstoqueMarmitas(estoque);
        lock.writeLock().lock();
        try {
            gravarAtomico(arquivoMarmitas, estoque);
            return estoque.total();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int definirInsumos(EstoqueInsumos estoque) throws IOException {
        validarEstoqueInsumos(estoque);
        lock.writeLock().lock();
        try {
            gravarAtomico(arquivoInsumos, estoque);
            return estoque.total();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void zerarMarmitas() throws IOException {
        definirMarmitas(new EstoqueMarmitas(0, 0, 0));
    }

    public void zerarInsumos() throws IOException {
        definirInsumos(new EstoqueInsumos(0, 0, 0, 0, 0, 0, 0, 0));
    }

    private void inicializarArquivosAusentes() throws IOException {
        if (Files.notExists(arquivoMarmitas)) {
            gravarAtomico(arquivoMarmitas, new EstoqueMarmitas(0, 0, 0));
        }
        if (Files.notExists(arquivoInsumos)) {
            gravarAtomico(arquivoInsumos, new EstoqueInsumos(0, 0, 0, 0, 0, 0, 0, 0));
        }
    }

    private EstoqueMarmitas lerMarmitasSemLock() throws IOException {
        return lerArquivo(arquivoMarmitas, EstoqueMarmitas.class);
    }

    private EstoqueInsumos lerInsumosSemLock() throws IOException {
        return lerArquivo(arquivoInsumos, EstoqueInsumos.class);
    }

    private <T> T lerArquivo(Path arquivo, Class<T> tipo) throws IOException {
        try (InputStream input = Files.newInputStream(arquivo)) {
            return objectMapper.readValue(input, tipo);
        }
    }

    private void gravarAtomico(Path destino, Object valor) throws IOException {
        Path temporario = Files.createTempFile(destino.getParent(), destino.getFileName().toString(), ".tmp");
        boolean movido = false;
        try {
            objectMapper.writeValue(temporario.toFile(), valor);
            try {
                Files.move(
                        temporario,
                        destino,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            movido = true;
        } finally {
            if (!movido) {
                Files.deleteIfExists(temporario);
            }
        }
    }

    private static int somarSemEstouro(int atual, int variacao) {
        try {
            return Math.addExact(atual, variacao);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("A operação ultrapassou o limite de um inteiro.", e);
        }
    }

    private static void validarNaoNegativo(int valor, String mensagem) {
        if (valor < 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private static void validarEstoqueMarmitas(EstoqueMarmitas estoque) {
        validarNaoNegativo(estoque.getQuantidadeP(), "A quantidade P não pode ser negativa.");
        validarNaoNegativo(estoque.getQuantidadeM(), "A quantidade M não pode ser negativa.");
        validarNaoNegativo(estoque.getQuantidadeG(), "A quantidade G não pode ser negativa.");
    }

    private static void validarEstoqueInsumos(EstoqueInsumos estoque) {
        validarNaoNegativo(estoque.getPorcoesArroz(), "As porções de arroz não podem ser negativas.");
        validarNaoNegativo(estoque.getPorcoesFeijao(), "As porções de feijão não podem ser negativas.");
        validarNaoNegativo(estoque.getPorcoesMacarrao(), "As porções de macarrão não podem ser negativas.");
        validarNaoNegativo(estoque.getPorcoesCarne(), "As porções de carne não podem ser negativas.");
        validarNaoNegativo(estoque.getPorcoesVerduras(), "As porções de verduras não podem ser negativas.");
        validarNaoNegativo(estoque.getEmbalagensP(), "As embalagens P não podem ser negativas.");
        validarNaoNegativo(estoque.getEmbalagensM(), "As embalagens M não podem ser negativas.");
        validarNaoNegativo(estoque.getEmbalagensG(), "As embalagens G não podem ser negativas.");
    }

    private static int obterQuantidadeMarmita(EstoqueMarmitas estoque, TamanhoMarmita tamanho) {
        return switch (tamanho) {
            case P -> estoque.getQuantidadeP();
            case M -> estoque.getQuantidadeM();
            case G -> estoque.getQuantidadeG();
        };
    }

    private static void definirQuantidadeMarmita(
            EstoqueMarmitas estoque,
            TamanhoMarmita tamanho,
            int quantidade) {
        switch (tamanho) {
            case P -> estoque.setQuantidadeP(quantidade);
            case M -> estoque.setQuantidadeM(quantidade);
            case G -> estoque.setQuantidadeG(quantidade);
        }
    }

    private static int obterQuantidadeInsumo(EstoqueInsumos estoque, Insumo insumo) {
        return switch (insumo) {
            case ARROZ -> estoque.getPorcoesArroz();
            case FEIJAO -> estoque.getPorcoesFeijao();
            case MACARRAO -> estoque.getPorcoesMacarrao();
            case CARNE -> estoque.getPorcoesCarne();
            case VERDURAS -> estoque.getPorcoesVerduras();
            case EMBALAGEM_P -> estoque.getEmbalagensP();
            case EMBALAGEM_M -> estoque.getEmbalagensM();
            case EMBALAGEM_G -> estoque.getEmbalagensG();
        };
    }

    private static void definirQuantidadeInsumo(
            EstoqueInsumos estoque,
            Insumo insumo,
            int quantidade) {
        switch (insumo) {
            case ARROZ -> estoque.setPorcoesArroz(quantidade);
            case FEIJAO -> estoque.setPorcoesFeijao(quantidade);
            case MACARRAO -> estoque.setPorcoesMacarrao(quantidade);
            case CARNE -> estoque.setPorcoesCarne(quantidade);
            case VERDURAS -> estoque.setPorcoesVerduras(quantidade);
            case EMBALAGEM_P -> estoque.setEmbalagensP(quantidade);
            case EMBALAGEM_M -> estoque.setEmbalagensM(quantidade);
            case EMBALAGEM_G -> estoque.setEmbalagensG(quantidade);
        }
    }
}

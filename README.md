# Sistema de Estoques gRPC — Marmitaria

Projeto didático que implementa um serviço de rede que lê e atualiza dados em arquivos JSON no servidor.
A mesma lógica de estoque é executada em três modelos de concorrência:

1. **Single-threaded** — um único worker processa todas as requisições.
2. **Thread por requisição** — cada requisição cria uma nova thread de plataforma.
3. **Pool de threads** — um conjunto fixo de workers processa as requisições.

O servidor é implementado em Java e o cliente em Python. O contrato comum está em Protocol Buffers/gRPC.

## Estrutura

```text
sistema-estoques-grpc/
├── dados/
│   ├── estoque_marmitas.json
│   └── estoque_insumos.json
├── src/main/proto/
│   └── SistemaDeEstoques.proto
├── src/main/java/br/ufg/marmitaria/estoques/
│   ├── concorrencia/
│   ├── dominio/
│   ├── modelo/
│   ├── repositorio/
│   ├── servico/
│   └── servidor/
├── cliente-python/
│   ├── cliente.py
│   ├── gerar_stubs.py
│   ├── requirements.txt
│   └── gerado/
└── pom.xml
```

Os JSON ficam em `dados/`. O servidor recebe esse diretório como argumento opcional; se os arquivos não existirem, ele os cria com quantidades zeradas.

## Operações gRPC

- consultar estoque geral;
- consultar estoque de marmitas;
- consultar estoque de insumos;
- ajustar uma quantidade, usando variação positiva ou negativa;
- definir todo o estoque de marmitas ou de insumos;
- zerar logicamente cada estoque.

As operações de arquivo são protegidas por `ReentrantReadWriteLock`. Leituras podem ocorrer juntas, mas as atualizações `ler → modificar → gravar` são exclusivas. A escrita usa arquivo temporário e substituição atômica quando o sistema operacional oferece suporte.

## Pré-requisitos

- JDK 17 ou superior;
- Maven 3.9 ou superior;
- Python 3.10 ou superior.

## 1. Compilar o servidor Java

Na raiz do projeto:

```bash
mvn clean compile
```

O Maven gera automaticamente as classes Java a partir de `src/main/proto/SistemaDeEstoques.proto`.

## 2. Preparar o cliente Python

### PowerShell

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r cliente-python\requirements.txt
python cliente-python\gerar_stubs.py
```

### Linux/macOS

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r cliente-python/requirements.txt
python cliente-python/gerar_stubs.py
```

Os stubs já estão incluídos no projeto, mas o script permite regenerá-los quando o `.proto` for alterado.

## 3. Executar os servidores

Abra um terminal para cada versão. As portas padrão são diferentes para permitir executá-las simultaneamente.

### Single-threaded — porta 50051

```bash
mvn exec:java -Dexec.mainClass=br.ufg.marmitaria.estoques.servidor.ServidorSingleThread
```

Argumentos opcionais: `porta diretorio-dos-json`.

```bash
mvn exec:java -Dexec.mainClass=br.ufg.marmitaria.estoques.servidor.ServidorSingleThread -Dexec.args="50051 dados"
```

### Thread criada por requisição — porta 50052

```bash
mvn exec:java -Dexec.mainClass=br.ufg.marmitaria.estoques.servidor.ServidorThreadPorRequisicao
```

Argumentos opcionais: `porta diretorio-dos-json`.

### Pool fixo — porta 50053, quatro workers

```bash
mvn exec:java -Dexec.mainClass=br.ufg.marmitaria.estoques.servidor.ServidorPoolThreads
```

Argumentos opcionais: `porta tamanho-do-pool diretorio-dos-json`.

```bash
mvn exec:java -Dexec.mainClass=br.ufg.marmitaria.estoques.servidor.ServidorPoolThreads -Dexec.args="50053 4 dados"
```

> Para comparar os três ao mesmo tempo sem compartilhar arquivos, copie a pasta `dados` para três diretórios e passe um diretório diferente a cada servidor. Caso usem o mesmo diretório, o lock Java protege apenas as threads do mesmo processo, não três processos separados.

## 4. Executar o cliente

Sem subcomando, abre um menu interativo:

```bash
python cliente-python/cliente.py --porta 50051
```

Consultar:

```bash
python cliente-python/cliente.py --porta 50051 consultar
```

Adicionar cinco marmitas P:

```bash
python cliente-python/cliente.py --porta 50051 ajustar-marmita P 5
```

Retirar dez porções de arroz:

```bash
python cliente-python/cliente.py --porta 50051 ajustar-insumo ARROZ -10
```

Definir o estoque de marmitas:

```bash
python cliente-python/cliente.py --porta 50051 definir-marmitas 20 15 10
```

## 5. Comparar os modelos de concorrência

A consulta aceita um atraso artificial opcional para tornar a diferença observável. O comando abaixo envia oito requisições concorrentes, cada uma com um segundo de atraso:

```bash
python cliente-python/cliente.py --porta 50051 teste --requisicoes 8 --clientes 8 --atraso-ms 1000
python cliente-python/cliente.py --porta 50052 teste --requisicoes 8 --clientes 8 --atraso-ms 1000
python cliente-python/cliente.py --porta 50053 teste --requisicoes 8 --clientes 8 --atraso-ms 1000
```

Comportamento esperado aproximado:

- single-threaded: cerca de 8 segundos;
- thread por requisição: cerca de 1 segundo;
- pool de quatro threads: cerca de 2 segundos.

O cliente também mostra o nome da thread do servidor que processou cada requisição.

## Relação com o CRUD

Como os tipos de marmita e insumo são categorias fixas, o CRUD foi adaptado ao domínio:

- **Create/Update:** definir ou ajustar as quantidades;
- **Read:** consultar os arquivos;
- **Delete:** zerar logicamente o estoque.

## Subir para o GitHub

```bash
git init
git add .
git commit -m "Implementa serviço gRPC de estoques com três modelos de concorrência"
git branch -M main
git remote add origin URL_DO_REPOSITORIO
git push -u origin main
```

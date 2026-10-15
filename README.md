# Atividade pratica: calculadora remota com sockets TCP

Dois programas independentes em Java: `Servidor` recebe e calcula as operacoes;
`Cliente` le o teclado, envia as requisicoes e exibe as respostas.
Requer JDK 11 ou superior. O projeto local esta configurado com JDK 27.

## Executar pelo IntelliJ IDEA

1. Abra `src/Servidor.java` e clique no triangulo ao lado de `main` para executar.
2. Aguarde a mensagem `Servidor aguardando conexoes na porta 5000...`.
3. Abra `src/Cliente.java` e execute seu metodo `main`, mantendo o servidor ativo.
4. No console do cliente, digite `10 + 5` e pressione Enter.
5. Experimente `20 - 8`, `6 * 7`, `10 / 4` e `10 / 0`.
6. Digite `SAIR` para fechar o cliente. Pare o servidor pelo botao Stop ao terminar.

Para testar varios clientes simultaneos, habilite **Allow multiple instances** na
configuracao de execucao de `Cliente` e execute outra instancia. Cada conexao e
atendida em uma thread independente, enquanto o servidor continua aceitando conexoes.

## Executar pelo PowerShell

Na pasta do projeto, compile uma vez:

```powershell
javac -encoding UTF-8 -d out src/Servidor.java src/Cliente.java
```

Em um terminal, inicie o servidor:

```powershell
java -cp out Servidor
```

Em outro terminal, inicie o cliente:

```powershell
java -cp out Cliente
```

Se `java` e `javac` nao estiverem no PATH, use o caminho completo, por exemplo:

```powershell
& 'C:/Program Files/Java/jdk-27/bin/javac.exe' -encoding UTF-8 -d out src/Servidor.java src/Cliente.java
& 'C:/Program Files/Java/jdk-27/bin/java.exe' -cp out Servidor
# Em outro terminal:
& 'C:/Program Files/Java/jdk-27/bin/java.exe' -cp out Cliente
```

Para maquinas diferentes, execute `java -cp out Cliente IP_DO_SERVIDOR`, substituindo
o ultimo argumento pelo IP da maquina que executa o servidor. As maquinas precisam
ter conectividade de rede e permitir TCP na porta 5000.

## Protocolo de mensagens (descricao para a entrega)

A aplicacao utiliza sockets TCP na porta fixa 5000. As mensagens sao texto UTF-8,
delimitadas por quebra de linha. `println` envia a mensagem e `readLine` le uma
mensagem completa; assim, nao se depende dos limites dos pacotes TCP.

O cliente envia uma linha no formato `NUMERO OPERADOR NUMERO`, com os tres campos
separados por espacos. Os operadores aceitos sao `+`, `-`, `*` e `/`. Numeros
negativos sao permitidos; casas decimais usam ponto, como `2.5 * -4`.
O servidor responde uma vez para cada linha recebida:

| Requisicao | Resposta |
| --- | --- |
| `10 + 5` | `Resultado: 15` |
| `10 / 4` | `Resultado: 2.5` |
| `10 / 0` | `ERRO: Divisao por zero.` |
| `10 % 3` | `ERRO: Operador invalido. Use +, -, * ou /.` |
| `SAIR` | `ATE_LOGO` |

Depois de cada resposta, a conexao permanece aberta para a proxima requisicao.
Uma entrada invalida gera uma resposta iniciada por `ERRO:` e permite continuar.
Ao receber `SAIR` (sem distinguir maiusculas e minusculas), o servidor responde
`ATE_LOGO` e fecha a conexao daquele cliente. Fim do fluxo tambem encerra a sessao.
O cliente usa limite de 5 segundos para conectar e de 10 segundos para receber uma
resposta. Ambos tratam excecoes de comunicacao e fecham recursos com
`try-with-resources`. Divisoes usam precisao de 16 digitos significativos.

## Como o codigo atende ao enunciado

- `ServerSocket` aguarda conexoes e `Socket` conecta cliente e servidor.
- A porta 5000 esta definida em ambos os programas.
- O cliente envia informacoes; o servidor valida, calcula e devolve a resposta.
- Os consoles mostram conexao, requisicao, resposta e encerramento.
- Excecoes de rede e de calculo sao tratadas; fluxos e sockets sao fechados.
- Desafio adicional 1: uma `Thread` independente por cliente.
- Desafio adicional 2: repeticao de requisicoes na mesma conexao, com protocolo por linha.

## Entrega e captura de tela

Entregue `src/Servidor.java`, `src/Cliente.java` e a descricao do protocolo acima.
Para produzir a captura solicitada, deixe os consoles do servidor e do cliente
visiveis apos executar algumas operacoes. Use `Win + Shift + S` e capture as duas
janelas, mostrando a conexao, `10 + 5`, a resposta `Resultado: 15` e o encerramento
com `SAIR`. A captura deve mostrar a execucao real na sua maquina.

Para explicar na apresentacao: `accept()` aguarda uma conexao; cada thread le
linhas do seu cliente; `processar()` faz o calculo; `println()` envia e descarrega
a resposta automaticamente; `readLine()` recebe a resposta; `SAIR` encerra a sessao.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Calculadora TCP: cada cliente tem uma thread e pode enviar varias operacoes. */
public class Servidor {
    public static final int PORTA = 5000;

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            System.out.println("Servidor aguardando conexoes na porta " + PORTA + "...");
            while (true) {
                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado: " + cliente.getRemoteSocketAddress());
                new Thread(() -> atenderCliente(cliente)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    private static void atenderCliente(Socket cliente) {
        // O try-with-resources fecha os fluxos e o socket, mesmo se ocorrer erro.
        try (Socket conexao = cliente;
             BufferedReader entrada = new BufferedReader(new InputStreamReader(
                     conexao.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter saida = new PrintWriter(conexao.getOutputStream(), true,
                     StandardCharsets.UTF_8)) {
            String requisicao;
            // Cada linha e uma mensagem completa do protocolo.
            while ((requisicao = entrada.readLine()) != null) {
                System.out.println("Recebido de " + conexao.getRemoteSocketAddress() + ": " + requisicao);
                String resposta = processar(requisicao);
                saida.println(resposta);
                if (saida.checkError()) {
                    throw new IOException("Nao foi possivel enviar a resposta ao cliente.");
                }
                System.out.println("Resposta enviada: " + resposta);
                if (requisicao.trim().equalsIgnoreCase("SAIR")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro na comunicacao com o cliente: " + e.getMessage());
        } finally {
            System.out.println("Conexao encerrada: " + cliente.getRemoteSocketAddress());
        }
    }

    private static String processar(String requisicao) {
        if (requisicao.trim().equalsIgnoreCase("SAIR")) {
            return "ATE_LOGO";
        }
        String[] partes = requisicao.trim().split("\\s+");
        if (partes.length != 3) {
            return "ERRO: Use NUMERO OPERADOR NUMERO. Exemplo: 10 + 5";
        }
        try {
            BigDecimal primeiro = new BigDecimal(partes[0]);
            BigDecimal segundo = new BigDecimal(partes[2]);
            BigDecimal resultado;
            // Todo o calculo ocorre no servidor; o cliente apenas envia e exibe.
            switch (partes[1]) {
                case "+": resultado = primeiro.add(segundo); break;
                case "-": resultado = primeiro.subtract(segundo); break;
                case "*": resultado = primeiro.multiply(segundo); break;
                case "/":
                    if (segundo.compareTo(BigDecimal.ZERO) == 0) {
                        return "ERRO: Divisao por zero.";
                    }
                    resultado = primeiro.divide(segundo, MathContext.DECIMAL64);
                    break;
                default: return "ERRO: Operador invalido. Use +, -, * ou /.";
            }
            return "Resultado: " + resultado.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return "ERRO: Numero invalido. Use ponto como separador decimal.";
        } catch (ArithmeticException e) {
            return "ERRO: Nao foi possivel calcular esses valores.";
        }
    }
}

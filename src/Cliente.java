import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Interface de console da calculadora remota. Execute o servidor primeiro. */
public class Cliente {
    private static final int PORTA = 5000;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        System.out.println("Conectando a " + host + ":" + PORTA + "...");
        try (Socket conexao = new Socket()) {
            conexao.connect(new InetSocketAddress(host, PORTA), 5000);
            conexao.setSoTimeout(10000);
            try (BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
                 BufferedReader entrada = new BufferedReader(new InputStreamReader(
                         conexao.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter saida = new PrintWriter(conexao.getOutputStream(), true,
                         StandardCharsets.UTF_8)) {
                System.out.println("Conexao estabelecida.");
                System.out.println("Operacoes: + (soma), - (subtracao), * (multiplicacao), / (divisao).");
                System.out.println("Digite uma operacao como 10 + 5 ou SAIR para encerrar.");
                while (true) {
                    System.out.print("> ");
                    String requisicao = teclado.readLine();
                    // Se a entrada do console terminar, encerra a sessao pelo protocolo.
                    if (requisicao == null) {
                        requisicao = "SAIR";
                    }
                    saida.println(requisicao);
                    if (saida.checkError()) {
                        throw new IOException("Nao foi possivel enviar a requisicao.");
                    }
                    System.out.println("Requisicao enviada: " + requisicao);
                    String resposta = entrada.readLine();
                    if (resposta == null) {
                        System.out.println("O servidor encerrou a conexao.");
                        break;
                    }
                    System.out.println("Resposta do servidor: " + resposta);
                    if (requisicao.trim().equalsIgnoreCase("SAIR")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro de comunicacao: " + e.getMessage());
            System.err.println("Verifique se o servidor esta em execucao no endereco e porta informados.");
        } finally {
            System.out.println("Cliente encerrado; recursos de rede liberados.");
        }
    }
}

package br.com.ecocoleta.server;

import br.com.ecocoleta.model.PontoDeColeta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EcoColetaServer {

    private static final int PORT = 8080;
    private static final List<PontoDeColeta> pontosDeColeta = Collections.synchronizedList(new ArrayList<>());

    private static final AtomicInteger idCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        if (pontosDeColeta.isEmpty()) {
            inicializarDados();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor EcoColeta iniciado na porta " + PORT + "...");
            System.out.println("Aguardando conexões de clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static void inicializarDados() {
        PontoDeColeta p1 = new PontoDeColeta("Ecoponto Central", "Rua das Flores, 100", List.of("Plástico", "Vidro", "Metal"));
        p1.setId(idCounter.getAndIncrement());
        pontosDeColeta.add(p1);

        PontoDeColeta p2 = new PontoDeColeta("Coleta Bairro Sul", "Av. Principal, 550", List.of("Papel", "Orgânico"));
        p2.setId(idCounter.getAndIncrement());
        pontosDeColeta.add(p2);

        PontoDeColeta p3 = new PontoDeColeta("Ponto Verde Oeste", "Travessa das Árvores, 30", List.of("Eletrônicos", "Pilhas", "Baterias"));
        p3.setId(idCounter.getAndIncrement());
        pontosDeColeta.add(p3);
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClientHandler(Socket socket) { this.clientSocket = socket; }


        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                String command;
                while ((command = (String) in.readObject()) != null) {
                    System.out.println("Comando recebido de " + clientSocket.getInetAddress().getHostAddress() + ": " + command);

                    switch (command) {
                        case "LISTAR_TODOS": {
                            out.writeObject(new ArrayList<>(pontosDeColeta));
                            break;
                        }
                        case "BUSCAR_POR_TIPO": {
                            String tipoResiduo = (String) in.readObject();
                            List<PontoDeColeta> resultado = pontosDeColeta.stream()
                                    .filter(ponto -> ponto.getTiposDeResiduos().stream()
                                            .anyMatch(tipo -> tipo.equalsIgnoreCase(tipoResiduo)))
                                    .collect(Collectors.toList());
                            out.writeObject(resultado);
                            break;
                        }
                        case "CADASTRAR": {
                            PontoDeColeta novoPonto = (PontoDeColeta) in.readObject();
                            novoPonto.setId(idCounter.getAndIncrement());
                            pontosDeColeta.add(novoPonto);
                            out.writeObject("SUCESSO: Ponto de coleta cadastrado com ID " + novoPonto.getId() + "!");
                            System.out.println("Novo ponto cadastrado: ID " + novoPonto.getId() + " - " + novoPonto.getNome());
                            break;
                        }
                        case "EXCLUIR": {
                            int idParaExcluir = (int) in.readObject();
                            boolean removido = pontosDeColeta.removeIf(ponto -> ponto.getId() == idParaExcluir);
                            if (removido) {
                                out.writeObject("SUCESSO: Ponto com ID " + idParaExcluir + " excluído!");
                                System.out.println("Ponto excluído: ID " + idParaExcluir);
                            } else {
                                out.writeObject("ERRO: Ponto com ID " + idParaExcluir + " não encontrado.");
                            }
                            break;
                        }
                        case "EDITAR": {
                            int idParaEditar = (int) in.readObject();
                            PontoDeColeta pontoAtualizado = (PontoDeColeta) in.readObject();

                            int indexParaAtualizar = -1;
                            for (int i = 0; i < pontosDeColeta.size(); i++) {
                                if (pontosDeColeta.get(i).getId() == idParaEditar) {
                                    indexParaAtualizar = i;
                                    break;
                                }
                            }

                            if (indexParaAtualizar != -1) {
                                pontoAtualizado.setId(idParaEditar);
                                pontosDeColeta.set(indexParaAtualizar, pontoAtualizado);
                                out.writeObject("SUCESSO: Ponto com ID " + idParaEditar + " atualizado!");
                                System.out.println("Ponto atualizado: ID " + idParaEditar);
                            } else {
                                out.writeObject("ERRO: Ponto com ID " + idParaEditar + " não encontrado para edição.");
                            }
                            break;
                        }
                        case "SAIR": { return; }
                        default: out.writeObject("ERRO: Comando desconhecido.");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Conexão com o cliente perdida ou erro: " + e.getMessage());
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                    System.out.println("Cliente desconectado: " + clientSocket.getInetAddress().getHostAddress());
                } catch (IOException ioException) {
                    System.err.println("Erro ao fechar recursos: " + ioException.getMessage());
                }
            }
        }
    }
}
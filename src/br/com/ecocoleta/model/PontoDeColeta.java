package br.com.ecocoleta.model;

import java.io.Serializable;
import java.util.List;

public class PontoDeColeta implements Serializable {

    private int id;
    private String nome;
    private String endereco;
    private List<String> tiposDeResiduos;

    public PontoDeColeta(String nome, String endereco, List<String> tiposDeResiduos) {
        this.nome = nome;
        this.endereco = endereco;
        this.tiposDeResiduos = tiposDeResiduos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public List<String> getTiposDeResiduos() {
        return tiposDeResiduos;
    }

    @Override
    public String toString() {
        return "PontoDeColeta {" +
                "ID = " + id +
                ", Nome = '" + nome + '\'' +
                ", Endereço = '" + endereco + '\'' +
                ", Aceita = " + tiposDeResiduos +
                '}';
    }
}
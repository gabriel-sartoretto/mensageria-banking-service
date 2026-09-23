package br.com.alura.domain.messaging;

public class AgenciaMensagem {

    private Integer id;
    private String nome;
    private String razaoSocial;
    private String cnpj;
    private String situacaoCadastral;

    public AgenciaMensagem(Integer id, String nome, String razaoSocial, String cnpj, String situacaoCadastral){
        this.id = id;
        this.nome = nome;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.situacaoCadastral = situacaoCadastral;
    }

    public AgenciaMensagem() {
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getSituacaoCadastral() {
        return situacaoCadastral;
    }
}

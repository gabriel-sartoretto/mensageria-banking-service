package br.com.alura.domain.http;

public class AgenciaHttp {

    private Long id;
    private String nome;
    private String razaoSocial;
    private String cnpj;
    private String situacaoCadastral;

    public Long getId() {
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

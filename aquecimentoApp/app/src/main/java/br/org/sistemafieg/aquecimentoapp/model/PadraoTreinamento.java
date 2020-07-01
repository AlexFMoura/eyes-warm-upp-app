package br.org.sistemafieg.aquecimentoapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.Set;

import br.org.sistemafieg.aquecimentoapp.persistence.core.IEntity;

@Entity
public class PadraoTreinamento implements IEntity, Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private Long id;

    @ColumnInfo(name = "NOME")
    private String nome;

    @ColumnInfo(name = "DESCRICAO")
    private String descricao;

    @ColumnInfo(name = "TEMPO_TROCA_ENTRE_DISPOSITIVOS")
    private Double tempoTrocaEntreDispositivosMillis;

    @ColumnInfo(name = "TEMPO_REPETICAO_EXERCICIO")
    private Double tempoRepeticaoExercicioMillis;

    @Ignore
    private Set<PadraoTreinamentoDispItem> padraoTreinamentoDispItems;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getTempoTrocaEntreDispositivosMillis() {
        return tempoTrocaEntreDispositivosMillis;
    }

    public void setTempoTrocaEntreDispositivosMillis(Double tempoTrocaEntreDispositivosMillis) {
        this.tempoTrocaEntreDispositivosMillis = tempoTrocaEntreDispositivosMillis;
    }

    public Double getTempoRepeticaoExercicioMillis() {
        return tempoRepeticaoExercicioMillis;
    }

    public void setTempoRepeticaoExercicioMillis(Double tempoRepeticaoExercicioMillis) {
        this.tempoRepeticaoExercicioMillis = tempoRepeticaoExercicioMillis;
    }

    public Set<PadraoTreinamentoDispItem> getPadraoTreinamentoDispItems() {
        return padraoTreinamentoDispItems;
    }

    public void setPadraoTreinamentoDispItems(Set<PadraoTreinamentoDispItem> padraoTreinamentoDispItems) {
        this.padraoTreinamentoDispItems = padraoTreinamentoDispItems;
    }
}

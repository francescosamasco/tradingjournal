package it.samfrafx.tradingjournal.datamodel.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Setup", schema = "public")
@Getter
@Setter
public class Setup {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "confluences", nullable = false)
    private String confluences;

    @Column(name = "voto")
    private Integer voto;
    
    @Column(name = "strategy_id", nullable = false)
    private String strategyId;

}
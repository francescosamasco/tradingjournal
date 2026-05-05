package it.samfrafx.tradingjournal.bl.data;

import java.util.List;

import it.samfrafx.tradingjournal.bl.data.enums.VotoSetupEnum;

public class Config {

    private final List<String> confluenze;
    private final VotoSetupEnum voto;

    public Config(List<String> confluenze, VotoSetupEnum voto) {
        this.confluenze = confluenze;
        this.voto = voto;
    }

    public List<String> getConfluenze() {
        return confluenze;
    }

    public VotoSetupEnum getVoto() {
        return voto;
    }
}
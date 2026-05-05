package it.samfrafx.tradingjournal.bl.data;

import java.util.List;
import java.util.Map;

import it.samfrafx.tradingjournal.bl.data.Config;
import it.samfrafx.tradingjournal.bl.data.enums.SetupEnum;
import it.samfrafx.tradingjournal.bl.data.enums.StrutturaEnum;
import it.samfrafx.tradingjournal.bl.data.enums.VotoSetupEnum;

public class TradingConfig {

    public static final Map<StrutturaEnum, Map<SetupEnum, Config>> CONFIG = Map.of(

        StrutturaEnum.PROSTRUTTURA, Map.of(
            SetupEnum.CONFERMA, new Config(
                List.of("strong", "discount", "irl-sweep", "bos-strong"),
                VotoSetupEnum.ALTO
            ),
            SetupEnum.MANIPOLAZIONE, new Config(
                List.of("t1-sweep", "bos-strong"),
                VotoSetupEnum.ALTO
            ),
            SetupEnum.DIRETTA, new Config(
                List.of("strong", "t1-sweep"),
                VotoSetupEnum.MEDIO
            )
        ),

        StrutturaEnum.CONTROSTRUTTURA, Map.of(
            SetupEnum.CONFERMA, new Config(
                List.of("strong", "discount", "irl-sweep", "bos-strong"),
                VotoSetupEnum.ALTO
            ),
            SetupEnum.DIRETTA, new Config(
                List.of("strong", "t1-as-target"),
                VotoSetupEnum.MEDIO
            )
        )
    );

}
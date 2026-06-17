package it.samfrafx.tradingjournal.bl.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import it.samfrafx.tradingjournal.bl.data.SetupData;
import it.samfrafx.tradingjournal.datamodel.data.Setup;
import it.samfrafx.tradingjournal.datamodel.repository.SetupRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SetupService {

    private final SetupRepository setupRepository;

    public List<SetupData> findAll() {

        return setupRepository.findAllByOrderByDescriptionAsc()
                .stream()
                .map(this::toData)
                .toList();
    }
    
    public SetupData findById(String id) {

        Setup entity = setupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setup non trovato"));

        return toData(entity);
    }

    private SetupData toData(Setup entity) {

        SetupData data = new SetupData();

        data.setId(entity.getId());
        data.setDescription(entity.getDescription());
        data.setConfluences(entity.getConfluences());
        data.setVoto(entity.getVoto());

        return data;
    }

    public List<String> getConfluences(String setupId) {

        Setup setup = setupRepository.findById(setupId)
                .orElseThrow(() -> new IllegalArgumentException("Setup non trovato"));

        return Arrays.stream(setup.getConfluences().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
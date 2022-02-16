package com.mcserby.playground.javademoplayground.service;

import com.mcserby.playground.javademoplayground.mapper.DtoToEntityMapper;
import com.mcserby.playground.javademoplayground.model.PriceChangedEvent;
import com.mcserby.playground.javademoplayground.persistence.model.SwapPrice;
import com.mcserby.playground.javademoplayground.persistence.repository.PriceArchiveRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PriceArchive implements PriceChangesSubscriber {

    private final PriceArchiveRepository priceArchiveRepository;
    private final String identifier;
    private final PriceSource priceSource;

    public PriceArchive(
            PriceArchiveRepository priceArchiveRepository,
            PriceSource priceSource) {
        this.priceArchiveRepository = priceArchiveRepository;
        this.identifier = UUID.randomUUID().toString();
        this.priceSource = priceSource;
        this.priceSource.subscribeForPriceChanges(this);
    }

    @PreDestroy
    public void destroy() {
        this.priceSource.unSubscribeForPriceChanges(this);
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public void onPriceChanged(PriceChangedEvent e) {
        SwapPrice swapPriceRecord = SwapPrice.builder()
                .agencyId(e.getAgencyId())
                .localDateTime(LocalDateTime.now())
                .tickerFrom(e.getTickerFrom())
                .tickerTo(e.getTickerTo())
                .price(e.getPrice())
                .build();
        this.priceArchiveRepository.save(swapPriceRecord);
    }

    public List<com.mcserby.playground.javademoplayground.dto.SwapPrice> getArchiveFor(long agencyId, String tickerFrom,
                                                                                       String tickerTo) {
        return this.priceArchiveRepository.findByAgencyIdAndTickerFromAndTickerTo(agencyId, tickerFrom, tickerTo).stream()
                .map(DtoToEntityMapper::map).collect(Collectors.toList());
    }
}

package nextstep.subway.station.application;

import nextstep.subway.section.exception.CannotRemoveRegisteredStationException;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import nextstep.subway.station.dto.StationRequest;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.station.exception.StationNameDuplicatedException;
import nextstep.subway.station.exception.StationNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public StationResponse saveStation(StationRequest stationRequest) {
        if (stationRepository.existsByName(stationRequest.getName())) {
            throw new StationNameDuplicatedException(stationRequest.getName());
        }

        Station persistStation = stationRepository.save(stationRequest.toStation());
        return StationResponse.of(persistStation);
    }

    @Transactional(readOnly = true)
    public StationResponse getStation(Long id) {
        return stationRepository.findById(id)
                .map(StationResponse::of)
                .orElseThrow(() -> new StationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<StationResponse> getStations() {
        List<Station> stations = stationRepository.findAll();

        return stations.stream()
                .map(StationResponse::of)
                .collect(Collectors.toList());
    }

    public void deleteStation(Long id) {
        deleteStationById(id);
    }

    private void deleteStationById(Long id) {
        try {
            stationRepository.deleteById(id);
            stationRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new CannotRemoveRegisteredStationException(id);
        }
    }
}

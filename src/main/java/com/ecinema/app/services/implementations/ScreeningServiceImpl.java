package com.ecinema.app.services.implementations;

import com.ecinema.app.domain.dtos.ScreeningDto;
import com.ecinema.app.domain.dtos.ScreeningSeatDto;
import com.ecinema.app.domain.entities.*;
import com.ecinema.app.domain.forms.ScreeningForm;
import com.ecinema.app.exceptions.ClashException;
import com.ecinema.app.exceptions.InvalidArgsException;
import com.ecinema.app.exceptions.NoEntityFoundException;
import com.ecinema.app.repositories.MovieRepository;
import com.ecinema.app.repositories.ScreeningRepository;
import com.ecinema.app.repositories.ShowroomRepository;
import com.ecinema.app.services.ScreeningSeatService;
import com.ecinema.app.services.ScreeningService;
import com.ecinema.app.utils.UtilMethods;
import com.ecinema.app.validators.ScreeningFormValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Screening service.
 */
@Service
@Transactional
public class ScreeningServiceImpl extends AbstractServiceImpl<Screening, ScreeningRepository>
        implements ScreeningService {

    private final MovieRepository movieRepository;
    private final ShowroomRepository showroomRepository;
    private final ScreeningSeatService screeningSeatService;
    private final ScreeningFormValidator screeningFormValidator;

    /**
     * Instantiates a new Screening service.
     *
     * @param repository the repository
     */
    public ScreeningServiceImpl(ScreeningRepository repository,
                                MovieRepository movieRepository,
                                ShowroomRepository showroomRepository,
                                ScreeningSeatService screeningSeatService,
                                ScreeningFormValidator screeningFormValidator) {
        super(repository);
        this.movieRepository = movieRepository;
        this.showroomRepository = showroomRepository;
        this.screeningSeatService = screeningSeatService;
        this.screeningFormValidator = screeningFormValidator;
    }

    @Override
    protected void onDelete(Screening screening) {
        // detach Movie
        Movie movie = screening.getMovie();
        if (movie != null) {
            movie.getScreenings().remove(screening);
            screening.setMovie(null);
        }
        // detach Showroom
        Showroom showroom = screening.getShowroom();
        if (showroom != null) {
            showroom.getScreenings().remove(screening);
            screening.setShowroom(null);
        }
        // cascade delete ScreeningSeats
        screeningSeatService.deleteAll(screening.getScreeningSeats());
    }

    @Override
    public void submitScreeningForm(ScreeningForm screeningForm)
            throws NoEntityFoundException, InvalidArgsException, ClashException {
        List<String> errors = new ArrayList<>();
        screeningFormValidator.validate(screeningForm, errors);
        if (!errors.isEmpty()) {
            throw new InvalidArgsException(errors);
        }
        Showroom showroom = showroomRepository
                .findById(screeningForm.getShowroomId())
                .orElseThrow(() -> new NoEntityFoundException(
                        "showroom", "showroom id", screeningForm.getShowroomId()));
        Movie movie = movieRepository
                .findById(screeningForm.getMovieId())
                .orElseThrow(() -> new NoEntityFoundException(
                        "movie", "movie id", screeningForm.getMovieId()));
        LocalDateTime showDateTime = LocalDateTime.of(screeningForm.getShowtimeYear(),
                                                      screeningForm.getShowtimeMonth(),
                                                      screeningForm.getShowtimeDay(),
                                                      screeningForm.getShowtimeHour(),
                                                      screeningForm.getShowtimeMinute());
        LocalDateTime endDateTime = showDateTime
                .plusHours(movie.getDuration().getHours())
                .plusMinutes(movie.getDuration().getMinutes());
        if (existsScreeningByShowroomAndInBetweenStartTimeAndEndTime(
                showroom, showDateTime, endDateTime)) {
            throw new ClashException("Screening request overlaps already existent screening");
        }
        Screening screening = new Screening();
        screening.setShowDateTime(showDateTime);
        screening.setEndDateTime(endDateTime);
        screening.setShowroom(showroom);
        showroom.getScreenings().add(screening);
        screening.setMovie(movie);
        movie.getScreenings().add(screening);
        save(screening);
        for (ShowroomSeat showroomSeat : showroom.getShowroomSeats()) {
            ScreeningSeat screeningSeat = new ScreeningSeat();
            screeningSeat.setTicket(null);
            screeningSeat.setScreening(screening);
            screening.getScreeningSeats().add(screeningSeat);
            screeningSeat.setShowroomSeat(showroomSeat);
            showroomSeat.getScreeningSeats().add(screeningSeat);
            screeningSeatService.save(screeningSeat);
        }
    }

    @Override
    public boolean existsScreeningByShowroomAndInBetweenStartTimeAndEndTime(
            Showroom showroom, LocalDateTime startTime, LocalDateTime endTime) {
        return existsScreeningByShowroomIdAndInBetweenStartTimeAndEndTime(
                showroom.getId(), startTime, endTime);
    }

    @Override
    public boolean existsScreeningByShowroomIdAndInBetweenStartTimeAndEndTime(
            Long showroomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Screening> screenings = repository.findAllByShowroomWithId(showroomId);
        for (Screening screening : screenings) {
            if (UtilMethods.localDateTimeOverlap(
                    startTime, endTime,
                    screening.getShowDateTime(), screening.getEndDateTime())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Page<ScreeningDto> findPageByMovieId(Long movieId, Pageable pageable) {
        return repository.findAllByMovieId(movieId, pageable).map(this::convertToDto);
    }

    @Override
    public List<ScreeningDto> findAllByShowDateTimeLessThanEqual(LocalDateTime localDateTime) {
        return repository.findAllByShowDateTimeLessThanEqual(localDateTime)
                         .stream().map(this::convertToDto)
                         .collect(Collectors.toList());
    }

    @Override
    public List<ScreeningDto> findAllByShowDateTimeGreaterThanEqual(LocalDateTime localDateTime) {
        return repository.findAllByShowDateTimeGreaterThanEqual(localDateTime)
                         .stream().map(this::convertToDto)
                         .collect(Collectors.toList());
    }

    @Override
    public List<ScreeningDto> findAllByShowDateTimeBetween(LocalDateTime l1, LocalDateTime l2) {
        return repository.findAllByShowDateTimeBetween(l1, l2)
                         .stream().map(this::convertToDto)
                         .collect(Collectors.toList());
    }

    @Override
    public List<ScreeningDto> findAllByMovie(Movie movie) {
        return repository.findAllByMovie(movie)
                         .stream().map(this::convertToDto)
                         .collect(Collectors.toList());
    }

    @Override
    public List<ScreeningDto> findAllByMovieWithId(Long movieId) {
        return repository.findAllByMovieWithId(movieId)
                         .stream().map(this::convertToDto)
                         .collect(Collectors.toList());
    }

    @Override
    public List<ScreeningDto> findAllByShowroom(Showroom showroom) {
        return repository.findAllByShowroom(showroom)
                         .stream().map(this::convertToDto)
                         .collect(Collectors.toList());
    }

    @Override
    public List<ScreeningDto> findAllByShowroomWithId(Long showroomId) {
        return repository.findAllByShowroomWithId(showroomId)
                         .stream().map(this::convertToDto)
                         .collect(Collectors.toList());
    }

    @Override
    public ScreeningDto convertToDto(Long id)
            throws NoEntityFoundException {
        Screening screening = findById(id).orElseThrow(
                () -> new NoEntityFoundException("screening", "id", id));
        ScreeningDto screeningDTO = new ScreeningDto();
        screeningDTO.setId(screening.getId());
        screeningDTO.setMovieTitle(screening.getMovie().getTitle());
        screeningDTO.setShowroomLetter(screening.getShowroom().getShowroomLetter());
        screeningDTO.setShowDateTime(screening.getShowDateTime());
        screeningDTO.setTotalSeatsInRoom(screening.getShowroom().getShowroomSeats().size());
        long numberOfSeatsBooked = screening
                .getScreeningSeats().stream().filter(
                        screeningSeat -> screeningSeat.getTicket() != null).count();
        screeningDTO.setSeatsBooked((int) numberOfSeatsBooked);
        screeningDTO.setSeatsAvailable(screeningDTO.getTotalSeatsInRoom() - (int) numberOfSeatsBooked);
        return screeningDTO;
    }

}

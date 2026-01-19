package com.nightflow.venueservice.service;

import com.nightflow.venueservice.dto.VenueRequest;
import com.nightflow.venueservice.dto.VenueResponse;
import com.nightflow.venueservice.entity.Venue;
import com.nightflow.venueservice.entity.VenueType;
import com.nightflow.venueservice.exception.ResourceNotFoundException;
import com.nightflow.venueservice.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueResponse create(VenueRequest request) {
        Venue venue = Venue.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .district(request.getDistrict())
                .capacity(request.getCapacity())
                .mapUrl(request.getMapUrl())
                .imageUrl(request.getImageUrl())
                .type(request.getType())
                .build();

        Venue saved = venueRepository.save(venue);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> findAll() {
        return venueRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VenueResponse findById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mekan", id));
        return toResponse(venue);
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> findByCity(String city) {
        return venueRepository.findByCityIgnoreCase(city).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> findByType(VenueType type) {
        return venueRepository.findByType(type).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> findByCityAndType(String city, VenueType type) {
        return venueRepository.findByCityAndType(city, type).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> findByMinCapacity(Integer minCapacity) {
        return venueRepository.findByCapacityGreaterThanEqual(minCapacity).stream()
                .map(this::toResponse)
                .toList();
    }

    public VenueResponse update(Long id, VenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mekan", id));

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setDistrict(request.getDistrict());
        venue.setCapacity(request.getCapacity());
        venue.setMapUrl(request.getMapUrl());
        venue.setImageUrl(request.getImageUrl());
        venue.setType(request.getType());

        Venue updated = venueRepository.save(venue);
        return toResponse(updated);
    }

    public void delete(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mekan", id);
        }
        venueRepository.deleteById(id);
    }

    private VenueResponse toResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .district(venue.getDistrict())
                .capacity(venue.getCapacity())
                .mapUrl(venue.getMapUrl())
                .imageUrl(venue.getImageUrl())
                .type(venue.getType())
                .createdAt(venue.getCreatedAt())
                .build();
    }
}

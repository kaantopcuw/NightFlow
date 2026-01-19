package com.nightflow.venueservice.service;

import com.nightflow.venueservice.dto.OrganizerRequest;
import com.nightflow.venueservice.dto.OrganizerResponse;
import com.nightflow.venueservice.entity.Organizer;
import com.nightflow.venueservice.entity.OrganizerStatus;
import com.nightflow.venueservice.exception.ResourceAlreadyExistsException;
import com.nightflow.venueservice.exception.ResourceNotFoundException;
import com.nightflow.venueservice.repository.OrganizerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizerService {

    private final OrganizerRepository organizerRepository;

    public OrganizerResponse create(OrganizerRequest request) {
        if (organizerRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Bu slug zaten kullanılıyor: " + request.getSlug());
        }
        if (request.getEmail() != null && organizerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Bu email zaten kayıtlı: " + request.getEmail());
        }

        Organizer organizer = Organizer.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .email(request.getEmail())
                .phone(request.getPhone())
                .logoUrl(request.getLogoUrl())
                .status(OrganizerStatus.PENDING)
                .build();

        Organizer saved = organizerRepository.save(organizer);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrganizerResponse> findAll() {
        return organizerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizerResponse findById(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizatör", id));
        return toResponse(organizer);
    }

    @Transactional(readOnly = true)
    public OrganizerResponse findBySlug(String slug) {
        Organizer organizer = organizerRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organizatör", slug));
        return toResponse(organizer);
    }

    public OrganizerResponse update(Long id, OrganizerRequest request) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizatör", id));

        if (!organizer.getSlug().equals(request.getSlug()) && 
            organizerRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Bu slug zaten kullanılıyor: " + request.getSlug());
        }

        organizer.setName(request.getName());
        organizer.setSlug(request.getSlug());
        organizer.setEmail(request.getEmail());
        organizer.setPhone(request.getPhone());
        organizer.setLogoUrl(request.getLogoUrl());

        Organizer updated = organizerRepository.save(organizer);
        return toResponse(updated);
    }

    public OrganizerResponse updateStatus(Long id, OrganizerStatus status) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizatör", id));

        organizer.setStatus(status);
        Organizer updated = organizerRepository.save(organizer);
        return toResponse(updated);
    }

    public void delete(Long id) {
        if (!organizerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Organizatör", id);
        }
        organizerRepository.deleteById(id);
    }

    private OrganizerResponse toResponse(Organizer organizer) {
        return OrganizerResponse.builder()
                .id(organizer.getId())
                .name(organizer.getName())
                .slug(organizer.getSlug())
                .email(organizer.getEmail())
                .phone(organizer.getPhone())
                .logoUrl(organizer.getLogoUrl())
                .status(organizer.getStatus())
                .createdAt(organizer.getCreatedAt())
                .updatedAt(organizer.getUpdatedAt())
                .build();
    }
}

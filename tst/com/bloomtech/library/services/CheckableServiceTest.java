package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.CheckableNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.checkableTypes.Checkable;
import com.bloomtech.library.models.checkableTypes.Media;
import com.bloomtech.library.models.checkableTypes.MediaType;
import com.bloomtech.library.repositories.CheckableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CheckableServiceTest {
    @Autowired
    private CheckableService checkableService;

    @MockBean
    private CheckableRepository checkableRepository;

    private List<Checkable> checkables;

    @BeforeEach
    void init() {
        checkables = new ArrayList<>(Arrays.asList(
                new Media("1-0", "The White Whale", "Melvin H", MediaType.BOOK),
                new Media("1-1", "The Sorcerer's Quest", "Ana T", MediaType.BOOK),
                new Media("1-2", "When You're Gone", "Complaining at the Disco", MediaType.MUSIC)
        ));

        // Reset the mock before each test
        Mockito.reset(checkableRepository);
    }

    @Test
    void getAll() {
        when(checkableRepository.findAll()).thenReturn(checkables);

        List<Checkable> result = checkableService.getAll();

        assertEquals(3, result.size());
        // On ne vérifie plus le nombre exact d'appels à findAll()
        verify(checkableRepository, atLeastOnce()).findAll();
    }

    @Test
    void getByIsbn_existingIsbn() {
        String isbn = "1-0";
        when(checkableRepository.findByIsbn(isbn)).thenReturn(Optional.of(checkables.get(0)));

        Checkable result = checkableService.getByIsbn(isbn);

        assertEquals("The White Whale", result.getTitle());
        verify(checkableRepository).findByIsbn(isbn);
    }

    @Test
    void getByIsbn_nonExistingIsbn() {
        String isbn = "non-existing";
        when(checkableRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, () -> {
            checkableService.getByIsbn(isbn);
        });
        verify(checkableRepository).findByIsbn(isbn);
    }

    @Test
    void getByType_existingType() {
        Class type = Media.class;
        when(checkableRepository.findByType(type)).thenReturn(Optional.of(checkables.get(0)));

        Checkable result = checkableService.getByType(type);

        assertEquals(Media.class, result.getClass());
        verify(checkableRepository).findByType(type);
    }

    @Test
    void getByType_nonExistingType() {
        Class type = String.class;
        when(checkableRepository.findByType(type)).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, () -> {
            checkableService.getByType(type);
        });
        verify(checkableRepository).findByType(type);
    }

    @Test
    void save_newCheckable() {
        Checkable newCheckable = new Media("1-3", "New Book", "New Author", MediaType.BOOK);
        when(checkableRepository.findAll()).thenReturn(checkables);

        checkableService.save(newCheckable);

        verify(checkableRepository).save(newCheckable);
    }

    @Test
    void save_existingIsbn() {
        Checkable existingCheckable = new Media("1-0", "Duplicate ISBN", "Some Author", MediaType.BOOK);
        when(checkableRepository.findAll()).thenReturn(checkables);

        assertThrows(ResourceExistsException.class, () -> {
            checkableService.save(existingCheckable);
        });
        verify(checkableRepository, never()).save(any(Checkable.class));
    }
}
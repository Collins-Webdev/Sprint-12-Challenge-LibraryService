package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.LibraryNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.*;
import com.bloomtech.library.models.checkableTypes.Checkable;
import com.bloomtech.library.models.checkableTypes.Media;
import com.bloomtech.library.repositories.LibraryRepository;
import com.bloomtech.library.models.CheckableAmount;
import com.bloomtech.library.views.LibraryAvailableCheckouts;
import com.bloomtech.library.views.OverdueCheckout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private CheckableService checkableService;

    public List<Library> getLibraries() {
        return libraryRepository.findAll();
    }

    public Library getLibraryByName(String name) {
        Optional<Library> libraryOptional = libraryRepository.findByName(name);
        return libraryOptional.orElseThrow(() -> new LibraryNotFoundException("Library not found with name: " + name));
    }

    public void save(Library library) {
        List<Library> libraries = libraryRepository.findAll();
        if (libraries.stream().filter(p->p.getName().equals(library.getName())).findFirst().isPresent()) {
            throw new ResourceExistsException("Library with name: " + library.getName() + " already exists!");
        }
        libraryRepository.save(library);
    }

    public CheckableAmount getCheckableAmount(String libraryName, String checkableIsbn) {
        Library library = getLibraryByName(libraryName);
        Checkable checkable = checkableService.getByIsbn(checkableIsbn);

        Optional<CheckableAmount> existingAmount = library.getCheckables().stream()
                .filter(ca -> ca.getCheckable().getIsbn().equals(checkableIsbn))
                .findFirst();

        return existingAmount.orElse(new CheckableAmount(checkable, 0));
    }

    public List<LibraryAvailableCheckouts> getLibrariesWithAvailableCheckout(String isbn) {
        Checkable checkable = checkableService.getByIsbn(isbn);
        List<Library> allLibraries = libraryRepository.findAll();
        List<LibraryAvailableCheckouts> available = new ArrayList<>();

        for (Library library : allLibraries) {
            Optional<CheckableAmount> checkableAmount = library.getCheckables().stream()
                    .filter(ca -> ca.getCheckable().getIsbn().equals(isbn))
                    .findFirst();

            if (checkableAmount.isPresent()) {
                available.add(new LibraryAvailableCheckouts(
                        checkableAmount.get().getAmount(),
                        library.getName()
                ));
            }
        }

        return available;
    }

    public List<OverdueCheckout> getOverdueCheckouts(String libraryName) {
        Library library = getLibraryByName(libraryName);
        List<OverdueCheckout> overdueCheckouts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (LibraryCard card : library.getLibraryCards()) {
            for (Checkout checkout : card.getCheckouts()) {
                if (checkout.getDueDate().isBefore(now)) {
                    overdueCheckouts.add(new OverdueCheckout(
                            card.getPatron(),
                            checkout
                    ));
                }
            }
        }

        return overdueCheckouts;
    }
}

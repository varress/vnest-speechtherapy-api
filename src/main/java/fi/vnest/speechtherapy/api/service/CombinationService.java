package fi.vnest.speechtherapy.api.service;

import fi.vnest.speechtherapy.api.dto.CombinationBatchRequest;
import fi.vnest.speechtherapy.api.dto.CombinationRequest;
import fi.vnest.speechtherapy.api.model.AllowedCombination;
import fi.vnest.speechtherapy.api.model.Word;
import fi.vnest.speechtherapy.api.repository.AllowedCombinationRepository;
import fi.vnest.speechtherapy.api.repository.WordRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CombinationService {

    private final AllowedCombinationRepository combinationRepository;
    private final WordRepository wordRepository;

    @Autowired
    public CombinationService(AllowedCombinationRepository combinationRepository, WordRepository wordRepository) {
        this.combinationRepository = combinationRepository;
        this.wordRepository = wordRepository;
    }

    /**
     * Retrieves all combinations, optionally filtered by verb ID.
     */
    public List<AllowedCombination> findAll(Long verbId) {
        if (verbId != null) {
            return combinationRepository.findByVerbId(verbId);
        }

        return combinationRepository.findAll();
    }

    /**
     * Creates a single new combination.
     * Throws NoSuchElementException if any word ID is invalid.
     */
    @Transactional
    public AllowedCombination createCombination(CombinationRequest request) {
        Word subject = wordRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new NoSuchElementException("Subject word not found with ID: " + request.getSubjectId()));

        Word verb = wordRepository.findById(request.getVerbId())
                .orElseThrow(() -> new NoSuchElementException("Verb word not found with ID: " + request.getVerbId()));

        Word object = wordRepository.findById(request.getObjectId())
                .orElseThrow(() -> new NoSuchElementException("Object word not found with ID: " + request.getObjectId()));

        // Check for existing combination
        if (combinationRepository.findBySubjectIdAndVerbIdAndObjectId(
                request.getSubjectId(), request.getVerbId(), request.getObjectId()).isPresent()) {
            throw new IllegalArgumentException("Combination already exists: " + subject.getText() + " " + verb.getText() + " " + object.getText());
        }

        AllowedCombination newCombination = new AllowedCombination(subject, verb, object);

        return combinationRepository.save(newCombination);
    }

    /**
     * Creates a batch of combinations (Verb x Subjects x Objects).
     * Skips combinations that already exist and counts only created ones.
     */
    @Transactional
    public List<AllowedCombination> createCombinationsBatch(CombinationBatchRequest batchRequest) {
        Word verb = wordRepository.findById(batchRequest.getVerbId())
                .orElseThrow(() -> new NoSuchElementException("Verb word not found with ID: " + batchRequest.getVerbId()));

        List<Long> subjectIds = batchRequest.getSubjectIds();
        List<Long> objectIds = batchRequest.getObjectIds();

        List<Long> allIds = new ArrayList<>(subjectIds);
        allIds.addAll(objectIds);

        // Fetch all required Words in one batch
        Map<Long, Word> wordMap = wordRepository.findAllById(allIds)
                .stream()
                .collect(Collectors.toMap(Word::getId, w -> w));

        List<AllowedCombination> combinationsToSave = new ArrayList<>();

        for (Long subId : subjectIds) {
            Word subject = wordMap.get(subId);
            if (subject == null) continue;

            for (Long objId : objectIds) {
                Word object = wordMap.get(objId);
                if (object == null) continue;

                // Check for existence before creating to prevent unique constraint violation
                if (combinationRepository.findBySubjectIdAndVerbIdAndObjectId(subId, verb.getId(), objId).isEmpty()) {
                    combinationsToSave.add(new AllowedCombination(subject, verb, object));
                }
            }
        }

        return combinationRepository.saveAll(combinationsToSave);
    }

    /**
     * Deletes a specific combination by ID.
     */
    @Transactional
    public void deleteCombination(Long id) {
        if (!combinationRepository.existsById(id)) {
            throw new NoSuchElementException("Allowed combination not found with ID: " + id);
        }

        combinationRepository.deleteById(id);
    }

    /**
     * Deletes all combinations associated with a specific verb ID.
     * Throws NoSuchElementException if the verb ID doesn't exist.
     */
    @Transactional
    public void deleteCombinationsByVerb(Long verbId) {
        if (!wordRepository.existsById(verbId)) {
            throw new NoSuchElementException("Verb word not found with ID: " + verbId);
        }

        combinationRepository.deleteAllByVerbId(verbId);
    }
}

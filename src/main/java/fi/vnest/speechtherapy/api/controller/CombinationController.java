package fi.vnest.speechtherapy.api.controller;

import fi.vnest.speechtherapy.api.dto.*;
import fi.vnest.speechtherapy.api.model.AllowedCombination;
import fi.vnest.speechtherapy.api.service.CombinationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing AllowedCombination entities.
 * Exceptions are handled by GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/combinations")
public class CombinationController {

    private final CombinationService combinationService;

    @Autowired
    public CombinationController(CombinationService combinationService) {
        this.combinationService = combinationService;
    }

    /**
     * GET /api/combinations - Get all combinations, optionally filtered by verb ID.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CombinationResponse>>> getAllCombinations(
            @RequestParam(required = false) Long verb_id) {

        List<AllowedCombination> combinations = combinationService.findAll(verb_id);
        List<CombinationResponse> responseData = combinations.stream()
                .map(CombinationResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, responseData));
    }

    /**
     * POST /api/combinations - Create one combinations
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCombination(@RequestBody @Valid CombinationRequest request) {
        AllowedCombination newCombination = combinationService.createCombination(request);
        CombinationResponse responseData = CombinationResponse.fromEntity(newCombination);
        return new ResponseEntity<>(new ApiResponse<>(true, responseData), HttpStatus.CREATED);
    }

    /**
     * POST /api/combinations - Create multiple combinations for a verb
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<?>> createCombinationsBatch(@RequestBody @Valid CombinationBatchRequest batchRequest) {
        List<AllowedCombination> createdCombinations = combinationService.createCombinationsBatch(batchRequest);
        List<CombinationResponse> responseList = createdCombinations.stream()
                .map(CombinationResponse::fromEntity)
                .collect(Collectors.toList());

        CombinationBatchResponse bulkResponse = new CombinationBatchResponse(
                responseList.size(),
                responseList
        );

        return new ResponseEntity<>(new ApiResponse<>(true, bulkResponse), HttpStatus.CREATED);
    }

    /**
     * DELETE /api/combinations/:id - Delete a specific combination.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCombination(@PathVariable Long id) {
        combinationService.deleteCombination(id);
        // Return 204 No Content for successful deletion
        return new ResponseEntity<>(new ApiResponse<>(true, null), HttpStatus.NO_CONTENT);
    }

    /**
     * DELETE /api/combinations/by-verb/:verb_id - Delete all combinations for a specific verb.
     */
    @DeleteMapping("/by-verb/{verb_id}")
    public ResponseEntity<ApiResponse<Void>> deleteCombinationsByVerb(@PathVariable Long verb_id) {
        combinationService.deleteCombinationsByVerb(verb_id);
        // Return 204 No Content for successful deletion
        return new ResponseEntity<>(new ApiResponse<>(true, null), HttpStatus.NO_CONTENT);
    }
}

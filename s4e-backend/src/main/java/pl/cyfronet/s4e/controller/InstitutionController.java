package pl.cyfronet.s4e.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.controller.response.InstitutionResponse;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.InstitutionUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.service.SlugService;

import javax.validation.Valid;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class InstitutionController {
    private final InstitutionService institutionService;
    private final SlugService slugService;

    @ApiOperation("Create a new institution")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If institution was created"),
            @ApiResponse(code = 400, message = "Institution not created")
    })
    @PostMapping("/institutions")
    public ResponseEntity<?> create(@RequestBody @Valid CreateInstitutionRequest request) throws InstitutionCreationException {
        institutionService.save(Institution.builder().name(request.getName()).slug(slugService.slugify(request.getName())).build());
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get a list of institutions")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved list")
    })
    @GetMapping("/institutions")
    public Page<InstitutionResponse> getAll(Pageable pageable) {
        Page<Institution> page = institutionService.getAll(pageable);
        return new PageImpl<>(
                page.stream()
                        .map(m -> InstitutionResponse.of(m))
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }

    @ApiOperation("Get an institution")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved an institution"),
            @ApiResponse(code = 404, message = "Institution not found")
    })
    @GetMapping("/institutions/{institution}")
    public InstitutionResponse get(@PathVariable("institution") String institutionSlug) throws NotFoundException {
        val institution = institutionService.getInstitution(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug));
        return InstitutionResponse.of(institution);
    }

    @ApiOperation("Update an institution")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If institution was updated"),
            @ApiResponse(code = 400, message = "Institution not updated"),
            @ApiResponse(code = 404, message = "Institution not found")
    })
    @PutMapping("/institutions/{institution}")
    public ResponseEntity<?> update(@RequestBody UpdateInstitutionRequest request,
                                    @PathVariable("institution") String institutionSlug)
            throws NotFoundException, InstitutionUpdateException {
        val institution = institutionService.getInstitution(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug));
        institution.setName(request.getName());
        institution.setSlug(slugService.slugify(request.getName()));
        institutionService.update(institution);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Delete an institution")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If institution was deleted.")
    })
    @DeleteMapping("/institutions/{institution}")
    public ResponseEntity<?> delete(@PathVariable("institution") String institutionSlug) {
        institutionService.delete(institutionSlug);
        return ResponseEntity.ok().build();
    }
}

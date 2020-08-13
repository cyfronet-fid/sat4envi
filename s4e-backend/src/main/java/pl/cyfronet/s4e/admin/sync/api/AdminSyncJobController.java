package pl.cyfronet.s4e.admin.sync.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.admin.sync.task.SyncJob;
import pl.cyfronet.s4e.admin.sync.task.SyncJobManager;
import pl.cyfronet.s4e.admin.sync.task.SyncJobStore;
import pl.cyfronet.s4e.ex.NotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/sync-jobs", produces = APPLICATION_JSON_VALUE)
@Tag(name = "admin-sync-jobs", description = "The Admin Scenes Sync Job API")
@RequiredArgsConstructor
@Slf4j
public class AdminSyncJobController {
    private final SyncJobStore syncJobStore;
    private final SyncJobManager syncJobManager;

    private final SyncJobMapper syncJobMapper;
    private final RunningDetailsMapper runningDetailsMapper;

    @Operation(summary = "List all jobs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public List<SyncJobResponse> get() {
        return syncJobStore.list().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Create a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public SyncJobResponse.Extended create(@RequestBody @Valid CreateSyncJobRequest request) {
        SyncJob syncJob = syncJobManager.create(request.getName(), request.getPrefix(), request.isFailFast());
        syncJobStore.register(syncJob);
        return toExtendedResponse(syncJob);
    }

    @Operation(summary = "Get a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping(path = "/{name}")
    public SyncJobResponse.Extended get(@PathVariable String name) throws NotFoundException {
        return syncJobStore.find(name)
                .map(this::toExtendedResponse)
                .orElseThrow(() -> constructNFE(name));
    }

    @Operation(summary = "Get a job's errors")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping(path = "/{name}/errors")
    public ErrorsResponse getErrors(@PathVariable String name) throws NotFoundException {
        SyncJob syncJob = syncJobStore.find(name)
                .orElseThrow(() -> constructNFE(name));
        synchronized (syncJob) {
            SyncJob.StateDetails details = syncJob.getDetails(SyncJob.State.RUNNING);
            if (!(details instanceof SyncJob.RunningDetails)) {
                return null;
            }
            SyncJob.RunningDetails runningDetails = (SyncJob.RunningDetails) details;
            return toResponse(runningDetails);
        }
    }

    @Operation(summary = "Delete a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping(path = "/{name}")
    public void delete(@PathVariable String name) throws NotFoundException {
        syncJobStore.delete(name);
    }

    @Operation(summary = "Run a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(path = "/{name}/run")
    public SyncJobResponse.Extended run(@PathVariable String name) throws NotFoundException {
        SyncJob syncJob = syncJobStore.find(name)
                .orElseThrow(() -> constructNFE(name));
        syncJobManager.run(syncJob);
        return toExtendedResponse(syncJob);
    }

    @Operation(summary = "Cancel a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(path = "/{name}/cancel")
    public SyncJobResponse.Extended cancel(@PathVariable String name) throws NotFoundException {
        SyncJob syncJob = syncJobStore.find(name)
                .orElseThrow(() -> constructNFE(name));
        syncJobManager.cancel(syncJob);
        return toExtendedResponse(syncJob);
    }

    private NotFoundException constructNFE(String name) {
        return new NotFoundException("SyncJob with name '" + name + "' not found");
    }

    private SyncJobResponse toResponse(SyncJob syncJob) {
        synchronized (syncJob) {
            return syncJobMapper.toResponse(syncJob);
        }
    }

    private SyncJobResponse.Extended toExtendedResponse(SyncJob syncJob) {
        synchronized (syncJob) {
            return syncJobMapper.toExtendedResponse(syncJob);
        }
    }

    private ErrorsResponse toResponse(SyncJob.RunningDetails runningDetails) {
        return runningDetailsMapper.toErrorsResponse(runningDetails);
    }
}

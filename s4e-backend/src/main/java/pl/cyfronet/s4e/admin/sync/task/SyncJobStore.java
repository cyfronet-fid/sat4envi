/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.admin.sync.task;

import lombok.extern.slf4j.Slf4j;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class SyncJobStore {
    private final Map<String, SyncJob> tasks;

    public SyncJobStore() {
        this(new HashMap<>());
    }

    public SyncJobStore(Map<String, SyncJob> tasks) {
        this.tasks = tasks;
    }

    public List<SyncJob> list() {
        return tasks.values().stream()
                .collect(Collectors.toUnmodifiableList());
    }

    public List<SyncJob> list(SyncJob.State state) {
        return tasks.values().stream()
                .filter(task -> task.getState() == state)
                .collect(Collectors.toUnmodifiableList());
    }

    public synchronized void register(SyncJob task) {
        String name = task.getName();
        if (tasks.putIfAbsent(name, task) != null) {
            throw new IllegalArgumentException("Task with name '" + name + "' already exists");
        }
    }

    public Optional<SyncJob> find(String name) {
        return Optional.ofNullable(tasks.get(name));
    }

    public synchronized void delete(String name) throws NotFoundException {
        SyncJob task = tasks.get(name);
        if (task == null) {
            throw new NotFoundException("Task with name '" + name + "' not found");
        }
        if (task.getState() == SyncJob.State.RUNNING) {
            throw new IllegalArgumentException("Cannot delete a task in state RUNNING, cancel it first to be able to delete it");
        }
        tasks.remove(name);
    }
}

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SyncJobStoreTest {
    private Map<String, SyncJob> tasks;
    private SyncJobStore store;

    @BeforeEach
    public void beforeEach() {
        tasks = new HashMap<>();
        store = new SyncJobStore(tasks);
    }

    @Nested
    class List {
        @BeforeEach
        public void beforeEach() {
            tasks.put("name-1", new SyncJob("name-1", "", true));
            tasks.put("name-2", new SyncJob("name-2", "", true));
            SyncJob task3 = new SyncJob("name-3", "", true);
            task3.setState(SyncJob.State.FINISHED);
            tasks.put("name-3", task3);
        }

        @Test
        public void shouldReturnAll() {
            assertThat(store.list(), hasSize(3));
        }

        @Test
        public void shouldReturnFiltered() {
            assertThat(store.list(SyncJob.State.FINISHED), hasSize(1));
        }
    }

    @Nested
    class Register {
        @Test
        public void shouldRegister() {
            SyncJob task = new SyncJob("name-1", "", true);

            assertThat(tasks, is(anEmptyMap()));

            store.register(task);

            assertThat(tasks, hasEntry(task.getName(), task));
        }

        @Test
        public void shouldThrowIfDuplicate() {
            SyncJob existingTask = new SyncJob("name-1", "other", false);
            SyncJob newTask = new SyncJob("name-1", "", true);
            tasks.put("name-1", existingTask);

            assertThat(tasks, hasEntry("name-1", existingTask));

            assertThrows(IllegalArgumentException.class, () -> store.register(newTask));

            assertThat(tasks, hasEntry("name-1", existingTask));
        }
    }

    @Nested
    class Delete {
        private SyncJob task;

        @BeforeEach
        public void beforeEach() {
            task = new SyncJob("name-1", "", true);
            tasks.put(task.getName(), task);
        }

        @Test
        public void shouldDelete() throws NotFoundException {
            assertThat(tasks, hasEntry(task.getName(), task));

            store.delete(task.getName());

            assertThat(tasks, is(anEmptyMap()));
        }

        @Test
        public void shouldThrowIfNotFound() {
            assertThat(tasks, hasEntry(task.getName(), task));

            assertThrows(NotFoundException.class, () -> store.delete("other"));

            assertThat(tasks, hasEntry(task.getName(), task));
        }

        @Test
        public void shouldThrowIfRunning() {
            task.setState(SyncJob.State.RUNNING);
            assertThat(tasks, hasEntry(task.getName(), task));

            assertThrows(IllegalArgumentException.class, () -> store.delete(task.getName()));

            assertThat(tasks, hasEntry(task.getName(), task));
        }
    }
}

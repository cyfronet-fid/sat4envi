/*
 * Copyright 2021 ACC Cyfronet AGH
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

import {handleHttpRequest$} from 'src/app/common/store.util';
import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ViewConfigurationStore} from './view-configuration.store';
import {
  HC_LOCAL_STORAGE_KEY,
  LARGE_FONT_LOCAL_STORAGE_KEY,
  ViewConfiguration
} from './view-configuration.model';
import {finalize, map, shareReplay, tap} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {IPageableResponse} from '../../../../state/pagable.model';
import {ViewConfigurationQuery} from './view-configuration.query';
import environment from 'src/environments/environment';
import {LocalStorage} from '../../../../app.providers';

@Injectable({providedIn: 'root'})
export class ViewConfigurationService {
  constructor(
    private store: ViewConfigurationStore,
    private query: ViewConfigurationQuery,
    @Inject(LocalStorage) private storage: Storage,
    private http: HttpClient
  ) {}

  add$(viewConfiguration: ViewConfiguration): Observable<boolean> {
    const url = `${environment.apiPrefixV1}/saved-views`;
    const post$ = this.http.post<ViewConfiguration>(url, viewConfiguration).pipe(
      handleHttpRequest$(this.store),
      map(r => true)
    );
    post$.subscribe();
    return post$;
  }

  delete(uuid: string) {
    const url = `${environment.apiPrefixV1}/saved-views/${uuid}`;
    const headers = {'Content-Type': 'application/json'};
    this.http
      .delete(url, {headers})
      .pipe(
        handleHttpRequest$(this.store),
        tap(() => this.store.remove(uuid))
      )
      .subscribe();
  }

  get() {
    const url = `${environment.apiPrefixV1}/saved-views`;
    const headers = {'Content-Type': 'application/json'};
    this.http
      .get<IPageableResponse<ViewConfiguration>>(url, {headers})
      .pipe(
        handleHttpRequest$(this.store),
        map(data => data.content)
      )
      .subscribe(data => this.store.set(data));
  }

  setActive(configuration: string | ViewConfiguration | null) {
    this.store.setLoading(true);
    this.store.setActive(configuration);
    this.store.setLoading(false);
  }

  toggleHighContract() {
    this.store.update(state => {
      this.storage.setItem(
        HC_LOCAL_STORAGE_KEY,
        JSON.stringify(!state.highContrast)
      );
      return {...state, highContrast: !state.highContrast};
    });
  }

  toggleLargeFont() {
    this.store.update(state => {
      this.storage.setItem(
        LARGE_FONT_LOCAL_STORAGE_KEY,
        JSON.stringify(!state.largeFont)
      );
      return {...state, largeFont: !state.largeFont};
    });
  }
}

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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ActivateQuery} from './state/activate.query';
import {Observable} from 'rxjs';
import {ActivateService} from './state/activate.service';
import {pluck} from 'rxjs/operators';
import {State} from './state/activate.model';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 's4e-activate',
  templateUrl: './activate.component.html',
  styleUrls: ['./activate.component.scss']
})
export class ActivateComponent implements OnInit, OnDestroy {
  public error$: Observable<any>;
  public loading$: Observable<boolean>;
  private token: string;
  private state$: Observable<State>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private query: ActivateQuery,
    private service: ActivateService
  ) {}

  ngOnInit() {
    this.error$ = this.query.selectError();
    this.loading$ = this.query.selectLoading();
    this.state$ = this.query.select(state => state.state);

    this.route.params.pipe(untilDestroyed(this), pluck('token')).subscribe(token => {
      this.token = token;
      this.service.activate(token);
    });
  }

  ngOnDestroy(): void {}

  resendToken() {
    this.service.resendToken(this.token);
  }
}

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

import {Component, ComponentFactoryResolver, Inject, OnInit, Type, ViewChild, ViewContainerRef} from '@angular/core';
import {Observable} from 'rxjs';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {environment} from '../../../../environments/environment';
import {ModalService} from '../../state/modal.service';
import {MODAL_PROVIDER, ModalProviderEntry} from '../../modal.providers';
import {ModalQuery} from '../../state/modal.query';
import {Modal} from '../../state/modal.model';

const TRANSITION_DURATION = environment.production ? 150 : 0;

@Component({
  selector: 's4e-modal-outlet',
  templateUrl: './modal-outlet.component.html',
  styleUrls: ['./modal-outlet.component.scss'],
  animations: [
    trigger('listAnimation', [
      state('true', style({opacity: 1.0})),
      state('void', style({opacity: 0.0})),
      transition('* => void', [ // each time the binding value changes
        animate(TRANSITION_DURATION)
      ]),
    ])
  ]
})

export class ModalOutletComponent implements OnInit {

  test: boolean = false;

  @ViewChild('container', {read: ViewContainerRef}) container: ViewContainerRef;

  modals$: Observable<Modal[]>;
  public showAnimations: boolean = environment.production;

  constructor(private componentFactoryResolver: ComponentFactoryResolver,
              private modalService: ModalService,
              private modalQuery: ModalQuery) {}

  ngOnInit() {
    this.modals$ = this.modalQuery.selectAll();
  }

  getModalId(modal: Modal) {
    return modal.id;
  }
}

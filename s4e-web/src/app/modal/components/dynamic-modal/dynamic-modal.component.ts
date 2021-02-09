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

import {
  Component,
  ComponentFactoryResolver,
  ComponentRef,
  Inject,
  Injector,
  Input,
  OnDestroy,
  OnInit,
  Type,
  ViewChild,
  ViewContainerRef,
  ViewEncapsulation
} from '@angular/core';
import {MODAL_DEF, MODAL_PROVIDER, ModalProviderEntry} from '../../modal.providers';
import {Modal} from '../../state/modal.model';

@Component({
  selector: 's4e-dynamic-modal',
  templateUrl: './dynamic-modal.component.html',
  styleUrls: ['./dynamic-modal.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class DynamicModalComponent implements OnInit, OnDestroy {
  private _modal: Modal | null = null;
  private _component: Type<any> | null = null;
  @Input() set modal(modal: Modal) {
    const cmp = this.mapComponent(modal.id);
    if (cmp == null || cmp === this._component) {
      return;
    }

    if (this._component != null) {
      this.componentRef.destroy();
    }

    this._component = cmp;
    this._modal = modal;
    let factory = this.componentFactoryResolver.resolveComponentFactory(
      this._component
    );
    const injector: Injector = Injector.create({
      providers: [{provide: MODAL_DEF, useValue: modal}],
      parent: this.injector
    });

    this.componentRef = this.container.createComponent(factory, 0, injector);

    if (this.componentRef == null) {
      throw Error(
        `${cmp.toString()} was not created. Did you add it to 'entryComponents'?`
      );
    }
  }

  @ViewChild('container', {read: ViewContainerRef, static: true})
  container: ViewContainerRef;
  componentRef: ComponentRef<any>;

  constructor(
    protected componentFactoryResolver: ComponentFactoryResolver,
    @Inject(MODAL_PROVIDER) private modalProviders: ModalProviderEntry[],
    private injector: Injector
  ) {}

  ngOnInit() {}

  ngOnDestroy() {
    /**
     * :TODO: THIS IS HACK, as for angular version 5+ ngOnDestroy is called before animations
     * have chance to finish, this hack postpones it for 5 seconds, which is a reasonable time
     * for any UI animation to finish. This code should be rewritten after bug is fixed by angular
     * team
     */
    setTimeout(() => {
      if (this.componentRef) {
        this.componentRef.destroy();
        this.componentRef = null;
      }
    }, 5000);
  }

  private mapComponent(componentName: string): Type<any> {
    const componentType = this.modalProviders.find(e => e.name === componentName);
    if (!componentType) {
      throw new Error(`${componentName} has not been provided via MODAL_PROVIDER`);
    }
    return componentType.component;
  }
}

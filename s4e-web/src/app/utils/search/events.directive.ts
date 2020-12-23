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

import { Directive, OnInit, OnDestroy, EventEmitter, Output, ViewChild, HostListener, ElementRef } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { untilDestroyed } from 'ngx-take-until-destroy';
import { filter } from 'rxjs/operators';

export function hasBeenClickedInside(source, target): boolean {
  return source.nativeElement.contains(target);
}

@Directive({
  selector: '[s4eEvents]'
})
export class EventsDirective implements OnInit, OnDestroy {
  @Output() insideClick: EventEmitter<void> = new EventEmitter<void>();
  @Output() outsideClick: EventEmitter<void> = new EventEmitter<void>();
  @Output() routerChange: EventEmitter<void> = new EventEmitter<void>();

  @HostListener('document:click', ['$event.target'])
  onClick(target) {
    if (!!this._elementRef) {
      hasBeenClickedInside(this._elementRef, target)
        ? this.insideClick.emit()
        : this.outsideClick.emit();
    }
  }

  constructor(
    private _elementRef: ElementRef,
    private _router: Router
  ) {}

  ngOnInit() {
    this._router.events
      .pipe(
        untilDestroyed(this),
        filter(event => event instanceof NavigationEnd)
      )
      .subscribe(() => this.routerChange.emit());
  }

  ngOnDestroy() {}
}

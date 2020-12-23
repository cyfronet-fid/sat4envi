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

import {AfterViewInit, Directive, ElementRef, Input, Optional} from '@angular/core';
import {NgSelectOption} from '@angular/forms';
import {SelectComponent} from './select.component';

/**
 * @author Michał Szostak
 *
 * @whatItDoes manually injects {@link SelectControlValueAccessor} into <option> tags to make
 * them work with {@link SelectComponent}
 *
 * @howToUse Should work automatically for every option element which is provided as ng-content
 * for ext-select
 *
 * This directive is injected with parent {@link SelectComponent}, if it is not null then manual injection
 * into NgSelectOption directive is performed (as well as setting id)
 * BEWARE - this reimplements {@link NgSelectOption} constructor, so it is very prone to angular updates.
 *
 * As everything is done in constructor, overriding @Input value and @Input ngValue is not required,
 * as at that time NgSelectOption will have manually injected {@link SelectControlValueAccessor}
 *
 * Testing should be done as part of the {@link SelectComponent}
 */
@Directive({
  selector: 'option'
})
export class ExtOptionDirective implements AfterViewInit{
  @Input() ngValue: any = undefined;
  @Input() value: string = undefined;

  constructor(public element: ElementRef, private option: NgSelectOption, @Optional() private parent: SelectComponent) {}

  ngAfterViewInit(): void {
    if(this.parent == null || this.parent.constructor !== SelectComponent) return;

    const select: any = this.parent.select;
    // noinspection TypeScriptUnresolvedFunction
    this.option.id = (select as any)._registerOption();
    (this.option as any)._select = select;

    if(this.ngValue != null)
      this.option.ngValue = this.ngValue;
    else if(this.value != null)
      this.option.value = this.value
  }
}

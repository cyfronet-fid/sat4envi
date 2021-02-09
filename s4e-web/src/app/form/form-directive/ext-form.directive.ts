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
  ComponentFactory,
  ComponentFactoryResolver,
  ComponentRef,
  Directive,
  ElementRef,
  Input,
  OnInit,
  Renderer2,
  ViewContainerRef
} from '@angular/core';
import {SpinnerComponent} from '../spinner/spinner.component';

@Directive({
  selector: '[extForm]'
})
export class ExtFormDirective implements OnInit {
  spinnerFct: ComponentFactory<SpinnerComponent>;
  spinnerRef: ComponentRef<SpinnerComponent> | null = null;

  constructor(
    private elementRef: ElementRef,
    private renderer: Renderer2,
    private cfr: ComponentFactoryResolver,
    private vcr: ViewContainerRef
  ) {
    this.spinnerFct = this.cfr.resolveComponentFactory(SpinnerComponent);
  }

  ngOnInit(): void {
    this.renderer.addClass(this.elementRef.nativeElement, 'ext-form');
    this.renderer.addClass(this.elementRef.nativeElement, 'form-horizontal');
  }
  @Input() controlIdPrefix: string = '';
  @Input() labelSize: number = 4;
  @Input() set isLoading(isLoading: boolean) {
    if (isLoading) {
      this.spinnerRef = this.vcr.createComponent(this.spinnerFct);
      // this.renderer.appendChild(this.elementRef.nativeElement, this.spinnerFct.create(this.vcr.injector))
      // this.viewContainer.clear();
      // If condition is true add template to DOM
      // this.viewContainer.createComponent(this.cfr.resolveComponentFactory(SpinnerComponent));
      // this.viewContainer.createEmbeddedView(this.templateRef);
    } else if (this.spinnerRef !== null) {
      this.spinnerRef.destroy();
      // Else remove template from DOM
      // this.viewContainer.clear();
      // this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }
}

import {Directive, Input, ElementRef, Renderer2} from '@angular/core';

@Directive({
  selector: '[s4eDynamicSpace]'
})
export class DynamicSpaceDirective {
  @Input('s4eDynamicSpace')
  set mimicElement(element: ElementRef) {
    const mimicElementHeight = !!element && !!element.nativeElement && element.nativeElement.offsetHeight || null;
    const newHeightInPx = `${mimicElementHeight}px`;
    this._renderer.setStyle(this._self.nativeElement, 'height', newHeightInPx);
  }

  constructor(
    private _self: ElementRef,
    private _renderer: Renderer2
  ) {}
}

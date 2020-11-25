import {Directive, Input, ElementRef, Renderer2} from '@angular/core';

@Directive({
  selector: '[s4eDynamicSpace]'
})
export class DynamicSpaceDirective {
  @Input('resize')
  set mimicElement(height: number) {
    height = !height || height < 0 ? 0 : height;
    this._renderer.setStyle(this._self.nativeElement, 'height', `${height}px`);
  }

  constructor(
    private _self: ElementRef,
    private _renderer: Renderer2
  ) {}
}

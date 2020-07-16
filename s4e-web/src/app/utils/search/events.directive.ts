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

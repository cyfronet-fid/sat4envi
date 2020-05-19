import { TilesDashboardModule } from './../tiles-dashboard.module';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { Component, DebugElement } from '@angular/core';

@Component({
  selector: 's4e-tile-mock-component',
  template: `
    <s4e-tile>
      <header class="panel__header" tile-title>Zarządzaj instytucjami</header>
      <p i18n tile-description>Lista wszystkich instytucji. W tym miejscu możesz je edytować</p>
      <button footer-navigation>Zarządzaj</button>
    </s4e-tile>
  `
})
export class TileMockComponent {}

describe('TileComponent', () => {
  let component: TileMockComponent;
  let fixture: ComponentFixture<TileMockComponent>;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TilesDashboardModule
      ],
      declarations: [
        TileMockComponent
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TileMockComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display', () => {
    const tileTitle = de.nativeElement.querySelector('.panel__item .panel__header');
    expect(tileTitle.innerHTML).toEqual('Zarządzaj instytucjami');

    const tileDescription = de.nativeElement.querySelector('.panel__item p');
    expect(tileDescription.innerHTML).toEqual('Lista wszystkich instytucji. W tym miejscu możesz je edytować');

    const navigationBtn = de.nativeElement.querySelector('.panel__footer button');
    expect(navigationBtn.textContent).toEqual('Zarządzaj');
  });
});

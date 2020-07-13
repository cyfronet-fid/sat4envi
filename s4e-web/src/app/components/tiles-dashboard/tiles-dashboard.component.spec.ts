import { TilesDashboardModule } from './tiles-dashboard.module';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement, Component } from '@angular/core';

@Component({
  selector: 's4e-tiles-dashboards-mock-component',
  template: `
    <s4e-tiles-dashboard>
      <h1 dashboard-title>Witamy w panelu administratora Sat4Envi</h1>
      <p dashboard-description>Jesteś superadministratorem</p>
      <ng-container dashboard-tiles>
        <p>Test</p>
      </ng-container>
    </s4e-tiles-dashboard>
  `
})
export class TilesDashboardMockComponent {}

describe('TilesDashboardComponent', () => {
  let component: TilesDashboardMockComponent;
  let fixture: ComponentFixture<TilesDashboardMockComponent>;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TilesDashboardModule
      ],
      declarations: [
        TilesDashboardMockComponent
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TilesDashboardMockComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display', () => {
    const headerTitle = de.nativeElement.querySelector('.content__header h1');
    expect(headerTitle.innerHTML).toEqual('Witamy w panelu administratora Sat4Envi');

    const headerDescription = de.nativeElement.querySelector('.content__header p');
    expect(headerDescription.innerHTML).toEqual('Jesteś superadministratorem');

    const tilesItem = de.nativeElement.querySelector('.panel p');
    expect(tilesItem.innerHTML).toEqual('Test');
  });
});

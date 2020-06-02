import { DynamicSpaceDirective } from './dynamic-space.directive';
import { TestBed, async, ComponentFixture, fakeAsync, tick } from '@angular/core/testing';
import { TestingConfigProvider } from 'src/app/app.configuration.spec';
import { Component, ViewChild, ElementRef, DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

@Component({
  template: `
    <div [s4eDynamicSpace]="dynamicHeight" id="dynamic-space"></div>
    <div #dynamicHeight style="height: 100px" id="dynamic-element"></div>
  `
})
export class DynamicSpaceMockComponent {
  @ViewChild('dynamicHeight')
  set dynamicHeightRef(element: ElementRef) {
    if (element) {
      setTimeout(() => {
        element.nativeElement.style.height = '1000px';
      }, 1000);
    }
  }
}

describe('DynamicSpaceDirective', () => {
  let fixture: ComponentFixture<DynamicSpaceMockComponent>;
  let component: DynamicSpaceMockComponent;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [],
      declarations: [
        DynamicSpaceMockComponent,
        DynamicSpaceDirective
      ],
      providers: [
        TestingConfigProvider
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DynamicSpaceMockComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement.query(By.directive(DynamicSpaceDirective));
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(component).toBeTruthy();
  });

  it('Should resize on initialization', () => {
    fixture.whenStable()
      .then(() => {
        const space = de.query(By.css('#dynamic-space'));
        const spaceHeight = space.nativeElement.offsetHeight;

        expect(spaceHeight).toEqual(100);
      });
  });
  it('Should resize on timeout change', fakeAsync(() => {
    fixture.whenStable()
      .then(() => {
        const space = de.query(By.css('#dynamic-space'));
        const spaceHeight = space.nativeElement.offsetHeight;

        tick(1000);

        expect(spaceHeight).toEqual(1000);
      });
  }));
});

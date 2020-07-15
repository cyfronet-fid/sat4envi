import { Component, DebugElement } from "@angular/core";
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";
import { Router } from "@angular/router";
import { EventsModule } from "./events.module";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { RouterTestingModule } from "@angular/router/testing";

@Component({
  template: `
    <body>
      <div class="outside"></div>
      <div class="inside" s4eEvents (outsideClick)="toggle()" (routerChange)="toggle()">
        <p>Test content</p>
      </div>
    </body>
  `
})
export class DropdownMockComponent {
  toggle = () => {};
}

describe('DropdownComponent', () => {
  let component: DropdownMockComponent;
  let fixture: ComponentFixture<DropdownMockComponent>;
  let de: DebugElement;
  let spyToggle: jasmine.Spy;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DropdownMockComponent],
      imports: [
        EventsModule,
        HttpClientTestingModule,
        RouterTestingModule
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DropdownMockComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    spyToggle = spyOn(component, 'toggle');
    router = TestBed.get(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should do nothing on click inside', () => {
    const insideContent = de.nativeElement.querySelector('.inside p');
    insideContent.click();

    expect(spyToggle).not.toHaveBeenCalled();
  });
  it('should call toggle on outside click', () => {
    const outsideContent = de.nativeElement.querySelector('.outside');
    outsideContent.click();

    expect(spyToggle).toHaveBeenCalled();
  });
  it('should call toggle on router event change', fakeAsync(() => {
    router.navigateByUrl('/');
    tick();

    expect(spyToggle).toHaveBeenCalled();
  }));
});

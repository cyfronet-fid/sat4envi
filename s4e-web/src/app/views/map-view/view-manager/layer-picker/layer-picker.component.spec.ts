import { By } from '@angular/platform-browser';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ItemsPickerComponent } from './layer-picker.component';

fdescribe('ItemsPickerComponent', () => {
  let component: ItemsPickerComponent;
  let fixture: ComponentFixture<ItemsPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ItemsPickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ItemsPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have star on enabled', () => {
    component.hasFavourite = true;
    component.items = [{cid: 0, caption: '', active: true, favourite: false}];
    spyOn(component.isFavoriteSelected, 'emit');
    fixture.detectChanges();

    expect(fixture.debugElement.queryAll(By.css('.fa-star'))).toBeTruthy();

    fixture.debugElement
      .queryAll(By.css('.fa-star'))[0].nativeElement.click();
    fixture.detectChanges();

    expect(component.isFavoriteSelected.emit).toHaveBeenCalled();
  });

  it('should not have star on disabled', () => {
    component.hasFavourite = false;
    component.items = [{cid: 0, caption: '', active: true, favourite: false}];
    fixture.detectChanges();

    expect(fixture.debugElement.queryAll(By.css('.fa-star')).length > 0).toBeFalsy();
  });
});

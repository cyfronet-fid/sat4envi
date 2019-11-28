import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import {Component} from '@angular/core';
import {ModalComponent} from './modal.component';
import {ModalService} from '../../state/modal.service';
import {ModalModule} from '../../modal.module';
import {makeModalProvider} from '../../modal.providers';

export const MODAL_ID = 'mock-modal';

@Component({
  selector: 'mock-modal',
  template: '',
  styles: []
})
class MockModalComponent extends ModalComponent {
  constructor(modalService: ModalService) {
    super(modalService, MODAL_ID);
  }
}

describe('ModalComponent', () => {
  let component: ModalComponent;
  let fixture: ComponentFixture<ModalComponent>;
  let service: ModalService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        MockModalComponent,
      ],
      imports: [
        ModalModule
      ],
      providers: [
        makeModalProvider(MODAL_ID, MockModalComponent)
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MockModalComponent);
    service = TestBed.get(ModalService);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have working dismiss()', () => {
    let spy = spyOn(service, 'hide');
    component.dismiss();

    expect(spy).toHaveBeenCalledTimes(1);
    expect(spy).toHaveBeenCalledWith(MODAL_ID, undefined);
  });
});

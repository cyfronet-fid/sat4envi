import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {DummyModalComponent} from './dummy-modal.component';
import {ModalModule} from '../../modal.module';
import {MODAL_DEF} from '../../modal.providers';
import {createModal} from '../../state/modal.model';
import {ALERT_MODAL_ID} from '../alert-modal/alert-modal.model';

describe('DummyModalComponent', () => {
  let component: DummyModalComponent;
  let fixture: ComponentFixture<DummyModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ModalModule],
      providers: [
        {provide: MODAL_DEF, useValue: createModal({id: ALERT_MODAL_ID})}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DummyModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

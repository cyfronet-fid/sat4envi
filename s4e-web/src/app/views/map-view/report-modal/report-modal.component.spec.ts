import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ReportModalComponent} from './report-modal.component';
import {MapModule} from '../map.module';
import {ModalModule} from '../../../modal/modal.module';
import {MODAL_DEF} from '../../../modal/modal.providers';
import {REPORT_MODAL_ID} from './report-modal.model';
import {By} from '@angular/platform-browser';

describe('ReportModalComponent', () => {
  let component: ReportModalComponent;
  let fixture: ComponentFixture<ReportModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule],
      providers: [
        {
          provide: MODAL_DEF, useValue: {
            id: REPORT_MODAL_ID,
            size: 'lg',
            mapHeight: 150,
            mapWidth: 300,
            mapImage: 'data:image/png;base64,00',
          }
        }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('submitting form should call accept', () => {
    const spy = spyOn(component, 'accept').and.stub();
    fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement.click();
    expect(spy).toHaveBeenCalled();
  });

  // NOTE - THIS test will be done via E2E testing
  it('should generate pdf', () => {});
});

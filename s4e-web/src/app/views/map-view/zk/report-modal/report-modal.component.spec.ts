import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ReportModalComponent} from './report-modal.component';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {REPORT_MODAL_ID, ReportModal} from './report-modal.model';
import {By} from '@angular/platform-browser';
import {MapModule} from '../../map.module';
import {filter, take} from 'rxjs/operators';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {filterFalse} from '../../../../utils/rxjs/observable';

describe('ReportModalComponent', () => {
  let component: ReportModalComponent;
  let fixture: ComponentFixture<ReportModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
      providers: [
        {
          provide: MODAL_DEF, useValue: {
            id: REPORT_MODAL_ID,
            size: 'lg',
            image: {
              width: 200,
              height: 200,
              pointResolution: 20,
              image: 'data:image/png;base64,00',
            }
          } as ReportModal
        },
        LocalStorageTestingProvider
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportModalComponent);
    component = fixture.componentInstance;
    spyOn(component.reportGenerator, 'loadAssets').and.callFake(function () {
      this.loading$.next(false);
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('submitting form should call accept', async () => {
    const spy = spyOn(component, 'accept').and.stub();
    await component.disabled$.pipe(filterFalse(), take(1)).toPromise();
    fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement.click();
    expect(spy).toHaveBeenCalled();
  });
});

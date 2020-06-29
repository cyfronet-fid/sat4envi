import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ReportModalComponent} from './report-modal.component';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {REPORT_MODAL_ID} from './report-modal.model';
import {By} from '@angular/platform-browser';
import {MapModule} from '../../map.module';
import {filter, take} from 'rxjs/operators';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';

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
    await component.disabled$.pipe(filter(d => d === false), take(1)).toPromise();
    fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement.click();
    expect(spy).toHaveBeenCalled();
  });
});

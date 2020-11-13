import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {By} from '@angular/platform-browser';
import {MapModule} from '../../map.module';
import {filter, take} from 'rxjs/operators';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {REPORT_TEMPLATES_MODAL_ID} from './report-templates-modal.model';
import {ReportTemplatesModalComponent} from './report-templates-modal.component';

describe('ReportModalComponent', () => {
  let component: ReportTemplatesModalComponent;
  let fixture: ComponentFixture<ReportTemplatesModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
      providers: [
        {
          provide: MODAL_DEF, useValue: {
            id: REPORT_TEMPLATES_MODAL_ID,
            size: 'lg'
          }
        }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportTemplatesModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

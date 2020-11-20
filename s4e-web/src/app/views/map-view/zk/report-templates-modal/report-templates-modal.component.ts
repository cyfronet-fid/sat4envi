import {Component, Inject, OnDestroy} from '@angular/core';
import {ModalService} from '../../../../modal/state/modal.service';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {ReportTemplateService} from '../state/report-templates/report-template.service';
import {Modal} from '../../../../modal/state/modal.model';
import {REPORT_TEMPLATES_MODAL_ID} from './report-templates-modal.model';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ReportTemplateQuery} from '../state/report-templates/report-template.query';
import {Observable} from 'rxjs';
import {ReportTemplate} from '../state/report-templates/report-template.model';
import {untilDestroyed} from 'ngx-take-until-destroy';


@Component({
  selector: 's4e-report-templates-modal',
  templateUrl: './report-templates-modal.component.html',
  styleUrls: ['./report-templates-modal.component.scss']
})
export class ReportTemplatesModalComponent extends ModalComponent implements OnDestroy {
  public reportsTemplates$: Observable<ReportTemplate[]> = this._reportTemplateQuery.selectAll();

  constructor(
    modalService: ModalService,
    @Inject(MODAL_DEF) modal: Modal,
    modalQuery: ModalQuery,

    private _reportTemplateService: ReportTemplateService,
    private _reportTemplateQuery: ReportTemplateQuery
  ) {
    super(modalService, REPORT_TEMPLATES_MODAL_ID);

    this._reportTemplateService.get$()
      .pipe(untilDestroyed(this))
      .subscribe();
  }

  load(reportTemplate: ReportTemplate) {
    this._reportTemplateService.load$(reportTemplate)
      .pipe(untilDestroyed(this))
      .subscribe(() => this.dismiss());
  }

  delete(reportTemplate: ReportTemplate) {
    this._reportTemplateService.delete$(reportTemplate)
      .pipe(untilDestroyed(this))
      .subscribe();
  }

  ngOnDestroy() {}
}

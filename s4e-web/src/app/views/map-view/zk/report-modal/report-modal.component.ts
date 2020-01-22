import {Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {FormModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {isReportModal, ReportForm, ReportModal} from './report-modal.model';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {assertModalType} from '../../../../modal/utils/modal/misc';
import {ReportGenerator} from './report-generator';
import {HttpClient} from "@angular/common/http";


@Component({
  selector: 's4e-report-modal',
  templateUrl: './report-modal.component.html',
  styleUrls: ['./report-modal.component.scss']
})
export class ReportModalComponent extends FormModalComponent<'report'> {
  image: string = '';
  imageWidth: number;
  imageHeight: number;
  imagePositionTop: number = 0;
  public reportGenerator: ReportGenerator;
  @ViewChild('reportTemplate', {read: ElementRef}) reportHTML: ElementRef;

  makeForm(): FormGroup<FormState["report"]> {
    return new FormGroup<ReportForm>({
      caption: new FormControl<string>('Przykładowy Tytuł'),
      notes: new FormControl<string>(''),
    });
  }

  constructor(http: HttpClient,
              modalService: ModalService,
              @Inject(MODAL_DEF) modal: ReportModal,
              modalQuery: ModalQuery,
              fm: AkitaNgFormsManager<FormState>) {
    super(fm, modalService, modalQuery, modal.id, 'report');

    assertModalType(isReportModal, modal);

    this.image = modal.mapImage;
    this.imageWidth = modal.mapWidth;
    this.imageHeight = modal.mapHeight;

    this.reportGenerator = new ReportGenerator(http, this.image, this.imageWidth, this.imageHeight);
  }

  ngOnInit(): void {
    // noinspection JSIgnoredPromiseFromCall
    this.reportGenerator.loadAssets();
    super.ngOnInit();
  }

  accept() {
    this.reportGenerator.generate(this.form.controls.caption.value, this.form.controls.notes.value).subscribe(() => this.dismiss());
  }
}

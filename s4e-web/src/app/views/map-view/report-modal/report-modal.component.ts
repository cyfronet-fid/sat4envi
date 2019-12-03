import {Component, ElementRef, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from '../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../modal/state/modal.service';
import {MODAL_DEF} from '../../../modal/modal.providers';
import {Modal} from '../../../modal/state/modal.model';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {isReportModal, REPORT_MODAL_ID, ReportForm} from './report-modal.model';
import {environment} from '../../../../environments/environment';
import {devRestoreFormState} from '../../../utils/miscellaneous/miscellaneous';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../state/form/form.model';
import {ModalQuery} from '../../../modal/state/modal.query';
import * as JsPDF from 'jspdf';


@Component({
  selector: 's4e-report-modal',
  templateUrl: './report-modal.component.html',
  styleUrls: ['./report-modal.component.scss']
})
export class ReportModalComponent extends ModalComponent implements OnInit, OnDestroy {
  isWorking: boolean = false;
  form: FormGroup<ReportForm>;
  formKey: keyof FormState = 'report';
  image: string = '';
  imageWidth: number;
  imageHeight: number;
  @ViewChild('reportTemplate', {read: ElementRef}) reportHTML: ElementRef;
  imagePositionTop: number = 0;

  constructor(modalService: ModalService, @Inject(MODAL_DEF) modal: Modal,
              private modalQuery: ModalQuery,
              protected fm: AkitaNgFormsManager<FormState>) {
    super(modalService, modal.id);

    if (!isReportModal(modal)) {
      throw new Error(`${modal} is not a valid ${REPORT_MODAL_ID}`);
    }
    this.image = modal.mapImage;
    this.imageWidth = modal.mapWidth;
    this.imageHeight = modal.mapHeight;
  }

  ngOnInit(): void {
    this.form = new FormGroup<ReportForm>({
      caption: new FormControl<string>('Przykładowy Tytuł'),
      notes: new FormControl<string>('Na tym obrazku możemy zobaczyć mapę...'),
    });

    import('./fonts/Ubuntu-Regular-normal').then((ubuntuRegular) => ubuntuRegular.registerFont(JsPDF));

    if (environment.hmr) {
      devRestoreFormState(this.fm.query.getValue()[this.formKey], this.form);
      this.fm.upsert(this.formKey, this.form);

      this.modalQuery.modalClosed$(REPORT_MODAL_ID).pipe(untilDestroyed(this)).subscribe(() => this.fm.remove(this.formKey));
    }
  }

  ngOnDestroy(): void {
    if (environment.hmr) {
      this.fm.unsubscribe(this.formKey);
    }
  }

  accept() {
    this.isWorking = true;

    // it is inside of the timeout to make sure not to freeze frontend, and allow for 'loader' to be displayed
    setTimeout(() => {
      let doc = new JsPDF({
        orientation: 'landscape',
        unit: 'mm',
        format: 'a4'
      });

      const A4Width = 297;
      const A4Height = 210;
      const DPM = (72.0 / 25.6); // density per mm
      const A4Ration = A4Width / A4Height;

      const printImageHeight = this.imageHeight / this.imageWidth * A4Width;

      const imageYOffset = (A4Height - printImageHeight) / 2.0;

      doc.setFont('Ubuntu-Regular');
      doc.addImage(this.image, 'PNG', 0, imageYOffset, A4Width, printImageHeight);
      doc.setFontSize(20);
      doc.text(this.form.controls.caption.value, 10, 10);
      const noteFontSize = 12.0;
      doc.setFontSize(noteFontSize);
      let textWidth = doc.getStringUnitWidth(this.form.controls.notes.value) * noteFontSize / DPM;
      doc.text(this.form.controls.notes.value, A4Width - 10 - textWidth, A4Height - 10);
      doc.save(`RAPORT.${new Date().toISOString()}.pdf`);
      this.isWorking = false;
      this.dismiss();
    });
  }
}

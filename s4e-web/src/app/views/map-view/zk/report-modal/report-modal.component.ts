import {Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {FormModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {isReportModal, ReportForm, ReportModal} from './report-modal.model';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {ModalQuery} from '../../../../modal/state/modal.query';
import * as JsPDF from 'jspdf';
import {assertModalType} from '../../../../modal/utils/modal/misc';


@Component({
  selector: 's4e-report-modal',
  templateUrl: './report-modal.component.html',
  styleUrls: ['./report-modal.component.scss']
})
export class ReportModalComponent extends FormModalComponent<'report'> {
  isWorking: boolean = false;
  image: string = '';
  imageWidth: number;
  imageHeight: number;
  imagePositionTop: number = 0;
  @ViewChild('reportTemplate', {read: ElementRef}) reportHTML: ElementRef;

  makeForm(): FormGroup<FormState["report"]> {
    return new FormGroup<ReportForm>({
      caption: new FormControl<string>('Przykładowy Tytuł'),
      notes: new FormControl<string>(''),
    });
  }

  constructor(modalService: ModalService, @Inject(MODAL_DEF) modal: ReportModal,
              modalQuery: ModalQuery,
              fm: AkitaNgFormsManager<FormState>) {
    super(fm, modalService, modalQuery, modal.id, 'report');

    assertModalType(isReportModal, modal);

    this.image = modal.mapImage;
    this.imageWidth = modal.mapWidth;
    this.imageHeight = modal.mapHeight;
  }

  ngOnInit(): void {
    import('./fonts/Ubuntu-Regular-normal').then((ubuntuRegular) => ubuntuRegular.registerFont(JsPDF));
    super.ngOnInit();
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

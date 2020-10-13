import { validateAllFormFields } from 'src/app/utils/miscellaneous/miscellaneous';
import { untilDestroyed } from 'ngx-take-until-destroy';
import {Component, ElementRef, Inject, ViewChild, OnInit, OnDestroy, AfterViewInit} from '@angular/core';
import {FormModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {isReportModal, ReportForm, ReportModal} from './report-modal.model';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {assertModalType} from '../../../../modal/utils/modal/misc';
import {ReportGenerator} from './report-generator';
import {HttpClient} from "@angular/common/http";
import {combineLatest, Observable} from "rxjs";
import {map} from "rxjs/operators";
import moment from "moment";
import Cropper from 'cropperjs';


@Component({
  selector: 's4e-report-modal',
  templateUrl: './report-modal.component.html',
  styleUrls: ['./report-modal.component.scss']
})
export class ReportModalComponent extends FormModalComponent<'report'> implements OnInit, AfterViewInit {
  image: string = '';
  public disabled$: Observable<boolean>;
  public reportGenerator: ReportGenerator;
  public productName: string|null = null;
  public sceneDate: string|null = null;
  private cropper: Cropper = null;
  @ViewChild('reportTemplate', {read: ElementRef}) reportHTML: ElementRef;
  @ViewChild('minimap', {read: ElementRef}) minimap: ElementRef;

  makeForm(): FormGroup<FormState['report']> {
    return new FormGroup<ReportForm>({
      caption: new FormControl<string>(null, [Validators.maxLength(80), Validators.required]),
      notes: new FormControl<string>(null, [Validators.maxLength(800), Validators.required]),
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
    if (modal.sceneDate != null) {
      this.sceneDate = moment(modal.sceneDate).format('DD.MM.YYYY g. HH:mm');
    }
    this.productName = modal.productName;

    this.reportGenerator = new ReportGenerator(http);
  }

  ngOnInit(): void {
    // noinspection JSIgnoredPromiseFromCall
    this.reportGenerator.loadAssets();
    super.ngOnInit();
    this.disabled$ = combineLatest([this.reportGenerator.working$, this.reportGenerator.loading$])
      .pipe(map(([w, l]) => w || l));
  }

  ngAfterViewInit(): void {
    this.cropper = new Cropper(this.minimap.nativeElement,
      {
        aspectRatio: 1,
        viewMode: 3,
        dragMode: 'move',
        responsive: true,
        autoCrop: true,
        autoCropArea: 1,
        cropBoxMovable: false,
        cropBoxResizable: false
      });
    this.cropper.replace(this.image)
  }

  async accept() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});

    if (this.form.invalid) {
      return;
    }

    const imageData = this.cropper.getCroppedCanvas({height: 2048, width: 2048}).toDataURL();
    const imageWidth = this.cropper.getCropBoxData().width
    const imageHeight = this.cropper.getCropBoxData().height

    this.reportGenerator
      .generate(
        imageData,
        imageWidth,
        imageHeight,
        this.form.controls.caption.value,
        this.form.controls.notes.value,
        this.productName,
        this.sceneDate
      )
      .pipe(untilDestroyed(this))
      .subscribe(() => this.dismiss());
  }
}

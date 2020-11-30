import {validateAllFormFields} from 'src/app/utils/miscellaneous/miscellaneous';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {AfterViewInit, Component, ElementRef, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
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
import {HttpClient} from '@angular/common/http';
import {combineLatest, Observable} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import moment from 'moment';
import Cropper from 'cropperjs';
import {ReportTemplateService} from '../state/report-templates/report-template.service';
import {ReportTemplateQuery} from '../state/report-templates/report-template.query';
import {ReportTemplate} from '../state/report-templates/report-template.model';
import {Legend} from '../../state/legend/legend.model';


@Component({
  selector: 's4e-report-modal',
  templateUrl: './report-modal.component.html',
  styleUrls: ['./report-modal.component.scss']
})
export class ReportModalComponent extends FormModalComponent<'report'> implements OnInit, AfterViewInit, OnDestroy {
  public image: string = '';
  private legend: Legend|null = null;
  private pointResolution: number;
  public disabled$: Observable<boolean>;
  public reportGenerator: ReportGenerator;
  public productName: string | null = null;
  public sceneDate: string | null = null;
  @ViewChild('reportTemplate', {read: ElementRef}) reportHTML: ElementRef;
  @ViewChild('minimap', {read: ElementRef}) minimap: ElementRef;
  private cropper: Cropper = null;

  constructor(http: HttpClient,
              modalService: ModalService,
              @Inject(MODAL_DEF) modal: ReportModal,
              modalQuery: ModalQuery,
              fm: AkitaNgFormsManager<FormState>,
              private _reportTemplateService: ReportTemplateService,
              private _reportTemplateQuery: ReportTemplateQuery) {
    super(fm, modalService, modalQuery, modal.id, 'report');

    assertModalType(isReportModal, modal);

    this.image = modal.image.image;
    if (modal.sceneDate != null) {
      this.sceneDate = moment.utc(modal.sceneDate).format('DD.MM.YYYY g. HH:mm UTC');
    }
    this.legend = modal.legend;
    this.pointResolution = modal.image.pointResolution;
    this.productName = modal.productName;

    this.reportGenerator = new ReportGenerator(http);
  }

  makeForm(): FormGroup<FormState['report']> {
    return new FormGroup<ReportForm>({
      caption: new FormControl<string>(null, [Validators.maxLength(80), Validators.required]),
      notes: new FormControl<string>(null, [Validators.maxLength(740), Validators.required]),
    });
  }

  ngOnInit(): void {
    // noinspection JSIgnoredPromiseFromCall
    this.reportGenerator.loadAssets(this.legend && this.legend.url);
    super.ngOnInit();
    this.disabled$ = combineLatest([this.reportGenerator.working$, this.reportGenerator.loading$])
      .pipe(map(([w, l]) => w || l));

    this._reportTemplateQuery.selectActiveId()
      .pipe(
        untilDestroyed(this),
        map(() => this._reportTemplateQuery.getActive() as ReportTemplate),
        filter(activeTemplate => !!activeTemplate)
      )
      .subscribe(activeTemplate => {
          const {caption, notes} = activeTemplate;
          this.form.setValue({caption, notes});
        }
      );
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
        cropBoxResizable: false,
        minCropBoxHeight: 2048,
        minCropBoxWidth: 2048
      });
    this.cropper.replace(this.image);
  }

  async accept() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});

    if (this.form.invalid) {
      return;
    }

    const IMAGE_SIZE = 2048;

    const imageData = this.cropper.getCroppedCanvas().toDataURL();
    const imageWidth = this.cropper.getCroppedCanvas().width;
    const imageHeight = this.cropper.getCroppedCanvas().height;

    this.reportGenerator
      .generate(
        imageData,
        imageWidth,
        imageHeight,
        this.form.controls.caption.value,
        this.form.controls.notes.value,
        this.productName,
        this.sceneDate,
        this.pointResolution,
        this.legend
      )
      .pipe(untilDestroyed(this))
      .subscribe(() => this.dismiss());
  }

  saveAsTemplate() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});

    if (this.form.invalid) {
      return;
    }

    this._reportTemplateService.create$(this.form.value)
      .pipe(untilDestroyed(this))
      .subscribe();
  }

  ngOnDestroy() {
    this._reportTemplateService.setActive(null);
    super.ngOnDestroy();
  }
}

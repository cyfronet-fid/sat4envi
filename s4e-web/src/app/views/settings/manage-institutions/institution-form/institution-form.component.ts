import { InstitutionsSearchResultsQuery } from './../../state/institutions-search/institutions-search-results.query';
import { Router, ActivatedRoute } from '@angular/router';
import { AkitaNgFormsManager } from '@datorama/akita-ng-forms-manager';
import { Component } from '@angular/core';
import { ModalService } from 'src/app/modal/state/modal.service';
import { ModalQuery } from 'src/app/modal/state/modal.query';
import {
  ParentInstitutionModal,
  PARENT_INSTITUTION_MODAL_ID,
  isParentInstitutionModal
} from '../parent-institution-modal/parent-institution-modal.model';
import { untilDestroyed } from 'ngx-take-until-destroy';
import { map, filter, switchMap } from 'rxjs/operators';
import { FormGroup, FormControl, Validators } from '@ng-stack/forms';
import { validateAllFormFields } from 'src/app/utils/miscellaneous/miscellaneous';
import { GenericFormComponent } from 'src/app/utils/miscellaneous/generic-form.component';
import { FormState } from 'src/app/state/form/form.model';
import { InstitutionQuery } from '../../state/institution/institution.query';
import { Institution } from '../../state/institution/institution.model';
import { InstitutionService } from '../../state/institution/institution.service';
import { File, ImageBase64 } from './files.utils';

@Component({
  selector: 's4e-add-institution',
  templateUrl: './institution-form.component.html',
  styleUrls: ['./institution-form.component.scss']
})
export class InstitutionFormComponent extends GenericFormComponent<InstitutionQuery, Institution> {
  form: FormGroup<Institution>;
  activeInstitution: Institution;
  emblemImgSrc: string;

  constructor(
    private _formsManager: AkitaNgFormsManager<FormState>,
    private _router: Router,
    private _institutionQuery: InstitutionQuery,
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _institutionService: InstitutionService,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery,
    private _activatedRoute: ActivatedRoute
  ) {
    super(_formsManager, _router, _institutionQuery, 'addInstitution');
  }

  ngOnInit() {
    this.form = this._createInstitutionForm();
    this._activatedRoute.data
      .pipe(
        untilDestroyed(this),
        filter(data => data.isEditMode),
        switchMap(() => this._institutionsSearchResultsQuery.getSelectedInstitutionBy$(this._activatedRoute))
      )
      .subscribe(async (activeInstitution: Institution) => {
        this.activeInstitution = activeInstitution;
        this._setFormWith(this.form, activeInstitution);
      });

    super.ngOnInit();
  }


  loadLogo($event) {
    const emblem = File.getFirst($event);
    if (!emblem) {
      return;
    }

    ImageBase64.getFromFile$(emblem)
      .subscribe(
        (imageBase64) => {
          if (imageBase64 === '') {
            $event.target.value = '';
            this.form.controls.emblem.setErrors({'incorrect': true});
            this.emblemImgSrc = null;
            return;
          }

          this.form.controls.emblem.setValue(imageBase64);
          this.emblemImgSrc = 'data:image/png;base64,' + this.form.controls.emblem.value;
        },
        (error) => console.log('Error while image loading: ', error)
      );
  }

  openParentInstitutionModal() {
    if (this.form.controls.parentSlug.disabled) {
      return;
    }

    const cachedParentInstitution = {
      name: this.form.value.parentName,
      slug: this.form.value.parentSlug
    };
    this._modalService.show<ParentInstitutionModal>({
      id: PARENT_INSTITUTION_MODAL_ID,
      size: 'lg',
      hasReturnValue: true,
      selectedInstitution: !!this.form.value.parentName  && cachedParentInstitution || null
    });

    this._modalQuery.modalClosed$(PARENT_INSTITUTION_MODAL_ID)
      .pipe(
        untilDestroyed(this),
        map(modal => isParentInstitutionModal(modal) ? modal.returnValue : null)
      )
      .toPromise()
      .then((institution: Institution) => {
        if (!!institution) {
          this.form.controls.parentName.setValue(institution.name);
          this.form.controls.parentSlug.setValue(institution.slug);
        }
      });
  }

  updateInstitution() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});
    if (this.form.invalid) {
      return;
    }

    this.activeInstitution
      ? this._institutionService.updateInstitution$(this.form.value)
      : this._institutionService.addInstitutionChild$(this.form.value);

    !!this.activeInstitution
      ? this.activeInstitution = this.form.value
      : this.form.reset();

    this._router.navigate(
      ['/settings/institution'],
      {
        relativeTo: this._activatedRoute,
        queryParamsHandling: 'merge'
      }
    );
  }

  resetForm() {
    !!this.activeInstitution
      ? this._setFormWith(this.form, this.activeInstitution)
      : this.form.reset();

    this._router.navigate(
      ['/settings/institution'],
      {
        relativeTo: this._activatedRoute,
        queryParamsHandling: 'merge'
      }
    );
  }

  protected _createInstitutionForm() {
    return new FormGroup<Institution>({
      parentName: new FormControl<string>(null, Validators.required),
      parentSlug: new FormControl<string>(null, Validators.required),

      slug: new FormControl<string>(),
      name: new FormControl<string>(null, Validators.required),
      address: new FormControl<string>(),
      postalCode: new FormControl<string>(),
      city: new FormControl<string>(null, Validators.required),
      phone: new FormControl<string>(),
      emblem: new FormControl<string>(),
      secondaryPhone: new FormControl<string>(),
      institutionAdminEmail: new FormControl<string>()
    });
  }

  protected _setFormWith(form: FormGroup<Institution>, activeInstitution: Institution) {
    if (!activeInstitution.parentSlug || !activeInstitution.parentName) {
      form.controls.parentSlug.disable();
      form.controls.parentName.disable();
    }

    form.patchValue(activeInstitution);
    ImageBase64.getFromUrl$(activeInstitution.emblem)
      .subscribe(
        (imgBase64) => {
          form.controls.emblem.setValue(imgBase64);
          this.emblemImgSrc = 'data:image/png;base64,' + this.form.controls.emblem.value;
          console.log(imgBase64);
        },
        (error) => console.log('Error while image converting: ', error)
      );
  }
}

/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {InvitationService} from '../../people/state/invitation/invitation.service';
import {InstitutionsSearchResultsQuery} from '../../state/institutions-search/institutions-search-results.query';
import {ActivatedRoute, Router} from '@angular/router';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {Component} from '@angular/core';
import {ModalService} from 'src/app/modal/state/modal.service';
import {ModalQuery} from 'src/app/modal/state/modal.query';
import {
  isParentInstitutionModal,
  PARENT_INSTITUTION_MODAL_ID,
  ParentInstitutionModal
} from '../parent-institution-modal/parent-institution-modal.model';
import {filter, finalize, map, switchMap} from 'rxjs/operators';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {validateAllFormFields} from 'src/app/utils/miscellaneous/miscellaneous';
import {GenericFormComponent} from 'src/app/utils/miscellaneous/generic-form.component';
import {FormState} from 'src/app/state/form/form.model';
import {InstitutionQuery} from '../../state/institution/institution.query';
import {
  Institution,
  InstitutionForm
} from '../../state/institution/institution.model';
import {InstitutionService} from '../../state/institution/institution.service';
import {File, ImageBase64} from './files.utils';
import {combineLatest, forkJoin, of} from 'rxjs';
import {EDIT_INSTITUTION_PATH} from '../../settings.breadcrumbs';
import {emailListValidator} from '../../email-list-validator.utils';
import {SessionService} from '../../../../state/session/session.service';
import {NotificationService} from '../../../../notifications/state/notification.service';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 's4e-add-institution',
  templateUrl: './institution-form.component.html',
  styleUrls: ['./institution-form.component.scss']
})
export class InstitutionFormComponent extends GenericFormComponent<
  InstitutionQuery,
  InstitutionForm
> {
  form: FormGroup<InstitutionForm> = new FormGroup<InstitutionForm>({
    adminsEmails: new FormControl<string>('', emailListValidator),
    parentName: new FormControl<string>(null, Validators.required),
    parentSlug: new FormControl<string>(null, Validators.required),
    slug: new FormControl<string>(),
    name: new FormControl<string>(null, Validators.required),
    address: new FormControl<string>(),
    postalCode: new FormControl<string>(null),
    city: new FormControl<string>(null, Validators.required),
    phone: new FormControl<string>(),
    emblem: new FormControl<string>(),
    secondaryPhone: new FormControl<string>()
  });
  activeInstitution: Institution;
  emblemImgSrc: string;
  isAddingChild$ = this._institutionsSearchResultsQuery.isChildAddition$(
    this._activatedRoute
  );

  constructor(
    private _formsManager: AkitaNgFormsManager<FormState>,
    private _router: Router,
    private _institutionQuery: InstitutionQuery,
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _institutionService: InstitutionService,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery,
    private _activatedRoute: ActivatedRoute,
    private _invitationService: InvitationService,
    private _sessionService: SessionService,
    private _notificationService: NotificationService
  ) {
    super(_formsManager, _router, _institutionQuery, 'addInstitution');
  }

  ngOnInit() {
    this._updateFormState();
    super.ngOnInit();
  }

  loadLogo($event) {
    const emblem = File.getFirst($event);

    if (!emblem) {
      return;
    }

    ImageBase64.getFromFile$(emblem).subscribe(
      imageBase64 => {
        if (imageBase64 === '') {
          $event.target.value = '';
          this.form.controls.emblem.setErrors({incorrect: true});
          this.emblemImgSrc = null;
          return;
        }

        this.form.controls.emblem.setValue(imageBase64);
        this.emblemImgSrc =
          'data:image/png;base64,' + this.form.controls.emblem.value;
        this.form.controls.emblem.setErrors(null);
      },
      error => {
        console.error('Error while image loading: ', error);
        $event.target.value = '';
        this.form.controls.emblem.setErrors({'niewłaściwy format zdjęcia': true});
        this.emblemImgSrc = null;
      }
    );
  }

  openParentInstitutionModal() {
    if (this.form.controls.parentSlug.disabled) {
      return;
    }

    const {parentName: name, parentSlug: slug} = this.form.value;
    this._modalService.show<ParentInstitutionModal>({
      id: PARENT_INSTITUTION_MODAL_ID,
      size: 'lg',
      hasReturnValue: true,
      selectedInstitution: (!!this.form.value.parentName && {name, slug}) || null
    });

    this._modalQuery
      .modalClosed$(PARENT_INSTITUTION_MODAL_ID)
      .pipe(
        untilDestroyed(this),
        map(modal => (isParentInstitutionModal(modal) ? modal.returnValue : null))
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

    const {adminsEmails, ...institution} = this.form.value;
    const adminsEmailsList: string[] = (adminsEmails || '')
      .split(',')
      .filter(email => !!email)
      .map(email => email.trim());

    this.form.disable();

    (this.activeInstitution
      ? this._institutionService.updateInstitution$(this.form.value)
      : this._institutionService.createInstitutionChild$(this.form.value)
    )
      .pipe(
        switchMap((updatedInstitution: Institution) =>
          forkJoin([of(updatedInstitution), this._sessionService.loadProfile$()])
        ),
        switchMap(([updatedInstitution, session]) =>
          (adminsEmailsList.length > 0
            ? forkJoin(
                adminsEmailsList.map(email =>
                  this._invitationService.send(updatedInstitution.slug, email, true)
                )
              )
            : of([])
          ).pipe(map(() => updatedInstitution))
        ),
        switchMap(updatedInstitution =>
          this._router.navigate(['/settings/institution'], {
            queryParamsHandling: 'merge',
            queryParams: {
              institution: updatedInstitution.slug
            }
          })
        ),
        finalize(() => this.form.enable())
      )
      .subscribe();
  }

  hasErrors(controlName: string) {
    const formControl = this.form.controls[controlName] as FormControl;
    return (
      !!formControl &&
      formControl.touched &&
      !!formControl.errors &&
      Object.keys(formControl.errors).length > 0
    );
  }

  resetForm() {
    !!this.activeInstitution
      ? this._setFormWith(this.form, this.activeInstitution)
      : this.form.reset();

    this._router.navigate(['/settings/institution'], {
      relativeTo: this._activatedRoute,
      queryParamsHandling: 'merge'
    });
  }

  protected _updateFormState() {
    combineLatest(
      this._institutionsSearchResultsQuery.selectActive$(this._activatedRoute),
      this._institutionsSearchResultsQuery.isChildAddition$(this._activatedRoute)
    )
      .pipe(
        untilDestroyed(this),
        filter(([institution, isChildAddition]) => !!institution)
      )
      .subscribe(([institution, isChildAddition]) => {
        const {
          slug: parentSlug,
          name: parentName,
          ...x
        } = institution as Institution;
        const parentInstitution = ((isChildAddition && {parentSlug, parentName}) ||
          institution) as any;
        this._setParent(this.form, parentInstitution);

        const isEditMode = this._router.url.includes(EDIT_INSTITUTION_PATH);
        if (isEditMode) {
          this.activeInstitution = institution as Institution;
          this._setFormWith(this.form, institution as Institution);
        }
      });
  }

  protected _setParent(form: FormGroup<Institution>, institution: Institution) {
    if (!institution.parentSlug || !institution.parentName) {
      form.controls.parentSlug.disable();
      form.controls.parentName.disable();
    }

    const {parentSlug, parentName, ...x} = institution;
    form.patchValue({parentSlug, parentName});
  }

  protected _setFormWith(
    form: FormGroup<Institution>,
    activeInstitution: Institution
  ) {
    form.patchValue(activeInstitution);
    ImageBase64.getFromUrl$(activeInstitution.emblem).subscribe(
      imgBase64 => {
        form.controls.emblem.setValue(imgBase64);
        this.emblemImgSrc =
          'data:image/png;base64,' + this.form.controls.emblem.value;
        this.form.controls.emblem.setErrors(null);
      },
      error => {
        this.emblemImgSrc = null;
        if (error.target.src.indexOf('/static/emblems') > -1) {
          this.form.controls.emblem.setErrors(null);
          this.form.controls.emblem.setValue(null);
          return;
        }

        this._notificationService.addGeneral({
          content:
            'Wgrane zdjęcie jest w niewłaściwym formacie, jest uszkodzone lub link źródłowy jest niepoprawny',
          type: 'error'
        });
      }
    );
  }
}

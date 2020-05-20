import { Router } from '@angular/router';
import { AkitaNgFormsManager } from '@datorama/akita-ng-forms-manager';
import { NotificationService } from 'notifications';
import { Component } from '@angular/core';
import { ModalService } from 'src/app/modal/state/modal.service';
import { ModalQuery } from 'src/app/modal/state/modal.query';
import {
  ParentInstitutionModal,
  PARENT_INSTITUTION_MODAL_ID,
  isParentInstitutionModal
} from '../parent-institution-modal/parent-institution-modal.model';
import { untilDestroyed } from 'ngx-take-until-destroy';
import { map } from 'rxjs/operators';
import { FormGroup, FormControl } from '@ng-stack/forms';
import { Validators } from '@angular/forms';
import { validateAllFormFields } from 'src/app/utils/miscellaneous/miscellaneous';
import { GenericFormComponent } from 'src/app/utils/miscellaneous/generic-form.component';
import { FormState } from 'src/app/state/form/form.model';
import { InstitutionQuery } from '../../state/institution/institution.query';
import { Institution } from '../../state/institution/institution.model';
import { InstitutionService } from '../../state/institution/institution.service';

@Component({
  selector: 's4e-add-institution',
  templateUrl: './add-institution.component.html',
  styleUrls: ['./add-institution.component.scss']
})
export class AddInstitutionComponent extends GenericFormComponent<InstitutionQuery, Institution> {
  form: FormGroup<Institution>;

  constructor(
    private _formsManager: AkitaNgFormsManager<FormState>,
    private _router: Router,
    private _institutionQuery: InstitutionQuery,
    private _institutionService: InstitutionService,
    private _modalService: ModalService,
    private _notificationService: NotificationService,
    private _modalQuery: ModalQuery
  ) {
    super(_formsManager, _router, _institutionQuery, 'addInstitution');
  }

  ngOnInit() {
    this.form = new FormGroup<Institution>({
      parentInstitutionName: new FormControl<string>(null, Validators.required),
      parentInstitutionSlug: new FormControl<string>(null, Validators.required),

      name: new FormControl<string>(null, Validators.required),
      address: new FormControl<string>(null, Validators.required),
      postalCode: new FormControl<string>(null, Validators.required),
      city: new FormControl<string>(null, Validators.required),
      phone: new FormControl<string>(null, Validators.required),
      emblem: new FormControl<string>(null, Validators.required),
      secondaryPhone: new FormControl<string>(),
      institutionAdminEmail: new FormControl<string>(null, [Validators.required, Validators.email])
    });

    super.ngOnInit();
    this.form.markAsTouched();
  }

  loadLogo($event) {
    const emblem = !!$event.target.files && !!$event.target.files[0]
      && $event.target.files[0];
    if (!!emblem) {
      const reader = new FileReader();
      reader.readAsDataURL(emblem);
      reader.onload = () => {
        if (!reader.result) {
          $event.target.value = '';
          this.form.controls.emblem.setValue('');
          this.form.controls.emblem.setErrors({'incorrect': true});

          return;
        } else if (reader.result as string === this.form.controls.emblem.value) {
          return;
        }

        this.form.controls.emblem.setValue((reader.result as string).substr('data:image/png;base64,'.length));
      };

      reader.onerror = (error) => console.log('Error: ', error);
    }
  }

  openParentInstitutionModal() {
    const cachedParentInstitution = {
      name: this.form.value.parentInstitutionName,
      slug: this.form.value.parentInstitutionSlug
    }
    this._modalService.show<ParentInstitutionModal>({
      id: PARENT_INSTITUTION_MODAL_ID,
      size: 'lg',
      hasReturnValue: true,
      selectedInstitution: !!this.form.value.parentInstitutionName && cachedParentInstitution || null
    });

    this._modalQuery.modalClosed$(PARENT_INSTITUTION_MODAL_ID)
      .pipe(
        untilDestroyed(this),
        map(modal => isParentInstitutionModal(modal) ? modal.returnValue : null)
      )
      .toPromise()
      .then((institution: Institution) => {
        if (!!institution) {
          this.form.controls.parentInstitutionName.setValue(institution.name);
          this.form.controls.parentInstitutionSlug.setValue(institution.slug);
        }
      });
  }

  addInstitution() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});
    if (this.form.invalid) {
      return;
    }

    this._institutionService.addInstitutionChild$(this.form.value);
  }

  resetForm() {
    this.form.reset();
  }
}

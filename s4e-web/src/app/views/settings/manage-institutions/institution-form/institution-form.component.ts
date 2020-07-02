import { IBreadcrumb } from './../../breadcrumb/breadcrumb.model';
import { BreadcrumbService } from './../../breadcrumb/breadcrumb.service';
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
import { map, filter, switchMap, tap } from 'rxjs/operators';
import { FormGroup, FormControl, Validators } from '@ng-stack/forms';
import { validateAllFormFields } from 'src/app/utils/miscellaneous/miscellaneous';
import { GenericFormComponent } from 'src/app/utils/miscellaneous/generic-form.component';
import { FormState } from 'src/app/state/form/form.model';
import { InstitutionQuery } from '../../state/institution/institution.query';
import { Institution } from '../../state/institution/institution.model';
import { InstitutionService } from '../../state/institution/institution.service';
import { File, ImageBase64 } from './files.utils';
import { combineLatest } from 'rxjs';
import { settingsRoutes, ADD_INSTITUTION_PATH, INSTITUTIONS_LIST_PATH, INSTITUTION_PROFILE_PATH } from '../../settings.routes';

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
    private _activatedRoute: ActivatedRoute,
    private _breadcrumbService: BreadcrumbService
  ) {
    super(_formsManager, _router, _institutionQuery, 'addInstitution');
  }

  ngOnInit() {
    this.form = this._createInstitutionForm();
    this._updateFormState();

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
        (error) => {
          console.log('Error while image loading: ', error);
          this.form.controls.emblem.setErrors({'incorrect': true});
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
      selectedInstitution: !!this.form.value.parentName  && {name, slug} || null
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
      [
        !!this.activeInstitution
          ? '/settings/institution'
          : '/settings/institutions'
      ],
      {
        relativeTo: this._activatedRoute,
        queryParamsHandling: 'merge'
      }
    );
  }

  protected _updateFormState() {
    combineLatest(
      this._institutionsSearchResultsQuery.getInstitutionFrom$(this._activatedRoute),
      this._institutionsSearchResultsQuery.addChild$(this._activatedRoute),
      this._breadcrumbService.breadcrumbs$,
      this._activatedRoute.data
    )
      .pipe(
        untilDestroyed(this),
        filter(([institution, addChild, breadcrumbs, data]) => !!institution && (!!addChild || !!data.isEditMode))
      )
      .subscribe(([institution, addChild, breadcrumbs, data]) => {
        const isAddChildState = !!addChild && !data.isEditMode;
        if (isAddChildState) {
          this._updateBreadcrumbsWithInstitutionProfile(breadcrumbs);
        }

        const {slug: parentSlug, name: parentName, ...x} = institution as Institution;
        const parentInstitution = (isAddChildState && {parentSlug, parentName} || institution) as any;
        this._setParent(this.form, parentInstitution);

        const isEditMode = !addChild && !!data.isEditMode;
        if (isEditMode) {
          this.activeInstitution = institution as Institution;
          this._setFormWith(this.form, institution as Institution);
        }
      });
  }

  protected _updateBreadcrumbsWithInstitutionProfile(breadcrumbs: IBreadcrumb[]) {
    const mainRouteIndex = 0;
    const institutionListBreadcrumbIndex = settingsRoutes[mainRouteIndex].children
      .find(route => route.path === ADD_INSTITUTION_PATH)
      .data
      .breadcrumbs
      .map(breadcrumb => breadcrumb.url)
      .indexOf(INSTITUTIONS_LIST_PATH);
    const finalBreadcrumbOffset = 1;
    breadcrumbs[institutionListBreadcrumbIndex + finalBreadcrumbOffset] = {
      label: 'Profil instytucji',
      url: INSTITUTION_PROFILE_PATH
    };
    this._breadcrumbService.replaceWith(breadcrumbs);
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

  protected _setParent(form: FormGroup<Institution>, institution: Institution) {
    if (!institution.parentSlug || !institution.parentName) {
      form.controls.parentSlug.disable();
      form.controls.parentName.disable();
    }


    const {parentSlug, parentName, ...x} = institution;
    form.patchValue({parentSlug, parentName});
  }

  protected _setFormWith(form: FormGroup<Institution>, activeInstitution: Institution) {
    form.patchValue(activeInstitution);
    ImageBase64.getFromUrl$(activeInstitution.emblem)
      .subscribe(
        (imgBase64) => {
          form.controls.emblem.setValue(imgBase64);
          this.emblemImgSrc = 'data:image/png;base64,' + this.form.controls.emblem.value;
        },
        (error) => console.log('Error while image converting: ', error)
      );
  }
}

import { InstitutionFactory } from '../../state/institution/institution.factory.spec';
import { InstitutionService } from './../../state/institution/institution.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ManageInstitutionsModalModule } from './../manage-institutions.module';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddInstitutionComponent } from './add-institution.component';
import { S4eConfig } from 'src/app/utils/initializer/config.service';

describe('AddInstitutionComponent', () => {
  let component: AddInstitutionComponent;
  let fixture: ComponentFixture<AddInstitutionComponent>;
  let institutionService: InstitutionService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ManageInstitutionsModalModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        S4eConfig
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddInstitutionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    institutionService = TestBed.get(InstitutionService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not send non valid form', () => {
    const spy = spyOn(institutionService, 'addInstitutionChild$');
    component.addInstitution();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should send valid form', () => {
    const spy = spyOn(institutionService, 'addInstitutionChild$');
    const {id, slug, ...formInstitution} = InstitutionFactory.build();
    component.form.setValue(formInstitution as undefined);
    component.addInstitution();
    expect(spy).toHaveBeenCalled();
  });

  it('should validate parent institution name', () => {
    component.form.controls.parentInstitutionName.setValue('');
    expect(component.form.controls.parentInstitutionName.valid).toBeFalsy();
    component.form.controls.parentInstitutionName.setValue(InstitutionFactory.build().parentInstitutionName);
    expect(component.form.controls.parentInstitutionName.valid).toBeTruthy();
  });

  it('should validate parent institution slug', () => {
    component.form.controls.parentInstitutionSlug.setValue('');
    expect(component.form.controls.parentInstitutionSlug.valid).toBeFalsy();
    component.form.controls.parentInstitutionSlug.setValue(InstitutionFactory.build().parentInstitutionSlug);
    expect(component.form.controls.parentInstitutionSlug.valid).toBeTruthy();
  });

  it('should validate name', () => {
    component.form.controls.name.setValue('');
    expect(component.form.controls.name.valid).toBeFalsy();
    component.form.controls.name.setValue(InstitutionFactory.build().name);
    expect(component.form.controls.name.valid).toBeTruthy();
  });

  it('should validate address', () => {
    component.form.controls.address.setValue('');
    expect(component.form.controls.address.valid).toBeFalsy();
    component.form.controls.address.setValue(InstitutionFactory.build().address);
    expect(component.form.controls.address.valid).toBeTruthy();
  });

  it('should validate zip code', () => {
    component.form.controls.postalCode.setValue('');
    expect(component.form.controls.postalCode.valid).toBeFalsy();
    component.form.controls.postalCode.setValue(InstitutionFactory.build().postalCode);
    expect(component.form.controls.postalCode.valid).toBeTruthy();
  });

  it('should validate city', () => {
    component.form.controls.city.setValue('');
    expect(component.form.controls.city.valid).toBeFalsy();
    component.form.controls.city.setValue(InstitutionFactory.build().city);
    expect(component.form.controls.city.valid).toBeTruthy();
  });

  it('should validate phone', () => {
    component.form.controls.phone.setValue('');
    expect(component.form.controls.phone.valid).toBeFalsy();
    component.form.controls.phone.setValue(InstitutionFactory.build().phone);
    expect(component.form.controls.phone.valid).toBeTruthy();
  });

  it('should validate emblem', () => {
    component.form.controls.emblem.setValue('');
    expect(component.form.controls.emblem.valid).toBeFalsy();
    component.form.controls.emblem.setValue(InstitutionFactory.build().emblem);
    expect(component.form.controls.emblem.valid).toBeTruthy();
  });

  it('should validate additional phone', () => {
    component.form.controls.secondaryPhone.setValue('');
    expect(component.form.controls.secondaryPhone.valid).toBeTruthy();
    component.form.controls.secondaryPhone.setValue(InstitutionFactory.build().secondaryPhone);
    expect(component.form.controls.secondaryPhone.valid).toBeTruthy();
  });

  it('should validate institution admin email', () => {
    component.form.controls.institutionAdminEmail.setValue('');
    expect(component.form.controls.institutionAdminEmail.valid).toBeFalsy();

    component.form.controls.institutionAdminEmail.setValue(InstitutionFactory.build().institutionAdminEmail.split('@').shift());
    expect(component.form.controls.institutionAdminEmail.valid).toBeFalsy();

    component.form.controls.institutionAdminEmail.setValue(InstitutionFactory.build().institutionAdminEmail);
    expect(component.form.controls.institutionAdminEmail.valid).toBeTruthy();
  });
});

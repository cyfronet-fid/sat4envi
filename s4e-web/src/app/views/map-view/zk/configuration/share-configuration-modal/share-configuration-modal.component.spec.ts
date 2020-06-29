import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {EmailListValidator, ShareConfigurationModalComponent} from './share-configuration-modal.component';
import {MapModule} from '../../../map.module';
import {MODAL_DEF} from '../../../../../modal/modal.providers';
import {ConfigurationModal, SHARE_CONFIGURATION_MODAL_ID} from '../state/configuration.model';
import {TestingConfigProvider} from '../../../../../app.configuration.spec';
import {By} from '@angular/platform-browser';
import {FormControl} from '@ng-stack/forms';
import {ConfigurationService} from '../state/configuration.service';
import {of} from 'rxjs';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';

describe('ShareConfigurationModalComponent', () => {
  let component: ShareConfigurationModalComponent;
  let fixture: ComponentFixture<ShareConfigurationModalComponent>;
  const base64Data = '00';
  const mapImage = `data:image/png;base64,${base64Data}`;
  const shareURL = '/sample?a=1&b=1';

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
      providers: [
        TestingConfigProvider,
        {
          provide: MODAL_DEF, useValue: {
            id: SHARE_CONFIGURATION_MODAL_ID,
            size: 'md',
            configurationUrl: shareURL,
            mapImage: mapImage,
          } as ConfigurationModal
        }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShareConfigurationModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show image from passed modal variables', () => {
    expect((fixture.debugElement.query(By.css('#share-link')).nativeElement as HTMLLinkElement).href).toEqual(document.location.origin + shareURL);
  });

  it('should show url from passed modal variables', () => {
    expect((fixture.debugElement.query(By.css('#map-miniature')).nativeElement as HTMLImageElement).src).toEqual(mapImage);
  });

  describe('invalid form', () => {
    it('should not submit', () => {
      const spy = spyOn(TestBed.get(ConfigurationService), 'shareConfiguration');
      component.submit();
      expect(spy).not.toHaveBeenCalled();
    });
  });

  describe('valid form', () => {
    const caption = 'Test Caption #1';
    const description = 'Test Description #1';
    const email = 'a@a';
    beforeEach(() => {
      component.form.controls.caption.setValue(caption);
      component.form.controls.description.setValue(description);
      component.form.controls.emails.setValue(email);
    });

    it('should submit', () => {
      const spy = spyOn(TestBed.get(ConfigurationService), 'shareConfiguration').and.returnValue(of(true));
      component.submit();
      expect(spy).toHaveBeenCalledWith({
        caption: caption,
        description: description,
        emails: [email],
        path: shareURL,
        thumbnail: base64Data
      });
    });

    it('if share succeeds dismiss modal', async () => {
      spyOn(TestBed.get(ConfigurationService), 'shareConfiguration').and.returnValue(of(true));
      const spy = spyOn(component, 'dismiss');
      await component.submit();
      expect(spy).toHaveBeenCalled();
    });

    it('if share fails do not dismiss modal', async () => {
      spyOn(TestBed.get(ConfigurationService), 'shareConfiguration').and.returnValue(of(false));
      const spy = spyOn(component, 'dismiss');
      await component.submit();
      expect(spy).not.toHaveBeenCalled();
    });
  });
});


describe('EmailListValidator', () => {
  it('should return null if valid', () => {
    expect(EmailListValidator(new FormControl<string>('a@a'))).toBeNull();
    expect(EmailListValidator(new FormControl<string>('a@a,b@b'))).toBeNull();
    expect(EmailListValidator(new FormControl<string>('a@a, b@b'))).toBeNull();
  });

  it('should return error if empty', () => {
    expect(EmailListValidator(new FormControl<string>(''))).toEqual({email: true});
  });

  it('should return error if invalid', () => {
    expect(EmailListValidator(new FormControl<string>('a@'))).toEqual({email: true});
  });

  it('should return error if single mail in string is invalid', () => {
    expect(EmailListValidator(new FormControl<string>('a@b, invalidEmail'))).toEqual({email: true});
  });
});

import { JWT_TOKEN_MODAL_ID } from './jwt-token-modal.model';
import { RouterTestingModule } from '@angular/router/testing';
import { MapModule } from './../map.module';
import { By } from '@angular/platform-browser';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JwtTokenModalComponent } from './jwt-token-modal.component';
import { of } from 'rxjs';
import { MODAL_DEF } from 'src/app/modal/modal.providers';

describe('JwtTokenModalComponent', () => {
  let component: JwtTokenModalComponent;
  let fixture: ComponentFixture<JwtTokenModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MapModule,
        RouterTestingModule
      ],
      providers: [
        {
          provide: MODAL_DEF,
          useValue: {
            id: JWT_TOKEN_MODAL_ID,
            size: 'lg'
          }
        }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JwtTokenModalComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should open window with auth form', () => {
    component.token = null;
    fixture.detectChanges();

    const passwordInput = fixture.debugElement
      .query(By.css('[data-ut="jwt-token-password-input"]'));
    expect(passwordInput).toBeTruthy();

    const getTokenBtn = fixture.debugElement
      .query(By.css('[data-ut="get-jwt-token-btn"]'));
    expect(getTokenBtn).toBeTruthy();

    const tokenTextarea = fixture.debugElement
      .query(By.css('[data-ut="jwt-token-txt"]'));
    expect(tokenTextarea).toBeFalsy();
  });
  it('should show jwt token', () => {
    const token = 'token';
    component.token = token;
    fixture.detectChanges();

    const passwordInput = fixture.debugElement
      .query(By.css('[data-ut="jwt-token-password-input"]'));
    expect(passwordInput).toBeFalsy();

    const getTokenBtn = fixture.debugElement
      .query(By.css('[data-ut="get-jwt-token-btn"]'));
    expect(getTokenBtn).toBeFalsy();

    const tokenTextarea = fixture.debugElement
      .query(By.css('[data-ut="jwt-token-txt"]'));
    expect(tokenTextarea).toBeTruthy();
  });
});

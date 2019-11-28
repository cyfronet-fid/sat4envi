import {Component, ComponentFactoryResolver, Inject, OnInit, Type, ViewChild, ViewContainerRef} from '@angular/core';
import {Observable} from 'rxjs';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {environment} from '../../../../environments/environment';
import {ModalService} from '../../state/modal.service';
import {MODAL_PROVIDER, ModalProviderEntry} from '../../modal.providers';
import {ModalQuery} from '../../state/modal.query';
import {Modal} from '../../state/modal.model';

const TRANSITION_DURATION = environment.production ? 150 : 0;

@Component({
  selector: 's4e-modal-outlet',
  templateUrl: './modal-outlet.component.html',
  styleUrls: ['./modal-outlet.component.scss'],
  animations: [
    trigger('listAnimation', [
      state('true', style({opacity: 1.0})),
      state('void', style({opacity: 0.0})),
      transition('* => void', [ // each time the binding value changes
        animate(TRANSITION_DURATION)
      ]),
    ])
  ]
})

export class ModalOutletComponent implements OnInit {

  test: boolean = false;

  @ViewChild('container', {read: ViewContainerRef}) container: ViewContainerRef;

  modals$: Observable<Modal[]>;
  public showAnimations: boolean = environment.production;

  constructor(private componentFactoryResolver: ComponentFactoryResolver,
              private modalService: ModalService,
              private modalQuery: ModalQuery) {}

  ngOnInit() {
    this.modals$ = this.modalQuery.selectAll();
  }

  getModalId(modal: Modal) {
    return modal.id;
  }
}

import {ModalService} from '../../state/modal.service';

export class ModalComponent<ReturnType=void> {
  public dismiss(returnValue?: ReturnType): void {
    this.modalService.hide(this.registeredId, returnValue);
  }

  constructor(protected modalService: ModalService, public registeredId?: string) {}
}

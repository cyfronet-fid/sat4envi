import { ModalStore } from './modal.store';

describe('ModalStore', () => {
  let store: ModalStore;

  beforeEach(() => {
    store = new ModalStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});

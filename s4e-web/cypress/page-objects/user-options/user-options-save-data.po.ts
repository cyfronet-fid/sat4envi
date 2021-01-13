import { Core } from './../core.po';

export class UserOptionsSaveData extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getSaveImageOnDiskBtn: () => cy.get('[data-e2e="open-save-on-disk-image"]'),
    getOpenSaveMetaDataOnDiskBtn: () => cy.get('[data-e2e="open-save-on-disk-metadata-modal"]'),
    getModal: () => cy.get('[data-e2e="modal-container"]'),
    getResultDownloadAllArtifactsBtn: () => cy.get('[data-e2e="btn--download"]')
  };

  static saveImageOnDisk() {
    UserOptionsSaveData
      .pageObject
      .getOptionsBtn()
      .click();

    UserOptionsSaveData
      .pageObject
      .getSaveImageOnDiskBtn()
      .invoke('removeAttr', 'target')
      .click();

    return UserOptionsSaveData;
  }

  static openSaveMetaDataOnDiskModal() {
    UserOptionsSaveData
      .pageObject
      .getOptionsBtn()
      .click();

    UserOptionsSaveData
      .pageObject
      .getOpenSaveMetaDataOnDiskBtn()
      .click();

    UserOptionsSaveData
      .pageObject
      .getModal()
      .should("be.visible");

    return UserOptionsSaveData;
  }

  static selectNthArtifactsToDownload(number: number) {
    UserOptionsSaveData
      .pageObject
      .getResultDownloadAllArtifactsBtn()
      .eq(number)
      .invoke('removeAttr', 'target')
      .click();

    return UserOptionsSaveData;
  }
};
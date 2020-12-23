import { Core } from './../core.po';

export class SaveData extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getSaveImageOnDiskBtn: () => cy.get('[data-e2e="open-save-on-disk-image"]'),
    getOpenSaveMetaDataOnDiskBtn: () => cy.get('[data-e2e="open-save-on-disk-metadata-modal"]'),
    getModal: () => cy.get('[data-e2e="modal-container"]'),
    getResultDownloadAllArtifactsBtn:() => cy.get('[data-e2e="btn--download"]')
  };

  static saveImageOnDisk() {
    SaveData
      .pageObject
      .getOptionsBtn()
      .click();

    SaveData
      .pageObject
      .getSaveImageOnDiskBtn()
      .invoke('removeAttr', 'target')
      .click();

      return SaveData;
  }

  static openSaveMetaDataOnDiskModal() {
    SaveData
      .pageObject
      .getOptionsBtn()
      .click();

    SaveData
      .pageObject
      .getOpenSaveMetaDataOnDiskBtn()
      .click();

    SaveData
      .pageObject
      .getModal()
      .should("be.visible");

      return SaveData;
  }

  static selectNthArtifactsToDownload(number: number) {
    SaveData
      .pageObject
      .getResultDownloadAllArtifactsBtn()
      .eq(number)
      .invoke('removeAttr', 'target')
      .click();

    return SaveData;
  }
};
const cypressTypeScriptPreprocessor = require('./cy-ts-preprocessor');
const fs = require('fs');

module.exports = (on, config) => {
  on('file:preprocessor', cypressTypeScriptPreprocessor);
  on('before:browser:launch', (browser = {}, launchOptions) => {
  });
  on('task', {
    compareDownloadReport(now, timeout = config.defaultCommandTimeout) {
      now = now.replace(/:/g, '_');
      const downloadsFolder = require('downloads-folder');
      const path = downloadsFolder();
      const expectedFilename = path + '/' + `RAPORT.${now}.pdf`;
      const promise = new Promise((resolve, reject) => {
        let interval = setInterval(() => {
          if (fs.existsSync(expectedFilename)) {
            if (config.env['DONT_DELETE_DOWNLOADED_FILES'] !== true) {
              fs.unlinkSync(expectedFilename);
            }
            clearInterval(interval);
            interval = null;
            resolve(true);
          }
        }, 500);

        setTimeout(() => {
          if (interval) {
            clearInterval(interval);
            resolve(false);
          }
        }, timeout);
      });

      return promise
    }
  });
};

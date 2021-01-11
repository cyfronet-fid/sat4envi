const cypressTypeScriptPreprocessor = require('./cy-ts-preprocessor');
const fs = require('fs');

module.exports = (on, config) => {
  on('file:preprocessor', cypressTypeScriptPreprocessor);
  on('before:browser:launch', (browser = {}, launchOptions) => {
    /**
     * Filter console logs to the terminal, improve debugging process
     */
    require('cypress-log-to-output').install(on);

    /**
     * Set max event listeners to resist memory leaks
     */
    require('events').EventEmitter.prototype._maxListeners = 10;

    /**
     * Chrome memory & security optimizations
     */
    if (browser.name === 'chrome') {
      /**
       * Disable same origin policy
       */
      launchOptions.args.push('--disable-web-security');

      /**
       * Enable tracing bugs inside iframes
       */
      launchOptions.args.push('--disable-site-isolation-trials');

      /**
       * Increase chrome tab memory limit
       * By default 512MB on 32-bit systems or 1.4GB on 64-bit systems
       */
      launchOptions.args.push('--max_old_space_size=1024');

      /**
       * Increase chrome shared memory space from 64MB to unlimited (since Chrome 65)
       */
      launchOptions.args.push('--disable-dev-shm-usage');
      return launchOptions;
    }
  });
  on('task', {
    /**
     * Show full log in console of test on fail
     */
    failed: require('cypress-failed-log/src/failed')(),

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

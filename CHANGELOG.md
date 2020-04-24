# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
The most recent changes are on top, in each type of changes category.

## [Unreleased]

### Added

- Add global error handling [#430](https://github.com/cyfronet-fid/sat4envi/issues/430)
- UI Refactoring [#448](https://github.com/cyfronet-fid/sat4envi/issues/448)
- Add /logout routing and clear storage on logout (inconsistent state of storage) [#445](https://github.com/cyfronet-fid/sat4envi/issues/445)
- Synchronize scenes from S3 (s4-sync-1 dataset) [#439](https://github.com/cyfronet-fid/sat4envi/issues/439)
- Audit selected entities [#158](https://github.com/cyfronet-fid/sat4envi/issues/158)

### Changed

### Fixed

### Updated

- GeoServer image to fiddev/geoserver:1.4.0-GS2.17.0
- 2020.05 backend dependencies update

## [v6.0.0]

### Added

- Password change front [#158](https://github.com/cyfronet-fid/sat4envi/issues/158)
- SceneAcceptor driven by files from S3 [#160](https://github.com/cyfronet-fid/sat4envi/issues/160)
- Notifications module [#423](https://github.com/cyfronet-fid/sat4envi/issues/423)
- Notification for successful share configuration [#435](https://github.com/cyfronet-fid/sat4envi/issues/435)

### Changed

- Use RSA key-pair to sign and verify JWT tokens [#428](https://github.com/cyfronet-fid/sat4envi/issues/428)

### Fixed

- [Bugfix] places input should keep state [#427](https://github.com/cyfronet-fid/sat4envi/issues/427)

## [v5.1.0]

### Added

- Favorites products [#420] (https://github.com/cyfronet-fid/sat4envi/issues/420)
- Favourite Products [#403](https://github.com/cyfronet-fid/sat4envi/issues/403)
- Forms module [#370](https://github.com/cyfronet-fid/sat4envi/issues/370)

### Changed

- Overflow for map, sidebar, modals [#359](https://github.com/cyfronet-fid/sat4envi/issues/359) 
- Run e2e and integration tests in gh-actions and modify docker-compose files [#401](https://github.com/cyfronet-fid/sat4envi/issues/401)
- Add more seeds for tests [#411](https://github.com/cyfronet-fid/sat4envi/issues/411)
- Update handlebars version to 4.6.0

## [v5.0.0]

### Added

- Schema data model and CRD [#69](https://github.com/cyfronet-fid/sat4envi/issues/69)

### Changed

- Integrate Image Mosaic with TIME parameter [#389](https://github.com/cyfronet-fid/sat4envi/issues/389)
- Separate backend and web tests in gh-actions [#399](https://github.com/cyfronet-fid/sat4envi/issues/399)
- Rework backend Spring Security [#390](https://github.com/cyfronet-fid/sat4envi/issues/390)
- Login/Settings/User functionality changed to dropdown button [#386](https://github.com/cyfronet-fid/sat4envi/issues/386)

## [v4.0.0]

### Added

- Thumbnails MediaType and dimensions validation [#286](https://github.com/cyfronet-fid/sat4envi/issues/286)
- Data set: s4e-demo-2 [#369](https://github.com/cyfronet-fid/sat4envi/pull/369)
- Thumbnail scaling before POSTing in share view and save view [#341](https://github.com/cyfronet-fid/sat4envi/pull/367/files)
- PDF Report generation [#345](https://github.com/cyfronet-fid/sat4envi/pull/345)
- Timezone support for s4e-web [#363](https://github.com/cyfronet-fid/sat4envi/pull/363)
- DisplayName field to Product and appropriate view [#365](https://github.com/cyfronet-fid/sat4envi/pull/365)
- Migration with 'On delete cascade' for AppUser field on PasswordReset and EmailVerification tables [#364](https://github.com/cyfronet-fid/sat4envi/pull/364)
- Possibility to set maven artifact version with properties [#356](https://github.com/cyfronet-fid/sat4envi/pull/356)
- This changelog file [#355](https://github.com/cyfronet-fid/sat4envi/pull/355)

### Changed

- Modify Product and Scene to work with GeoServer ImageMosaic [#371](https://github.com/cyfronet-fid/sat4envi/pull/371)
- Update GeoServer image to 1.3.0-GS2.16.2, which includes JNDI and removes JDBCConfig support [#378](https://github.com/cyfronet-fid/sat4envi/pull/378)
- Update db image to postgis/postgis:12-3.0-alpine and use Postgis hibernate dialect [#371](https://github.com/cyfronet-fid/sat4envi/pull/371)
- Update GeoServer image to 1.2.0-GS2.16.2 and use URL-correct S3Geotiff endpoint [#371](https://github.com/cyfronet-fid/sat4envi/pull/371)

### Fixed

- Return site-key instead of secret-key in ConfigController [#381](https://github.com/cyfronet-fid/sat4envi/issues/381)
- Build in private scope [#372](https://github.com/cyfronet-fid/sat4envi/pull/372)
- Constraints names on Product and Scene tables [#364](https://github.com/cyfronet-fid/sat4envi/pull/364)

### Updates

- 2020.03 backend dependencies update

[unreleased]: https://github.com/cyfronet-fid/sat4envi/compare/v6.0.0...HEAD
[v6.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v5.1.0...v6.0.0
[v5.1.0]: https://github.com/cyfronet-fid/sat4envi/compare/v5.0.0...v5.1.0
[v5.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/v4.0.0...v5.0.0
[v4.0.0]: https://github.com/cyfronet-fid/sat4envi/compare/0ebb1138...v4.0.0

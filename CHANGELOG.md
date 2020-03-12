# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

- Update GeoServer image to 1.3.0-GS2.16.2, which includes JNDI and removes JDBCConfig support [#378](https://github.com/cyfronet-fid/sat4envi/pull/378)
- Update db image to postgis/postgis:12-3.0-alpine and use Postgis hibernate dialect [#371](https://github.com/cyfronet-fid/sat4envi/pull/371)
- Update GeoServer image to 1.2.0-GS2.16.2 and use URL-correct S3Geotiff endpoint [#371](https://github.com/cyfronet-fid/sat4envi/pull/371)

### Fixed

- Return site-key instead of secret-key in ConfigController [#381](https://github.com/cyfronet-fid/sat4envi/issues/381)
- Build in private scope [#372](https://github.com/cyfronet-fid/sat4envi/pull/372)
- Constraints names on Product and Scene tables [#364](https://github.com/cyfronet-fid/sat4envi/pull/364)

### Updates

- 2020.03 backend dependencies update

[unreleased]: https://github.com/cyfronet-fid/sat4envi/compare/0ebb1138...HEAD
